package info.kgeorgiy.ja.petrasiuk.bank.src;

import java.rmi.RemoteException;
import java.util.Objects;

/**
 * Abstract class which represents basic {@link Person} logic
 */
public abstract class AbstractPerson implements Person {
    String name;
    String surname;
    String passport;

    public AbstractPerson(String name, String surname, String passport) {
        this.name = name;
        this.surname = surname;
        this.passport = passport;
    }


    @Override
    public String getName() throws RemoteException {
        return name;
    }

    @Override
    public String getSurname() {
        return surname;
    }

    @Override
    public String getPassport() {
        return passport;
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, surname, passport);
    }
}
