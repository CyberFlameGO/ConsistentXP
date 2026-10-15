package net.cyberflame.consistentxp.config;

public enum MigrationMode {
    CONVERT_TOTAL_XP,
    KEEP_LEVEL;

    public static MigrationMode fromString(String value) {
        if (value == null) {
            return CONVERT_TOTAL_XP;
        }
        try {
            return MigrationMode.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException e) {
            return CONVERT_TOTAL_XP;
        }
    }
}
