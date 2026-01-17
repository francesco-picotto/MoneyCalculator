package software.ulpgc.moneycalculator.domain.model;

import java.util.Objects;

/**
 * Represents a currency in the system.
 * This is an immutable value object that ensures currency code validity.
 */
public final class Currency {
    private final String code;
    private final String name;

    private Currency(String code, String name) {
        this.code = validateCode(code);
        this.name = validateName(name);
    }

    public static Currency of(String code, String name) {
        return new Currency(code, name);
    }

    private String validateCode(String code) {
        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Currency code cannot be null or empty");
        }
        String trimmed = code.trim().toUpperCase();
        if (trimmed.length() != 3) {
            throw new IllegalArgumentException(
                "Currency code must be exactly 3 characters, got: " + code
            );
        }
        return trimmed;
    }

    private String validateName(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Currency name cannot be null or empty");
        }
        return name.trim();
    }

    public String code() {
        return code;
    }

    public String name() {
        return name;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Currency)) return false;
        Currency currency = (Currency) obj;
        return code.equals(currency.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", code, name);
    }
}
