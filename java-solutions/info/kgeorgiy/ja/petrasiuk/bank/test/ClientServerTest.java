package info.kgeorgiy.ja.petrasiuk.bank.test;

import info.kgeorgiy.ja.petrasiuk.bank.src.Client;
import org.junit.jupiter.api.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

/**
 * Test for Client and Server functionality
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class ClientServerTest {
    private static ByteArrayOutputStream output;
    private static Registry registry;

    @BeforeAll
    public static void setUp() throws RemoteException {
        registry = LocateRegistry.createRegistry(1099);
    }

    @BeforeEach
    public void setupStreams() {
        output = new ByteArrayOutputStream();
        System.setOut(new PrintStream(output));
    }

    @AfterEach
    public void cleanup() {
        try {
            if (registry != null) {
                registry.unbind("bank");
            }
        } catch (Exception ignored) {
        }
    }

    @Test
    @Order(1)
    public void testConnectToDownServer() throws RemoteException {
        String name = TestUtils.randomName();
        String surname = TestUtils.randomSurname();
        String passport = TestUtils.randomPassport();
        Client.main(name, surname, passport, "0", "114");
        Assertions.assertEquals(
                "Bank is not bound\n",
                output.toString()
        );
    }

    @Test
    @Order(2)
    public void testNormalUsage() throws RemoteException, InterruptedException {
        String name = TestUtils.randomName();
        String surname = TestUtils.randomSurname();
        String passport = TestUtils.randomPassport();
        String accountId = "0";
        String amount = TestUtils.randomAmount();
        TestUtils.startServerAndRunClient(name, surname, passport, accountId, amount);
        Assertions.assertEquals(
                """
                        Server started
                        Searching person %s
                        Creating person %s %s %s
                        Retrieving account %s:%s
                        Creating account for %s %s %s
                        Account id: %s:%s
                        Current amount: 0
                        Setting amount of money for account %s:%s
                        New amount: %s
                        """.formatted(passport, name, surname, passport, passport, accountId, name, surname, passport, passport, accountId, passport, accountId, amount),
                output.toString()
        );
    }

    @Test
    @Order(3)
    public void testExistingPersonNewAccount() throws RemoteException {
        String name = TestUtils.randomName();
        String surname = TestUtils.randomSurname();
        String passport = TestUtils.randomPassport();
        String firstAccountId = "0";
        String secondAccountId = "1";
        String firstAmount = TestUtils.randomAmount();
        String secondAmount = TestUtils.randomAmount();
        TestUtils.startServerAndRunClient(name, surname, passport, firstAccountId, firstAmount);
        setupStreams();
        Client.main(name, surname, passport, secondAccountId, secondAmount);
        Assertions.assertEquals(
                """
                        Searching person %s
                        Person already exists
                        Person data is correct
                        Retrieving account %s:%s
                        Creating account for %s %s %s
                        Account id: %s:%s
                        Current amount: 0
                        Setting amount of money for account %s:%s
                        New amount: %s
                        """.formatted(passport, passport, secondAccountId, name, surname, passport, passport, secondAccountId, passport, secondAccountId, secondAmount),
                output.toString()
        );
    }

    @Test
    @Order(4)
    public void testExistingAccountSameAmount() throws RemoteException {
        String name = TestUtils.randomName();
        String surname = TestUtils.randomSurname();
        String passport = TestUtils.randomPassport();
        String accountId = "0";
        String amount = TestUtils.randomAmount();
        TestUtils.startServerAndRunClient(name, surname, passport, accountId, amount);
        setupStreams();
        Client.main(name, surname, passport, accountId, amount);
        Assertions.assertEquals(
                """
                        Searching person %s
                        Person already exists
                        Person data is correct
                        Retrieving account %s:%s
                        Account already exists
                        Account id: %s:%s
                        Current amount: %s
                        Account already have the same amount
                        """.formatted(passport, passport, accountId, passport, accountId, amount),
                output.toString()
        );
    }

    @Test
    @Order(5)
    public void testInvalidAmountFormat() throws RemoteException {
        String name = TestUtils.randomName();
        String surname = TestUtils.randomSurname();
        String passport = TestUtils.randomPassport();
        String accountId = "0";
        String invalidAmount = "invalid";
        TestUtils.startServerAndRunClient(name, surname, passport, accountId, invalidAmount);
        Assertions.assertEquals("""
                        Server started
                        Invalid amount specified: %s
                        """.formatted(invalidAmount),
                output.toString()
        );
    }

    @Test
    @Order(6)
    public void testMismatchedName() throws RemoteException {
        String name = TestUtils.randomName();
        String surname = TestUtils.randomSurname();
        String passport = TestUtils.randomPassport();
        String accountId = "0";
        String amount = TestUtils.randomAmount();
        String wrongName = TestUtils.randomName();
        while (wrongName.equals(name)) {
            wrongName = TestUtils.randomName();
        }
        TestUtils.startServerAndRunClient(name, surname, passport, accountId, amount);
        setupStreams();
        Client.main(wrongName, surname, passport, accountId, amount);
        Assertions.assertEquals(
                """
                        Searching person %s
                        Person already exists
                        Person name doesn't match
                        """.formatted(passport),
                output.toString()
        );
    }

    @Test
    @Order(7)
    public void testMismatchedSurname() throws RemoteException {
        String name = TestUtils.randomName();
        String surname = TestUtils.randomSurname();
        String passport = TestUtils.randomPassport();
        String accountId = "0";
        String amount = TestUtils.randomAmount();
        String wrongSurname = TestUtils.randomSurname();
        while (wrongSurname.equals(surname)) {
            wrongSurname = TestUtils.randomSurname();
        }
        TestUtils.startServerAndRunClient(name, surname, passport, accountId, amount);
        setupStreams();
        Client.main(name, wrongSurname, passport, accountId, amount);
        Assertions.assertEquals(
                """
                        Searching person %s
                        Person already exists
                        Person surname doesn't match
                        """.formatted(passport),
                output.toString()
        );
    }
}