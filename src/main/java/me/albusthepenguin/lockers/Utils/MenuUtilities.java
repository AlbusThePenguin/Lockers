package me.albusthepenguin.lockers.Utils;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.entity.Player;

@Getter
@Setter
public class MenuUtilities {

    private Player player;

    public MenuUtilities(Player player) {
        this.player = player;
    }

}