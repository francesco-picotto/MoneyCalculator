package software.ulpgc.moneycalculator;

import software.ulpgc.moneycalculator.application.port.input.ExchangeMoneyCommand;
import software.ulpgc.moneycalculator.application.port.output.CurrencyRepository;
import software.ulpgc.moneycalculator.application.port.output.MoneyPresenter;
import software.ulpgc.moneycalculator.config.ApplicationConfig;
import software.ulpgc.moneycalculator.config.DependencyInjectionConfig;
import software.ulpgc.moneycalculator.control.Command;
import software.ulpgc.moneycalculator.control.ExchangeMoneyCommandAdapter;
import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.ui.adapter.MoneyDisplayPresenterAdapter;
import software.ulpgc.moneycalculator.ui.swing.MainFrame;

import javax.swing.*;
import java.util.List;

/**
 * Main entry point for the Money Calculator application.
 *
 * <p>This class is responsible for bootstrapping the application by:</p>
 * <ul>
 *   <li>Loading configuration from properties file</li>
 *   <li>Initializing the dependency injection container</li>
 *   <li>Creating and wiring the Swing UI components</li>
 *   <li>Connecting the presentation layer with the application/domain layers</li>
 * </ul>
 *
 * <p><strong>Architecture:</strong></p>
 * <p>This application follows Clean Architecture principles with clear separation between:</p>
 * <ul>
 *   <li><strong>Domain Layer:</strong> Business entities (Money, Currency, ExchangeRate)</li>
 *   <li><strong>Application Layer:</strong> Use cases and ports (ExchangeMoneyCommand)</li>
 *   <li><strong>Infrastructure Layer:</strong> API adapters, HTTP clients, JSON parsers</li>
 *   <li><strong>Presentation Layer:</strong> Swing UI components and adapters</li>
 * </ul>
 *
 * <p><strong>Design Patterns:</strong></p>
 * <ul>
 *   <li><strong>Dependency Injection:</strong> Components receive dependencies through constructors</li>
 *   <li><strong>Adapter Pattern:</strong> Bridges between incompatible interfaces (UI ↔ Use Cases)</li>
 *   <li><strong>Repository Pattern:</strong> Abstracts data access for currencies and exchange rates</li>
 *   <li><strong>Command Pattern:</strong> Encapsulates conversion requests as objects</li>
 * </ul>

 */
public class Main {

