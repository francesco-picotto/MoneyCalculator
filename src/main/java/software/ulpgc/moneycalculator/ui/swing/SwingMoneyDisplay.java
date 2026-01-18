package software.ulpgc.moneycalculator.ui.swing;

import software.ulpgc.moneycalculator.domain.model.Money;
import software.ulpgc.moneycalculator.ui.MoneyDisplay;

import javax.swing.*;
import java.awt.*;
import java.text.DecimalFormat;

/**
 * SwingMoneyDisplay implementa l'interfaccia MoneyDisplay
 *
 * SCOPO:
 * Mostrare il risultato della conversione di valuta in modo chiaro e leggibile
 *
 * CARATTERISTICHE:
 * - Display di sola lettura (l'utente non può modificarlo)
 * - Formattazione del numero con separatori delle migliaia e 2 decimali
 * - Testo grande e chiaro per facile lettura
 * - Aggiornamento automatico quando viene chiamato show()
 *
 * PATTERN MVC:
 * Questo è un componente "Display" della Vista
 * Si aggiorna automaticamente quando il Model cambia (tramite il Command)
 */
public class SwingMoneyDisplay extends JPanel implements MoneyDisplay {
    private JLabel resultLabel;
    private final DecimalFormat numberFormatter;

    /**
     * Costruttore: inizializza il display e il formatter per i numeri
     */
    public SwingMoneyDisplay() {
        // DecimalFormat per formattare i numeri: 1,234.56
        this.numberFormatter = new DecimalFormat("#,##0.00");
        createComponents();
    }

    /**
     * Crea e configura i componenti grafici
     *
     * Layout: BorderLayout semplice
     * - Label centrata con il risultato
     * - Bordo titolato "Result"
     * - Font grande per facile lettura
     */
    private void createComponents() {
        setLayout(new BorderLayout());
        setBorder(BorderFactory.createTitledBorder("Result"));
        setPreferredSize(new Dimension(0, 100));

        // Label per mostrare il risultato
        resultLabel = new JLabel("No conversion yet", SwingConstants.CENTER);
        resultLabel.setFont(new Font("Arial", Font.BOLD, 24));
        resultLabel.setForeground(new Color(0, 100, 0)); // Verde scuro

        add(resultLabel, BorderLayout.CENTER);
    }

    /**
     * Metodo dell'interfaccia MoneyDisplay
     *
     * Viene chiamato da ExchangeMoneyCommand dopo aver calcolato la conversione
     *
     * FUNZIONAMENTO:
     * 1. Riceve l'oggetto Money con il risultato
     * 2. Formatta l'importo con separatori e decimali
     * 3. Crea una stringa leggibile: "1,234.56 EUR"
     * 4. Aggiorna la label
     *
     * @param money Il risultato della conversione da mostrare
     */
    @Override
    public void show(Money money) {
        if (money == null) {
            resultLabel.setText("Error: Invalid conversion");
            resultLabel.setForeground(Color.RED);
            return;
        }

        // Formatta il numero: 1234.567 -> "1,234.57"
        String formattedAmount = numberFormatter.format(money.amount());

        // Crea il testo completo: "1,234.57 EUR"
        String result = formattedAmount + " " + money.currency().code();

        // Aggiorna la label
        resultLabel.setText(result);
        resultLabel.setForeground(new Color(0, 100, 0)); // Verde per successo
    }

    /**
     * Metodo aggiuntivo per mostrare messaggi di errore
     * Non fa parte dell'interfaccia ma utile per gestire errori
     *
     * @param errorMessage Messaggio di errore da mostrare
     */
    public void showError(String errorMessage) {
        resultLabel.setText("Error: " + errorMessage);
        resultLabel.setForeground(Color.RED);
    }

    /**
     * Resetta il display allo stato iniziale
     * Utile se si vuole pulire il risultato precedente
     */
    public void clear() {
        resultLabel.setText("No conversion yet");
        resultLabel.setForeground(Color.GRAY);
    }

    /**
     * Restituisce il pannello per essere aggiunto al MainFrame
     */
    public JPanel getPanel() {
        return this;
    }
}