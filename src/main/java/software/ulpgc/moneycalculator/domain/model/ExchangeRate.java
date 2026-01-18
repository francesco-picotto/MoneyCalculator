package software.ulpgc.moneycalculator.domain.model;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;

/**
 * Represents an exchange rate between two currencies at a specific point in time.
 *
 * <p>This class is an immutable Value Object in Domain-Driven Design terms, encapsulating
 * the conversion rate from one currency to another along with temporal information. Exchange
 * rates are fundamental to currency conversion operations and are typically obtained from
 * external financial data providers.</p>
 *
 * <p><strong>Value Object Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Immutability:</strong> Once created, no properties can be modified</li>
 *   <li><strong>Self-Validation:</strong> Invalid exchange rates cannot be constructed</li>
 *   <li><strong>Equality by Value:</strong> Two rates are equal if all properties match</li>
 *   <li><strong>No Identity:</strong> Exchange rates have no unique identifier beyond their values</li>
 * </ul>
 *
 * <p><strong>Precision and Accuracy:</strong></p>
 * <p>Exchange rates are stored with 6 decimal places of precision using {@link BigDecimal}
 * to avoid floating-point arithmetic errors. This precision is sufficient for most currency
 * exchange operations while maintaining reasonable performance and storage requirements.</p>
 *
 * <p><strong>Business Rules Enforced:</strong></p>
 * <ul>
 *   <li>Exchange rate must be positive (greater than zero)</li>
 *   <li>Source and target currencies must be different</li>
 *   <li>Date cannot be null or in the future</li>
 *   <li>Rate precision is normalized to 6 decimal places</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This class is immutable and therefore inherently thread-safe. ExchangeRate instances
 * can be safely shared across threads without synchronization.</p>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * // Create exchange rate from BigDecimal
 * Currency usd = Currency.of("USD", "United States Dollar");
 * Currency eur = Currency.of("EUR", "Euro");
 * ExchangeRate rate = ExchangeRate.of(
 *     LocalDate.now(),
 *     usd,
 *     eur,
 *     BigDecimal.valueOf(0.85)
 * );
 *
 * // Create from double (convenience method)
 * ExchangeRate rateFromDouble = ExchangeRate.of(
 *     LocalDate.now(),
 *     usd,
 *     eur,
 *     0.85
 * );
 *
 * // Get inverse rate (EUR to USD)
 * ExchangeRate inverseRate = rate.inverse();
 *
 * // Check if rate is expired
 * boolean isOld = rate.isExpired(30); // Expired after 30 days
 *
 * // Use in conversion
 * Money dollars = Money.of(100, usd);
 * Money euros = dollars.convert(rate);
 * }</pre>
 */
public final class ExchangeRate {

    /**
     * Number of decimal places for exchange rate precision.
     * 6 decimal places provides sufficient accuracy for most currency conversions
     * while avoiding excessive precision that could lead to rounding artifacts.
     */
    private static final int RATE_SCALE = 6;

    /**
     * Rounding mode used when normalizing exchange rates.
     * HALF_UP is the standard rounding mode used in financial calculations,
     * rounding 0.5 and above up to the next integer.
     */
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;

    /**
     * The date when this exchange rate was valid.
     * Typically represents the date the rate was retrieved or last updated.
     * Must not be null and cannot be in the future.
     */
    private final LocalDate date;

    /**
     * The source currency - the currency being converted from.
     * Must not be null and must be different from the target currency.
     */
    private final Currency from;

    /**
     * The target currency - the currency being converted to.
     * Must not be null and must be different from the source currency.
     */
    private final Currency to;

    /**
     * The exchange rate value - how much of the target currency equals one unit
     * of the source currency.
     *
     * <p>For example, if 1 USD = 0.85 EUR, then:</p>
     * <ul>
     *   <li>from = USD</li>
     *   <li>to = EUR</li>
     *   <li>rate = 0.85</li>
     * </ul>
     *
     * <p>The rate is always positive and normalized to 6 decimal places.</p>
     */
    private final BigDecimal rate;

    /**
     * Private constructor to enforce use of factory methods.
     *
     * <p>Performs comprehensive validation of all parameters to ensure the exchange
     * rate is in a valid, consistent state. All validation failures result in
     * {@link IllegalArgumentException} with descriptive messages.</p>
     *
     * @param date the date the rate is valid for
     * @param from the source currency
     * @param to the target currency
     * @param rate the exchange rate value
     * @throws IllegalArgumentException if any validation fails
     */
    private ExchangeRate(LocalDate date, Currency from, Currency to, BigDecimal rate) {
        this.date = validateDate(date);
        this.from = validateCurrency(from, "from");
        this.to = validateCurrency(to, "to");
        validateDifferentCurrencies(from, to);
        this.rate = validateRate(rate);
    }

