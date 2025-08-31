package info.kgeorgiy.ja.petrasiuk.bank.src;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Bank which uses RMI to present functionality
 */
public class RemoteBank implements Bank {
    private final int port;
    private final ConcurrentMap<String, Account> accounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Person> persons = new ConcurrentHashMap<>();
    private final ConcurrentMap<Person, List<Account>> personAccounts = new ConcurrentHashMap<>();

    public RemoteBank(final int port) {
        this.port = port;
    }

    @Override
    public Account createAccount(Person person) throws RemoteException {
        System.out.println(String.join(" ", "Creating account for", person.getName(), person.getSurname(), person.getPassport()));
        String accountId = computeAccountId(person);
        final Account account = new RemoteAccount(accountId);
        if (accounts.putIfAbsent(accountId, account) == null) {
            personAccounts.computeIfAbsent(person, _ -> new ArrayList<>()).add(account);
            UnicastRemoteObject.exportObject(account, port);
            return account;
        } else {
            return getAccount(accountId);
        }
    }

    @Override
    public Account getAccount(final String id) {
        System.out.println("Retrieving account " + id);
        return accounts.get(id);
    }

    @Override
    public Person getPerson(String passport, PersonType type) throws RemoteException {
        System.out.println("Searching person " + passport);
        Person person = persons.get(passport);
        return switch (type) {
            case REMOTE -> person;
            case LOCAL -> {
                if (person == null) {
                    yield null;
                }
                yield new LocalPerson(person.getName(),
                        person.getSurname(),
                        passport,
                        person.getAccounts().stream()
                                .map(account -> {
                                    Account newAccount;
                                    try {
                                        newAccount = new RemoteAccount(account.getId());
                                        newAccount.setAmount(account.getAmount());
                                    } catch (RemoteException e) {
                                        throw new RuntimeException(e);
                                    }
                                    return newAccount;
                                }).toList()
                );
            }
        };
    }

    @Override
    public Person createPerson(String name, String surname, String passport) throws RemoteException {
        System.out.println("Creating person " + name + " " + surname + " " + passport);
        final RemotePerson person = new RemotePerson(name, surname, passport, this);
        Person existingPerson = persons.putIfAbsent(passport, person);
        if (existingPerson == null) {
            UnicastRemoteObject.exportObject(person, port);
            return person;
        } else {
            return existingPerson;
        }
    }

    @Override
    public List<Account> getAccounts(Person person) throws RemoteException {
        return personAccounts.getOrDefault(person, new ArrayList<>());
    }

    private String computeAccountId(Person person) throws RemoteException {
        synchronized (personAccounts.computeIfAbsent(person, _ -> new ArrayList<>())) {
            List<Account> accounts = personAccounts.get(person);
            int id = accounts.isEmpty() ? 0 : Integer.parseInt(accounts.getLast().getId().split(":")[1]) + 1;
            return Account.computeId(person.getPassport(), String.valueOf(id));
        }
    }
}