package software.ulpgc.moneycalculator.application.port.input;

import software.ulpgc.moneycalculator.domain.model.Currency;

import java.util.List;

/**
 * Input port interface for querying available currencies in the system.
 *
 * <p>This interface represents a query use case in the CQRS (Command Query
 * Responsibility Segregation) pattern. It provides read-only operations for
 * retrieving currency information without modifying system state.</p>
 *
 * <p>As an input port in the Hexagonal Architecture, this interface is defined
 * in the application layer and invoked by external adapters (e.g., UI controllers,
 * REST API endpoints). The application core implements this interface to provide
 * currency query functionality.</p>
 *
 * @author Software ULPGC
 * @version 1.0
 * @since 1.0
 */
public interface GetCurrenciesQuery {

    /**
     * Retrieves all currencies available in the system for exchange operations.
     *
     * <p>This method provides the complete catalog of currencies that users can
     * select for money conversion. The returned list typically includes all major
     * world currencies and may include additional supported currencies.</p>
     *
     * @return a list containing all available {@link Currency} objects;
     *         never returns null, but may return an empty list if no currencies
     *         are configured in the system
     */
    List<Currency> getAllCurrencies();

    /**
     * Finds a specific currency by its ISO 4217 currency code.
     *
     * <p>This method performs a lookup for a currency using its standardized
     * three-letter code (e.g., "USD" for US Dollar, "EUR" for Euro). The lookup
     * should be case-insensitive and handle leading/trailing whitespace.</p>
     *
     * @param code the ISO 4217 currency code to search for; must not be null or blank
     * @return the {@link Currency} object matching the provided code
     * @throws IllegalArgumentException if the code is null or blank
     * @throws software.ulpgc.moneycalculator.domain.exception.CurrencyNotFoundException
     *         if no currency with the given code exists in the system
     */
    Currency findByCode(String code);
}