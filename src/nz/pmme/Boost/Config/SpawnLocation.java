package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.util.Vector;

public class SpawnLocation
{
    private Main plugin;
    private String name;
    private Location spawn;
    private int spread;
    private String configPath;

    public SpawnLocation( Main plugin, String spawnConfigPath )
    {
        this.plugin = plugin;
        this.configPath = spawnConfigPath + ".";
        this.name = spawnConfigPath.substring( spawnConfigPath.lastIndexOf( '.' ) + 1 );
        String spawnWorldName = plugin.getConfig().getString( configPath + "world" );
        if( spawnWorldName != null ) {
            World spawnWorld = plugin.getServer().getWorld( spawnWorldName );
            if( spawnWorld != null ) {
                int spawnX = plugin.getConfig().getInt( configPath + "x", 0 );
                int spawnY = plugin.getConfig().getInt( configPath + "y", 64 );
                int spawnZ = plugin.getConfig().getInt( configPath + "z", 0 );
                int spawnYaw = plugin.getConfig().getInt( configPath + "yaw", 0 );
                spawn = new Location( spawnWorld, spawnX + 0.5D, spawnY, spawnZ + 0.5D, (float)spawnYaw, 0.0F );
                spread = plugin.getConfig().getInt( configPath + "spread", 0 );
            } else {
                plugin.getLogger().severe( "World '" + spawnWorldName + "' not found for configuration item '" + configPath + "world'." );
                StringBuilder availableWorlds = new StringBuilder();
                for( World world : plugin.getServer().getWorlds() ) {
                    availableWorlds.append( world.getName() ).append( ", " );
                }
                availableWorlds.delete( availableWorlds.length()-2, availableWorlds.length()-1 );
                plugin.getLogger().severe( "Available worlds are: " + availableWorlds.toString() );
            }
        }
    }

    public void setConfig()
    {
        if( spawn == null ) return;     // New config will have null spawn initially.
        if( spawn.getWorld() != null ) plugin.getConfig().set( configPath + "world", spawn.getWorld().getName() );
        plugin.getConfig().set( configPath + "x", spawn.getBlockX() );
        plugin.getConfig().set( configPath + "y", spawn.getBlockY() );
        plugin.getConfig().set( configPath + "z", spawn.getBlockZ() );
        plugin.getConfig().set( configPath + "yaw", (int)spawn.getYaw() );
        plugin.getConfig().set( configPath + "spread", spread );
    }

    public void removeConfig()
    {
        if( spawn == null ) return;
        plugin.getConfig().set( this.configPath.substring( 0, this.configPath.length()-1 ), null );
        plugin.saveConfig();
    }

    public String getStartSpawnNode() {
        return name;
    }

    public Location getSpawn() {
        if( spread > 0 && spawn != null ) {
            Location location = new Location( spawn.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), (float)( Math.random() * 360.0 ), 0 );
            double angle = Math.random() * 2 * Math.PI;
            double length = Math.random() * spread;
            Vector spreadVector = new Vector( Math.cos(angle) * length, 0, -Math.sin(angle) * length );
            location.add( spreadVector );
            return location;
        } else {
            return spawn;
        }
    }

    public Location getConfiguredSpawn()
    {
        return spawn;
    }

    public int getSpread()
    {
        return spread;
    }

    public void setSpawn( Location spawn )
    {
        this.spawn = spawn;
        this.setConfig();
        plugin.saveConfig();
    }

    public void setSpread( int spread )
    {
        this.spread = spread;
        this.setConfig();
        plugin.saveConfig();
    }
}
