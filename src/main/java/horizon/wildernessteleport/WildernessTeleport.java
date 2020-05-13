package horizon.wildernessteleport;

import horizon.wildernessteleport.commands.WildCMD;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class WildernessTeleport extends JavaPlugin {

    private HashMap<UUID, Integer> cooldowns;
    private HashMap<UUID, Location> pastLocation;

    public void onEnable() {
        pastLocation = new HashMap<>();
        cooldowns = new HashMap<>();
        getCommand("wild").setExecutor(new WildCMD());
    }

    public void onDisable() {
    }

    public String colorize(final String input) {
        return ChatColor.translateAlternateColorCodes('&', input);
    }
    public HashMap<UUID, Integer> getCooldowns() {
        return cooldowns;
    }
    public HashMap<UUID, Location> getPastLocation() {
        return pastLocation;
    }
}
