package software.ulpgc.moneycalculator.ui.swing;

import software.ulpgc.moneycalculator.domain.model.Currency;
import software.ulpgc.moneycalculator.ui.CurrencyDialog;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * SwingCurrencyDialog implementa l'interfaccia CurrencyDialog
 *
 * SCOPO:
 * Permette all'utente di selezionare la valuta di destinazione per la conversione
 *
 * DIFFERENZA con SwingMoneyDialog:
 * - SwingMoneyDialog: importo + valuta di PARTENZA
 * - SwingCurrencyDialog: solo valuta di DESTINAZIONE
 *
 * FIXED: Now correctly returns Currency instead of Money
 */
public class SwingCurrencyDialog extends JPanel implements CurrencyDialog {
    private JComboBox<Currency> currencyComboBox;

    /**
     * Costruttore: crea il pannello con la ComboBox per la valuta
     */
    public SwingCurrencyDialog() {
        createComponents();
    }

    /**
     * Crea e configura i componenti grafici
     *
     * Layout: FlowLayout semplice
     * - Label "To Currency:"
     * - ComboBox per selezione valuta
     */
    private void createComponents() {
        setLayout(new FlowLayout(FlowLayout.LEFT));
        setBorder(BorderFactory.createTitledBorder("Target Currency"));

        add(new JLabel("To Currency:"));

        currencyComboBox = new JComboBox<>();
        currencyComboBox.setPreferredSize(new Dimension(300, 30));
        currencyComboBox.setFont(new Font("Arial", Font.PLAIN, 14));

        // Renderer per mostrare il formato: "EUR - Euro"
        // FIXED: Now shows "EUR - Euro" instead of "EUR - EUR"
        currencyComboBox.setRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value,
                                                          int index, boolean isSelected,
                                                          boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Currency currency) {
                    setText(currency.code() + " - " + currency.name()); // FIXED: was currency.code() twice
                }
                return this;
            }
        });

        add(currencyComboBox);
    }

    /**
     * Metodo dell'interfaccia CurrencyDialog
     *
     * FIXED: Now returns Currency directly instead of wrapping it in Money
     *
     * @return The selected Currency
     */
    @Override
    public Currency get() {
        Currency currency = (Currency) currencyComboBox.getSelectedItem();

        if (currency == null) {
            JOptionPane.showMessageDialog(this,
                    "Please select a target currency",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            // Return default EUR if nothing selected
            return Currency.of("EUR", "Euro");
        }

        return currency;
    }

    /**
     * Popola la ComboBox con le valute disponibili
     *
     * @param currencies Lista di tutte le valute supportate dall'API
     */
    public void setCurrencies(List<Currency> currencies) {
        currencyComboBox.removeAllItems();
        for (Currency currency : currencies) {
            currencyComboBox.addItem(currency);
        }

        // Imposta EUR come valuta predefinita se presente
        for (int i = 0; i < currencyComboBox.getItemCount(); i++) {
            Currency c = currencyComboBox.getItemAt(i);
            if (c.code().equals("EUR")) {
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
     * Restituisce il pannello per essere aggiunto al MainFrame
     */
    public JPanel getPanel() {
        return this;
    }
}