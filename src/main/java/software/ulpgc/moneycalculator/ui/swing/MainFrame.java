package software.ulpgc.moneycalculator.ui.swing;

import software.ulpgc.moneycalculator.control.Command;
import software.ulpgc.moneycalculator.domain.model.Currency;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Main application window for the Money Calculator.
 *
 * <p>This class serves as the primary container and coordinator for all UI components
 * in the application. It manages the layout, user interactions, and command execution.</p>
 *
 * <p><strong>Responsibilities:</strong></p>
 * <ul>
 *   <li>Creating and organizing all visual components (dialogs and displays)</li>
 *   <li>Managing the overall window layout and appearance</li>
 *   <li>Coordinating user interactions with command handlers</li>
 *   <li>Providing keyboard shortcuts for improved usability</li>
 * </ul>
 *
 * <p><strong>Architecture & Patterns:</strong></p>
 * <ul>
 *   <li><strong>MVC Pattern:</strong> Acts as the main View component</li>
 *   <li><strong>Command Pattern:</strong> Stores and executes commands triggered by user actions</li>
 *   <li><strong>Mediator Pattern:</strong> Coordinates interactions between child components</li>
 * </ul>
 *
 * <p><strong>UI Components:</strong></p>
 * <ul>
 *   <li>{@link SwingMoneyDialog}: Input for source amount and currency</li>
 *   <li>{@link SwingCurrencyDialog}: Selection for target currency</li>
 *   <li>{@link SwingMoneyDisplay}: Display for conversion results</li>
 * </ul>
 *
 * <p><strong>Keyboard Shortcuts:</strong></p>
 * <ul>
 *   <li><kbd>Ctrl+Enter</kbd>: Execute currency conversion</li>
 * </ul>
 *
 * @author Money Calculator Team
 * @version 2.0
 * @since 1.0
 * @see SwingMoneyDialog
 * @see SwingCurrencyDialog
 * @see SwingMoneyDisplay
 */
public class MainFrame extends JFrame {

    /** Registry of commands mapped by name for dynamic execution */
    private final Map<String, Command> commands;

    /** Dialog for entering the source money (amount + currency) */
    private SwingMoneyDialog moneyDialog;

    /** Dialog for selecting the target currency */
    private SwingCurrencyDialog currencyDialog;

    /** Display component for showing conversion results */
    private SwingMoneyDisplay moneyDisplay;

    /** Button to trigger currency conversion */
    private JButton convertButton;

    /**
     * Constructs the main application frame.
     *
     * <p>Initializes the command registry and sets up all UI components,
     * layout, and keyboard bindings.</p>
     */
    public MainFrame() {
        this.commands = new HashMap<>();
        setupFrame();
        createComponents();
        arrangeComponents();
        setupKeyBindings();
    }

    /**
     * Configures the basic properties of the main window.
     *
     * <p>Sets up:</p>
     * <ul>
     *   <li>Window title</li>
     *   <li>Window dimensions (600×450)</li>
     *   <li>Close operation (exit application)</li>
     *   <li>Center position on screen</li>
     *   <li>Non-resizable state for consistent layout</li>
     * </ul>
     */
    private void setupFrame() {
        setTitle("Money Calculator");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
    }

    /**
     * Creates all UI components used in the application.
     *
     * <p>Instantiates:</p>
     * <ul>
     *   <li>Money input dialog (amount + source currency)</li>
     *   <li>Currency selection dialog (target currency)</li>
     *   <li>Result display panel</li>
     * </ul>
     */
    private void createComponents() {
        moneyDialog = new SwingMoneyDialog();
        currencyDialog = new SwingCurrencyDialog();
        moneyDisplay = new SwingMoneyDisplay();
    }

    /**
     * Arranges UI components using a BorderLayout structure.
     *
     * <p><strong>Layout Structure:</strong></p>
     * <pre>
     * ┌─────────────────────────────┐
     * │  NORTH: Input Panel         │
     * │  (Amount + Currencies)      │
     * ├─────────────────────────────┤
     * │  CENTER: Button Panel       │
     * │  (Convert + Swap)           │
     * ├─────────────────────────────┤
     * │  SOUTH: Display Panel       │
     * │  (Conversion Result)        │
     * └─────────────────────────────┘
     * </pre>
     */
    private void arrangeComponents() {
        setLayout(new BorderLayout(10, 10));

        // Top section: input fields
        add(createInputPanel(), BorderLayout.NORTH);

        // Middle section: action buttons
        add(createButtonPanel(), BorderLayout.CENTER);

        // Bottom section: result display
        add(moneyDisplay.getPanel(), BorderLayout.SOUTH);
    }

