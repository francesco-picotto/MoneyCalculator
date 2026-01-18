package software.ulpgc.moneycalculator.domain.model;

import software.ulpgc.moneycalculator.domain.exception.InvalidMoneyAmountException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Represents a monetary amount in a specific currency.
 *
 * <p>This is a <strong>Value Object</strong> in Domain-Driven Design terminology,
 * meaning it is:</p>
 * <ul>
 *   <li><strong>Immutable:</strong> Once created, the amount and currency cannot be changed</li>
 *   <li><strong>Self-validating:</strong> Invalid states cannot be constructed</li>
 *   <li><strong>Comparable by value:</strong> Two Money objects are equal if they have the same amount and currency</li>
 * </ul>
 *
 * <p><strong>Precision and Accuracy:</strong></p>
 * <p>Uses {@link BigDecimal} for decimal arithmetic to avoid floating-point precision errors
 * that are common with {@code double} or {@code float}. All amounts are normalized to 2 decimal
 * places using {@link RoundingMode#HALF_UP} rounding.</p>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * // Create money instances
 * Money tenDollars = Money.of(10.00, Currency.of("USD", "United States Dollar"));
 * Money fiveEuros = Money.of(5.00, Currency.of("EUR", "Euro"));
 *
 * // Arithmetic operations
 * Money fifteen = tenDollars.add(Money.of(5.00, usd));  // Same currency required
 * Money doubled = tenDollars.multiply(2.0);
 *
 * // Currency conversion
 * ExchangeRate usdToEur = ExchangeRate.of(usd, eur, BigDecimal.valueOf(0.85));
 * Money convertedAmount = tenDollars.convert(usdToEur);
 * }</pre>
 *
 * <p><strong>Thread Safety:</strong> Immutable and thread-safe</p>
 * <p><strong>Design Pattern:</strong> Value Object pattern</p>
 *
 * @author Money Calculator Team
 * @version 2.0
 * @since 1.0
 * @see Currency
 * @see ExchangeRate
 */
public final class Money {

    /** Number of decimal places for all monetary amounts */
    private static final int DECIMAL_PLACES = 2;

    /** Rounding mode used when normalizing amounts */
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /** The monetary amount, always normalized to 2 decimal places */
    private final BigDecimal amount;

    /** The currency of this monetary amount */
    private final Currency currency;

    /**
     * Private constructor to enforce use of factory methods.
     *
     * @param amount the monetary amount (will be validated and normalized)
     * @param currency the currency (will be validated)
     */
    private Money(BigDecimal amount, Currency currency) {
        this.amount = validateAndNormalize(amount);
        this.currency = validateCurrency(currency);
    }

    /**
     * Creates a Money instance from a double value.
     *
     * <p>This is a convenience factory method for creating Money from common
     * numeric types. The double is converted to BigDecimal internally.</p>
     *
     * @param amount the monetary amount as a double
     * @param currency the currency for this amount
     * @return a new Money instance
     * @throws InvalidMoneyAmountException if amount is null or negative
     * @throws IllegalArgumentException if currency is null
     */
    public static Money of(double amount, Currency currency) {
        return new Money(BigDecimal.valueOf(amount), currency);
    }

    /**
     * Creates a Money instance from a BigDecimal value.
     *
     * <p>This is the preferred factory method when precise decimal arithmetic
     * is required from the start.</p>
     *
     * @param amount the monetary amount as a BigDecimal
     * @param currency the currency for this amount
     * @return a new Money instance
     * @throws InvalidMoneyAmountException if amount is null or negative
     * @throws IllegalArgumentException if currency is null
     */
    public static Money of(BigDecimal amount, Currency currency) {
        return new Money(amount, currency);
    }

    /**
     * Creates a Money instance representing zero in the given currency.
     *
     * <p>Useful for initialization or as a starting point for accumulation.</p>
     *
     * @param currency the currency for the zero amount
     * @return a Money instance with amount 0.00
     * @throws IllegalArgumentException if currency is null
     */
    public static Money zero(Currency currency) {
        return new Money(BigDecimal.ZERO, currency);
    }

