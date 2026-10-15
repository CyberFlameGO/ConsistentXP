package net.cyberflame.consistentxp.config;

/**
 * A sealed hierarchy defining how experience points are dropped upon player death.
 * Sealed interfaces provide exhaustive pattern matching and strongly-typed strategy parameters.
 */
public sealed interface DeathDropStrategy permits
        DeathDropStrategy.Vanilla,
        DeathDropStrategy.Percentage,
        DeathDropStrategy.Flat,
        DeathDropStrategy.All {

    /**
     * Calculates the experience points to drop based on player state.
     *
     * @param totalXp total consistent experience points
     * @param level   current experience level
     * @return points to drop on death
     */
    int calculateDrop(long totalXp, int level);

    record Vanilla() implements DeathDropStrategy {
        @Override
        public int calculateDrop(long totalXp, int level) {
            return Math.max(0, Math.min(level * 7, 100));
        }
    }

    record Percentage(double percentage, int maxDrop) implements DeathDropStrategy {
        @Override
        public int calculateDrop(long totalXp, int level) {
            double pct = Math.max(0.0, Math.min(100.0, percentage));
            int dropped = (int) Math.round(totalXp * (pct / 100.0));
            return (maxDrop >= 0) ? Math.min(dropped, maxDrop) : Math.max(0, dropped);
        }
    }

    record Flat(int amount, int maxDrop) implements DeathDropStrategy {
        @Override
        public int calculateDrop(long totalXp, int level) {
            int dropped = Math.max(0, amount);
            return (maxDrop >= 0) ? Math.min(dropped, maxDrop) : dropped;
        }
    }

    record All(int maxDrop) implements DeathDropStrategy {
        @Override
        public int calculateDrop(long totalXp, int level) {
            int dropped = (int) Math.min(Integer.MAX_VALUE, Math.max(0L, totalXp));
            return (maxDrop >= 0) ? Math.min(dropped, maxDrop) : dropped;
        }
    }

    static DeathDropStrategy fromConfig(String calculationName, double percentage, int flatDrop, int maxDrop) {
        DeathDropCalculation type = DeathDropCalculation.fromString(calculationName);
        return switch (type) {
            case VANILLA -> new Vanilla();
            case PERCENTAGE -> new Percentage(percentage, maxDrop);
            case FLAT -> new Flat(flatDrop, maxDrop);
            case ALL -> new All(maxDrop);
        };
    }
}
