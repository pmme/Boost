package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.Location;
import org.bukkit.World;

public class SpawnLocation
{
    private Main plugin;
    private Location spawn;
    private String configPath;

    public SpawnLocation( Main plugin, String spawnConfigPath )
    {
        this.plugin = plugin;
        this.configPath = spawnConfigPath + ".";
        World spawnWorld = plugin.getServer().getWorld( plugin.getConfig().getString( configPath + "world", "world" ) );
        if( spawnWorld != null ) {
            int spawnX = plugin.getConfig().getInt( configPath + "x", 0 );
            int spawnY = plugin.getConfig().getInt( configPath + "y", 64 );
            int spawnZ = plugin.getConfig().getInt( configPath + "z", 0 );
            int spawnYaw = plugin.getConfig().getInt( configPath + "yaw", 0 );
            spawn = new Location( spawnWorld, spawnX + 0.5D, spawnY, spawnZ + 0.5D, (float)spawnYaw, 0.0F );
        }
    }

    public void setConfig()
    {
        if( spawn.getWorld() != null ) plugin.getConfig().set( configPath + "world", spawn.getWorld().getName() );
        plugin.getConfig().set( configPath + "x", spawn.getBlockX() );
        plugin.getConfig().set( configPath + "y", spawn.getBlockY() );
        plugin.getConfig().set( configPath + "z", spawn.getBlockZ() );
        plugin.getConfig().set( configPath + "yaw", (int)spawn.getYaw() );
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn( Location spawn )
    {
        this.spawn = spawn;
        this.setConfig();
        plugin.saveConfig();
    }
}
