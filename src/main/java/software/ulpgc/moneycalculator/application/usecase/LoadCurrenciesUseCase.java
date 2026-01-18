package software.ulpgc.moneycalculator.application.usecase;

import software.ulpgc.moneycalculator.application.port.input.GetCurrenciesQuery;
import software.ulpgc.moneycalculator.application.port.output.CurrencyRepository;
import software.ulpgc.moneycalculator.domain.exception.CurrencyNotFoundException;
import software.ulpgc.moneycalculator.domain.model.Currency;

import java.util.List;

/**
 * Use case implementation for loading and querying available currencies.
 *
 * <p>This class implements the {@link GetCurrenciesQuery} input port, providing
 * the business logic for retrieving currency information. It orchestrates the
 * interaction between the application layer and the infrastructure layer through
 * the {@link CurrencyRepository} output port.</p>
 *
 * <p>The use case follows the Hexagonal Architecture pattern by depending on
 * abstractions (ports) rather than concrete implementations, ensuring the
 * application core remains independent of infrastructure concerns.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Validate input parameters (currency codes)</li>
 *   <li>Delegate data retrieval to the repository</li>
 *   <li>Transform repository responses into domain objects</li>
 *   <li>Handle error cases with appropriate exceptions</li>
 * </ul>
 */
public class LoadCurrenciesUseCase implements GetCurrenciesQuery {

    /**
     * The repository used to access currency data from the infrastructure layer.
     * This dependency is injected through the constructor, following the
     * Dependency Inversion Principle.
     */
    private final CurrencyRepository currencyRepository;

    /**
     * Constructs a new LoadCurrenciesUseCase with the specified repository.
     *
     * <p>This constructor follows the Dependency Injection pattern, allowing
     * the infrastructure implementation to be provided from outside the
     * application core.</p>
     *
     * @param currencyRepository the repository implementation for accessing
     *                          currency data; must not be null
     * @throws NullPointerException if currencyRepository is null
     */
    public LoadCurrenciesUseCase(CurrencyRepository currencyRepository) {
        this.currencyRepository = currencyRepository;
    }

    /**
     * Retrieves all available currencies from the repository.
     *
     * <p>This method delegates directly to the repository without applying
     * additional business logic, as retrieving all currencies is a
     * straightforward data access operation.</p>
     *
     * @return a list of all {@link Currency} objects available in the system;
     *         never returns null, but may return an empty list
     * @throws RuntimeException if there's an error accessing the repository
     */
    @Override
    public List<Currency> getAllCurrencies() {
        return currencyRepository.findAll();
    }

    /**
     * Finds a specific currency by its ISO 4217 currency code.
     *
     * <p>This method performs input validation and normalization before
     * delegating to the repository. The currency code is trimmed of whitespace
     * and converted to uppercase to ensure consistent lookups regardless of
     * input format.</p>
     *
     * <p><strong>Validation rules:</strong></p>
     * <ul>
     *   <li>Currency code must not be null</li>
     *   <li>Currency code must not be empty or contain only whitespace</li>
     * </ul>
     *
     * @param code the ISO 4217 currency code to search for (e.g., "USD", "eur");
     *            case-insensitive and whitespace is trimmed
     * @return the {@link Currency} object matching the provided code
     * @throws IllegalArgumentException if the code is null, empty, or blank
     * @throws CurrencyNotFoundException if no currency with the given code
     *         exists in the repository
     */
    @Override
    public Currency findByCode(String code) {
        // Validate that the currency code is not null or blank
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }

        // Normalize the code by trimming whitespace and converting to uppercase
        // to ensure consistent repository queries
        return currencyRepository.findByCode(code.trim().toUpperCase())
                .orElseThrow(() -> new CurrencyNotFoundException(
                        "Currency not found: " + code
                ));
    }
}