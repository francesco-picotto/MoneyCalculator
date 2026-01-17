package software.ulpgc.moneycalculator.control;

/**
 * Backward-compatible Command interface.
 * Keep this if you have UI code that depends on it.
 */
public interface Command {
    void execute();
}
