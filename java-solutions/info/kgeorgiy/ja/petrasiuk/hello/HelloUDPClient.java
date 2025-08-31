package info.kgeorgiy.ja.petrasiuk.hello;

import info.kgeorgiy.java.advanced.hello.NewHelloClient;

import java.io.IOException;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Pattern;

/**
 * A UDP client implementation for sending and receiving hello messages to a server.
 * This class implements the {@link NewHelloClient} interface to provide functionality
 * for sending formatted requests to a specified host and port, and processing responses.
 */
public class HelloUDPClient implements NewHelloClient {
    private static final int TIMEOUT = 100;

    private String fixEncoding(String input) {
        StringBuilder result = new StringBuilder();
        for (char c : input.toCharArray()) {
            if (Character.isDigit(c)) {
                result.append(Character.getNumericValue(c));
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * Executes a list of requests using a specified number of threads.
     * Each request is sent to the server via UDP, and the client waits for a matching response.
     *
     * @param requests the list of requests to be sent, each containing host, port, and message template
     * @param threads  the number of threads to use for sending requests
     */
    @Override
    public void newRun(List<Request> requests, int threads) {
        try (ExecutorService executor = Executors.newFixedThreadPool(threads)) {
            for (int i = 1; i <= threads; i++) {
                int threadIdx = i;
                executor.submit(() -> {
                    try (DatagramSocket socket = new DatagramSocket()) {
                        socket.setSoTimeout(TIMEOUT);
                        final int bufferSize = socket.getReceiveBufferSize();
                        DatagramPacket answer = new DatagramPacket(new byte[bufferSize], bufferSize);
                        for (Request request : requests) {
                            InetAddress address = InetAddress.getByName(request.host());
                            String message = request.template().replaceAll("\\$", String.valueOf(threadIdx));
                            DatagramPacket toSend = new DatagramPacket(message.getBytes(), message.getBytes().length, address,
                                    request.port());
                            while (true) {
                                try {
                                    socket.send(toSend);
                                } catch (IOException e) {
                                    System.err.println("Error while sending: " + e.getMessage());
                                    continue;
                                }
                                try {
                                    socket.receive(answer);
                                    String response = new String(answer.getData(), answer.getOffset(),
                                            answer.getLength(),
                                            StandardCharsets.UTF_8);
                                    String fixedResponse = fixEncoding(response);
                                    String startPattern = request.template().startsWith("$") ? "^(.*\\D)?" : "^.*";
                                    String endPattern = request.template().endsWith("$") ? "(\\D.*)?$" : ".*$";
                                    String regex = startPattern + Pattern.quote(message) + endPattern;
                                    if (!fixedResponse.matches(regex)) {
                                        continue;
                                    }
                                    System.out.println(response);
                                    break;
                                } catch (IOException e) {
                                    System.err.println("Error while receiving: " + e.getMessage());
                                }
                            }
                        }
                    } catch (SocketException e) {
                        System.err.println("Socket error: " + e.getMessage());
                    } catch (UnknownHostException e) {
                        throw new RuntimeException(e);
                    }
                });
            }
        }
    }

    /**
     * Runs the client with the specified parameters, generating a list of requests
     * and delegating to {@link #newRun(List, int)}.
     *
     * @param host     the hostname or IP address of the server
     * @param port     the port number of the server
     * @param prefix   the prefix to use in the request messages
     * @param requests the number of requests to send
     * @param threads  the number of threads to use
     */
    @Override
    public void run(String host, int port, String prefix, int requests, int threads) {
        List<Request> requestsList = new ArrayList<>();
        for (int i = 1; i <= requests; i++) {
            requestsList.add(new Request(host, port, prefix + i + "_$"));
        }
        newRun(requestsList, threads);
    }

    /**
     * The main entry point for running the UDP client from the command line.
     * Expects five arguments: host, port, prefix, threads, and requests.
     *
     * @param args command-line arguments: [host, port, prefix, threads, requests]
     */
    public static void main(String[] args) {
        if (args.length != 5) {
            System.err.println("Usage: host name/ip, port, prefix, threads, requests");
            return;
        }
        String host = args[0];
        int port = parse(args[1], "port");
        String prefix = args[2];
        int threads = parse(args[3], "threads");
        int requests = parse(args[4], "requests");
        if (port < 0 || threads < 0 || requests < 0) {
            return;
        }
        new HelloUDPClient().run(host, port, prefix, threads, requests);
    }

    private static int parse(String input, String fieldName) {
        try {
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number of : " + fieldName + e.getMessage());
            return -1;
        }
    }
}