package info.kgeorgiy.ja.petrasiuk.walk;

import java.io.IOException;
import java.io.Writer;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;

public class RecursiveWalk {
    static void process(Path path, Writer writer) throws IOException {
        Files.walkFileTree(path, new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path path, BasicFileAttributes attrs) throws IOException {
                try {
                    Walk.process(path, writer);
                } catch (WalkException e) {
                    System.out.println("Walk error: " + e.getMessage());
                }
                return FileVisitResult.CONTINUE;
            }

            @Override
            public FileVisitResult visitFileFailed(Path path, IOException e) throws IOException {
                System.out.println("I/O error while hashing file: " + path + " " + e.getMessage());
                Walk.writeResult(Walk.invalidHash, path.toString(), writer);
                return FileVisitResult.TERMINATE;
            }
        });
    }

    public static void main(final String[] args) {
        Walk.preprocess(args, RecursiveWalk::process);
    }
}
