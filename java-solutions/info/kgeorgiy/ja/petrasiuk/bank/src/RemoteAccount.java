package info.kgeorgiy.ja.petrasiuk.bank.src;

import java.math.BigDecimal;

/**
 * Account which uses RMI to present functionality
 */
public class RemoteAccount implements Account {
    private final String id;
    private BigDecimal amount;
    private long fractionPart;

    public RemoteAccount(final String id) {
        this.id = id;
        amount = BigDecimal.ZERO;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public synchronized BigDecimal getAmount() {
        return amount;
    }

    @Override
    public synchronized void setAmount(final BigDecimal amount) {
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Negative amount not allowed");
        }
        System.out.println("Setting amount of money for account " + id);
        this.amount = amount;
    }
}
