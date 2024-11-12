package me.albusthepenguin.lockers.Commands;

import me.albusthepenguin.lockers.Locker.LockerMenu;
import me.albusthepenguin.lockers.Lockers;
import me.albusthepenguin.lockers.Utils.MenuUtilities;
import me.albusthepenguin.lockers.Utils.MinecraftSubCommand;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.Map;

public class OpenPlayerLockerCommand extends MinecraftSubCommand {
    /**
     * Constructor to initialize the subcommand name and permission.
     *
     * @param lockers    the plugin.
     * @param name       The name of the subcommand.
     * @param permission The permission required to execute the subcommand.
     * @param syntax gets the syntax.
     */
    protected OpenPlayerLockerCommand(Lockers lockers, String name, String permission, String syntax) {
        super(lockers, name, permission, syntax);
    }

    @Override
    public void perform(Player player, String[] args) {

        if(args.length != 2) {
            player.sendMessage(lockers.getConfiguration().color(this.getSyntax()));
            return;
        }

        OfflinePlayer offlinePlayer = this.getOfflinePlayerByName(args[1]);
        if(offlinePlayer == null) {
            player.sendMessage(this.lockers.getConfiguration().getReplace("error_player_exists", true, Map.of("%player%", args[1])));
            return;
        }

        MenuUtilities menuUtilities = new MenuUtilities(player);
        LockerMenu lockerMenu = new LockerMenu(lockers, menuUtilities, offlinePlayer.getUniqueId());
        lockerMenu.open();
    }

    @SuppressWarnings("all")
    private OfflinePlayer getOfflinePlayerByName(String playerName) {
        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(playerName);
        if(offlinePlayer.hasPlayedBefore()) return offlinePlayer;
        return null;
    }

    @Override
    public void perform(ConsoleCommandSender console, String[] args) {
        console.sendMessage("Can't use this command in console.");
    }

    @Override
    public List<String> getSubcommandArguments(Player player, String[] args) {
        return List.of();
    }
}
