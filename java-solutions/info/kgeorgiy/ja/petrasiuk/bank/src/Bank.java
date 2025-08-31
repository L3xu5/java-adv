package info.kgeorgiy.ja.petrasiuk.bank.src;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface which declarates common Bank methods
 */
public interface Bank extends Remote {
    /**
     * Creates a new account with specified identifier if it does not already exist.
     *
     * @param person {@link Person} who owns this account
     * @return created or existing account.
     */
    Account createAccount(Person person) throws RemoteException;

    /**
     * Returns account by identifier.
     *
     * @param id account id
     * @return account with specified identifier or {@code null} if such account does not exist.
     */
    Account getAccount(String id) throws RemoteException;

    /**
     * Returns {@link Person}  by passport.
     *
     * @param passport {@link String} person's passport
     * @param type     {@link PersonType} specifies type of person ({@link LocalPerson} or {@link RemotePerson})
     * @return {@link Person} with specified passport or {@code null} if such person does not exist.
     */
    Person getPerson(String passport, PersonType type) throws RemoteException;

    /**
     * Creates a new person with specified name, surname and passport if it does not already exist.
     *
     * @param name     {@link String} person's name
     * @param surname  {@link String} person's surname
     * @param passport {@link String} person's passport
     * @return created or existing {@link Person}.
     */
    Person createPerson(String name, String surname, String passport) throws RemoteException;

    /**
     * Returns list of person's accounts.
     *
     * @param person {@link Person} person who's accounts needed
     * @return {@link List<Account>} of given person's accountsd
     */
    List<Account> getAccounts(Person person) throws RemoteException;
}