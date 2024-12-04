/*
 * This file is part of Lockers.
 *
 * Lockers is a free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lockers are distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Lockers. If not, see <http://www.gnu.org/licenses/>.
 */
package me.albusthepenguin.lockers.Commands;

import me.albusthepenguin.lockers.Lockers;
import me.albusthepenguin.lockers.Utils.CMD.MinecraftSubCommand;
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
