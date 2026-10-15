package net.cyberflame.consistentxp.service;

import net.cyberflame.consistentxp.ConsistentXP;
import net.cyberflame.consistentxp.config.ConsistentXPConfig;
import net.cyberflame.consistentxp.config.MigrationMode;
import net.cyberflame.consistentxp.math.ExperienceMath;
import org.bukkit.NamespacedKey;
import org.bukkit.SoundCategory;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public final class ExperienceManager {

    private final ConsistentXP plugin;
    private final ConsistentXPConfig config;
    private final NamespacedKey migratedKey;

    public ExperienceManager(ConsistentXP plugin, ConsistentXPConfig config) {
        this.plugin = plugin;
        this.config = config;
        this.migratedKey = new NamespacedKey(plugin, "migrated");
    }

    public ConsistentXPConfig getConfig() {
        return config;
    }

    public int getXpPerLevel() {
        return config.getXpPerLevel();
    }

    public int getLevel(Player player) {
        return player.getLevel();
    }

    public float getExp(Player player) {
        return player.getExp();
    }

    public long getTotalExperience(Player player) {
        return ExperienceMath.calculateTotalExperience(player.getLevel(), player.getExp(), getXpPerLevel());
    }

    public int getPointsInCurrentLevel(Player player) {
        return ExperienceMath.calculatePointsInLevel(getTotalExperience(player), getXpPerLevel());
    }

    public int getPointsNeededForNextLevel(Player player) {
        return ExperienceMath.calculatePointsNeededForNextLevel(getPointsInCurrentLevel(player), getXpPerLevel());
    }

    public float getProgress(Player player) {
        return ExperienceMath.calculateProgress(getPointsInCurrentLevel(player), getXpPerLevel());
    }

    /**
     * Retrieves an immutable snapshot of all experience statistics for a player.
     * Computes level, points, needed, and progress in a single pass.
     *
     * @param player the player
     * @return experience snapshot record
     */
    public ExperienceSnapshot getSnapshot(Player player) {
        int maxPoints = getXpPerLevel();
        int level = player.getLevel();
        long total = ExperienceMath.calculateTotalExperience(level, player.getExp(), maxPoints);
        int pointsInLevel = ExperienceMath.calculatePointsInLevel(total, maxPoints);
        int needed = ExperienceMath.calculatePointsNeededForNextLevel(pointsInLevel, maxPoints);
        float progress = ExperienceMath.calculateProgress(pointsInLevel, maxPoints);
        return new ExperienceSnapshot(level, pointsInLevel, needed, maxPoints, progress, total);
    }

    /**
     * Adds experience points using consistent progression.
     *
     * @param player the player
     * @param points the amount of XP points to add
     */
    public void addExperience(Player player, int points) {
        if (points == 0) {
            return;
        }
        long currentTotal = getTotalExperience(player);
        long newTotal = Math.max(0L, currentTotal + points);
        setExperience(player, newTotal);
    }

    /**
     * Sets the player's total experience points and updates level, progress bar, and totalExperience.
     *
     * @param player      the player
     * @param totalPoints total experience points
     */
    public void setExperience(Player player, long totalPoints) {
        int xpPerLevel = getXpPerLevel();
        int oldLevel = player.getLevel();
        long clampedTotal = Math.max(0L, Math.min((long) Integer.MAX_VALUE, totalPoints));

        int newLevel = ExperienceMath.calculateLevel(clampedTotal, xpPerLevel);
        int pointsInLevel = ExperienceMath.calculatePointsInLevel(clampedTotal, xpPerLevel);
        float newExp = ExperienceMath.calculateProgress(pointsInLevel, xpPerLevel);

        if (newLevel != oldLevel) {
            player.setLevel(newLevel);
        }
        player.setExp(newExp);
        player.setTotalExperience((int) Math.min(Integer.MAX_VALUE, clampedTotal));

        if (newLevel > oldLevel && config.isLevelUpSoundEnabled()) {
            player.playSound(player.getLocation(), config.getLevelUpSound(), SoundCategory.PLAYERS,
                    config.getLevelUpVolume(), config.getLevelUpPitch());
        }
    }

    /**
     * Sets the player's level while preserving current points within the level.
     *
     * @param player the player
     * @param level  new level
     */
    public void setLevel(Player player, int level) {
        int clampedLevel = Math.max(0, level);
        int pointsInLevel = getPointsInCurrentLevel(player);
        long newTotal = (long) clampedLevel * getXpPerLevel() + pointsInLevel;
        setExperience(player, newTotal);
    }

    /**
     * Adds or removes levels.
     *
     * @param player the player
     * @param levels levels to add (can be negative)
     */
    public void addLevels(Player player, int levels) {
        setLevel(player, player.getLevel() + levels);
    }

    /**
     * Sets points accumulated within the current level.
     *
     * @param player the player
     * @param points points in level (0 to xpPerLevel - 1)
     */
    public void setPointsInCurrentLevel(Player player, int points) {
        int clampedPoints = Math.max(0, Math.min(getXpPerLevel() - 1, points));
        long newTotal = (long) player.getLevel() * getXpPerLevel() + clampedPoints;
        setExperience(player, newTotal);
    }

    /**
     * Migrates an existing player from vanilla XP scaling if they have not already been migrated.
     *
     * @param player the player joining
     */
    public void handlePlayerJoin(Player player) {
        if (!config.isMigrationEnabled()) {
            return;
        }

        PersistentDataContainer pdc = player.getPersistentDataContainer();
        if (pdc.has(migratedKey, PersistentDataType.BYTE)) {
            return;
        }

        MigrationMode mode = config.getMigrationMode();
        if (mode == MigrationMode.CONVERT_TOTAL_XP) {
            long vanillaTotal = ExperienceMath.calculateVanillaTotalExperience(player.getLevel(), player.getExp());
            setExperience(player, vanillaTotal);
            plugin.getLogger().info("Migrated " + player.getName() + " from vanilla XP (" + vanillaTotal
                    + " total XP) to level " + player.getLevel() + " under ConsistentXP.");
        } else {
            int points = Math.min(getXpPerLevel() - 1, Math.round(player.getExp() * getXpPerLevel()));
            setPointsInCurrentLevel(player, points);
            plugin.getLogger().info("Migrated " + player.getName() + " preserving level " + player.getLevel()
                    + " under ConsistentXP.");
        }

        pdc.set(migratedKey, PersistentDataType.BYTE, (byte) 1);
    }
}
