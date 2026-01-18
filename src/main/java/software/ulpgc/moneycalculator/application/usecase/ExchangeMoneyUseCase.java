package software.ulpgc.moneycalculator.application.usecase;

import software.ulpgc.moneycalculator.application.port.input.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.application.port.output.ExchangeRateProvider;
import software.ulpgc.moneycalculator.application.port.output.MoneyPresenter;
import software.ulpgc.moneycalculator.domain.exception.ExchangeRateUnavailableException;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.ExchangeRate;
import software.ulpgc.moneycalculator.domain.model.Money;

/**
 * Use case implementation for exchanging money between different currencies.
 *
 * <p>This class implements the {@link ExchangeMoneyCommand} input port, orchestrating
 * the complete money exchange workflow. It coordinates between multiple output ports
 * to fetch exchange rates, perform conversions, and present results to the user.</p>
 *
 * <p>Following the Hexagonal Architecture pattern, this use case contains orchestration
 * logic but delegates actual business rules to domain objects. The {@link Money} domain
 * entity contains the conversion calculation logic, while this use case focuses on
 * coordinating the flow and handling various error scenarios.</p>
 *
 * <p><strong>Workflow:</strong></p>
 * <ol>
 *   <li>Validate input parameters (source money and target currency)</li>
 *   <li>Fetch the current exchange rate from the provider</li>
 *   <li>Delegate conversion calculation to the Money domain object</li>
 *   <li>Present the result through the presenter (success or error)</li>
 * </ol>
 *
 * <p><strong>Error Handling:</strong></p>
 * <p>This use case implements comprehensive error handling, catching specific
 * exceptions and presenting user-friendly error messages through the presenter.
 * It ensures that all failure scenarios are gracefully communicated to the user.</p>
 *
 * @author Software ULPGC
 * @version 1.0
 * @since 1.0
 */
public class ExchangeMoneyUseCase implements ExchangeMoneyCommand {

    /**
     * Provider for fetching current exchange rates between currencies.
     * This output port is implemented by infrastructure adapters that connect
     * to external exchange rate APIs or data sources.
     */
    private final ExchangeRateProvider exchangeRateProvider;

    /**
     * Presenter for displaying conversion results or error messages to the user.
     * This output port is implemented by UI adapters that handle the presentation
     * logic for different interface types (desktop, web, mobile).
     */
    private final MoneyPresenter moneyPresenter;

    /**
     * Constructs a new ExchangeMoneyUseCase with the required dependencies.
     *
     * <p>This constructor follows the Dependency Injection pattern, allowing
     * infrastructure implementations to be provided from outside the application
     * core. Both dependencies are required for the use case to function.</p>
     *
     * @param exchangeRateProvider the provider for fetching exchange rates;
     *                            must not be null
     * @param moneyPresenter the presenter for displaying results to the user;
     *                      must not be null
     * @throws NullPointerException if any parameter is null
     */
    public ExchangeMoneyUseCase(
            ExchangeRateProvider exchangeRateProvider,
            MoneyPresenter moneyPresenter
    ) {
        this.exchangeRateProvider = exchangeRateProvider;
        this.moneyPresenter = moneyPresenter;
    }

    /**
     * Executes the money exchange operation from source to target currency.
     *
     * <p>This method orchestrates the complete exchange workflow, including
     * validation, rate fetching, conversion, and result presentation. All errors
     * are caught and presented to the user through the presenter rather than
     * being thrown.</p>
     *
     * <p><strong>Process flow:</strong></p>
     * <ol>
     *   <li>Validate that both source money and target currency are provided</li>
     *   <li>Verify that source and target currencies are different</li>
     *   <li>Fetch the current exchange rate from the provider</li>
     *   <li>Perform the conversion using domain logic</li>
     *   <li>Present the successful result with conversion details</li>
     * </ol>
     *
     * <p><strong>Error scenarios handled:</strong></p>
     * <ul>
     *   <li>Null source money or target currency</li>
     *   <li>Identical source and target currencies</li>
     *   <li>Exchange rate unavailable from provider</li>
     *   <li>Invalid input data</li>
     *   <li>Unexpected system errors</li>
     * </ul>
     *
     * @param sourceMoney the money to be converted, including amount and source currency;
     *                   must not be null
     * @param targetCurrency the currency to convert to; must not be null and must
     *                      be different from the source currency
     */
    @Override
    public void execute(Money sourceMoney, Currency targetCurrency) {
        try {
            // Step 1: Validate all input parameters before proceeding
            validateInputs(sourceMoney, targetCurrency);

            // Step 2: Fetch the current exchange rate between the two currencies
            // This may involve calling an external API or accessing cached rates
            ExchangeRate exchangeRate = exchangeRateProvider.getRate(
                    sourceMoney.currency(),
                    targetCurrency
            );

            // Step 3: Perform the conversion using the Money domain object's logic
            // The Money object encapsulates the conversion calculation rules
            Money convertedMoney = sourceMoney.convert(exchangeRate);

            // Step 4: Present the successful conversion result to the user
            // Include both the converted amount and the exchange rate used
            moneyPresenter.presentSuccess(convertedMoney, exchangeRate);

        } catch (ExchangeRateUnavailableException e) {
            // Handle the specific case where exchange rates cannot be fetched
            // This could occur due to API errors, network issues, or unavailable currency pairs
            moneyPresenter.presentError(
                    "Unable to fetch exchange rate: " + e.getMessage()
            );
        } catch (IllegalArgumentException e) {
            // Handle validation errors from input parameters
            // These are typically caught by validateInputs() or domain object methods
            moneyPresenter.presentError(
                    "Invalid input: " + e.getMessage()
            );
        } catch (Exception e) {
            // Catch-all for any unexpected errors to ensure graceful failure
            // Prevents the application from crashing on unforeseen issues
            moneyPresenter.presentError(
                    "An unexpected error occurred: " + e.getMessage()
            );
        }
    }

    /**
     * Validates the input parameters for the money exchange operation.
     *
     * <p>This method ensures that all preconditions are met before attempting
     * the exchange. It checks for null values and business rule violations
     * such as attempting to exchange money to the same currency.</p>
     *
     * <p><strong>Validation checks:</strong></p>
     * <ul>
     *   <li>Source money must not be null</li>
     *   <li>Target currency must not be null</li>
     *   <li>Source and target currencies must be different</li>
     * </ul>
     *
     * @param sourceMoney the money to be validated; checked for null
     * @param targetCurrency the target currency to be validated; checked for null
     * @throws IllegalArgumentException if any validation check fails
     */
    private void validateInputs(Money sourceMoney, Currency targetCurrency) {
        // Check that source money is provided
        if (sourceMoney == null) {
            throw new IllegalArgumentException("Source money cannot be null");
        }

        // Check that target currency is provided
        if (targetCurrency == null) {
            throw new IllegalArgumentException("Target currency cannot be null");
        }

        // Verify that the conversion makes sense (different currencies)
        // Converting between the same currency is not a valid operation
        if (sourceMoney.currency().equals(targetCurrency)) {
            throw new IllegalArgumentException(
                    "Source and target currencies must be different"
            );
        }
    }
}