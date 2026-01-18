package software.ulpgc.moneycalculator.domain.model;

import java.util.Objects;

/**
 * Represents a currency in the monetary system.
 *
 * <p>This class is an immutable Value Object following Domain-Driven Design principles.
 * It encapsulates the concept of a currency with its standardized code and display name,
 * ensuring that all Currency instances are valid and consistent throughout the application.</p>
 *
 * <p><strong>Value Object Characteristics:</strong></p>
 * <ul>
 *   <li><strong>Immutability:</strong> Once created, currency properties cannot be modified</li>
 *   <li><strong>Self-Validation:</strong> Invalid currencies cannot be constructed</li>
 *   <li><strong>Equality by Value:</strong> Two currencies are equal if their codes match</li>
 *   <li><strong>No Identity:</strong> Currency instances have no unique identifier beyond their code</li>
 * </ul>
 *
 * <p><strong>ISO 4217 Standard:</strong></p>
 * <p>Currency codes follow the ISO 4217 international standard, which defines three-letter
 * alphabetic codes for currencies. Examples include:</p>
 * <ul>
 *   <li><strong>USD</strong> - United States Dollar</li>
 *   <li><strong>EUR</strong> - Euro</li>
 *   <li><strong>GBP</strong> - British Pound Sterling</li>
 *   <li><strong>JPY</strong> - Japanese Yen</li>
 *   <li><strong>CHF</strong> - Swiss Franc</li>
 * </ul>
 *
 * <p><strong>Design Decisions:</strong></p>
 * <ul>
 *   <li><strong>Factory Method Pattern:</strong> Uses {@code of()} instead of public constructor
 *       to provide a clear API and enable future extension (e.g., caching, validation)</li>
 *   <li><strong>Automatic Normalization:</strong> Currency codes are automatically trimmed and
 *       converted to uppercase for consistency</li>
 *   <li><strong>Fail-Fast Validation:</strong> Invalid inputs are rejected immediately with
 *       clear error messages</li>
 * </ul>
 *
 * <p><strong>Thread Safety:</strong></p>
 * <p>This class is immutable and therefore inherently thread-safe. Currency instances can
 * be safely shared across threads without synchronization.</p>
 *
 * <p><strong>Usage Examples:</strong></p>
 * <pre>{@code
 * // Create currency instances
 * Currency usd = Currency.of("USD", "United States Dollar");
 * Currency eur = Currency.of("EUR", "Euro");
 * Currency gbp = Currency.of("gbp", "British Pound"); // Automatically normalized to "GBP"
 *
 * // Equality comparison (by code only)
 * Currency usd1 = Currency.of("USD", "United States Dollar");
 * Currency usd2 = Currency.of("USD", "US Dollar");
 * assert usd1.equals(usd2); // true - same code, different names
 *
 * // Use in collections
 * Set<Currency> currencies = new HashSet<>();
 * currencies.add(usd);
 * currencies.add(eur);
 *
 * // Display to users
 * System.out.println(usd); // "USD (United States Dollar)"
 * }</pre>
 *
 */
public final class Currency {

    /**
     * The three-letter ISO 4217 currency code in uppercase.
     * This is the primary identifier for the currency and is used for equality comparisons.
     * Examples: "USD", "EUR", "GBP"
     */
    private final String code;

    /**
     * The human-readable name of the currency.
     * This is for display purposes and does not affect equality.
     * Examples: "United States Dollar", "Euro", "British Pound Sterling"
     */
    private final String name;

    /**
     * Private constructor to enforce use of factory method.
     *
     * <p>This constructor performs validation and normalization of both the currency
     * code and name to ensure all Currency instances are in a valid, consistent state.</p>
     *
     * @param code the ISO 4217 currency code (will be validated and normalized)
     * @param name the human-readable currency name (will be validated and trimmed)
     * @throws IllegalArgumentException if code or name is invalid
     */
    private Currency(String code, String name) {
        this.code = validateCode(code);
        this.name = validateName(name);
    }

