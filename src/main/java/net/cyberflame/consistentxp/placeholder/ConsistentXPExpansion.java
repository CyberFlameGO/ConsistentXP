package net.cyberflame.consistentxp.placeholder;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.cyberflame.consistentxp.ConsistentXP;
import net.cyberflame.consistentxp.service.ExperienceManager;
import net.cyberflame.consistentxp.service.ExperienceSnapshot;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public final class ConsistentXPExpansion extends PlaceholderExpansion {

    private final ConsistentXP plugin;
    private final ExperienceManager experienceManager;

    public ConsistentXPExpansion(ConsistentXP plugin, ExperienceManager experienceManager) {
        this.plugin = plugin;
        this.experienceManager = experienceManager;
    }

    @Override
    public @NotNull String getIdentifier() {
        return "consistentxp";
    }

    @Override
    public @NotNull String getAuthor() {
        return String.join(", ", plugin.getPluginMeta().getAuthors());
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        if (offlinePlayer == null || !offlinePlayer.isOnline()) {
            return null;
        }

        Player player = offlinePlayer.getPlayer();
        if (player == null) {
            return null;
        }

        ExperienceSnapshot snapshot = experienceManager.getSnapshot(player);

        return switch (params.toLowerCase(Locale.ROOT)) {
            case "level" -> String.valueOf(snapshot.level());
            case "points" -> String.valueOf(snapshot.pointsInCurrentLevel());
            case "needed" -> String.valueOf(snapshot.pointsNeededForNextLevel());
            case "max_points", "xp_per_level" -> String.valueOf(snapshot.xpPerLevel());
            case "total" -> String.valueOf(snapshot.totalExperience());
            case "progress", "progress_percent" -> String.format(Locale.ROOT, "%.1f", snapshot.progressPercentage());
            default -> null;
        };
    }
}
