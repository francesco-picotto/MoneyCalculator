package software.ulpgc.moneycalculator.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents an exchange rate between two currencies at a specific date.
 * This is an immutable value object.
 */
public final class ExchangeRate {
    private static final int RATE_SCALE = 6;
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    private final LocalDate date;
    private final Currency from;
    private final Currency to;
    private final BigDecimal rate;

    private ExchangeRate(LocalDate date, Currency from, Currency to, BigDecimal rate) {
        this.date = validateDate(date);
        this.from = validateCurrency(from, "from");
        this.to = validateCurrency(to, "to");
        validateDifferentCurrencies(from, to);
        this.rate = validateRate(rate);
    }

    public static ExchangeRate of(LocalDate date, Currency from, Currency to, BigDecimal rate) {
        return new ExchangeRate(date, from, to, rate);
    }

    public static ExchangeRate of(LocalDate date, Currency from, Currency to, double rate) {
        return new ExchangeRate(date, from, to, BigDecimal.valueOf(rate));
    }

    private LocalDate validateDate(LocalDate date) {
        if (date == null) {
            throw new IllegalArgumentException("Date cannot be null");
        }
        if (date.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Date cannot be in the future: " + date
            );
        }
        return date;
    }

    private Currency validateCurrency(Currency currency, String paramName) {
        if (currency == null) {
            throw new IllegalArgumentException(
                paramName + " currency cannot be null"
            );
        }
        return currency;
    }

    private void validateDifferentCurrencies(Currency from, Currency to) {
        if (from.equals(to)) {
            throw new IllegalArgumentException(
                "From and to currencies must be different, got: " + from.code()
            );
        }
    }

    private BigDecimal validateRate(BigDecimal rate) {
        if (rate == null) {
            throw new IllegalArgumentException("Exchange rate cannot be null");
        }
        if (rate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException(
                "Exchange rate must be positive, got: " + rate
            );
        }
        return rate.setScale(RATE_SCALE, ROUNDING_MODE);
    }

    public LocalDate date() {
        return date;
    }

    public Currency from() {
        return from;
    }

    public Currency to() {
        return to;
    }

    public BigDecimal rate() {
        return rate;
    }

    /**
     * Checks if this exchange rate is expired based on the given validity period.
     * 
     * @param daysValid number of days the rate is considered valid
     * @return true if the rate is expired
     */
    public boolean isExpired(int daysValid) {
        if (daysValid <= 0) {
            throw new IllegalArgumentException("Days valid must be positive");
        }
        return LocalDate.now().isAfter(date.plusDays(daysValid));
    }

    /**
     * Creates the inverse exchange rate (swaps from/to currencies).
     * 
     * @return the inverse exchange rate
     */
    public ExchangeRate inverse() {
        BigDecimal inverseRate = BigDecimal.ONE.divide(rate, RATE_SCALE, ROUNDING_MODE);
        return new ExchangeRate(date, to, from, inverseRate);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ExchangeRate)) return false;
        ExchangeRate that = (ExchangeRate) obj;
        return date.equals(that.date) &&
               from.equals(that.from) &&
               to.equals(that.to) &&
               rate.compareTo(that.rate) == 0;
    }

    @Override
    public int hashCode() {
        return Objects.hash(date, from, to, rate);
    }

    @Override
    public String toString() {
        return String.format("1 %s = %s %s (as of %s)", 
            from.code(), rate, to.code(), date);
    }
}
