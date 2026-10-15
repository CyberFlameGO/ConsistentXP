package net.cyberflame.consistentxp.command;

import io.papermc.paper.command.brigadier.BasicCommand;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import net.cyberflame.consistentxp.ConsistentXP;
import net.cyberflame.consistentxp.config.ConsistentXPConfig;
import net.cyberflame.consistentxp.service.ExperienceManager;
import net.cyberflame.consistentxp.service.ExperienceSnapshot;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.List;
import java.util.Locale;
import java.util.SequencedCollection;

public final class ConsistentXPCommand implements BasicCommand {

    private static final SequencedCollection<String> ROOT_SUBCOMMANDS = List.of("check", "add", "set", "reload");
    private static final SequencedCollection<String> SUGGESTED_AMOUNTS = List.of("1", "5", "10", "50", "100");
    private static final SequencedCollection<String> SUGGESTED_TYPES = List.of("points", "levels");

    private final ConsistentXP plugin;
    private final ExperienceManager experienceManager;

    public ConsistentXPCommand(ConsistentXP plugin, ExperienceManager experienceManager) {
        this.plugin = plugin;
        this.experienceManager = experienceManager;
    }

    @Override
    public void execute(CommandSourceStack stack, String[] args) {
        CommandSender sender = stack.getSender();
        ConsistentXPConfig config = experienceManager.getConfig();

        if (args.length == 0) {
            if (sender instanceof Player player) {
                if (!sender.hasPermission("consistentxp.check")) {
                    sender.sendMessage(config.getNoPermissionMessage());
                    return;
                }
                sendPlayerXpInfo(sender, player, true);
                return;
            }
            sendHelp(sender);
            return;
        }

        String subCommand = args[0].toLowerCase(Locale.ROOT);
        switch (subCommand) {
            case "check", "info", "get", "query" -> handleCheck(sender, args);
            case "add" -> handleAdd(sender, args);
            case "set" -> handleSet(sender, args);
            case "reload" -> handleReload(sender);
            default -> sendHelp(sender);
        }
    }

    private void handleCheck(CommandSender sender, String[] args) {
        ConsistentXPConfig config = experienceManager.getConfig();

        if (args.length == 1) {
            if (sender instanceof Player player) {
                if (!sender.hasPermission("consistentxp.check")) {
                    sender.sendMessage(config.getNoPermissionMessage());
                    return;
                }
                sendPlayerXpInfo(sender, player, true);
                return;
            }
            sender.sendMessage(config.parseMessage("<red>Usage: /cxp check <player>"));
            return;
        }

        if (!sender.hasPermission("consistentxp.check.others") && !sender.hasPermission("consistentxp.admin")) {
            sender.sendMessage(config.getNoPermissionMessage());
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(config.getPlayerNotFoundMessage(args[1]));
            return;
        }

        boolean isSelf = (sender instanceof Player player) && player.getUniqueId().equals(target.getUniqueId());
        sendPlayerXpInfo(sender, target, isSelf);
    }

