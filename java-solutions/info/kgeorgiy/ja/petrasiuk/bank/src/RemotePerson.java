package info.kgeorgiy.ja.petrasiuk.bank.src;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

/**
 * Person which uses RMI to present functionality
 */
public class RemotePerson extends AbstractPerson implements Remote {
    private final Bank bank;

    public RemotePerson(String name, String surname, String passport, Bank bank) {
        super(name, surname, passport);
        this.bank = bank;
    }

    @Override
    public List<Account> getAccounts() throws RemoteException {
        return bank.getAccounts(this);
    }
}