    /**
     * Factory method to create a Currency instance.
     *
     * <p>This is the primary way to create Currency objects. The method validates and
     * normalizes the inputs, ensuring that only valid currencies can be created.</p>
     *
     * <p><strong>Normalization Rules:</strong></p>
     * <ul>
     *   <li>Currency code is trimmed of whitespace and converted to uppercase</li>
     *   <li>Currency name is trimmed of leading/trailing whitespace</li>
     * </ul>
     *
     * <p><strong>Examples:</strong></p>
     * <pre>{@code
     * // Standard usage
     * Currency usd = Currency.of("USD", "United States Dollar");
     *
     * // Automatic normalization
     * Currency eur = Currency.of(" eur ", "Euro"); // Becomes "EUR"
     * Currency gbp = Currency.of("Gbp", "British Pound"); // Becomes "GBP"
     * }</pre>
     *
     * @param code the ISO 4217 currency code (3 letters, case-insensitive); must not be null
     * @param name the human-readable currency name; must not be null or blank
     * @return a new Currency instance with validated and normalized values
     * @throws IllegalArgumentException if code is not exactly 3 characters after trimming,
     *                                 or if code or name is null or blank
     */
    public static Currency of(String code, String name) {
        return new Currency(code, name);
    }

    /**
     * Validates and normalizes the currency code according to ISO 4217 standards.
     *
     * <p>This method ensures that:</p>
     * <ul>
     *   <li>The code is not null or blank</li>
     *   <li>The code is exactly 3 characters after trimming and uppercasing</li>
     *   <li>The code is converted to uppercase for consistency</li>
     * </ul>
     *
     * <p><strong>Validation Process:</strong></p>
     * <ol>
     *   <li>Check for null or blank</li>
     *   <li>Trim whitespace</li>
     *   <li>Convert to uppercase</li>
     *   <li>Verify length is exactly 3</li>
     * </ol>
     *
     * @param code the currency code to validate
     * @return the validated and normalized code (trimmed and uppercase)
     * @throws IllegalArgumentException if code is null, blank, or not exactly 3 characters
     */
    private String validateCode(String code) {
        // Check for null or blank input
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }

        // Normalize: trim whitespace and convert to uppercase
        String trimmed = code.trim().toUpperCase();

        // Validate length according to ISO 4217 standard
        if (trimmed.length() != 3) {
            throw new IllegalArgumentException(
                    "Currency code must be exactly 3 characters, got: " + code
            );
        }

