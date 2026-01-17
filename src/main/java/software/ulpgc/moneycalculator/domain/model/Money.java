package software.ulpgc.moneycalculator.domain.model;

import software.ulpgc.moneycalculator.domain.exception.InvalidMoneyAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents money with an amount and currency.
 * Uses BigDecimal for precise decimal arithmetic.
 * This is an immutable value object.
 *
 * FIXED: Now properly uses InvalidMoneyAmountException for validation errors
 */
public final class Money {
    private static final int DECIMAL_PLACES = 2;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final BigDecimal amount;
    private final Currency currency;

    private Money(BigDecimal amount, Currency currency) {
        this.amount = validateAndNormalize(amount);
        this.currency = validateCurrency(currency);
    }

    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * FIXED: Now throws InvalidMoneyAmountException instead of generic IllegalArgumentException
     */
    private BigDecimal validateAndNormalize(BigDecimal amount) {
        if (amount == null) {
            throw new InvalidMoneyAmountException("Amount cannot be null");
        }
        if (amount.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMoneyAmountException(
                    "Amount cannot be negative: " + amount
            );
        }
        return amount.setScale(DECIMAL_PLACES, ROUNDING_MODE);
    }

    private Currency validateCurrency(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        return currency;
    }

    /**
     * Converts this money to another currency using the given exchange rate.
     *
     * @param exchangeRate the rate to use for conversion
     * @return the converted money
     * @throws IllegalArgumentException if currencies don't match
     */
    public Money convert(ExchangeRate exchangeRate) {
        if (!this.currency.equals(exchangeRate.from())) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot convert: currency mismatch (expected %s, got %s)",
                            exchangeRate.from().code(),
                            this.currency.code()
                    )
            );
        }

        BigDecimal convertedAmount = amount
                .multiply(exchangeRate.rate())
                .setScale(DECIMAL_PLACES, ROUNDING_MODE);

        return Money.of(convertedAmount, exchangeRate.to());
    }

    /**
     * Add two money amounts (must be same currency)
     */
    public Money add(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot add different currencies: %s and %s",
                            this.currency.code(),
                            other.currency.code()
                    )
            );
        }
        return Money.of(this.amount.add(other.amount), this.currency);
    }

    /**
     * Subtract two money amounts (must be same currency)
     */
    public Money subtract(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    String.format(
                            "Cannot subtract different currencies: %s and %s",
                            this.currency.code(),
                            other.currency.code()
                    )
            );
        }
        BigDecimal result = this.amount.subtract(other.amount);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new InvalidMoneyAmountException(
                    "Subtraction would result in negative amount: " + result
            );
        }
        return Money.of(result, this.currency);
    }

    /**
     * Multiply money by a factor
     */
    public Money multiply(double factor) {
        if (factor < 0) {
            throw new InvalidMoneyAmountException(
                    "Cannot multiply by negative factor: " + factor
            );
        }
        return Money.of(
                this.amount.multiply(BigDecimal.valueOf(factor)),
                this.currency
        );
    }

    /**
     * Check if this money is zero
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Check if this money is positive
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Compare amounts (only works for same currency)
     */
    public int compareTo(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot compare different currencies"
            );
        }
        return this.amount.compareTo(other.amount);
    }

    public BigDecimal amount() {
        return amount;
    }

    public Currency currency() {
        return currency;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Money)) return false;
        Money money = (Money) obj;
        return amount.compareTo(money.amount) == 0 &&
                currency.equals(money.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return String.format("%.2f %s", amount, currency.code());
    }
}