package net.cyberflame.consistentxp.config;

public enum DeathDropCalculation {
    VANILLA,
    PERCENTAGE,
    FLAT,
    ALL;

    public static DeathDropCalculation fromString(String value) {
        if (value == null) {
            return VANILLA;
        }
        try {
            return DeathDropCalculation.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return VANILLA;
        }
    }
}
