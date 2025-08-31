package info.kgeorgiy.ja.petrasiuk.bank.src;

import java.math.BigDecimal;
import java.rmi.Remote;
import java.rmi.RemoteException;


/**
 * Interface which declares common Account methods
 */
public interface Account extends Remote {
    static String computeId(String passport, String id) throws RemoteException {
        return passport + ":" + id;
    }

    /**
     * Returns account identifier.
     */
    String getId() throws RemoteException;

    /**
     * Returns amount of money in the account.
     */
    BigDecimal getAmount() throws RemoteException;

    /**
     * Sets amount of money in the account.
     */
    void setAmount(BigDecimal amount) throws RemoteException;
}
