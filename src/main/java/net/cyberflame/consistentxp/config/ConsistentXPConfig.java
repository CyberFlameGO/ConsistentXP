package net.cyberflame.consistentxp.config;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.logging.Logger;

public final class ConsistentXPConfig {

    private final MiniMessage miniMessage = MiniMessage.miniMessage();
    private final Logger logger;

    private int xpPerLevel;
    private boolean levelUpSoundEnabled;
    private Sound levelUpSound;
    private float levelUpVolume;
    private float levelUpPitch;

    private DeathDropCalculation deathDropCalculation;
    private DeathDropStrategy deathDropStrategy;
    private double deathDropPercentage;
    private int deathFlatDroppedXp;
    private int deathMaxDroppedXp;
    private boolean deathKeepXp;

    private boolean migrationEnabled;
    private MigrationMode migrationMode;

    private String prefix;
    private String noPermission;
    private String playerNotFound;
    private String invalidAmount;
    private String reloaded;
    private String xpCheckSelf;
    private String xpCheckOther;
    private String xpAdded;
    private String xpSet;

    public ConsistentXPConfig(Logger logger) {
        this.logger = logger;
    }

    public void load(FileConfiguration config) {
        this.xpPerLevel = Math.max(1, config.getInt("xp-per-level", 50));

        this.levelUpSoundEnabled = config.getBoolean("sound.level-up.enabled", true);
        String soundName = config.getString("sound.level-up.sound", "entity.player.levelup");
        org.bukkit.NamespacedKey soundKey = (soundName != null && soundName.contains(":"))
                ? org.bukkit.NamespacedKey.fromString(soundName.toLowerCase(java.util.Locale.ROOT))
                : (soundName != null ? org.bukkit.NamespacedKey.minecraft(soundName.toLowerCase(java.util.Locale.ROOT).replace('_', '.')) : null);
        org.bukkit.Sound foundSound = (soundKey != null) ? org.bukkit.Registry.SOUNDS.get(soundKey) : null;
        if (foundSound != null) {
            this.levelUpSound = foundSound;
        } else {
            this.logger.warning("Unknown sound '" + soundName + "', defaulting to ENTITY_PLAYER_LEVELUP.");
            this.levelUpSound = org.bukkit.Sound.ENTITY_PLAYER_LEVELUP;
        }
        this.levelUpVolume = (float) config.getDouble("sound.level-up.volume", 0.75);
        this.levelUpPitch = (float) config.getDouble("sound.level-up.pitch", 1.0);

        this.deathDropCalculation = DeathDropCalculation.fromString(config.getString("death.drop-calculation", "VANILLA"));
        this.deathDropPercentage = Math.max(0.0, Math.min(100.0, config.getDouble("death.drop-percentage", 50.0)));
        this.deathFlatDroppedXp = Math.max(0, config.getInt("death.flat-dropped-xp", 50));
        this.deathMaxDroppedXp = config.getInt("death.max-dropped-xp", 100);
        this.deathKeepXp = config.getBoolean("death.keep-xp-on-death", false);
        this.deathDropStrategy = DeathDropStrategy.fromConfig(
                config.getString("death.drop-calculation", "VANILLA"),
                this.deathDropPercentage,
                this.deathFlatDroppedXp,
                this.deathMaxDroppedXp
        );

        this.migrationEnabled = config.getBoolean("migration.enabled", true);
        this.migrationMode = MigrationMode.fromString(config.getString("migration.mode", "CONVERT_TOTAL_XP"));

        this.prefix = config.getString("messages.prefix", "<dark_aqua>[<aqua>Consistent XP<dark_aqua>] <gray>");
        this.noPermission = config.getString("messages.no-permission", "<red>You do not have permission to execute this command.");
        this.playerNotFound = config.getString("messages.player-not-found", "<red>Player '<yellow><player></yellow>' not found or is offline.");
        this.invalidAmount = config.getString("messages.invalid-amount", "<red>Amount must be a positive integer.");
        this.reloaded = config.getString("messages.reloaded", "<green>Configuration reloaded successfully!");
        this.xpCheckSelf = config.getString("messages.xp-check-self", "<gray>Level: <aqua><level></aqua> | Progress: <aqua><points></aqua>/<aqua><max_points></aqua> XP (<aqua><percent>%</aqua>) | Next level: <aqua><needed></aqua> XP | Total: <aqua><total></aqua> XP");
        this.xpCheckOther = config.getString("messages.xp-check-other", "<gray><player>'s Level: <aqua><level></aqua> | Progress: <aqua><points></aqua>/<aqua><max_points></aqua> XP (<aqua><percent>%</aqua>) | Next level: <aqua><needed></aqua> XP | Total: <aqua><total></aqua> XP");
        this.xpAdded = config.getString("messages.xp-added", "<green>Added <aqua><amount></aqua> <type> to <yellow><player></yellow>.");
        this.xpSet = config.getString("messages.xp-set", "<green>Set <type> of <yellow><player></yellow> to <aqua><amount></aqua>.");

        this.cachedNoPermission = parseMessage(this.noPermission);
        this.cachedInvalidAmount = parseMessage(this.invalidAmount);
        this.cachedReloaded = parseMessage(this.reloaded);
    }

    private Component cachedNoPermission;
    private Component cachedInvalidAmount;
    private Component cachedReloaded;

    public int getXpPerLevel() {
        return xpPerLevel;
    }

    public boolean isLevelUpSoundEnabled() {
        return levelUpSoundEnabled;
    }

    public Sound getLevelUpSound() {
        return levelUpSound;
    }

    public float getLevelUpVolume() {
        return levelUpVolume;
    }

    public float getLevelUpPitch() {
        return levelUpPitch;
    }

    public DeathDropCalculation getDeathDropCalculation() {
        return deathDropCalculation;
    }

    public DeathDropStrategy getDeathDropStrategy() {
        return deathDropStrategy;
    }

    public double getDeathDropPercentage() {
        return deathDropPercentage;
    }

    public int getDeathFlatDroppedXp() {
        return deathFlatDroppedXp;
    }

    public int getDeathMaxDroppedXp() {
        return deathMaxDroppedXp;
    }

    public boolean isDeathKeepXp() {
        return deathKeepXp;
    }

    public boolean isMigrationEnabled() {
        return migrationEnabled;
    }

    public MigrationMode getMigrationMode() {
        return migrationMode;
    }

    public Component parseMessage(String rawMessage, TagResolver... resolvers) {
        String full = prefix + rawMessage;
        return miniMessage.deserialize(full, resolvers);
    }

    public Component getNoPermissionMessage() {
        return cachedNoPermission;
    }

    public Component getInvalidAmountMessage() {
        return cachedInvalidAmount;
    }

    public Component getReloadedMessage() {
        return cachedReloaded;
    }

    public Component getPlayerNotFoundMessage(String playerName) {
        return parseMessage(playerNotFound, net.kyori.adventure.text.minimessage.tag.resolver.Placeholder.unparsed("player", playerName));
    }

    public String getRawPrefix() {
        return prefix;
    }

    public String getRawNoPermission() {
        return noPermission;
    }

    public String getRawPlayerNotFound() {
        return playerNotFound;
    }

    public String getRawInvalidAmount() {
        return invalidAmount;
    }

    public String getRawReloaded() {
        return reloaded;
    }

    public String getRawXpCheckSelf() {
        return xpCheckSelf;
    }

    public String getRawXpCheckOther() {
        return xpCheckOther;
    }

    public String getRawXpAdded() {
        return xpAdded;
    }

    public String getRawXpSet() {
        return xpSet;
    }
}
