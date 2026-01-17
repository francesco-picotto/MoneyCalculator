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
 * Main definitivo: Unisce la nuova architettura con l'interfaccia Swing.
 * Utilizza Dependency Injection e Repository per caricare i dati.
 *
 * FIXED: Now properly wires the new architecture with adapters:
 * 1. Creates MoneyPresenter adapter for the MoneyDisplay
 * 2. Creates the ExchangeMoneyUseCase with the presenter
 * 3. Creates the Command adapter that bridges UI and use case
 */
public class Main {

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

    private static void initializeApp(DependencyInjectionConfig diConfig) {
        CurrencyRepository repository = diConfig.currencyRepository();

        JFrame loadingFrame = createLoadingFrame();
        loadingFrame.setVisible(true);

        new SwingWorker<List<Currency>, Void>() {
            @Override
            protected List<Currency> doInBackground() {
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

                    MainFrame mainFrame = new MainFrame();

                    mainFrame.loadCurrencies(currencies);

                    MoneyPresenter presenter = new MoneyDisplayPresenterAdapter(
                            mainFrame.getMoneyDisplay()
                    );

                    ExchangeMoneyCommand exchangeMoneyUseCase = diConfig.exchangeMoneyCommand(presenter);


                    Command exchangeCommand = new ExchangeMoneyCommandAdapter(
                            mainFrame.getMoneyDialog(),      // Where user enters amount & source currency
                            mainFrame.getCurrencyDialog(),   // Where user selects target currency
                            exchangeMoneyUseCase             // The use case that orchestrates the conversion
                    );

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

    // --- Helper methods ---

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

    private static ApplicationConfig createConfig() {
        try {
            return new ApplicationConfig("application.properties");
        } catch (Exception e) {
            System.err.println("Warning: Could not load application.properties, using defaults");
            return createDefaultConfig();
        }
    }

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

    private static void showError(String message) {
        JOptionPane.showMessageDialog(null, message, "System Error", JOptionPane.ERROR_MESSAGE);
    }
}