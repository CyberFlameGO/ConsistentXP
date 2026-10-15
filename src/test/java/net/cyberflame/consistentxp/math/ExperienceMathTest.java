package net.cyberflame.consistentxp.math;

import net.cyberflame.consistentxp.config.DeathDropCalculation;
import net.cyberflame.consistentxp.config.DeathDropStrategy;
import net.cyberflame.consistentxp.config.MigrationMode;
import net.cyberflame.consistentxp.service.ExperienceSnapshot;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ExperienceMathTest {

    @Test
    @DisplayName("Level and progress calculations for consistent XP")
    void testBasicConsistentCalculations() {
        int xpPerLevel = 50;

        assertEquals(0, ExperienceMath.calculateLevel(0, xpPerLevel));
        assertEquals(0, ExperienceMath.calculatePointsInLevel(0, xpPerLevel));
        assertEquals(0.0f, ExperienceMath.calculateProgress(0, xpPerLevel), 0.0001f);
        assertEquals(50, ExperienceMath.calculatePointsNeededForNextLevel(0, xpPerLevel));

        assertEquals(0, ExperienceMath.calculateLevel(25, xpPerLevel));
        assertEquals(25, ExperienceMath.calculatePointsInLevel(25, xpPerLevel));
        assertEquals(0.5f, ExperienceMath.calculateProgress(25, xpPerLevel), 0.0001f);
        assertEquals(25, ExperienceMath.calculatePointsNeededForNextLevel(25, xpPerLevel));

        assertEquals(1, ExperienceMath.calculateLevel(50L, xpPerLevel));
        assertEquals(0, ExperienceMath.calculatePointsInLevel(50L, xpPerLevel));
        assertEquals(0.0f, ExperienceMath.calculateProgress(50L, xpPerLevel), 0.0001f);
        assertEquals(50, ExperienceMath.calculatePointsNeededForNextLevel(0, xpPerLevel));

        assertEquals(30, ExperienceMath.calculateLevel(1525L, xpPerLevel));
        assertEquals(25, ExperienceMath.calculatePointsInLevel(1525L, xpPerLevel));
        assertEquals(0.5f, ExperienceMath.calculateProgress(1525L, xpPerLevel), 0.0001f);
        assertEquals(25, ExperienceMath.calculatePointsNeededForNextLevel(25, xpPerLevel));
    }

    @ParameterizedTest
    @ValueSource(ints = {10, 30, 50, 100, 139, 500})
    @DisplayName("Total XP round-trips correctly for various xpPerLevel values")
    void testTotalXpRoundTrip(int xpPerLevel) {
        for (int level = 0; level <= 50; level += 5) {
            for (int points = 0; points < xpPerLevel; points += Math.max(1, xpPerLevel / 10)) {
                long expectedTotal = (long) level * xpPerLevel + points;
                float progress = ExperienceMath.calculateProgress(points, xpPerLevel);

                long reconstructedTotal = ExperienceMath.calculateTotalExperience(level, progress, xpPerLevel);
                assertEquals(expectedTotal, reconstructedTotal, "Failed round-trip for level " + level + " points " + points);

                int calcLevel = ExperienceMath.calculateLevel(reconstructedTotal, xpPerLevel);
                int calcPoints = ExperienceMath.calculatePointsInLevel(reconstructedTotal, xpPerLevel);
                assertEquals(level, calcLevel);
                assertEquals(points, calcPoints);
            }
        }
    }

    @Test
    @DisplayName("Level progression is consistent and does not scale linearly")
    void testConsistencyAcrossLevels() {
        int xpPerLevel = 50;

        long xpForLevel1 = ExperienceMath.calculateTotalExperience(1, 0.0f, xpPerLevel);
        long xpForLevel0 = ExperienceMath.calculateTotalExperience(0, 0.0f, xpPerLevel);
        assertEquals(50, xpForLevel1 - xpForLevel0);

        long xpForLevel30 = ExperienceMath.calculateTotalExperience(30, 0.0f, xpPerLevel);
        long xpForLevel29 = ExperienceMath.calculateTotalExperience(29, 0.0f, xpPerLevel);
        assertEquals(50, xpForLevel30 - xpForLevel29);

        long xpForLevel100 = ExperienceMath.calculateTotalExperience(100, 0.0f, xpPerLevel);
        long xpForLevel99 = ExperienceMath.calculateTotalExperience(99, 0.0f, xpPerLevel);
        assertEquals(50, xpForLevel100 - xpForLevel99);
    }

    @Test
    @DisplayName("Vanilla total experience calculations match known vanilla thresholds")
    void testVanillaTotalExperience() {
        assertEquals(0, ExperienceMath.calculateVanillaTotalExperience(0, 0.0f));
        assertEquals(7, ExperienceMath.calculateVanillaTotalExperience(1, 0.0f));
        assertEquals(315, ExperienceMath.calculateVanillaTotalExperience(15, 0.0f));
        assertEquals(1395, ExperienceMath.calculateVanillaTotalExperience(30, 0.0f));
        assertEquals(1451, ExperienceMath.calculateVanillaTotalExperience(30, 0.5f));
    }

    @Test
    @DisplayName("Death dropped XP calculations respect all modes and caps")
    void testDeathDropCalculations() {
        assertEquals(0, ExperienceMath.calculateDeathDroppedXp(DeathDropCalculation.VANILLA, 0, 0, 50, 100, 50));
        assertEquals(49, ExperienceMath.calculateDeathDroppedXp(DeathDropCalculation.VANILLA, 350, 7, 50, 100, 50));
        assertEquals(100, ExperienceMath.calculateDeathDroppedXp(DeathDropCalculation.VANILLA, 1500, 30, 50, 100, 50));

        assertEquals(750, ExperienceMath.calculateDeathDroppedXp(DeathDropCalculation.PERCENTAGE, 1500, 30, 50.0, -1, 50));
        assertEquals(100, ExperienceMath.calculateDeathDroppedXp(DeathDropCalculation.PERCENTAGE, 1500, 30, 50.0, 100, 50));

        assertEquals(50, ExperienceMath.calculateDeathDroppedXp(DeathDropCalculation.FLAT, 1500, 30, 50.0, 100, 50));
        assertEquals(20, ExperienceMath.calculateDeathDroppedXp(DeathDropCalculation.FLAT, 1500, 30, 50.0, 20, 50));

        assertEquals(1500, ExperienceMath.calculateDeathDroppedXp(DeathDropCalculation.ALL, 1500, 30, 50.0, -1, 50));
        assertEquals(100, ExperienceMath.calculateDeathDroppedXp(DeathDropCalculation.ALL, 1500, 30, 50.0, 100, 50));
    }

    @Test
    @DisplayName("DeathDropStrategy sealed hierarchy calculations and pattern matching")
    void testDeathDropStrategySealedHierarchy() {
        DeathDropStrategy vanilla = new DeathDropStrategy.Vanilla();
        assertEquals(0, vanilla.calculateDrop(0, 0));
        assertEquals(49, vanilla.calculateDrop(350, 7));
        assertEquals(100, vanilla.calculateDrop(1500, 30));

        DeathDropStrategy pct = new DeathDropStrategy.Percentage(50.0, 100);
        assertEquals(100, pct.calculateDrop(1500, 30));
        DeathDropStrategy pctUncapped = new DeathDropStrategy.Percentage(50.0, -1);
        assertEquals(750, pctUncapped.calculateDrop(1500, 30));

        DeathDropStrategy flat = new DeathDropStrategy.Flat(50, 20);
        assertEquals(20, flat.calculateDrop(1500, 30));

        DeathDropStrategy all = new DeathDropStrategy.All(100);
        assertEquals(100, all.calculateDrop(1500, 30));

        DeathDropStrategy created = DeathDropStrategy.fromConfig("PERCENTAGE", 25.0, 50, 100);
        String strategyName = switch (created) {
            case DeathDropStrategy.Vanilla v -> "vanilla";
            case DeathDropStrategy.Percentage p -> "percentage";
            case DeathDropStrategy.Flat f -> "flat";
            case DeathDropStrategy.All a -> "all";
        };
        assertEquals("percentage", strategyName);
    }

    @Test
    @DisplayName("Edge cases and clamped inputs")
    void testEdgeCases() {
        assertEquals(0, ExperienceMath.calculateLevel(-10, 50));
        assertEquals(0, ExperienceMath.calculatePointsInLevel(-10, 50));
        assertEquals(0.0f, ExperienceMath.calculateProgress(-10L, 50));
        assertEquals(0L, ExperienceMath.calculateTotalExperience(-5, -0.5f, 50));

        assertEquals(5345, ExperienceMath.calculateVanillaTotalExperience(50, 0.0f));

        assertEquals(DeathDropCalculation.VANILLA, DeathDropCalculation.fromString(null));
        assertEquals(DeathDropCalculation.PERCENTAGE, DeathDropCalculation.fromString("percentage"));
        assertEquals(DeathDropCalculation.FLAT, DeathDropCalculation.fromString("FLAT"));
        assertEquals(DeathDropCalculation.ALL, DeathDropCalculation.fromString("all"));
        assertEquals(DeathDropCalculation.VANILLA, DeathDropCalculation.fromString("unknown_val"));

        assertEquals(MigrationMode.CONVERT_TOTAL_XP, MigrationMode.fromString(null));
        assertEquals(MigrationMode.KEEP_LEVEL, MigrationMode.fromString("keep_level"));
        assertEquals(MigrationMode.CONVERT_TOTAL_XP, MigrationMode.fromString("convert_total_xp"));
        assertEquals(MigrationMode.CONVERT_TOTAL_XP, MigrationMode.fromString("invalid_mode"));
    }

    @Test
    @DisplayName("ExperienceSnapshot record calculations")
    void testExperienceSnapshot() {
        ExperienceSnapshot snapshot = new ExperienceSnapshot(10, 25, 25, 50, 0.5f, 525L);

        assertEquals(10, snapshot.level());
        assertEquals(25, snapshot.pointsInCurrentLevel());
        assertEquals(25, snapshot.pointsNeededForNextLevel());
        assertEquals(50, snapshot.xpPerLevel());
        assertEquals(0.5f, snapshot.progress());
        assertEquals(525L, snapshot.totalExperience());
        assertEquals(50.0, snapshot.progressPercentage());

        ExperienceSnapshot zeroSnapshot = new ExperienceSnapshot(0, 0, 0, 0, 0.0f, 0L);
        assertEquals(0.0, zeroSnapshot.progressPercentage());
    }
}
