/*
 * This file is part of Lockers.
 *
 * Lockers is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Lockers is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with Lockers. If not, see <http://www.gnu.org/licenses/>.
 */
package me.albusthepenguin.lockers.Utils;

import me.albusthepenguin.lockers.Lockers;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Abstract base class representing a subcommand for a Bukkit plugin command.
 * <p>
 * Subclasses of this class should define the specific behavior, permissions,
 * and argument handling for each subcommand.
 */
@SuppressWarnings("all")
public abstract class MinecraftSubCommand {

    public final Lockers lockers;

    public final String name;        // Name of the subcommand
    public final String permission;   // Permission required to execute the subcommand
    private final String syntax;

    /**
     * Constructor to initialize the subcommand name and permission.
     *
     * @param name       The name of the subcommand.
     * @param permission The permission required to execute the subcommand.
     */
    protected MinecraftSubCommand(Lockers lockers, String name, String permission, String syntax) {
        this.lockers = lockers;
        this.name = name;
        this.permission = permission;
        this.syntax = syntax;
    }

    /**
     * Gets the name of the subcommand.
     * <p>
     * This is the identifier used to trigger the subcommand (e.g., "/command <subcommand>").
     *
     * @return The name of the subcommand.
     */
    protected String getName() {
        return name;
    }

    /**
     * Gets the permission required to execute the subcommand.
     * <p>
     * This permission string is used to check if the player has the necessary rights
     * to use the subcommand.
     *
     * @return The permission string required to execute the subcommand, or null if no permission is required.
     */
    protected String getPermission() {
        return permission;
    }

    /**
     * Gets the syntax or usage message for the subcommand.
     * <p>
     * This syntax string typically explains how to use the subcommand and what arguments it expects.
     *
     * @return The syntax message for the subcommand.
     */
    protected String getSyntax() { return syntax; }

    /**
     * Executes the subcommand when invoked by a player.
     * <p>
     * This method contains the logic to be executed when the subcommand is run by a player.
     *
     * @param player The player who executed the subcommand.
     * @param args   The arguments passed to the subcommand.
     */
    public abstract void perform(Player player, String[] args);

    /**
     * Executes the subcommand when invoked by the console.
     * <p>
     * This method contains the logic to be executed when the subcommand is run by the console.
     *
     * @param console The console command sender who executed the subcommand.
     * @param args    The arguments passed to the subcommand.
     */
    public abstract void perform(ConsoleCommandSender console, String[] args);

    /**
     * Provides tab completion suggestions for the subcommand's arguments.
     * <p>
     * This method returns a list of suggested completions based on the current input provided
     * by the player.
     *
     * @param player The player who is typing the command.
     * @param args   The arguments that have been typed so far.
     * @return A list of suggested completions for the subcommand's arguments.
     */
    public abstract List<String> getSubcommandArguments(Player player, String[] args);
}