package info.kgeorgiy.ja.petrasiuk.bank.src;

import java.math.BigDecimal;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

/**
 * Simple Client which can be used for creation users and updating theirs accounts
 */
public final class Client {
    /**
     * Utility class.
     */
    private Client() {
    }

    /**
     * Create/modify user with given data to given amount
     * <p>
     * Expected arguments:
     * <ul>
     *     <li>name — Person's name URL</li>
     *     <li>surname — Person's surname </li>
     *     <li>passport — Person's passport</li>
     *     <li>accountId — id of account to modify</li>
     *     <li>amount — amount to set</li>
     * </ul>
     * <p>
     *
     * @param args the command-line arguments
     */
    public static void main(final String... args) throws RemoteException {
        if (args.length != 5) {
            System.out.println("Usage: name surname passport accountId amount");
            return;
        }
        String name = args[0];
        String surname = args[1];
        String passport = args[2];
        String accountId = args[3];
        BigDecimal amount;
        try {
            amount = BigDecimal.valueOf(Double.parseDouble(args[4]));
        } catch (NumberFormatException e) {
            System.out.println("Invalid amount specified: " + args[4]);
            return;
        }

        final Bank bank;
        try {
            bank = (Bank) Naming.lookup("//localhost/bank");
        } catch (final NotBoundException e) {
            System.out.println("Bank is not bound");
            return;
        } catch (final MalformedURLException e) {
            System.out.println("Bank URL is invalid");
            return;
        }

        Person person = bank.getPerson(passport, PersonType.REMOTE);
        if (person == null) {
            person = bank.createPerson(name, surname, passport);
        } else {
            System.out.println("Person already exists");
            if (!person.getName().equals(name)) {
                System.out.println("Person name doesn't match");
                return;
            }
            if (!person.getSurname().equals(surname)) {
                System.out.println("Person surname doesn't match");
                return;
            }
            System.out.println("Person data is correct");
        }
        Account account = bank.getAccount(Account.computeId(passport, accountId));
        if (account == null) {
            account = bank.createAccount(person);
        } else {
            System.out.println("Account already exists");
        }
        System.out.println("Account id: " + account.getId());
        System.out.println("Current amount: " + account.getAmount());
        if (account.getAmount().equals(amount)) {
            System.out.println("Account already have the same amount");
            return;
        }
        account.setAmount(amount);
        System.out.println("New amount: " + account.getAmount());
    }
}
