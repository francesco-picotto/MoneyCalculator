package software.ulpgc.moneycalculator.application.port.output;

import software.ulpgc.moneycalculator.domain.model.ExchangeRate;
import software.ulpgc.moneycalculator.domain.model.Money;

/**
 * Output port interface for presenting money conversion results to the user.
 *
 * <p>This interface defines the contract for displaying money exchange outcomes
 * in the user interface layer. It follows the Hexagonal Architecture pattern by
 * serving as an output port that is implemented by UI adapters (e.g., desktop GUI
 * presenters, web controllers, CLI formatters).</p>
 *
 * <p>The interface supports both success and error scenarios, allowing the
 * application core to remain decoupled from specific UI technologies while
 * ensuring consistent presentation of results across different interfaces.</p>
 *
 * <p><strong>Design Pattern:</strong></p>
 * <p>This follows the Presenter pattern from Clean Architecture, where the
 * presenter is responsible for formatting and displaying data without containing
 * business logic. The application layer calls these methods, and the UI layer
 * implements them to render the information appropriately.</p>
 */
public interface MoneyPresenter {

    /**
     * Presents a successful money conversion result to the user.
     *
     * <p>This method is called when a currency exchange operation completes
     * successfully. The implementing class should display the converted amount
     * along with relevant exchange rate information in a user-friendly format.</p>
     *
     * <p>The presenter may choose to display various details such as:</p>
     * <ul>
     *   <li>The converted amount with proper currency formatting</li>
     *   <li>The exchange rate used for the conversion</li>
     *   <li>The source and target currencies</li>
     *   <li>The date/time of the exchange rate</li>
     *   <li>Additional metadata (rate source, calculation details, etc.)</li>
     * </ul>
     *
     * @param convertedMoney the resulting {@link Money} object after conversion,
     *                      containing the target amount and currency; must not be null
     * @param exchangeRate the {@link ExchangeRate} that was applied for the conversion,
     *                    including the rate value and involved currencies; must not be null
     */
    void presentSuccess(Money convertedMoney, ExchangeRate exchangeRate);

    /**
     * Presents an error message to the user when the conversion fails.
     *
     * <p>This method is called when a currency exchange operation cannot be
     * completed due to various error conditions such as validation failures,
     * unavailable exchange rates, network issues, or system errors.</p>
     *
     * <p>The implementing class should display the error message in a way that
     * is clear and helpful to the user, potentially including:</p>
     * <ul>
     *   <li>A user-friendly error description</li>
     *   <li>Suggestions for resolving the issue</li>
     *   <li>Visual indicators (error icons, colors, etc.)</li>
     *   <li>Options to retry or take alternative actions</li>
     * </ul>
     *
     * @param errorMessage a descriptive error message explaining what went wrong;
     *                    must not be null or empty
     */
    void presentError(String errorMessage);
}