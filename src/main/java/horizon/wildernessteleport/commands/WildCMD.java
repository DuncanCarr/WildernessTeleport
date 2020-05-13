package horizon.wildernessteleport.commands;

import horizon.wildernessteleport.WildernessTeleport;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public class WildCMD implements CommandExecutor {

    private final WildernessTeleport plugin = JavaPlugin.getPlugin(WildernessTeleport.class);
    private HashMap<UUID, Location> teleportLocations = new HashMap<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.colorize("&c(!) You cannot use this command if you are not a player!"));
            return true;
        }

        Player p = (Player) sender;

        if (args.length == 1) {
            if (args[0].equalsIgnoreCase("reload")) {
                if (p.hasPermission("wilderness.reload")) {
                    plugin.saveConfig();
                    plugin.reloadConfig();
                    p.sendMessage(plugin.colorize("&a(!) Successfully reloaded configuration file."));
                } else {
                    p.sendMessage(plugin.colorize("&cYou do not have permission to use this command!"));
                }
            }
        } else if (args.length == 0) {
            if (p.hasPermission("wilderness.use." + p.getWorld().getName())) {

                // Cooldown logic
                final int cooldownTime = plugin.getConfig().getInt("teleport-cooldown");
                if (plugin.getCooldowns().containsKey(p.getUniqueId())) {
                    final long secondsLeft = plugin.getCooldowns().get(p.getUniqueId()) / 1000L + cooldownTime - System.currentTimeMillis() / 1000L;
                    if (secondsLeft > 0L) {
                        sender.sendMessage(plugin.colorize("&c(!) You cannot use this command for &c&n" + secondsLeft + "&c second(s)!"));
                        return true;
                    }
                }
                plugin.getCooldowns().put(p.getUniqueId(), plugin.getConfig().getInt("teleport-cooldown"));
                p.sendMessage(plugin.colorize("&e(!) Generating safe teleport location... &c[DO NOT MOVE]"));
                plugin.getPastLocation().put(p.getUniqueId(), p.getLocation());
                generateLocation(p);
                if (!compareLocations(p.getLocation(), plugin.getPastLocation().get(p.getUniqueId()))) {
                    p.sendMessage(plugin.colorize("&cTeleport failed! You moved!"));
                    return true;
                }
                p.teleport(teleportLocations.get(p.getUniqueId()));
                p.sendMessage(plugin.colorize("&a(!) You have been teleported to &7(" + (int) teleportLocations.get(p.getUniqueId()).getX() + ", " + (int) teleportLocations.get(p.getUniqueId()).getY() + ", " + (int) teleportLocations.get(p.getUniqueId()).getZ() + ")&a!"));
                teleportLocations.remove(p.getUniqueId());

            } else {
                p.sendMessage(plugin.colorize("&c(!) You do not have permission to use this command in this world!"));
            }
        }
        return true;
    }

    public void generateLocation(Player player) {
        Location location = new Location(player.getWorld(), 0, 0, 0);
        int range = plugin.getConfig().getInt("teleport-range");
        location.setX(player.getLocation().getX() + Math.random() * range * 2.0 - range);
        location.setZ(player.getLocation().getZ() + Math.random() * range * 2.0 - range);
        for (int i = 255; i >= 0; i--) {
            Block block = location.getWorld().getBlockAt(location.getBlockX(), i, location.getBlockZ());
            if (block.getType().isSolid()) {
                location.setY(i+1);
                break;
            }
        }
        while (!teleportLocations.containsKey(player.getUniqueId())) {
            if (checkLocation(location)) {
                teleportLocations.put(player.getUniqueId(), location);
            } else {
                location.setX(player.getLocation().getX() + Math.random() * range * 2.0 - range);
                location.setZ(player.getLocation().getZ() + Math.random() * range * 2.0 - range);
                for (int i = 255; i >= 0; i--) {
                    Block block = location.getWorld().getBlockAt(location.getBlockX(), i, location.getBlockZ());
                    if (block.getType().isSolid()) {
                        location.setY(i+1);
                        break;
                    }
                }
            }
        }
    }

    private boolean checkLocation(final Location location) {
        boolean condition;
        World world = location.getWorld();
        Block blockUnder = world.getBlockAt(new Location(world, location.getBlockX(), (location.getBlockY() - 1), location.getBlockZ()));
        Block blockAt = world.getBlockAt(location.getBlockX(), location.getBlockY(), location.getBlockZ());
        if (blockUnder.getBlockData().getMaterial() != Material.WATER && blockUnder.getBlockData().getMaterial() != Material.LAVA) {
            if (blockAt.isLiquid()) {
                condition = false;
            } else {
                if (blockAt.getType() == Material.TALL_SEAGRASS || blockAt.getType() == Material.SEAGRASS) {
                    condition = false;
                } else {
                    condition = blockAt.getType() != Material.KELP_PLANT && blockAt.getType() != Material.SEA_PICKLE;
                }
            }
        } else {
            condition = false;
        }
        return condition;
    }

    private boolean compareLocations(Location loc1, Location loc2) {
        if (loc1.getWorld().getName().equals(loc2.getWorld().getName())) {
            if (loc1.getX() == loc2.getX()) {
                if (loc1.getY() == loc2.getY()) {
                    return loc1.getZ() == loc2.getZ();
                } else {
                    return false;
                }
            } else {
                return false;
            }
        } else {
            return false;
        }
    }
}