    private void handleAdd(CommandSender sender, String[] args) {
        ConsistentXPConfig config = experienceManager.getConfig();
        if (!sender.hasPermission("consistentxp.admin")) {
            sender.sendMessage(config.getNoPermissionMessage());
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(config.parseMessage("<red>Usage: /cxp add <player> <amount> [points|levels]"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(config.getPlayerNotFoundMessage(args[1]));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount <= 0) {
                sender.sendMessage(config.getInvalidAmountMessage());
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(config.getInvalidAmountMessage());
            return;
        }

        String type = (args.length >= 4) ? args[3].toLowerCase(Locale.ROOT) : "points";
        if (type.startsWith("level")) {
            experienceManager.addLevels(target, amount);
            sender.sendMessage(config.parseMessage(config.getRawXpAdded(),
                    Placeholder.unparsed("amount", String.valueOf(amount)),
                    Placeholder.unparsed("type", "level(s)"),
                    Placeholder.unparsed("player", target.getName())));
        } else {
            experienceManager.addExperience(target, amount);
            sender.sendMessage(config.parseMessage(config.getRawXpAdded(),
                    Placeholder.unparsed("amount", String.valueOf(amount)),
                    Placeholder.unparsed("type", "experience point(s)"),
                    Placeholder.unparsed("player", target.getName())));
        }
    }

    private void handleSet(CommandSender sender, String[] args) {
        ConsistentXPConfig config = experienceManager.getConfig();
        if (!sender.hasPermission("consistentxp.admin")) {
            sender.sendMessage(config.getNoPermissionMessage());
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(config.parseMessage("<red>Usage: /cxp set <player> <amount> [points|levels]"));
            return;
        }

        Player target = Bukkit.getPlayerExact(args[1]);
        if (target == null) {
            sender.sendMessage(config.getPlayerNotFoundMessage(args[1]));
            return;
        }

        int amount;
        try {
            amount = Integer.parseInt(args[2]);
            if (amount < 0) {
                sender.sendMessage(config.getInvalidAmountMessage());
                return;
            }
        } catch (NumberFormatException e) {
            sender.sendMessage(config.getInvalidAmountMessage());
            return;
        }

        String type = (args.length >= 4) ? args[3].toLowerCase(Locale.ROOT) : "points";
        if (type.startsWith("level")) {
            experienceManager.setLevel(target, amount);
            sender.sendMessage(config.parseMessage(config.getRawXpSet(),
                    Placeholder.unparsed("amount", String.valueOf(amount)),
                    Placeholder.unparsed("type", "level"),
                    Placeholder.unparsed("player", target.getName())));
        } else {
            experienceManager.setExperience(target, amount);
            sender.sendMessage(config.parseMessage(config.getRawXpSet(),
                    Placeholder.unparsed("amount", String.valueOf(amount)),
                    Placeholder.unparsed("type", "total experience points"),
                    Placeholder.unparsed("player", target.getName())));
        }
    }

    private void handleReload(CommandSender sender) {
        ConsistentXPConfig config = experienceManager.getConfig();
        if (!sender.hasPermission("consistentxp.admin")) {
            sender.sendMessage(config.getNoPermissionMessage());
            return;
        }

        plugin.reloadPluginConfig();
        sender.sendMessage(config.getReloadedMessage());
    }

    private void sendPlayerXpInfo(CommandSender sender, Player target, boolean isSelf) {
        ConsistentXPConfig config = experienceManager.getConfig();
        ExperienceSnapshot snapshot = experienceManager.getSnapshot(target);

        String template = isSelf ? config.getRawXpCheckSelf() : config.getRawXpCheckOther();
        Component msg = config.parseMessage(template,
                Placeholder.unparsed("player", target.getName()),
                Placeholder.unparsed("level", String.valueOf(snapshot.level())),
                Placeholder.unparsed("points", String.valueOf(snapshot.pointsInCurrentLevel())),
                Placeholder.unparsed("max_points", String.valueOf(snapshot.xpPerLevel())),
                Placeholder.unparsed("percent", String.format(Locale.ROOT, "%.1f", snapshot.progressPercentage())),
                Placeholder.unparsed("needed", String.valueOf(snapshot.pointsNeededForNextLevel())),
                Placeholder.unparsed("total", String.valueOf(snapshot.totalExperience())));
        sender.sendMessage(msg);
    }

    private void sendHelp(CommandSender sender) {
        ConsistentXPConfig config = experienceManager.getConfig();
        sender.sendMessage(config.parseMessage("<aqua><b>ConsistentXP Commands:</b></aqua>"));
        sender.sendMessage(config.parseMessage("<dark_aqua>/cxp check [player]<gray> - Check consistent XP statistics"));
        if (sender.hasPermission("consistentxp.admin")) {
            sender.sendMessage(config.parseMessage("<dark_aqua>/cxp add <player> <amount> [points|levels]<gray> - Add XP or levels"));
            sender.sendMessage(config.parseMessage("<dark_aqua>/cxp set <player> <amount> [points|levels]<gray> - Set XP or level"));
            sender.sendMessage(config.parseMessage("<dark_aqua>/cxp reload<gray> - Reload plugin configuration"));
        }
    }

    @Override
    public Collection<String> suggest(CommandSourceStack stack, String[] args) {
        CommandSender sender = stack.getSender();
        if (args.length <= 1) {
            String current = (args.length == 1) ? args[0].toLowerCase(Locale.ROOT) : "";
            return ROOT_SUBCOMMANDS.stream()
                    .filter(cmd -> cmd.equals("check") || sender.hasPermission("consistentxp.admin"))
                    .filter(cmd -> cmd.startsWith(current))
                    .toList();
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 2 && (sub.equals("check") || sub.equals("add") || sub.equals("set"))) {
            String current = args[1].toLowerCase(Locale.ROOT);
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase(Locale.ROOT).startsWith(current))
                    .toList();
        }

        if (args.length == 3 && (sub.equals("add") || sub.equals("set"))) {
            String current = args[2];
            return SUGGESTED_AMOUNTS.stream().filter(amt -> amt.startsWith(current)).toList();
        }

        if (args.length == 4 && (sub.equals("add") || sub.equals("set"))) {
            String current = args[3].toLowerCase(Locale.ROOT);
            return SUGGESTED_TYPES.stream().filter(t -> t.startsWith(current)).toList();
        }

        return List.of();
    }
}