    /**
     * Validates and normalizes the monetary amount.
     *
     * <p>Ensures that:</p>
     * <ul>
     *   <li>Amount is not null</li>
     *   <li>Amount is not negative</li>
     *   <li>Amount is scaled to exactly 2 decimal places</li>
     * </ul>
     *
     * @param amount the amount to validate
     * @return the normalized amount with 2 decimal places
     * @throws InvalidMoneyAmountException if amount is null or negative
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

    /**
     * Validates that the currency is not null.
     *
     * @param currency the currency to validate
     * @return the validated currency
     * @throws IllegalArgumentException if currency is null
     */
    private Currency validateCurrency(Currency currency) {
        if (currency == null) {
            throw new IllegalArgumentException("Currency cannot be null");
        }
        return currency;
    }

    /**
     * Converts this money to another currency using the given exchange rate.
     *
     * <p>The conversion formula is: {@code convertedAmount = amount × rate}</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * Money usd100 = Money.of(100, usd);
     * ExchangeRate usdToEur = ExchangeRate.of(usd, eur, BigDecimal.valueOf(0.85));
     * Money eur85 = usd100.convert(usdToEur);  // 85.00 EUR
     * }</pre>
     *
     * @param exchangeRate the rate to use for conversion
     * @return the converted money in the target currency
     * @throws IllegalArgumentException if the exchange rate's source currency doesn't match this money's currency
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
     * Adds another monetary amount to this one.
     *
     * <p><strong>Important:</strong> Both amounts must be in the same currency.
     * You cannot add different currencies directly - convert first if needed.</p>
     *
     * @param other the money to add
     * @return a new Money instance with the sum
     * @throws IllegalArgumentException if currencies don't match
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
     * Subtracts another monetary amount from this one.
     *
     * <p><strong>Important:</strong> Both amounts must be in the same currency,
     * and the result must be non-negative.</p>
     *
     * @param other the money to subtract
     * @return a new Money instance with the difference
     * @throws IllegalArgumentException if currencies don't match
     * @throws InvalidMoneyAmountException if the result would be negative
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
     * Multiplies this monetary amount by a scalar factor.
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * Money price = Money.of(10.00, usd);
     * Money total = price.multiply(3.5);  // 35.00 USD
     * }</pre>
     *
     * @param factor the multiplication factor (must be non-negative)
     * @return a new Money instance with the product
     * @throws InvalidMoneyAmountException if factor is negative
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
     * Checks if this monetary amount is exactly zero.
     *
     * @return {@code true} if amount is 0.00, {@code false} otherwise
     */
    public boolean isZero() {
        return amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Checks if this monetary amount is greater than zero.
     *
     * @return {@code true} if amount is positive, {@code false} otherwise
     */
    public boolean isPositive() {
        return amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Compares this monetary amount with another.
     *
     * <p><strong>Important:</strong> Only amounts in the same currency can be compared.</p>
     *
     * @param other the money to compare with
     * @return a negative integer, zero, or a positive integer as this amount
     *         is less than, equal to, or greater than the specified amount
     * @throws IllegalArgumentException if currencies don't match
     */
    public int compareTo(Money other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                    "Cannot compare different currencies"
            );
        }
        return this.amount.compareTo(other.amount);
    }

    /**
     * Returns the monetary amount.
     *
     * @return the amount as a BigDecimal with 2 decimal places
     */
    public BigDecimal amount() {
        return amount;
    }

    /**
     * Returns the currency of this monetary amount.
     *
     * @return the currency
     */
    public Currency currency() {
        return currency;
    }

    /**
     * Compares this Money with another object for equality.
     *
     * <p>Two Money objects are equal if they have the same amount and currency.
     * Note that amounts are compared by value, not by reference.</p>
     *
     * @param obj the object to compare with
     * @return {@code true} if the objects are equal, {@code false} otherwise
     */
    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Money)) return false;
        Money money = (Money) obj;
        return amount.compareTo(money.amount) == 0 &&
                currency.equals(money.currency);
    }

    /**
     * Returns a hash code value for this Money.
     *
     * @return a hash code value consistent with equals()
     */
    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    /**
     * Returns a string representation of this Money.
     *
     * <p>Format: {@code "AMOUNT CURRENCY_CODE"} (e.g., "100.00 USD")</p>
     *
     * @return a string representation suitable for display
     */
    @Override
    public String toString() {
        return String.format("%.2f %s", amount, currency.code());
    }
}