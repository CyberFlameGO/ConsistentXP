package net.cyberflame.consistentxp.math;

import net.cyberflame.consistentxp.config.DeathDropCalculation;

/**
 * Pure mathematical utilities for consistent experience calculations,
 * conversions, and death penalties.
 */
public final class ExperienceMath {

    private ExperienceMath() {
    }

    /**
     * Calculates total experience points from level and level progress under consistent scaling.
     *
     * @param level       current experience level (>= 0)
     * @param expProgress current progress bar (0.0f to 1.0f)
     * @param xpPerLevel  fixed XP required per level (> 0)
     * @return total experience points
     */
    public static long calculateTotalExperience(int level, float expProgress, int xpPerLevel) {
        if (level < 0) {
            level = 0;
        }
        if (xpPerLevel <= 0) {
            xpPerLevel = 1;
        }
        float clampedProgress = Math.max(0.0f, Math.min(0.9999f, expProgress));
        int pointsInLevel = Math.min(xpPerLevel - 1, Math.max(0, Math.round(clampedProgress * xpPerLevel)));
        return (long) level * xpPerLevel + pointsInLevel;
    }

    /**
     * Calculates the level from total consistent experience.
     *
     * @param totalXp    total experience points (>= 0)
     * @param xpPerLevel fixed XP required per level (> 0)
     * @return experience level
     */
    public static int calculateLevel(long totalXp, int xpPerLevel) {
        if (totalXp <= 0 || xpPerLevel <= 0) {
            return 0;
        }
        long level = totalXp / xpPerLevel;
        return (int) Math.min(Integer.MAX_VALUE, level);
    }

    /**
     * Calculates experience points within the current level.
     *
     * @param totalXp    total experience points (>= 0)
     * @param xpPerLevel fixed XP required per level (> 0)
     * @return points in the current level (0 to xpPerLevel - 1)
     */
    public static int calculatePointsInLevel(long totalXp, int xpPerLevel) {
        if (totalXp <= 0 || xpPerLevel <= 0) {
            return 0;
        }
        return (int) (totalXp % xpPerLevel);
    }

    /**
     * Calculates the progress bar value (0.0f to 1.0f) from total consistent experience.
     *
     * @param totalXp    total experience points (>= 0)
     * @param xpPerLevel fixed XP required per level (> 0)
     * @return progress value between 0.0f (inclusive) and 1.0f (exclusive)
     */
    public static float calculateProgress(long totalXp, int xpPerLevel) {
        if (totalXp <= 0 || xpPerLevel <= 0) {
            return 0.0f;
        }
        int pointsInLevel = (int) (totalXp % xpPerLevel);
        return calculateProgress(pointsInLevel, xpPerLevel);
    }

    /**
     * Calculates the progress bar value (0.0f to 1.0f) from points in the current level.
     *
     * @param pointsInLevel points accumulated in the current level
     * @param xpPerLevel    fixed XP required per level (> 0)
     * @return progress value between 0.0f (inclusive) and 1.0f (exclusive)
     */
    public static float calculateProgress(int pointsInLevel, int xpPerLevel) {
        if (pointsInLevel <= 0 || xpPerLevel <= 0) {
            return 0.0f;
        }
        if (pointsInLevel >= xpPerLevel) {
            pointsInLevel = xpPerLevel - 1;
        }
        float progress = (float) pointsInLevel / (float) xpPerLevel;
        return Math.max(0.0f, Math.min(0.9999f, progress));
    }

    /**
     * Calculates points needed to reach the next level.
     *
     * @param pointsInLevel points accumulated in the current level
     * @param xpPerLevel    fixed XP required per level (> 0)
     * @return points required for next level
     */
    public static int calculatePointsNeededForNextLevel(int pointsInLevel, int xpPerLevel) {
        if (xpPerLevel <= 0) {
            return 1;
        }
        int clampedPoints = Math.max(0, Math.min(xpPerLevel - 1, pointsInLevel));
        return xpPerLevel - clampedPoints;
    }

    /**
     * Calculates total experience according to vanilla Minecraft piecewise quadratic formulas.
     *
     * @param level       vanilla experience level
     * @param expProgress vanilla progress bar (0.0f to 1.0f)
     * @return total vanilla experience points
     */
    public static long calculateVanillaTotalExperience(int level, float expProgress) {
        if (level < 0) {
            level = 0;
        }
        long baseTotal;
        if (level <= 15) {
            baseTotal = (long) level * level + 6L * level;
        } else if (level <= 30) {
            baseTotal = (long) (2.5 * level * level - 40.5 * level + 360);
        } else {
            baseTotal = (long) (4.5 * level * level - 162.5 * level + 2220);
        }

        int needed = getVanillaExperienceNeededForNextLevel(level);
        float clampedProgress = Math.max(0.0f, Math.min(0.9999f, expProgress));
        int pointsInLevel = Math.round(clampedProgress * needed);
        return baseTotal + pointsInLevel;
    }

    /**
     * Returns the experience points needed to advance to the next level in vanilla Minecraft.
     *
     * @param level current level
     * @return XP required for next level under vanilla rules
     */
    public static int getVanillaExperienceNeededForNextLevel(int level) {
        if (level <= 0) {
            return 7;
        } else if (level <= 15) {
            return 2 * level + 7;
        } else if (level <= 30) {
            return 5 * level - 38;
        } else {
            return 9 * level - 158;
        }
    }

    /**
     * Computes dropped experience when a player dies according to the chosen death calculation.
     *
     * @param calculation   the drop calculation strategy
     * @param totalXp       the player's total consistent XP
     * @param level         the player's current level
     * @param percentage    drop percentage (for PERCENTAGE mode)
     * @param maxDrop       maximum allowed drop (-1 for unlimited)
     * @param flatDrop      flat drop amount (for FLAT mode)
     * @return experience points to drop
     */
    public static int calculateDeathDroppedXp(DeathDropCalculation calculation,
                                             long totalXp,
                                             int level,
                                             double percentage,
                                             int maxDrop,
                                             int flatDrop) {
        if (calculation == null) {
            calculation = DeathDropCalculation.VANILLA;
        }

        int dropped;
        switch (calculation) {
            case VANILLA -> {
                dropped = Math.max(0, Math.min(level * 7, 100));
            }
            case PERCENTAGE -> {
                double pct = Math.max(0.0, Math.min(100.0, percentage));
                dropped = (int) Math.round(totalXp * (pct / 100.0));
            }
            case FLAT -> {
                dropped = Math.max(0, flatDrop);
            }
            case ALL -> {
                dropped = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, totalXp));
            }
            default -> dropped = Math.max(0, Math.min(level * 7, 100));
        }

        if (maxDrop >= 0) {
            dropped = Math.min(dropped, maxDrop);
        }
        return Math.max(0, dropped);
    }
}
