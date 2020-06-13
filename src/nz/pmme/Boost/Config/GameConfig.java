package nz.pmme.Boost.Config;

import nz.pmme.Boost.Exceptions.GameDisplayNameMustMatchConfigurationException;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;

import java.util.Collections;
import java.util.List;

public class GameConfig
{
    private Main plugin;
    private String name;
    private String displayName;

    private String configPath;

    private int groundLevel;
    private int targetDist;
    private int countdown;
    private int minPlayers;
    private int maxPlayers;
    private boolean autoQueue;

    private SpawnLocation lobbySpawn;
    private SpawnLocation startSpawn;
    private SpawnLocation lossSpawn;

    private int countdownAnnounceTime;
    private List<String> winCommands;

    public GameConfig( Main plugin, String name )
    {
        this.plugin = plugin;
        this.name = name;

        this.configPath = "games." + this.name + ".";

        this.displayName = plugin.getConfig().getString( configPath + "name", this.name );
        this.groundLevel = plugin.getConfig().getInt( configPath + "ground", 64 );
        this.targetDist = plugin.getConfig().getInt( configPath + "target_dist", 150 );

        this.countdown = plugin.getConfig().getInt( configPath + "countdown", 30 );
        this.minPlayers = plugin.getConfig().getInt( configPath + "min_players", 2 );
        this.maxPlayers = plugin.getConfig().getInt( configPath + "max_players", 0 );
        this.autoQueue = plugin.getConfig().getBoolean( configPath + "auto_queue", false );

        this.lobbySpawn = new SpawnLocation( plugin, configPath + "game_lobby" );
        this.startSpawn = new SpawnLocation( plugin, configPath + "game_start" );
        this.lossSpawn = new SpawnLocation( plugin, configPath + "game_loss" );

        this.countdownAnnounceTime = plugin.getConfig().getInt( configPath + "countdown_announce_time", 10 );
        this.winCommands = plugin.getConfig().getStringList( configPath + "win_commands" );

        Location spawn = this.startSpawn.getConfiguredSpawn();
        if( spawn != null && spawn.getBlockY() <= this.groundLevel ) {
            plugin.getLogger().warning( "Start spawn Y " + spawn.getBlockY() + " should be higher than ground " + this.groundLevel + " for game " + this.name );
        }
    }

    public void setConfig()
    {
        plugin.getConfig().set( configPath + "name", this.displayName );
        plugin.getConfig().set( configPath + "ground", this.groundLevel );
        plugin.getConfig().set( configPath + "target_dist", this.targetDist );

        plugin.getConfig().set( configPath + "countdown", this.countdown );
        plugin.getConfig().set( configPath + "min_players", this.minPlayers );
        plugin.getConfig().set( configPath + "max_players", this.maxPlayers );
        plugin.getConfig().set( configPath + "auto_queue", this.autoQueue );

        this.lobbySpawn.setConfig();
        this.startSpawn.setConfig();
        this.lossSpawn.setConfig();

        plugin.getConfig().set( configPath + "countdown_announce_time", this.countdownAnnounceTime );
        plugin.getConfig().set( configPath + "win_commands", this.winCommands != null ? this.winCommands : Collections.emptyList() );
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return ChatColor.translateAlternateColorCodes( '&', displayName );
    }

    public void setDisplayName( String newDisplayName ) throws GameDisplayNameMustMatchConfigurationException
    {
        String newDisplayNamePlain = ChatColor.stripColor( ChatColor.translateAlternateColorCodes( '&', newDisplayName ) );
        if( !newDisplayNamePlain.equalsIgnoreCase( this.name ) ) {
            throw new GameDisplayNameMustMatchConfigurationException();
        }
        displayName = newDisplayName;
        this.setConfig();
        plugin.saveConfig();
    }

    public int getCountdown() {
        return countdown;
    }

