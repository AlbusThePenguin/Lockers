package me.albusthepenguin.lockers.Commands;

import me.albusthepenguin.lockers.Lockers;
import me.albusthepenguin.lockers.Utils.MinecraftSubCommand;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class LockerReloadCommand extends MinecraftSubCommand {

    /**
     * Constructor to initialize the subcommand name and permission.
     *
     * @param lockers jeez
     * @param name       The name of the subcommand.
     * @param permission The permission required to execute the subcommand.
     * @param syntax x
     */
    protected LockerReloadCommand(Lockers lockers, String name, String permission, String syntax) {
        super(lockers, name, permission, syntax);
    }

    @Override
    public void perform(Player player, String[] args) {
        if(args.length != 1) {
            player.sendMessage(lockers.getConfiguration().color(this.getSyntax()));
            return;
        }

        this.lockers.getConfiguration().loadYamlConfiguration();
        this.lockers.getConfiguration().save();
        player.sendMessage(lockers.getConfiguration().get("success_command_reload", true));
    }

    @Override
    public void perform(ConsoleCommandSender console, String[] args) {
        if(args.length != 1) {
            console.sendMessage("Invalid syntax: " + getSyntax());
            return;
        }

        this.lockers.getConfiguration().loadYamlConfiguration();
        this.lockers.getConfiguration().save();
        console.sendMessage("Reloaded configurations.");
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return List.of();
    }
}
