package info.kgeorgiy.ja.petrasiuk.bank.src;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Interface which declarates common Person methods
 */
public interface Person extends Remote {
    /**
     * @return person's name
     * @throws RemoteException // :NOTE: tag description is missing, аналогично в остальных местах
     */
    String getName() throws RemoteException;

    /**
     * @return person's surname
     * @throws RemoteException
     */
    String getSurname() throws RemoteException;

    /**
     * @return person's passport
     * @throws RemoteException
     */
    String getPassport() throws RemoteException;

    /**
     * @return {@link java.util.List} of person's {@link Account}
     * @throws RemoteException
     */
    List<Account> getAccounts() throws RemoteException;
}
