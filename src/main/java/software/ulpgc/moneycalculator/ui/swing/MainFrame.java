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
 * MainFrame è la finestra principale dell'applicazione Money Calculator.
 *
 * RESPONSABILITÀ:
 * - Crea e organizza tutti i componenti grafici (dialog e display)
 * - Gestisce il layout dell'interfaccia utente
 * - Contiene i pulsanti per eseguire i comandi
 * - Coordina l'interazione tra i vari componenti
 *
 * PATTERN UTILIZZATI:
 * - MVC: Questa è la Vista principale
 * - Command: Memorizza i comandi da eseguire quando l'utente interagisce
 *
 * IMPROVEMENTS:
 * - Added swap button to reverse currencies
 * - Added keyboard shortcut (Ctrl+Enter) for conversion
 * - Better button layout
 * - Error handling improvements
 */
public class MainFrame extends JFrame {
    private final Map<String, Command> commands;
    private SwingMoneyDialog moneyDialog;
    private SwingCurrencyDialog currencyDialog;
    private SwingMoneyDisplay moneyDisplay;
    private JButton convertButton;

    /**
     * Costruttore del MainFrame
     * Inizializza la mappa dei comandi (sarà popolata dopo con put())
     */
    public MainFrame() {
        this.commands = new HashMap<>();
        setupFrame();
        createComponents();
        arrangeComponents();
        setupKeyBindings();
    }

    /**
     * Configura le proprietà base della finestra
     * - Titolo
     * - Dimensioni
     * - Comportamento alla chiusura
     * - Posizione centrata sullo schermo
     */
    private void setupFrame() {
        setTitle("Money Calculator");
        setSize(600, 450); // Slightly taller for swap button
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Centra la finestra
        setResizable(false); // Prevent resizing for cleaner look
    }

    /**
     * Crea tutti i componenti dell'interfaccia:
     * - MoneyDialog: per inserire importo e valuta di partenza
     * - CurrencyDialog: per scegliere la valuta di destinazione
     * - MoneyDisplay: per mostrare il risultato della conversione
     */
    private void createComponents() {
        moneyDialog = new SwingMoneyDialog();
        currencyDialog = new SwingCurrencyDialog();
        moneyDisplay = new SwingMoneyDisplay();
    }

    /**
     * Organizza i componenti nel layout della finestra
     *
     * LAYOUT UTILIZZATO: BorderLayout
     * - NORTH: Pannello di input (importo e valute)
     * - CENTER: Pulsanti (convert e swap)
     * - SOUTH: Display del risultato
     */
    private void arrangeComponents() {
        setLayout(new BorderLayout(10, 10));

        // Pannello superiore con i campi di input
        add(createInputPanel(), BorderLayout.NORTH);

        // Pannello centrale con i pulsanti
        add(createButtonPanel(), BorderLayout.CENTER);

        // Pannello inferiore con il display del risultato
        add(moneyDisplay.getPanel(), BorderLayout.SOUTH);
    }

    /**
     * Crea il pannello di input che contiene:
     * - MoneyDialog (importo + valuta di partenza)
     * - CurrencyDialog (valuta di destinazione)
     *
     * Usa BoxLayout per disporre i componenti verticalmente
     */
    private JPanel createInputPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Aggiunge il pannello per inserire l'importo e la valuta di partenza
        panel.add(moneyDialog.getPanel());
        panel.add(Box.createVerticalStrut(10)); // Spazio tra i componenti

        // Aggiunge il pannello per scegliere la valuta di destinazione
        panel.add(currencyDialog.getPanel());

        return panel;
    }

    /**
     * Crea il pannello con i pulsanti "Convert" e "Swap"
     *
     * IMPROVEMENT: Added swap button to quickly reverse currencies
     */
    private JPanel createButtonPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout(FlowLayout.CENTER, 15, 20));

        // Convert button
        convertButton = new JButton("Convert");
        convertButton.setFont(new Font("Arial", Font.BOLD, 16));
        convertButton.setPreferredSize(new Dimension(150, 50));
        convertButton.setToolTipText("Convert currency (Ctrl+Enter)");

        // ActionListener: cosa succede quando si clicca il pulsante
        convertButton.addActionListener(e -> executeConversion());

        // Swap button - IMPROVEMENT
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
     * Setup keyboard shortcuts
     * IMPROVEMENT: Added Ctrl+Enter to trigger conversion
     */
    private void setupKeyBindings() {
        // Get the root pane
        JRootPane rootPane = getRootPane();

        // Ctrl+Enter to convert
        KeyStroke ctrlEnter = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
        rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ctrlEnter, "convert");
        rootPane.getActionMap().put("convert", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                executeConversion();
            }
        });

        // Also allow just Enter key when amount field has focus
        // This is handled in the MoneyDialog
    }

    /**
     * Execute the conversion command
     * Extracted to a method so it can be called from multiple places
     * (button click, keyboard shortcut, etc.)
     */
    public void executeConversion() {
        Command command = commands.get("exchange");
        if (command != null) {
            // SwingWorker esegue il comando in background
            // evitando di bloccare l'interfaccia grafica
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
     * Swap source and target currencies
     * IMPROVEMENT: New feature to quickly reverse conversion
     */
    private void swapCurrencies() {
        try {
            Currency fromCurrency = moneyDialog.getSelectedCurrency();
            Currency toCurrency = currencyDialog.getSelectedCurrency();

            if (fromCurrency != null && toCurrency != null) {
                moneyDialog.setSelectedCurrency(toCurrency);
                currencyDialog.setSelectedCurrency(fromCurrency);

                // Clear the display when swapping
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
     * Registra un comando con un nome
     * Esempio: put("exchange", exchangeMoneyCommand)
     *
     * Questo permette di associare azioni a pulsanti in modo flessibile
     */
    public void put(String name, Command command) {
        commands.put(name, command);
    }

    /**
     * Inizializza le ComboBox con la lista delle valute disponibili
     * Viene chiamato dopo aver caricato le valute dalla API
     *
     * @param currencies Lista di tutte le valute supportate
     */
    public void loadCurrencies(List<Currency> currencies) {
        moneyDialog.setCurrencies(currencies);
        currencyDialog.setCurrencies(currencies);
    }

    // Getter per i componenti - necessari per ExchangeMoneyCommand

    public SwingMoneyDialog getMoneyDialog() {
        return moneyDialog;
    }

    public SwingCurrencyDialog getCurrencyDialog() {
        return currencyDialog;
    }

    public SwingMoneyDisplay getMoneyDisplay() {
        return moneyDisplay;
    }
}