    /**
     * Factory method to create an ExchangeRate from a BigDecimal rate value.
     *
     * <p>This is the preferred factory method when working with precise decimal values,
     * as it avoids any potential floating-point conversion issues.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * ExchangeRate rate = ExchangeRate.of(
     *     LocalDate.now(),
     *     usd,
     *     eur,
     *     new BigDecimal("0.850000")
     * );
     * }</pre>
     *
     * @param date the date when this rate is valid; must not be null or in the future
     * @param from the source currency; must not be null
     * @param to the target currency; must not be null and different from source
     * @param rate the exchange rate value; must be positive
     * @return a new ExchangeRate instance
     * @throws IllegalArgumentException if any validation fails
     */
    public static ExchangeRate of(LocalDate date, Currency from, Currency to, BigDecimal rate) {
        return new ExchangeRate(date, from, to, rate);
    }

    /**
     * Factory method to create an ExchangeRate from a double rate value.
     *
     * <p>This is a convenience factory method for common cases where exchange rates
     * are provided as simple decimal numbers. The double is converted to BigDecimal
     * internally for precise calculations.</p>
     *
     * <p><strong>Note:</strong> While convenient, be aware that floating-point literals
     * like {@code 0.1} cannot be exactly represented in binary floating-point. For
     * maximum precision, use the {@link #of(LocalDate, Currency, Currency, BigDecimal)}
     * method with a BigDecimal constructed from a string.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * ExchangeRate rate = ExchangeRate.of(
     *     LocalDate.now(),
     *     usd,
     *     eur,
     *     0.85
     * );
     * }</pre>
     *
     * @param date the date when this rate is valid; must not be null or in the future
     * @param from the source currency; must not be null
     * @param to the target currency; must not be null and different from source
     * @param rate the exchange rate value as a double; must be positive
     * @return a new ExchangeRate instance
     * @throws IllegalArgumentException if any validation fails
     */
    public static ExchangeRate of(LocalDate date, Currency from, Currency to, double rate) {
        return new ExchangeRate(date, from, to, BigDecimal.valueOf(rate));
    }

    /**
     * Validates that the date is not null and not in the future.
     *
     * <p>Exchange rates represent historical or current data, never future projections.
     * This validation ensures temporal consistency.</p>
     *
     * @param date the date to validate
     * @return the validated date
     * @throws IllegalArgumentException if date is null or after the current date
     */
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

    /**
     * Validates that a currency is not null.
     *
     * @param currency the currency to validate
     * @param paramName the parameter name for error messaging ("from" or "to")
     * @return the validated currency
     * @throws IllegalArgumentException if currency is null
     */
    private Currency validateCurrency(Currency currency, String paramName) {
        if (currency == null) {
            throw new IllegalArgumentException(
                    paramName + " currency cannot be null"
            );
        }
        return currency;
    }

    /**
     * Validates that the source and target currencies are different.
     *
     * <p>An exchange rate between the same currency makes no semantic sense
     * (it would always be 1.0) and likely indicates a programming error.</p>
     *
     * @param from the source currency
     * @param to the target currency
     * @throws IllegalArgumentException if currencies are the same
     */
    private void validateDifferentCurrencies(Currency from, Currency to) {
        if (from.equals(to)) {
            throw new IllegalArgumentException(
                    "From and to currencies must be different, got: " + from.code()
            );
        }
    }

    /**
     * Validates and normalizes the exchange rate value.
     *
     * <p>This method ensures the rate is:</p>
     * <ul>
     *   <li>Not null</li>
     *   <li>Positive (greater than zero)</li>
     *   <li>Normalized to exactly 6 decimal places</li>
     * </ul>
     *
     * @param rate the rate value to validate
     * @return the validated and normalized rate
     * @throws IllegalArgumentException if rate is null or not positive
     */
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

    /**
     * Returns the date when this exchange rate was valid.
     *
     * @return the rate's validity date
     */
    public LocalDate date() {
        return date;
    }

    /**
     * Returns the source currency (the currency being converted from).
     *
     * @return the source currency
     */
    public Currency from() {
        return from;
    }

    /**
     * Returns the target currency (the currency being converted to).
     *
     * @return the target currency
     */
    public Currency to() {
        return to;
    }

    /**
     * Returns the exchange rate value.
     *
     * <p>This represents how much of the target currency equals one unit of the
     * source currency. For example, if 1 USD = 0.85 EUR, this method returns 0.85.</p>
     *
     * @return the exchange rate value with 6 decimal places of precision
     */
    public BigDecimal rate() {
        return rate;
    }

