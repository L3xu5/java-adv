package info.kgeorgiy.ja.petrasiuk.hello;

import info.kgeorgiy.java.advanced.hello.HelloServer;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * A UDP server implementation for handling hello messages.
 * This class implements the {@link HelloServer} interface to provide functionality
 * for receiving UDP requests and responding with formatted messages.
 */
public class HelloUDPServer implements HelloServer {
    private DatagramSocket socket;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    /**
     * Starts the UDP server on the specified port with the given number of threads.
     * Each thread processes incoming requests and sends responses concurrently.
     *
     * @param port    the port number to listen on
     * @param threads the number of threads to handle requests
     */
    @Override
    public void start(int port, int threads) {
        if (!running.compareAndSet(false, true)) {
            System.err.println("Server is already running");
            return;
        }

        try {
            socket = new DatagramSocket(port);
            executor = Executors.newFixedThreadPool(threads);

            final int bufferSize = socket.getReceiveBufferSize();
            // :NOTE: single thread pool executor
            Thread receiverThread = new Thread(() -> {
                while (!socket.isClosed() && running.get()) {
                    try {
                        // :NOTE: reuse request
                        DatagramPacket request = new DatagramPacket(new byte[bufferSize], bufferSize);
                        socket.receive(request);
                        executor.submit(() -> {
                            DatagramPacket response = getDatagramPacket(request);
                            try {
                                socket.send(response);
                            } catch (IOException e) {
                                if (!socket.isClosed()) {
                                    System.err.println("Send error: " + e.getMessage());
                                }
                            }
                        });
                    } catch (IOException e) {
                        if (!socket.isClosed()) {
                            System.err.println("Recive error: " + e.getMessage());
                        }
                    }
                }
            });
            receiverThread.start();
            // :NOTE: join thread
        } catch (SocketException e) {
            System.err.println("Socket error: " + e.getMessage());
        }
    }

    /**
     * Creates a response packet for a given request packet.
     * The response contains the string "Hello, " prepended to the request message.
     *
     * @param request the incoming request packet
     * @return a {@link DatagramPacket} containing the response
     */
    private static DatagramPacket getDatagramPacket(DatagramPacket request) {
        String requestString = new String(request.getData(), request.getOffset(), request.getLength(), StandardCharsets.UTF_8);
        String responseString = "Hello, " + requestString;
        byte[] responseBytes = responseString.getBytes(StandardCharsets.UTF_8);

        return new DatagramPacket(
                responseBytes, responseBytes.length,
                request.getAddress(), request.getPort()
        );
    }

    /**
     * Closes the server, stopping all threads and closing the socket.
     */
    @Override
    public void close() {
        running.set(false);
        if (socket != null && !socket.isClosed()) {
            socket.close();
        }
        if (executor != null) {
            // :NOTE: some logic about awaiting termination
            executor.shutdownNow();
            boolean isTerminated = false;
            try {
                isTerminated = executor.awaitTermination(10, TimeUnit.SECONDS);
            } catch (InterruptedException e) {
                System.err.println("Interrupted while awaiting executor termination: " + e.getMessage());
            }
            if (!isTerminated) {
                System.err.println("Executor did not terminate");
            }
        }
    }

    /**
     * The main entry point for running the UDP server from the command line.
     * Expects two arguments: port and threads.
     *
     * @param args command-line arguments: [port, threads]
     */
    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Usage: port, threads");
            return;
        }
        int port, threads;
        try {
            port = Integer.parseInt(args[0]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid port number: " + e.getMessage());
            return;
        }
        try {
            threads = Integer.parseInt(args[1]);
        } catch (NumberFormatException e) {
            System.err.println("Invalid number of threads: " + e.getMessage());
            return;
        }
        try (HelloServer server = new HelloUDPServer()) {
            server.start(port, threads);
        }
    }
}