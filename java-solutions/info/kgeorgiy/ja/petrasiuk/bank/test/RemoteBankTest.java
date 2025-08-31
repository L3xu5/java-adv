package info.kgeorgiy.ja.petrasiuk.bank.test;

import info.kgeorgiy.ja.petrasiuk.bank.src.*;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test for RemoteBank functionality
 */
public class RemoteBankTest {
    private static RemoteBank bank;
    private static Registry registry;

    @BeforeAll
    static void setUp() throws Exception {
        registry = LocateRegistry.createRegistry(1099);
        bank = new RemoteBank(8888);
        UnicastRemoteObject.exportObject(bank, 8888);
        registry.bind("bank", bank);
    }

    @AfterAll
    static void tearDown() throws Exception {
        registry.unbind("bank");
        UnicastRemoteObject.unexportObject(bank, true);
    }

    @Test
    void testCreatePerson() throws Exception {
        String name = TestUtils.randomName();
        String surname = TestUtils.randomSurname();
        String passport = TestUtils.randomPassport();

        Person person = bank.createPerson(name, surname, passport);
        assertNotNull(person);
        assertEquals(name, person.getName());
        assertEquals(surname, person.getSurname());
        assertEquals(passport, person.getPassport());
    }

    @Test
    void testGetPersonRemote() throws Exception {
        String name = TestUtils.randomName();
        String surname = TestUtils.randomSurname();
        String passport = TestUtils.randomPassport();

        bank.createPerson(name, surname, passport);
        Person person = bank.getPerson(passport, PersonType.REMOTE);
        assertNotNull(person);
        assertEquals(name, person.getName());
        assertEquals(surname, person.getSurname());
        assertEquals(passport, person.getPassport());
    }

    @Test
    void testGetPersonLocal() throws Exception {
        String name = TestUtils.randomName();
        String surname = TestUtils.randomSurname();
        String passport = TestUtils.randomPassport();

        bank.createPerson(name, surname, passport);
        Person person = bank.getPerson(passport, PersonType.LOCAL);
        assertNotNull(person);
        assertInstanceOf(LocalPerson.class, person);
        assertEquals(name, person.getName());
        assertEquals(surname, person.getSurname());
        assertEquals(passport, person.getPassport());
    }

    @Test
    void testCreateAccount() throws Exception {
        String passport = TestUtils.randomPassport();
        Person person = bank.createPerson(TestUtils.randomName(), TestUtils.randomSurname(), passport);
        Account account = bank.createAccount(person);
        assertNotNull(account);
        assertEquals(Account.computeId(passport, "0"), account.getId());
    }

    @Test
    void testGetAccount() throws Exception {
        String passport = TestUtils.randomPassport();
        Person person = bank.createPerson(TestUtils.randomName(), TestUtils.randomSurname(), passport);
        Account createdAccount = bank.createAccount(person);
        Account retrievedAccount = bank.getAccount(Account.computeId(passport, "0"));
        assertNotNull(retrievedAccount);
        assertEquals(createdAccount.getId(), retrievedAccount.getId());
    }

    @Test
    void testThreadSafeCreateAccounts() throws Exception {
        String passport = TestUtils.randomPassport();
        Person person = bank.createPerson(TestUtils.randomName(), TestUtils.randomSurname(), passport);
        int threadCount = 10;
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {

            IntStream.range(0, threadCount).forEach(_ -> executor.submit(() -> {
                try {
                    bank.createAccount(person);
                } catch (RemoteException e) {
                    fail("RemoteException in thread: " + e.getMessage());
                }
            }));
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor did not terminate in time");
        }

        List<Account> accounts = bank.getPerson(passport, PersonType.REMOTE).getAccounts();
        assertEquals(threadCount, accounts.size(), "Expected " + threadCount + " accounts to be created");
        for (int i = 0; i < threadCount; i++) {
            String expectedId = Account.computeId(passport, String.valueOf(i));
            assertTrue(accounts.stream().anyMatch(a -> {
                try {
                    return a.getId().equals(expectedId);
                } catch (RemoteException e) {
                    fail("RemoteException while checking account ID: " + e.getMessage());
                    return false;
                }
            }), "Account with ID " + expectedId + " not found");
        }
    }

    @Test
    void testThreadSafeCreatePersons() throws Exception {
        String[] passports;
        int threadCount = 10;
        try (ExecutorService executor = Executors.newFixedThreadPool(threadCount)) {
            passports = IntStream.range(0, threadCount)
                    .mapToObj(_ -> TestUtils.randomPassport())
                    .toArray(String[]::new);

            IntStream.range(0, threadCount).forEach(i -> executor.submit(() -> {
                try {
                    bank.createPerson(TestUtils.randomName(), TestUtils.randomSurname(), passports[i]);
                } catch (RemoteException e) {
                    fail("RemoteException in thread: " + e.getMessage());
                }
            }));
            executor.shutdown();
            assertTrue(executor.awaitTermination(10, TimeUnit.SECONDS), "Executor did not terminate in time");
        }

        for (String passport : passports) {
            Person person = bank.getPerson(passport, PersonType.REMOTE);
            assertNotNull(person, "Person with passport " + passport + " not found");
        }
    }
}