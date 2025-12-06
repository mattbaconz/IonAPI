package com.ionapi.economy;

import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

/**
 * Represents a currency in the economy system.
 */
public interface Currency {

    /**
     * Gets the unique identifier for this currency.
     */
    @NotNull String getId();

    /**
     * Gets the singular name (e.g., "Dollar").
     */
    @NotNull String getSingularName();

    /**
     * Gets the plural name (e.g., "Dollars").
     */
    @NotNull String getPluralName();

    /**
     * Gets the currency symbol (e.g., "$").
     */
    @NotNull String getSymbol();

    /**
     * Gets the number of decimal places for this currency.
     */
    int getDecimalPlaces();

    /**
     * Formats an amount using this currency's rules.
     */
    @NotNull String format(@NotNull BigDecimal amount);

    /**
     * Gets the appropriate name based on amount (singular or plural).
     */
    default @NotNull String getName(@NotNull BigDecimal amount) {
        return amount.compareTo(BigDecimal.ONE) == 0 ? getSingularName() : getPluralName();
    }

    /**
     * Checks if this is the default/primary currency.
     */
    boolean isDefault();
}
