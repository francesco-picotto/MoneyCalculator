package software.ulpgc.moneycalculator.ui.swing;

import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.domain.model.Money;
import software.ulpgc.moneycalculator.ui.MoneyDialog;

import javax.swing.*;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import java.awt.*;
import java.util.List;

/**
 * SwingMoneyDialog implementa l'interfaccia MoneyDialog
 *
 * SCOPO:
 * Permette all'utente di inserire l'importo e selezionare la valuta di partenza
 *
 * COMPONENTI:
 * - JTextField: per inserire l'importo numerico
 * - JComboBox: per scegliere la valuta di partenza
 *
 * RESPONSABILITÀ:
 * - Raccogliere l'input dell'utente
 * - Validare che l'importo sia un numero valido
 * - Creare un oggetto Money con i dati inseriti
 *
 * IMPROVEMENTS:
 * - Fixed currency display (now shows "USD - United States Dollar")
 * - Added input validation (only allows numbers and decimal point)
 * - Added helper methods for swap functionality
 */
public class SwingMoneyDialog extends JPanel implements MoneyDialog {
    private JTextField amountField;
    private JComboBox<Currency> currencyComboBox;

    /**
     * Costruttore: crea il pannello con i componenti di input
     */
    public SwingMoneyDialog() {
        createComponents();
    }

    /**
     * Crea e configura i componenti grafici
     *
     * Layout: GridLayout 2x2
     * - Label "Amount:" | TextField per importo
     * - Label "From Currency:" | ComboBox per valuta
     */
    private void createComponents() {
        setLayout(new GridLayout(2, 2, 10, 10));
        setBorder(BorderFactory.createTitledBorder("Source Money"));

        // Campo per l'importo
        add(new JLabel("Amount:"));
        amountField = new JTextField("100"); // Valore predefinito
        amountField.setFont(new Font("Arial", Font.PLAIN, 14));

        // IMPROVEMENT: Add input validation - only allow numbers and decimal point
        ((AbstractDocument) amountField.getDocument()).setDocumentFilter(new DecimalDocumentFilter());

        add(amountField);

        // ComboBox per la valuta di partenza
        add(new JLabel("From Currency:"));
        currencyComboBox = new JComboBox<>();
        currencyComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        // Renderer personalizzato per mostrare codice e nome della valuta
        // FIXED: Now correctly shows "USD - United States Dollar" instead of "USD - USD"
        currencyComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Currency currency) {
                    // FIXED: was currency.code() + " - " + currency.code()
                    setText(currency.code() + " - " + currency.name());
                }
                return this;
            }
        });
        add(currencyComboBox);
    }

    /**
     * Metodo dell'interfaccia MoneyDialog
     *
     * FUNZIONAMENTO:
     * 1. Legge il testo dal campo importo
     * 2. Lo converte in double
     * 3. Prende la valuta selezionata dalla ComboBox
     * 4. Crea e restituisce un oggetto Money
     *
     * GESTIONE ERRORI:
     * Se l'importo non è un numero valido, mostra un messaggio di errore
     *
     * @return Money object con importo e valuta inseriti dall'utente
     */
    @Override
    public Money get() {
        try {
            String amountText = amountField.getText().trim();
            if (amountText.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                        "Please enter an amount",
                        "Missing Amount",
                        JOptionPane.WARNING_MESSAGE);
                return Money.of(0, getSelectedCurrency());
            }

            double amount = Double.parseDouble(amountText);

            if (amount < 0) {
                JOptionPane.showMessageDialog(this,
                        "Amount cannot be negative",
                        "Invalid Amount",
                        JOptionPane.ERROR_MESSAGE);
                return Money.of(0, getSelectedCurrency());
            }

            Currency currency = getSelectedCurrency();

            if (currency == null) {
                JOptionPane.showMessageDialog(this,
                        "Please select a currency",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return Money.of(0, Currency.of("USD", "United States Dollar"));
            }

            return Money.of(amount, currency);
        } catch (NumberFormatException e) {
            // Se l'utente inserisce testo invece di un numero
            JOptionPane.showMessageDialog(this,
                    "Please enter a valid number",
                    "Invalid Amount",
                    JOptionPane.ERROR_MESSAGE);
            return Money.of(0, getSelectedCurrency());
        }
    }

    /**
     * Popola la ComboBox con la lista delle valute disponibili
     * Viene chiamato dal MainFrame dopo aver caricato le valute dalla API
     *
     * @param currencies Lista di valute da mostrare nella ComboBox
     */
    public void setCurrencies(List<Currency> currencies) {
        currencyComboBox.removeAllItems();
        for (Currency currency : currencies) {
            currencyComboBox.addItem(currency);
        }

        // Set USD as default if available
        for (int i = 0; i < currencyComboBox.getItemCount(); i++) {
            Currency c = currencyComboBox.getItemAt(i);
            if (c.code().equals("USD")) {
                currencyComboBox.setSelectedIndex(i);
                break;
            }
        }
    }

    /**
     * Get the currently selected currency (for swap functionality)
     */
    public Currency getSelectedCurrency() {
        return (Currency) currencyComboBox.getSelectedItem();
    }

    /**
     * Set the selected currency programmatically (for swap functionality)
     */
    public void setSelectedCurrency(Currency currency) {
        if (currency != null) {
            currencyComboBox.setSelectedItem(currency);
        }
    }

    /**
     * Get the current amount text (useful for validation)
     */
    public String getAmountText() {
        return amountField.getText();
    }

    /**
     * Set the amount programmatically
     */
    public void setAmount(double amount) {
        amountField.setText(String.format("%.2f", amount));
    }

    /**
     * Restituisce il pannello per essere aggiunto al MainFrame
     */
    public JPanel getPanel() {
        return this;
    }

    /**
     * DocumentFilter that only allows decimal numbers (digits and one decimal point)
     * This prevents users from entering invalid characters
     */
    private static class DecimalDocumentFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            if (isValid(fb, offset, string, 0)) {
                super.insertString(fb, offset, string, attr);
            }
        }

        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            if (isValid(fb, offset, text, length)) {
                super.replace(fb, offset, length, text, attrs);
            }
        }

        private boolean isValid(FilterBypass fb, int offset, String string, int length)
                throws BadLocationException {
            // Allow empty string (for backspace/delete)
            if (string == null || string.isEmpty()) {
                return true;
            }

            // Only allow digits and decimal point
            if (!string.matches("[0-9.]+")) {
                return false;
            }

            // Get the current text
            String currentText = fb.getDocument().getText(0, fb.getDocument().getLength());

            // Build the new text that would result from this edit
            String beforeOffset = currentText.substring(0, offset);
            String afterOffset = currentText.substring(Math.min(offset + length, currentText.length()));
            String newText = beforeOffset + string + afterOffset;

            // Don't allow multiple decimal points
            if (newText.indexOf('.') != newText.lastIndexOf('.')) {
                return false;
            }

            // Don't allow decimal point at the start (must have at least one digit before)
            if (newText.startsWith(".")) {
                return false;
            }

            return true;
        }
    }
}