package info.kgeorgiy.ja.petrasiuk.crawler;

import info.kgeorgiy.java.advanced.crawler.*;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.*;
import java.util.concurrent.*;

public class WebCrawler implements NewCrawler {
    private static final int DEFAULT_DOWNLOADERS = 10;
    private static final int DEFAULT_EXTRACTORS = 10;
    private static final int DEFAULT_PER_HOST = 10;

    private final Downloader downloader;
    private final ExecutorService downloaders;
    private final ExecutorService extractors;
    private final int perHostCount;
    private final Map<String, HostLimiter> hostSemaphores;

    public WebCrawler(Downloader downloader, int downloaders, int extractors, int perHost) {
        this.downloader = downloader;
        this.downloaders = Executors.newFixedThreadPool(downloaders);
        this.extractors = new ForkJoinPool(extractors);
        this.perHostCount = perHost;
        this.hostSemaphores = new ConcurrentHashMap<>();
    }

    private CompletableFuture<Void> processSingleLevel(String url, Set<String> nextLevel,
                                                       Set<String> processedUrls, Set<String> results,
                                                       Map<String, IOException> errors, Set<String> excludes) {
        if (!processedUrls.add(url)) {
            return CompletableFuture.completedFuture(null);
        }

        return downloadDocument(url, excludes, results, errors)
                .thenAcceptAsync(document -> extractLinks(document, url, nextLevel, errors), extractors);
    }

    void extractLinks(Document document, String url, Set<String> nextLevel, Map<String, IOException> errors) {
        if (document == null) return;
        try {
            nextLevel.addAll(document.extractLinks());
        } catch (IOException e) {
            errors.put(url, e);
        }
    }

    private CompletableFuture<Document> downloadDocument(String url, Set<String> excludes,
                                                         Set<String> results, Map<String, IOException> errors) {
        CompletableFuture<Document> future = new CompletableFuture<>();

        String host;
        try {
            host = URLUtils.getHost(url);
            if (excludes.parallelStream().anyMatch(host::contains)) {
                future.complete(null);
                return future;
            }
        } catch (MalformedURLException e) {
            errors.put(url, e);
            future.complete(null);
            return future;
        }

        HostLimiter limiter = hostSemaphores.compute(host, (_, existing) -> {
            if (existing != null) {
                existing.users.incrementAndGet();
                return existing;
            } else {
                HostLimiter newLimiter = new HostLimiter(perHostCount);
                newLimiter.users.incrementAndGet();
                return newLimiter;
            }
        });

        Runnable task = new Runnable() {
            @Override
            public void run() {
                if (limiter.semaphore.tryAcquire()) {
                    try {
                        Document document = downloader.download(url);
                        results.add(url);
                        future.complete(document);
                    } catch (IOException e) {
                        errors.put(url, e);
                        future.complete(null);
                    } finally {
                        limiter.semaphore.release();
                        if (limiter.semaphore.availablePermits() == perHostCount &&
                                limiter.users.decrementAndGet() == 0) {
                            hostSemaphores.remove(host, limiter);
                        }
                    }
                } else {
                    downloaders.submit(this);
                }
            }
        };

        downloaders.submit(task);
        return future;
    }

    @Override
    public Result download(String url, int depth, List<String> excludes) {
        Set<String> processedUrls = ConcurrentHashMap.newKeySet();
        Set<String> results = ConcurrentHashMap.newKeySet();
        Map<String, IOException> errors = new ConcurrentHashMap<>();
        Set<String> currentLevel = ConcurrentHashMap.newKeySet();
        Set<String> excludesSet = new HashSet<>(excludes);
        currentLevel.add(url);

        for (int i = 0; i < depth; i++) {
            Set<String> nextLevel = ConcurrentHashMap.newKeySet();

            CompletableFuture<?>[] futures = currentLevel.stream()
                    .map(currentUrl ->
                            processSingleLevel(currentUrl, nextLevel, processedUrls, results, errors, excludesSet))
                    .toArray(CompletableFuture[]::new);
            CompletableFuture.allOf(futures).join();
            nextLevel.removeAll(processedUrls);
            currentLevel = nextLevel;
        }

        return new Result(List.copyOf(results), errors);
    }

    @Override
    public void close() {
        downloaders.shutdownNow();
        extractors.shutdownNow();
    }

    private static int getArgument(String[] args, int index, int defaultValue) {
        return args.length > index ? Integer.parseInt(args[index]) : defaultValue;
    }

    public static void main(String[] args) {
        if (args.length < 1 || args.length > 5) {
            System.err.println("Usage: WebCrawler url [depth [downloaders [extractors [perHost]]]]");
            return;
        }

        try {
            String url = args[0];
            int depth = getArgument(args, 1, 1);
            int downloaders = getArgument(args, 2, DEFAULT_DOWNLOADERS);
            int extractors = getArgument(args, 3, DEFAULT_EXTRACTORS);
            int perHost = getArgument(args, 4, DEFAULT_PER_HOST);

            Downloader downloader = new CachingDownloader(0.);
            try (WebCrawler crawler = new WebCrawler(downloader, downloaders, extractors, perHost)) {
                crawler.download(url, depth, List.of());
            }
        } catch (IOException | NumberFormatException e) {
            System.err.println("Error: " + e.getMessage());
        }
    }
}
