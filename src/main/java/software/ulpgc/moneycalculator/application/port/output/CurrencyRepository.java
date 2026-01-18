package software.ulpgc.moneycalculator.application.port.output;

import software.ulpgc.moneycalculator.domain.model.Currency;

import java.util.List;
import java.util.Optional;

/**
 * Output port interface for accessing currency data from external data sources.
 *
 * <p>This interface defines the contract for currency data persistence operations
 * following the Hexagonal Architecture pattern. It represents an output port that
 * is implemented by infrastructure adapters (e.g., database repositories, REST API
 * clients, file system readers).</p>
 *
 * <p>The application core depends on this abstraction rather than concrete
 * implementations, ensuring independence from specific infrastructure concerns.</p>
 */
public interface CurrencyRepository {

    /**
     * Retrieves all available currencies from the data source.
     *
     * <p>This method fetches the complete list of currencies that the system
     * supports. The returned list should contain all currencies available for
     * exchange operations.</p>
     *
     * @return a list containing all available {@link Currency} objects;
     *         returns an empty list if no currencies are available
     * @throws RuntimeException if there's an error accessing the data source
     */
    List<Currency> findAll();

    /**
     * Searches for a specific currency by its ISO 4217 currency code.
     *
     * <p>This method performs a lookup for a currency using its standardized
     * three-letter code (e.g., "USD", "EUR", "GBP"). The search should be
     * case-insensitive.</p>
     *
     * @param code the ISO 4217 currency code to search for; must not be null
     * @return an {@link Optional} containing the matching {@link Currency} if found,
     *         or {@link Optional#empty()} if no currency with the given code exists
     * @throws RuntimeException if there's an error accessing the data source
     */
    Optional<Currency> findByCode(String code);
}