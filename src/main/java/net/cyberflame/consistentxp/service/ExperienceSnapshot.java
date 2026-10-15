package net.cyberflame.consistentxp.service;

/**
 * An immutable modern Java record representing a point-in-time snapshot
 * of a player's consistent experience statistics.
 *
 * @param level                     current experience level
 * @param pointsInCurrentLevel      points accumulated within the current level
 * @param pointsNeededForNextLevel  points required to reach the next level
 * @param xpPerLevel                fixed points required per level
 * @param progress                  progress bar float between 0.0f and 1.0f
 * @param totalExperience           total consistent experience points
 */
public record ExperienceSnapshot(
        int level,
        int pointsInCurrentLevel,
        int pointsNeededForNextLevel,
        int xpPerLevel,
        float progress,
        long totalExperience
) {
    /**
     * Calculates the progress towards the next level as a percentage rounded to 1 decimal place.
     *
     * @return progress percentage (0.0 to 100.0)
     */
    public double progressPercentage() {
        if (xpPerLevel <= 0) {
            return 0.0;
        }
        return Math.round(((double) pointsInCurrentLevel / xpPerLevel) * 1000.0) / 10.0;
    }
}
