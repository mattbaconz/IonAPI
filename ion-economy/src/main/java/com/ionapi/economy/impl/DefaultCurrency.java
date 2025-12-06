package com.ionapi.economy.impl;

import com.ionapi.economy.Currency;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;

/**
 * Default currency implementation.
 */
public class DefaultCurrency implements Currency {

    private final String id;
    private final String singularName;
    private final String pluralName;
    private final String symbol;
    private final int decimalPlaces;
    private final boolean isDefault;
    private final DecimalFormat format;

    public DefaultCurrency(@NotNull String id, @NotNull String singularName, @NotNull String pluralName,
                           @NotNull String symbol, int decimalPlaces, boolean isDefault) {
        this.id = id;
        this.singularName = singularName;
        this.pluralName = pluralName;
        this.symbol = symbol;
        this.decimalPlaces = decimalPlaces;
        this.isDefault = isDefault;
        
        // Build format pattern
        StringBuilder pattern = new StringBuilder("#,##0");
        if (decimalPlaces > 0) {
            pattern.append(".");
            pattern.append("0".repeat(decimalPlaces));
        }
        this.format = new DecimalFormat(pattern.toString());
    }

    /**
     * Creates a simple dollar-based currency.
     */
    public static DefaultCurrency dollars() {
        return new DefaultCurrency("default", "Dollar", "Dollars", "$", 2, true);
    }

    /**
     * Creates a simple coin-based currency (no decimals).
     */
    public static DefaultCurrency coins() {
        return new DefaultCurrency("coins", "Coin", "Coins", "⛃", 0, true);
    }

    @Override
    public @NotNull String getId() {
        return id;
    }

    @Override
    public @NotNull String getSingularName() {
        return singularName;
    }

    @Override
    public @NotNull String getPluralName() {
        return pluralName;
    }

    @Override
    public @NotNull String getSymbol() {
        return symbol;
    }

    @Override
    public int getDecimalPlaces() {
        return decimalPlaces;
    }

    @Override
    public @NotNull String format(@NotNull BigDecimal amount) {
        BigDecimal rounded = amount.setScale(decimalPlaces, RoundingMode.HALF_UP);
        return symbol + format.format(rounded) + " " + getName(rounded);
    }

    @Override
    public boolean isDefault() {
        return isDefault;
    }
}
