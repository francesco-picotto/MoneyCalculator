package software.ulpgc.moneycalculator.control;

/**
 * Legacy command interface for backward compatibility with existing UI code.
 *
 * <p>This interface represents the classic Command pattern, encapsulating a request
 * as an object. It provides a parameterless {@code execute()} method that triggers
 * the command's action without requiring arguments at execution time.</p>
 *
 * <p><strong>Design Pattern:</strong></p>
 * <p>This follows the Gang of Four Command pattern, which:</p>
 * <ul>
 *   <li>Encapsulates a request as an object</li>
 *   <li>Allows parameterization of clients with different requests</li>
 *   <li>Supports undoable operations</li>
 *   <li>Enables queueing and logging of requests</li>
 * </ul>
 *
 * <p><strong>Backward Compatibility Note:</strong></p>
 * <p>This interface is maintained for compatibility with UI code that was designed
 * before the introduction of the Hexagonal Architecture and use case pattern. Modern
 * code should prefer using the more explicit use case interfaces like
 * {@code ExchangeMoneyCommand} which accept parameters directly in their execute methods.</p>
 *
 * <p><strong>Usage Pattern:</strong></p>
 * <p>Typically, implementations of this interface are used with UI event handlers:</p>
 * <pre>{@code
 * // Wire command to UI button
 * button.addActionListener(e -> command.execute());
 *
 * // Or as a method reference
 * button.addActionListener(e -> exchangeCommand.execute());
 * }</pre>
 *
 * <p><strong>Modern Alternative:</strong></p>
 * <p>For new code, consider using the application layer's use case interfaces which
 * provide more explicit contracts:</p>
 * <pre>{@code
 * // Instead of parameterless Command
 * public interface Command {
 *     void execute();
 * }
 *
 * // Use explicit use case interfaces
 * public interface ExchangeMoneyCommand {
 *     void execute(Money sourceMoney, Currency targetCurrency);
 * }
 * }</pre>
 *
 * <p><strong>Adapter Pattern:</strong></p>
 * <p>To bridge between this legacy interface and modern use cases, use adapter classes
 * like {@code ExchangeMoneyCommandAdapter} which extract parameters from UI dialogs and
 * delegate to proper use case implementations.</p>
 */
public interface Command {

    /**
     * Executes the command's action.
     *
     * <p>This method performs the command's intended operation. Since this interface
     * is parameterless, implementations must obtain any required data through other
     * means (constructor injection, UI dialogs, application state, etc.).</p>
     *
     * <p><strong>Implementation Guidelines:</strong></p>
     * <ul>
     *   <li>Keep the method execution fast for UI responsiveness</li>
     *   <li>Handle errors gracefully (don't let exceptions propagate to UI)</li>
     *   <li>Provide user feedback for both success and error cases</li>
     *   <li>Consider making long-running operations asynchronous</li>
     * </ul>
     *
     * <p><strong>Error Handling:</strong></p>
     * <p>Implementations should catch and handle exceptions internally rather than
     * propagating them to the caller, as UI event handlers typically don't have
     * sophisticated error handling mechanisms.</p>
     *
     * <p><strong>Thread Safety:</strong></p>
     * <p>If the command is executed from UI event handlers, it will typically run
     * on the Event Dispatch Thread (EDT) in Swing applications. Long-running
     * operations should be offloaded to background threads to maintain UI responsiveness.</p>
     */
    void execute();
}