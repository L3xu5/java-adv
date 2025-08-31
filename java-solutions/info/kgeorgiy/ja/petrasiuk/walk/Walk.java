package info.kgeorgiy.ja.petrasiuk.walk;

import java.io.*;
import java.nio.file.*;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.HexFormat;

public class Walk {
    static final String invalidHash = "0".repeat(16);

    private static final int hashBufferSize = 8192;

    static void writeResult(String hashString, String stringPath, Writer writer) throws IOException {
        writer.write(hashString + " " + stringPath + System.lineSeparator());
    }

    static String hashFile(Path path) throws IOException, NoSuchAlgorithmException {
        try (
                InputStream fileStream = Files.newInputStream(path);
                DigestInputStream digestStream = new DigestInputStream(fileStream, MessageDigest.getInstance("SHA-256"))
        ) {
            byte[] buffer = new byte[hashBufferSize];
            while (digestStream.read(buffer) != -1) {}
            MessageDigest digest = digestStream.getMessageDigest();
            return HexFormat.of().formatHex(Arrays.copyOfRange(digest.digest(), 0, 8));
        }
    }

    static void process(Path path, Writer writer) throws WalkException, IOException {
        String stringHash;
        try {
            stringHash = hashFile(path);
        } catch (IOException | SecurityException | NoSuchAlgorithmException e) {
            writeResult(invalidHash, path.toString(), writer);
            String errorType = e.getClass().getSimpleName();
            throw new WalkException(
                    String.format("%s while hashing file: %s: %s", errorType, path, e.getMessage()),
                    e
            );
        }
        writeResult(stringHash, path.toString(), writer);
    }

    static void preprocess(final String[] args, IOThrowableBiConsumer<Path, Writer> processor) {
        if (args == null) {
            System.err.println("Arguments missing");
        } else if (args.length != 2) {
            System.err.println("Wrong number of arguments. Should be 2.");
        } else if (args[0] == null ||  args[1] == null) {
            System.err.println("Arguments can't be null. Should be path");
        } else {
            try {
                final Path outputPath = Path.of(args[1]);
                final Path outputParentPath = outputPath.getParent();
                if (outputParentPath == null) {
                    throw new InvalidPathException(outputPath.toString(), "Output path's parent is null");
                }
                Files.createDirectories(outputPath.getParent());
                try (
                        BufferedReader reader = Files.newBufferedReader(Path.of(args[0]));
                        Writer writer = Files.newBufferedWriter(outputPath)
                ) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        try {
                            processor.accept(Path.of(line), writer);
                        } catch (InvalidPathException e) {
                            System.err.println("Invalid path to hashing file: " + e.getMessage());
                            writeResult(invalidHash, line, writer);
                        } catch (WalkException e) {
                            System.err.println("Walk error: " + e.getMessage());
                        } catch (IOException e) {
                            System.err.println("I/O error with output file: " + e.getMessage());
                            break;
                        }
                    }
                } catch (IOException e) {
                    System.err.println("I/O error with input file: " + e.getMessage());
                } catch (InvalidPathException e) {
                    System.err.println("Invalid path to input file: " + e.getMessage());
                }
            } catch (NoSuchFileException e) {
                System.err.println("No such file: " + e.getMessage());
            } catch (InvalidPathException e) {
                System.err.println("Invalid path to output file: " + e.getMessage());
            } catch (SecurityException e) {
                System.err.println("Security error: " + e.getMessage());
            } catch (IOException e) {
                System.err.println("I/O error while creating unexisting directories to output file: " + e.getMessage());
            }
        }
    }

    public static void main(final String[] args) {
        preprocess(args, Walk::process);
    }
}