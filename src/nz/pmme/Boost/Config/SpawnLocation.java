package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.Location;
import org.bukkit.World;

public class SpawnLocation
{
    private Main plugin;
    private Location spawn;
    private String gameSpawnConfig;

    public SpawnLocation( Main plugin, String gameName, String spawnConfig )
    {
        this.plugin = plugin;
        gameSpawnConfig = "games." + gameName + "." + spawnConfig + ".";
        World spawnWorld = plugin.getServer().getWorld( plugin.getConfig().getString( gameSpawnConfig + "world", "world" ) );
        if( spawnWorld != null ) {
            int spawnX = plugin.getConfig().getInt( gameSpawnConfig + "x", 0 );
            int spawnY = plugin.getConfig().getInt( gameSpawnConfig + "y", 64 );
            int spawnZ = plugin.getConfig().getInt( gameSpawnConfig + "z", 0 );
            int spawnYaw = plugin.getConfig().getInt( gameSpawnConfig + "yaw", 0 );
            spawn = new Location( spawnWorld, spawnX, spawnY, spawnZ, (float)spawnYaw, 0.0F );
        }
    }

    public Location getSpawn() {
        return spawn;
    }

    public void setSpawn( Location spawn )
    {
        this.spawn = spawn;
        if( spawn.getWorld() != null ) plugin.getConfig().set( gameSpawnConfig + "world", spawn.getWorld().getName() );
        plugin.getConfig().set( gameSpawnConfig + "x", spawn.getBlockX() );
        plugin.getConfig().set( gameSpawnConfig + "y", spawn.getBlockY() );
        plugin.getConfig().set( gameSpawnConfig + "z", spawn.getBlockZ() );
        plugin.getConfig().set( gameSpawnConfig + "yaw", (int)spawn.getYaw() );
        plugin.saveConfig();
    }
}
