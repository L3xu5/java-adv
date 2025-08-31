package info.kgeorgiy.ja.petrasiuk.bank.src;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

/**
 * Simple Server which deploy bank object to RMI
 */
public final class Server {
    private final static int DEFAULT_PORT = 8888;

    /**
     * Creates server instance in RMI
     * <p>
     * Expected arguments:
     * <ul>
     *     <li>port — port which should be used to bind server on it</li>
     * </ul>
     * <p>
     *
     * @param args the command-line arguments
     */
    public static void main(final String... args) {
        final int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        final Bank bank = new RemoteBank(port);
        try {
            UnicastRemoteObject.exportObject(bank, port);
            Naming.rebind("//localhost/bank", bank);
            System.out.println("Server started");
        } catch (final RemoteException e) {
            System.out.println("Cannot export object: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        } catch (final MalformedURLException e) {
            System.out.println("Malformed URL");
        }
    }
}
