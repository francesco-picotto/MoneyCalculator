package software.ulpgc.moneycalculator.application.port.input;

import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.Money;

/**
 * Input port interface for executing money exchange operations.
 *
 * <p>This interface represents a command use case in the CQRS (Command Query
 * Responsibility Segregation) pattern. It defines a write operation that changes
 * system state by performing a currency conversion and presenting the result.</p>
 *
 * <p>As an input port in the Hexagonal Architecture, this interface serves as
 * the entry point for the money exchange use case. It is defined in the application
 * layer and invoked by external adapters such as:</p>
 * <ul>
 *   <li><strong>UI Controllers:</strong> Desktop application controllers,
 *       web MVC controllers</li>
 *   <li><strong>REST API Endpoints:</strong> HTTP request handlers</li>
 *   <li><strong>Command-Line Interfaces:</strong> CLI command processors</li>
 *   <li><strong>Message Queue Handlers:</strong> Asynchronous event processors</li>
 * </ul>
 *
 * <p><strong>Command Pattern:</strong></p>
 * <p>This interface follows the Command pattern, encapsulating the money exchange
 * request as an object. The command contains all information needed to perform
 * the action (source money and target currency) without exposing how the action
 * is executed.</p>
 *
 * <p><strong>Separation of Concerns:</strong></p>
 * <p>By depending on this interface rather than concrete implementations, the
 * UI layer remains decoupled from business logic and can be easily tested with
 * mock implementations.</p>
 */
public interface ExchangeMoneyCommand {

    /**
     * Executes a money exchange from the source money to the target currency.
     *
     * <p>This method initiates the complete currency conversion workflow,
     * including validation, exchange rate lookup, conversion calculation, and
     * result presentation. The operation does not return a value; instead,
     * results are communicated through a presenter interface.</p>
     *
     * <p><strong>Expected Behavior:</strong></p>
     * <ul>
     *   <li>Validate that source money and target currency are provided and valid</li>
     *   <li>Verify that source and target currencies are different</li>
     *   <li>Fetch the current exchange rate for the currency pair</li>
     *   <li>Calculate the converted amount</li>
     *   <li>Present the successful result or error message to the user</li>
     * </ul>
     *
     * <p><strong>Error Handling:</strong></p>
     * <p>This method does not throw exceptions. All errors (validation failures,
     * exchange rate unavailability, system errors) are caught internally and
     * communicated to the user through the presenter mechanism. This design
     * ensures graceful error handling and consistent user experience.</p>
     *
     * <p><strong>Usage Example:</strong></p>
     * <pre>{@code
     * // Create source money (100 US Dollars)
     * Money sourceMoney = new Money(100.0, usdCurrency);
     * Currency targetCurrency = eurCurrency;
     *
     * // Execute the exchange
     * exchangeMoneyCommand.execute(sourceMoney, targetCurrency);
     *
     * // Result is presented through the MoneyPresenter
     * }</pre>
     *
     * @param sourceMoney the {@link Money} object to be converted, containing
     *                   the amount and source currency; must not be null and
     *                   must have a positive amount
     * @param targetCurrency the {@link Currency} to convert to; must not be null
     *                      and must be different from the source currency
     */
    void execute(Money sourceMoney, Currency targetCurrency);
}