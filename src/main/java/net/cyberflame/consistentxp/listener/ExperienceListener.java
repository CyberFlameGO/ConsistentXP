package net.cyberflame.consistentxp.listener;

import net.cyberflame.consistentxp.config.ConsistentXPConfig;
import net.cyberflame.consistentxp.math.ExperienceMath;
import net.cyberflame.consistentxp.service.ExperienceManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;
import org.bukkit.event.player.PlayerJoinEvent;

public final class ExperienceListener implements Listener {

    private final ExperienceManager experienceManager;
    private final ConsistentXPConfig config;

    public ExperienceListener(ExperienceManager experienceManager, ConsistentXPConfig config) {
        this.experienceManager = experienceManager;
        this.config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        int amount = event.getAmount();
        if (amount <= 0) {
            return;
        }

        event.setAmount(0);
        experienceManager.addExperience(event.getPlayer(), amount);
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        experienceManager.handlePlayerJoin(event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getPlayer();

        if (config.isDeathKeepXp()) {
            event.setKeepLevel(true);
            event.setDroppedExp(0);
            return;
        }

        if (!event.getKeepLevel()) {
            long totalXp = experienceManager.getTotalExperience(player);
            int dropped = config.getDeathDropStrategy().calculateDrop(totalXp, player.getLevel());
            event.setDroppedExp(dropped);
        }
    }
}