    /**
     * Checks if this exchange rate has expired based on a validity period.
     *
     * <p>This method is useful for determining if cached exchange rates should be
     * refreshed. Different applications may have different requirements for rate
     * freshness - some may accept rates that are hours old, while others may require
     * rates no more than minutes old.</p>
     *
     * <p><strong>Example Usage:</strong></p>
     * <pre>{@code
     * // Check if rate is more than 1 day old
     * if (rate.isExpired(1)) {
     *     rate = fetchFreshRate();
     * }
     *
     * // Check if rate is more than 1 hour old (using fractional days)
     * if (rate.isExpired(0)) { // 0 days means any time has passed
     *     rate = fetchFreshRate();
     * }
     * }</pre>
     *
     * @param daysValid number of days the rate should be considered valid; must be positive
     * @return {@code true} if the current date is after (rate date + daysValid), {@code false} otherwise
     * @throws IllegalArgumentException if daysValid is not positive
     */
    public boolean isExpired(int daysValid) {
        if (daysValid <= 0) {
            throw new IllegalArgumentException("Days valid must be positive");
        }
        return LocalDate.now().isAfter(date.plusDays(daysValid));
    }

    /**
     * Creates the inverse exchange rate by swapping the currencies and inverting the rate.
     *
     * <p>If this rate represents "1 USD = 0.85 EUR", the inverse represents "1 EUR = 1.176471 USD".
     * This is useful when you need to convert in the opposite direction without fetching a new rate.</p>
     *
     * <p><strong>Mathematical Formula:</strong></p>
     * <pre>
     * If rate(A → B) = r, then rate(B → A) = 1/r
     * </pre>
     *
     * <p><strong>Precision Note:</strong></p>
     * <p>The inverse rate is calculated with 6 decimal places of precision. Due to rounding,
     * applying a rate and then its inverse may not return exactly the original amount, though
     * the difference will be negligible for practical purposes.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * // Original rate: USD to EUR
     * ExchangeRate usdToEur = ExchangeRate.of(LocalDate.now(), usd, eur, 0.85);
     *
     * // Get inverse: EUR to USD
     * ExchangeRate eurToUsd = usdToEur.inverse();
     * // eurToUsd.rate() ≈ 1.176471
     *
     * // Verify currencies are swapped
     * assert eurToUsd.from().equals(eur);
     * assert eurToUsd.to().equals(usd);
     * }</pre>
     *
     * @return a new ExchangeRate with swapped currencies and inverted rate
     */
    public ExchangeRate inverse() {
        BigDecimal inverseRate = BigDecimal.ONE.divide(rate, RATE_SCALE, ROUNDING_MODE);
        return new ExchangeRate(date, to, from, inverseRate);
    }

    /**
     * Compares this exchange rate with another object for equality.
     *
     * <p>Two ExchangeRate objects are considered equal if all their properties match:
     * date, source currency, target currency, and rate value. This strict equality
     * ensures that exchange rates used in calculations are exactly equivalent.</p>
     *
     * <p><strong>Rate Comparison:</strong></p>
     * <p>Rates are compared using {@code BigDecimal.compareTo()} rather than {@code equals()}
     * to handle different scales correctly. For example, 0.85 and 0.850000 are considered
     * equal even though they have different scales.</p>
     *
     * @param obj the object to compare with
     * @return {@code true} if all properties are equal, {@code false} otherwise
     * @see #hashCode()
     */
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

    /**
     * Returns a hash code value for this exchange rate.
     *
     * <p>The hash code is computed from all properties (date, from, to, rate) to be
     * consistent with the {@link #equals(Object)} implementation.</p>
     *
     * @return a hash code value for this object
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        return Objects.hash(date, from, to, rate);
    }

    /**
     * Returns a human-readable string representation of this exchange rate.
     *
     * <p>The format is: {@code "1 FROM = RATE TO (as of DATE)"}, which clearly
     * shows the conversion relationship and temporal validity.</p>
     *
     * <p><strong>Examples:</strong></p>
     * <pre>
     * "1 USD = 0.850000 EUR (as of 2025-01-18)"
     * "1 GBP = 1.270000 USD (as of 2025-01-15)"
     * "1 EUR = 1.176471 USD (as of 2025-01-18)"
     * </pre>
     *
     * @return a formatted string suitable for display and logging
     */
    @Override
    public String toString() {
        return String.format("1 %s = %s %s (as of %s)",
                from.code(), rate, to.code(), date);
    }
}