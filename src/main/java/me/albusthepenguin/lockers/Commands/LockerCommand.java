package me.albusthepenguin.lockers.Commands;

import me.albusthepenguin.lockers.Locker.LockerMenu;
import me.albusthepenguin.lockers.Lockers;
import me.albusthepenguin.lockers.Utils.MenuUtilities;
import me.albusthepenguin.lockers.Utils.MinecraftCommand;
import me.albusthepenguin.lockers.Utils.MinecraftSubCommand;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

public class LockerCommand extends MinecraftCommand {

    private final Lockers lockers;

    private final ArrayList<MinecraftSubCommand> subcommands = new ArrayList<>();


    /**
     * Constructor for creating a new command.
     *
     * @param lockers            The plugin ID.
     * @param name               The name of the command.
     * @param permission         The permission for the index command. For sub commands a player will need both this + the sub command permission.
     * @param description        The description of the command.
     * @param usageMessage       The usage message for the command.
     * @param aliases            A list of aliases for the command.
     * @param subCommandsSection The sub commands id.
     */
    public LockerCommand(@Nonnull Lockers lockers, @Nonnull String name, @Nonnull String permission, @Nonnull String description, @Nonnull String usageMessage, @Nonnull List<String> aliases, ConfigurationSection subCommandsSection) {
        super(lockers, name, permission, description, usageMessage, aliases);

        this.lockers = lockers;

        ConfigurationSection openLocker = subCommandsSection.getConfigurationSection("open_locker");
        if(openLocker == null) {
            throw new IllegalArgumentException("The command structure has broken. Can't find 'Command.subcommands.open_locker' section in config.yml. Please correct this.");
        }

        String openLabel = openLocker.getString("label");
        String openPermission = openLocker.getString("permission");
        String openUsage = openLocker.getString("usage");

        this.subcommands.add(new OpenPlayerLockerCommand(lockers, openLabel, openPermission, openUsage));

        ConfigurationSection reloadLocker = subCommandsSection.getConfigurationSection("reload_config");
        if(reloadLocker == null) {
            throw new IllegalArgumentException("The command structure has broken. Can't find 'Command.subcommands.reload_config' section in config.yml. Please correct this.");
        }

        String reloadLabel = reloadLocker.getString("label");
        String reloadPermission = reloadLocker.getString("permission");
        String reloadUsage = reloadLocker.getString("usage");

        this.subcommands.add(new LockerReloadCommand(lockers, reloadLabel, reloadPermission, reloadUsage));

        this.register(lockers);
    }


    @Override
    public boolean onCommand(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String label, @Nonnull String[] args) {
        if (args.length > 0 && !subcommands.isEmpty()) {
            for (MinecraftSubCommand subCommand : subcommands) {
                if (args[0].equalsIgnoreCase(subCommand.name)) {
                    if (sender instanceof Player player) {
                        if (player.hasPermission(subCommand.permission)) {
                            subCommand.perform(player, args);
                        } else {
                            player.sendMessage(this.lockers.getConfiguration().get("error_command_permission", true));
                        }
                        return true;
                    } else if (sender instanceof ConsoleCommandSender consoleCommandSender) {
                        subCommand.perform(consoleCommandSender, args);
                        return true;
                    }
                }
            }
            return false;
        } else {
            if(sender instanceof Player player) {
                MenuUtilities menuUtilities = new MenuUtilities(player);
                LockerMenu lockerMenu = new LockerMenu(this.lockers, menuUtilities, player.getUniqueId());
                lockerMenu.open();
                return true;
            }
        }
        return true;
    }

    @Override
    public List<String> onTabComplete(@Nonnull CommandSender sender, @Nonnull Command command, @Nonnull String alias, @Nonnull String[] args) {
        if(sender instanceof Player player) {
            if (!player.hasPermission(Objects.requireNonNull(getPermission()))) {
                return Collections.emptyList();
            }

            List<String> arguments = new ArrayList<>();

            if (args.length == 0) {
                return arguments;
            }

            for (MinecraftSubCommand subCommand : subcommands) {
                if (subCommand == null) {
                    throw new IllegalArgumentException("Failed to execute '" + args[0] + "' because sub command is null.");
                }

                if (!player.hasPermission(subCommand.permission)) {
                    continue;
                }

                if (args.length == 1) {
                    arguments.add(subCommand.name);
                } else if (args[0].equalsIgnoreCase(subCommand.permission)) {
                    List<String> subArgs = subCommand.getSubcommandArguments(player, args);
                    arguments = subArgs != null ? subArgs : Collections.emptyList();
                    break;
                }
            }

            return arguments;
        }
        return Collections.emptyList();
    }
}