        return trimmed;
    }

    /**
     * Validates and normalizes the currency name.
     *
     * <p>This method ensures that:</p>
     * <ul>
     *   <li>The name is not null or blank</li>
     *   <li>Leading and trailing whitespace is removed</li>
     * </ul>
     *
     * @param name the currency name to validate
     * @return the validated and normalized name (trimmed)
     * @throws IllegalArgumentException if name is null or blank
     */
    private String validateName(String name) {
        // Check for null or blank input
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Currency name cannot be null or empty");
        }

        // Normalize by trimming whitespace
        return name.trim();
    }

    /**
     * Returns the ISO 4217 currency code.
     *
     * <p>The code is always returned in uppercase, consisting of exactly 3 letters.
     * This is the primary identifier for the currency.</p>
     *
     * @return the three-letter currency code in uppercase (e.g., "USD", "EUR")
     */
    public String code() {
        return code;
    }

    /**
     * Returns the human-readable currency name.
     *
     * <p>This is the full name of the currency, suitable for display to users in
     * UI components like dropdowns, labels, and reports.</p>
     *
     * @return the currency name (e.g., "United States Dollar", "Euro")
     */
    public String name() {
        return name;
    }

    /**
     * Compares this currency with another object for equality.
     *
     * <p><strong>Equality Semantics:</strong></p>
     * <p>Two Currency objects are considered equal if and only if they have the same
     * currency code. The currency name is <em>not</em> considered in equality comparisons,
     * as different sources might use slightly different names for the same currency.</p>
     *
     * <p><strong>Examples:</strong></p>
     * <pre>{@code
     * Currency usd1 = Currency.of("USD", "United States Dollar");
     * Currency usd2 = Currency.of("USD", "US Dollar");
     * Currency eur = Currency.of("EUR", "Euro");
     *
     * assert usd1.equals(usd2); // true - same code
     * assert !usd1.equals(eur); // false - different codes
     * assert !usd1.equals(null); // false
     * assert !usd1.equals("USD"); // false - different type
     * }</pre>
     *
     * <p><strong>Contract Compliance:</strong></p>
     * <p>This method satisfies the equals contract:</p>
     * <ul>
     *   <li><strong>Reflexive:</strong> {@code x.equals(x)} returns true</li>
     *   <li><strong>Symmetric:</strong> {@code x.equals(y)} ⟺ {@code y.equals(x)}</li>
     *   <li><strong>Transitive:</strong> If {@code x.equals(y)} and {@code y.equals(z)},
     *       then {@code x.equals(z)}</li>
     *   <li><strong>Consistent:</strong> Multiple invocations return the same result</li>
     *   <li><strong>Null-safe:</strong> {@code x.equals(null)} returns false</li>
     * </ul>
     *
     * @param obj the object to compare with
     * @return {@code true} if the objects have the same currency code, {@code false} otherwise
     * @see #hashCode()
     */
    @Override
    public boolean equals(Object obj) {
        // Identity check - same object reference
        if (this == obj) return true;

        // Type check - must be Currency instance
        if (!(obj instanceof Currency)) return false;

        // Cast and compare codes
        Currency currency = (Currency) obj;
        return code.equals(currency.code);
    }

    /**
     * Returns a hash code value for this currency.
     *
     * <p>The hash code is computed based solely on the currency code, consistent with
     * the {@link #equals(Object)} implementation. This ensures that currencies with
     * the same code have the same hash code, as required by the hash code contract.</p>
     *
     * <p><strong>Hash Code Contract:</strong></p>
     * <ul>
     *   <li>If {@code x.equals(y)}, then {@code x.hashCode() == y.hashCode()}</li>
     *   <li>Multiple invocations on the same object return the same value</li>
     *   <li>The hash code should be reasonably distributed to minimize collisions</li>
     * </ul>
     *
     * <p><strong>Collection Usage:</strong></p>
     * <p>This hash code implementation allows Currency objects to be used efficiently
     * in hash-based collections:</p>
     * <pre>{@code
     * Set<Currency> currencies = new HashSet<>();
     * currencies.add(Currency.of("USD", "United States Dollar"));
     * currencies.add(Currency.of("USD", "US Dollar")); // Won't add duplicate
     *
     * Map<Currency, ExchangeRate> rates = new HashMap<>();
     * rates.put(usd, someRate);
     * }</pre>
     *
     * @return a hash code value consistent with the equals method
     * @see #equals(Object)
     */
    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    /**
     * Returns a string representation of this currency.
     *
     * <p>The format is: {@code "CODE (Name)"}, which provides both the standardized
     * code and the human-readable name in a concise format suitable for logging,
     * debugging, and user display.</p>
     *
     * <p><strong>Examples:</strong></p>
     * <pre>
     * "USD (United States Dollar)"
     * "EUR (Euro)"
     * "GBP (British Pound Sterling)"
     * "JPY (Japanese Yen)"
     * </pre>
     *
     * <p><strong>Use Cases:</strong></p>
     * <ul>
     *   <li>Logging and debugging output</li>
     *   <li>Display in dropdown menus and selection lists</li>
     *   <li>Error messages and validation feedback</li>
     *   <li>Report generation</li>
     * </ul>
     *
     * @return a string in the format "CODE (Name)"
     */
    @Override
    public String toString() {
        return String.format("%s (%s)", code, name);
    }
}