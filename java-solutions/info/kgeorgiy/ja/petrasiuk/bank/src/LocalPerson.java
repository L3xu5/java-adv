package info.kgeorgiy.ja.petrasiuk.bank.src;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.List;

/**
 * {@link info.kgeorgiy.ja.petrasiuk.bank.src.Person} entity which can be used without connection to {@link info.kgeorgiy.ja.petrasiuk.bank.src.Server}
 */
public class LocalPerson extends AbstractPerson implements Serializable {
    List<Account> accounts;

    // :NOTE: на конструкторах лучше тоже писать документацию, аналогично для всех других классов
    public LocalPerson(String name, String surname, String passport, List<Account> accounts) {
        super(name, surname, passport);
        this.accounts = accounts;
    }

    @Override
    public List<Account> getAccounts() throws RemoteException {
        return accounts;
    }
}
