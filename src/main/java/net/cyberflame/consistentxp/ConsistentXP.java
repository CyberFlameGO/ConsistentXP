package net.cyberflame.consistentxp;

import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import net.cyberflame.consistentxp.command.ConsistentXPCommand;
import net.cyberflame.consistentxp.config.ConsistentXPConfig;
import net.cyberflame.consistentxp.listener.ExperienceListener;
import net.cyberflame.consistentxp.placeholder.ConsistentXPExpansion;
import net.cyberflame.consistentxp.service.ExperienceManager;
import org.bukkit.Bukkit;
import org.bukkit.permissions.Permission;
import org.bukkit.permissions.PermissionDefault;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

public final class ConsistentXP extends JavaPlugin {

    private static ConsistentXP instance;
    private ConsistentXPConfig pluginConfig;
    private ExperienceManager experienceManager;

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();
        this.pluginConfig = new ConsistentXPConfig(getLogger());
        this.pluginConfig.load(getConfig());

        this.experienceManager = new ExperienceManager(this, this.pluginConfig);

        registerPermissions();

        getServer().getPluginManager().registerEvents(
                new ExperienceListener(this.experienceManager, this.pluginConfig),
                this
        );

        getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, event -> {
            ConsistentXPCommand command = new ConsistentXPCommand(this, this.experienceManager);
            event.registrar().register("consistentxp", List.of("cxp"), command);
        });

        if (getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
            new ConsistentXPExpansion(this, this.experienceManager).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }

        getLogger().info("ConsistentXP enabled! Experience required per level is fixed at: "
                + this.pluginConfig.getXpPerLevel() + " XP.");
    }

    @Override
    public void onDisable() {
        instance = null;
        getLogger().info("ConsistentXP disabled.");
    }

    public void reloadPluginConfig() {
        reloadConfig();
        this.pluginConfig.load(getConfig());
    }

    private void registerPermissions() {
        PluginManager pm = getServer().getPluginManager();
        if (pm.getPermission("consistentxp.check") == null) {
            pm.addPermission(new Permission("consistentxp.check", "Allows checking own consistent XP stats", PermissionDefault.TRUE));
        }
        if (pm.getPermission("consistentxp.check.others") == null) {
            pm.addPermission(new Permission("consistentxp.check.others", "Allows checking another player's consistent XP stats", PermissionDefault.OP));
        }
        if (pm.getPermission("consistentxp.admin") == null) {
            pm.addPermission(new Permission("consistentxp.admin", "Allows modifying player XP and reloading ConsistentXP configuration", PermissionDefault.OP));
        }
    }

    public static ConsistentXP getInstance() {
        return instance;
    }

    public ExperienceManager getExperienceManager() {
        return experienceManager;
    }

    public ConsistentXPConfig getPluginConfig() {
        return pluginConfig;
    }
}