    /**
     * Creates the input panel containing money and currency dialogs.
     *
     * <p>Uses a vertical BoxLayout to stack the input components with
     * appropriate spacing between them.</p>
     *
     * @return a configured JPanel with all input components
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Add source money input (amount + source currency)
        panel.add(moneyDialog.getPanel());
        panel.add(Box.createVerticalStrut(10));

        // Add target currency selection
        panel.add(currencyDialog.getPanel());

        return panel;
    }

    /**
     * Creates the button panel with conversion and swap controls.
     *
     * <p><strong>Buttons:</strong></p>
     * <ul>
     *   <li><strong>Convert:</strong> Executes the currency conversion</li>
     *   <li><strong>Swap (⇄):</strong> Reverses source and target currencies</li>
     * </ul>
     *
     * @return a configured JPanel with action buttons
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 20));

        // Convert button
        convertButton = new JButton("Convert");
        convertButton.setFont(new Font("Arial", Font.BOLD, 16));
        convertButton.setPreferredSize(new Dimension(150, 50));
        convertButton.setToolTipText("Convert currency (Ctrl+Enter)");
        convertButton.addActionListener(e -> executeConversion());

        // Swap button for reversing currencies
        JButton swapButton = new JButton("⇄ Swap");
        swapButton.setFont(new Font("Arial", Font.BOLD, 14));
        swapButton.setPreferredSize(new Dimension(100, 50));
        swapButton.setToolTipText("Swap source and target currencies");
        swapButton.addActionListener(e -> swapCurrencies());

        panel.add(convertButton);
        panel.add(swapButton);

        return panel;
    }

    /**
     * Configures keyboard shortcuts for the application.
     *
     * <p><strong>Shortcuts Configured:</strong></p>
     * <ul>
     *   <li><kbd>Ctrl+Enter</kbd>: Trigger conversion</li>
     * </ul>
     *
     * <p>Keyboard shortcuts are registered at the root pane level to work
     * regardless of which component has focus.</p>
     */
    private void setupKeyBindings() {
        JRootPane rootPane = getRootPane();

        // Ctrl+Enter to execute conversion
        KeyStroke ctrlEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlEnter, "convert");
        rootPane.getActionMap().put("convert", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeConversion();
            }
        });
    }

    /**
     * Executes the currency conversion command.
     *
     * <p>This method orchestrates the conversion process:</p>
     * <ol>
     *   <li>Retrieves the registered "exchange" command</li>
     *   <li>Executes it asynchronously using {@link SwingWorker}</li>
     *   <li>Updates UI to show "Converting..." during execution</li>
     *   <li>Handles any errors that occur during conversion</li>
     *   <li>Restores button state after completion</li>
     * </ol>
     *
     * <p><strong>Thread Safety:</strong> Uses SwingWorker to execute the command
     * on a background thread, preventing UI freezing during API calls.</p>
     */
    public void executeConversion() {
        Command command = commands.get("exchange");
        if (command != null) {
            // Execute command asynchronously to avoid blocking the UI
            new SwingWorker<Void, Void>() {
                @Override
                protected Void doInBackground() {
                    SwingUtilities.invokeLater(() -> {
                        convertButton.setEnabled(false);
                        convertButton.setText("Converting...");
                    });

                    try {
                        command.execute();
                    } catch (Exception e) {
                        e.printStackTrace();
                        SwingUtilities.invokeLater(() -> {
                            JOptionPane.showMessageDialog(
                                    MainFrame.this,
                                    "Error executing conversion: " + e.getMessage(),
                                    "Error",
                                    JOptionPane.ERROR_MESSAGE
                            );
                        });
                    }

                    return null;
                }

                @Override
                protected void done() {
                    convertButton.setEnabled(true);
                    convertButton.setText("Convert");
                }
            }.execute();
        } else {
            JOptionPane.showMessageDialog(
                    this,
                    "Conversion command not initialized",
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Swaps the source and target currencies.
     *
     * <p>This convenience feature allows users to quickly reverse the conversion
     * direction without manually reselecting currencies. The display is cleared
     * when currencies are swapped.</p>
     *
     * <p><strong>Example:</strong> If converting USD → EUR, after swap it becomes EUR → USD</p>
     */
    private void swapCurrencies() {
        try {
            Currency fromCurrency = moneyDialog.getSelectedCurrency();
            Currency toCurrency = currencyDialog.getSelectedCurrency();

            if (fromCurrency != null && toCurrency != null) {
                moneyDialog.setSelectedCurrency(toCurrency);
                currencyDialog.setSelectedCurrency(fromCurrency);

                // Clear previous result when swapping
                moneyDisplay.clear();
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(
                    this,
                    "Error swapping currencies: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    /**
     * Registers a command with a specific name.
     *
     * <p>Commands are stored in a registry and can be executed by name.
     * This allows flexible binding of actions to UI events.</p>
     *
     * <p><strong>Example:</strong></p>
     * <pre>{@code
     * Command exchangeCommand = new ExchangeMoneyCommandAdapter(...);
     * mainFrame.put("exchange", exchangeCommand);
     * }</pre>
     *
     * @param name the command identifier (e.g., "exchange")
     * @param command the command implementation to execute
     */
    public void put(String name, Command command) {
        commands.put(name, command);
    }

    /**
     * Initializes the currency selection dropdowns with available currencies.
     *
     * <p>This method should be called after loading currencies from the API
     * and before displaying the main window.</p>
     *
     * @param currencies the list of all supported currencies
     */
    public void loadCurrencies(List<Currency> currencies) {
        moneyDialog.setCurrencies(currencies);
        currencyDialog.setCurrencies(currencies);
    }

    /**
     * Returns the money input dialog component.
     *
     * @return the dialog for entering source money
     */
    public SwingMoneyDialog getMoneyDialog() {
        return moneyDialog;
    }

    /**
     * Returns the currency selection dialog component.
     *
     * @return the dialog for selecting target currency
     */
    public SwingCurrencyDialog getCurrencyDialog() {
        return currencyDialog;
    }

    /**
     * Returns the result display component.
     *
     * @return the display for showing conversion results
     */
    public SwingMoneyDisplay getMoneyDisplay() {
        return moneyDisplay;
    }
}