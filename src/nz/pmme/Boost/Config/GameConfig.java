package nz.pmme.Boost.Config;

import nz.pmme.Boost.Main;
import org.bukkit.Location;

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
    }

    public void setConfig()
    {
        plugin.getConfig().set( configPath + "name", this.name );
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

    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getCountdown() {
        return countdown;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public boolean isAutoQueue() {
        return autoQueue;
    }

    public int getGroundLevel()
    {
        return groundLevel;
    }

    public void setGroundLevel( int newGround )
    {
        groundLevel = newGround;
        plugin.getConfig().set( configPath + "ground", groundLevel );
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

    public boolean isProperlyConfigured(){
        if( this.getLobbySpawn() == null ) return false;
        if( this.getLossSpawn() == null ) return false;
        if( this.getStartSpawn() == null ) return false;
        return true;
    }
}