    /**
     * Application entry point.
     *
     * <p>Initializes the application on the Swing Event Dispatch Thread (EDT)
     * to ensure thread safety for all UI operations.</p>
     *
     * @param args command line arguments (currently unused)
     */
    public static void main(String[] args) {
        ApplicationConfig config = createConfig();
        DependencyInjectionConfig diConfig = new DependencyInjectionConfig(config);

        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
                initializeApp(diConfig);
            } catch (Exception e) {
                showError("Failed to start application: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * Initializes the application components and displays the main window.
     *
     * <p>This method orchestrates the initialization process:</p>
     * <ol>
     *   <li>Shows a loading screen while fetching currencies from the API</li>
     *   <li>Creates and configures the main UI frame</li>
     *   <li>Wires together the presentation and application layers using adapters</li>
     *   <li>Registers command handlers for user actions</li>
     * </ol>
     *
     * <p>The currency loading is performed asynchronously using {@link SwingWorker}
     * to prevent blocking the UI thread.</p>
     *
     * @param diConfig the dependency injection configuration containing all required services
     * @throws RuntimeException if currency loading fails or no currencies are found
     */
    private static void initializeApp(DependencyInjectionConfig diConfig) {
        CurrencyRepository repository = diConfig.currencyRepository();

        JFrame loadingFrame = createLoadingFrame();
        loadingFrame.setVisible(true);

        new SwingWorker<List<Currency>, Void>() {
            @Override
            protected List<Currency> doInBackground() {
                // Fetch currencies from API in background thread
                return repository.findAll();
            }

            @Override
            protected void done() {
                try {
                    List<Currency> currencies = get();
                    loadingFrame.dispose();

                    if (currencies == null || currencies.isEmpty()) {
                        showError("Error: No currencies found. Check your API key and connection.");
                        return;
                    }

                    // Create the main application window
                    MainFrame mainFrame = new MainFrame();
                    mainFrame.loadCurrencies(currencies);

                    // Wire the presentation layer to the application layer using adapters
                    // MoneyPresenter adapter converts domain results to UI updates
                    MoneyPresenter presenter = new MoneyDisplayPresenterAdapter(
                            mainFrame.getMoneyDisplay()
                    );

                    // Create the use case with the presenter for handling results
                    ExchangeMoneyCommand exchangeMoneyUseCase = diConfig.exchangeMoneyCommand(presenter);

                    // Command adapter bridges the parameterless UI Command with the parameterized use case
                    Command exchangeCommand = new ExchangeMoneyCommandAdapter(
                            mainFrame.getMoneyDialog(),      // Source: amount + currency
                            mainFrame.getCurrencyDialog(),   // Target: destination currency
                            exchangeMoneyUseCase             // Business logic orchestrator
                    );

                    // Register the command with the UI
                    mainFrame.put("exchange", exchangeCommand);

                    mainFrame.setVisible(true);

                } catch (Exception e) {
                    loadingFrame.dispose();
                    showError("Initialization failed: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        }.execute();
    }

    /**
     * Creates a modal loading frame displayed during currency data synchronization.
     *
     * <p>The loading frame provides visual feedback to the user while the application
     * fetches currency data from the external API. It features an indeterminate
     * progress bar to indicate ongoing activity.</p>
     *
     * @return a configured JFrame with loading indicator
     */
    private static JFrame createLoadingFrame() {
        JFrame frame = new JFrame("Money Calculator");
        frame.setSize(300, 120);
        frame.setLocationRelativeTo(null);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        JPanel panel = new JPanel();
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel label = new JLabel("Synchronizing with API...");
        label.setAlignmentX(JLabel.CENTER_ALIGNMENT);
        JProgressBar pb = new JProgressBar();
        pb.setIndeterminate(true);

        panel.add(label);
        panel.add(Box.createVerticalStrut(10));
        panel.add(pb);
        frame.add(panel);
        return frame;
    }

    /**
     * Creates the application configuration by loading properties from file.
     *
     * <p>Attempts to load configuration from <code>application.properties</code>.
     * If the file is not found or cannot be loaded, falls back to default configuration.</p>
     *
     * @return the application configuration instance
     * @see #createDefaultConfig()
     */
    private static ApplicationConfig createConfig() {
        try {
            return new ApplicationConfig("application.properties");
        } catch (Exception e) {
            System.err.println("Warning: Could not load application.properties, using defaults");
            return createDefaultConfig();
        }
    }

    /**
     * Creates a default configuration with hardcoded values.
     *
     * <p>This fallback configuration is used when the properties file is unavailable.
     * It includes default values for:</p>
     * <ul>
     *   <li>API base URL</li>
     *   <li>API key (demo key - should be replaced in production)</li>
     *   <li>HTTP timeout settings</li>
     * </ul>
     *
     * <p><strong>Warning:</strong> The default API key is a demo key and should be
     * replaced with a valid key for production use.</p>
     *
     * @return an ApplicationConfig instance with default values
     */
    private static ApplicationConfig createDefaultConfig() {
        java.util.Properties props = new java.util.Properties();
        props.setProperty("api.exchangerate.baseurl", "https://v6.exchangerate-api.com");
        props.setProperty("api.exchangerate.key", "aeb1cd5ef6081142040d717f");
        props.setProperty("http.timeout.seconds", "10");

        return new ApplicationConfig("application.properties") {
            @Override
            public String get(String key) {
                return props.getProperty(key);
            }

            @Override
            public String get(String key, String defaultValue) {
                return props.getProperty(key, defaultValue);
            }

            @Override
            public int getInt(String key, int defaultValue) {
                String value = props.getProperty(key);
                return value != null ? Integer.parseInt(value) : defaultValue;
            }
        };
    }

    /**
     * Displays an error dialog to the user.
     *
     * <p>This is used for critical errors that prevent the application from
     * starting or functioning properly.</p>
     *
     * @param message the error message to display
     */
    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "System Error", JOptionPane.ERROR_MESSAGE);
    }
}