    public void setCountdown( int newCountdown )
    {
        countdown = newCountdown;
        this.setConfig();
        plugin.saveConfig();
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public void setMinPlayers( int newMinPlayers )
    {
        minPlayers = newMinPlayers;
        this.setConfig();
        plugin.saveConfig();
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void setMaxPlayers( int newMaxPlayers )
    {
        maxPlayers = newMaxPlayers;
        this.setConfig();
        plugin.saveConfig();
    }

    public boolean isAutoQueue() {
        return autoQueue;
    }

    public void setAutoQueue( boolean newAutoQueue )
    {
        autoQueue = newAutoQueue;
        this.setConfig();
        plugin.saveConfig();
    }

    public int getGroundLevel()
    {
        return groundLevel;
    }

    public void setGroundLevel( int newGround )
    {
        groundLevel = newGround;
        this.setConfig();
        plugin.saveConfig();
    }

    public int getTargetDist()
    {
        return targetDist;
    }

    public Location getLobbySpawn() {
        return lobbySpawn.getSpawn();
    }

    public void setLobbySpawn( Location spawn )
    {
        lobbySpawn.setSpawn( spawn );
    }

    public Location getStartSpawn() {
        return startSpawn.getSpawn();
    }

    public void setStartSpawn( Location spawn )
    {
        startSpawn.setSpawn( spawn );
    }

    public Location getLossSpawn() {
        return lossSpawn.getSpawn();
    }

    public void setLossSpawn( Location spawn )
    {
        lossSpawn.setSpawn( spawn );
    }

    public void setSpawnSpread( int spread )
    {
        startSpawn.setSpread( spread );
    }

    public int getCountdownAnnounceTime() {
        return countdownAnnounceTime;
    }

    public void setCountdownAnnounceTime( int newCountdownAnnounceTime )
    {
        countdownAnnounceTime = newCountdownAnnounceTime;
        this.setConfig();
        plugin.saveConfig();
    }

    public List<String> getWinCommands() {
        return winCommands;
    }

    public void addWinCommand( String winCommand )
    {
        if( winCommand != null && !winCommand.isEmpty() ) {
            winCommands.add( winCommand );
            this.setConfig();
            plugin.saveConfig();
        }
    }

    public void removeWinCommand( int index ) throws IndexOutOfBoundsException
    {
        winCommands.remove( index );
        this.setConfig();
        plugin.saveConfig();
    }

    public boolean isProperlyConfigured(){
        if( this.getLobbySpawn() == null ) return false;
        if( this.getLossSpawn() == null ) return false;
        if( this.getStartSpawn() == null ) return false;
        return true;
    }

    public void displayConfig( CommandSender sender )
    {
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5Boost game config:" ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Config name: &3" + this.name ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Display name: &3" + this.displayName ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ground level: &3" + this.groundLevel ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Target distance: &3" + this.targetDist ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Start countdown: &3" + this.countdown ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Countdown announcement period: &3" + this.countdownAnnounceTime ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Minimum players to start: &3" + this.minPlayers ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Maximum players: &3" + this.maxPlayers ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Auto queue: &3" + ( this.autoQueue ? "&aon" : "&coff" ) ) );
        if( this.lobbySpawn.getConfiguredSpawn() == null ) {
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Lobby spawn: &cNot configured" ) );
        } else {
            Location spawn = this.lobbySpawn.getConfiguredSpawn();
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Lobby spawn: &3" + spawn.getWorld().getName() + " " + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ() ) );
        }
        if( this.startSpawn.getConfiguredSpawn() == null ) {
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Start spawn: &cNot configured" ) );
        } else {
            Location spawn = this.startSpawn.getConfiguredSpawn();
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Start spawn: &3" + spawn.getWorld().getName() + " " + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ() ) );
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Start spawn spread: &3" + this.startSpawn.getSpread() ) );
            if( spawn.getBlockY() <= this.groundLevel ) {
                sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&c Start spawn Y " + spawn.getBlockY() + " should be higher than ground " + this.groundLevel ) );
            }
        }
        if( this.lossSpawn.getConfiguredSpawn() == null ) {
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Loss spawn: &cNot configured" ) );
        } else {
            Location spawn = this.lossSpawn.getConfiguredSpawn();
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Loss spawn: &3" + spawn.getWorld().getName() + " " + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ() ) );
        }
        if( this.winCommands == null || this.winCommands.isEmpty() ) {
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Win commands: &3None configured" ) );
        } else {
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Win commands:" ) );
            for( String winCommand : this.winCommands ) {
                sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|   &3- '" ) + winCommand + ChatColor.translateAlternateColorCodes( '&',"&3'." ) );
            }
        }
    }
}
