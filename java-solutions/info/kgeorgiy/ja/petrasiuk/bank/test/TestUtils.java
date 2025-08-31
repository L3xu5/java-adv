package info.kgeorgiy.ja.petrasiuk.bank.test;

import info.kgeorgiy.ja.petrasiuk.bank.src.Client;
import info.kgeorgiy.ja.petrasiuk.bank.src.Server;

import java.rmi.RemoteException;
import java.util.List;
import java.util.Random;

/**
 * Some utils to provide spoof data to tests and start server/client
 */
public class TestUtils {
    private static final List<String> names = List.of("John", "Jane", "Jack", "Jill", "Frank", "Bob");
    private static final List<String> surnames = List.of("Doe", "Smith", "Jones", "Brown", "Einstein");
    private static final Random rand = new Random();

    public static String randomName() {
        return names.get(rand.nextInt(names.size()));
    }

    public static String randomSurname() {
        return surnames.get(rand.nextInt(surnames.size()));
    }

    public static String randomPassport() {
        return String.format("%010d", rand.nextLong(1_000_000_000L, 10_000_000_000L));
    }

    public static String randomAmount() {
        return String.format("%f", rand.nextDouble(0., 10000000.));
    }

    public static void startServerAndRunClient(String name, String surname, String passport, String accountId,
                                               String amount) throws RemoteException {
        Server.main();

        Client.main(name, surname, passport, accountId, amount);
    }
}
