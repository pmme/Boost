package nz.pmme.Boost.Config;

import nz.pmme.Boost.Enums.GameType;
import nz.pmme.Boost.Exceptions.GameDisplayNameMustMatchConfigurationException;
import nz.pmme.Boost.Exceptions.StartSpawnNodeNotFoundException;
import nz.pmme.Boost.Main;
import nz.pmme.Utils.RandomisedDistributor;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GameConfig
{
    private Main plugin;
    private String name;
    private String displayName;

    private String configPath;

    private GameType gameType;
    private int groundLevel;
    private int ceilingLevel;
    private boolean returnToStartAtGround;
    private int targetDist;
    private boolean targetPlayers;
    private int countdown;
    private int minPlayers;
    private int maxPlayers;
    private boolean autoQueue;
    private boolean requiresPermission;
    private Material winBlock = null;
    private Material boostBlock = null;
    private Material guiItem = null;

    private SpawnLocation lobbySpawn;
    private SpawnLocation lossSpawn;
    private List<SpawnLocation> startSpawns = new ArrayList<>();
    private RandomisedDistributor startDistributor;

    private int countdownAnnounceTime;
    private List<String> winCommands;

    public GameConfig( Main plugin, String name )
    {
        this.plugin = plugin;
        this.name = name;

        this.configPath = "games." + this.name + ".";

        this.displayName = plugin.getConfig().getString( configPath + "name", this.name );

        this.gameType = GameType.fromString( plugin.getConfig().getString( configPath + "game_type", null ) );
        this.groundLevel = plugin.getConfig().getInt( configPath + "ground", 64 );
        this.ceilingLevel = plugin.getConfig().getInt( configPath + "ceiling", -1 );
        this.returnToStartAtGround = plugin.getConfig().getBoolean( configPath + "return_to_start_at_ground", false );
        if( this.gameType == null ) {
            if( this.ceilingLevel != -1 && this.groundLevel != -1 && !this.returnToStartAtGround ) this.gameType = GameType.ELIMINATION_RACE;
            else if( this.ceilingLevel != -1 ) this.gameType = GameType.RACE;
            else this.gameType = GameType.ELIMINATION;
        }

        this.targetDist = plugin.getConfig().getInt( configPath + "target_dist", 150 );
        this.targetPlayers = plugin.getConfig().getBoolean( configPath + "target_players", true );

        this.countdown = plugin.getConfig().getInt( configPath + "countdown", 30 );
        this.minPlayers = plugin.getConfig().getInt( configPath + "min_players", 2 );
        this.maxPlayers = plugin.getConfig().getInt( configPath + "max_players", 0 );
        this.autoQueue = plugin.getConfig().getBoolean( configPath + "auto_queue", false );
        this.requiresPermission = plugin.getConfig().getBoolean( configPath + "requires_permission", false );

        String winBlockName = plugin.getConfig().getString( configPath + "win_block", "" ).toUpperCase();
        if( !winBlockName.isEmpty() ) {
            this.winBlock = Material.getMaterial( winBlockName );
            if( this.winBlock == null ) {
                plugin.getLogger().warning( "Win block " + winBlockName + " is not a recognised material name." );
            }
        }

        String boostBlockName = plugin.getConfig().getString( configPath + "boost_block", "" ).toUpperCase();
        if( !boostBlockName.isEmpty() ) {
            this.boostBlock = Material.getMaterial( boostBlockName );
            if( this.boostBlock == null ) {
                plugin.getLogger().warning( "Boost block " + boostBlockName + " is not a recognised material name." );
            }
        }

        String guiItemName = plugin.getConfig().getString( configPath + "gui_item", "" ).toUpperCase();
        if( !guiItemName.isEmpty() ) {
            this.guiItem = Material.getMaterial( guiItemName );
            if( this.guiItem == null ) {
                plugin.getLogger().warning( "GUI item " + guiItemName + " is not a recognised material name." );
            }
        }

        this.lobbySpawn = new SpawnLocation( plugin, configPath + "game_lobby" );
        this.lossSpawn = new SpawnLocation( plugin, configPath + "game_loss" );
        if( !plugin.getConfig().contains( configPath + "game_starts" ) ) {
            // Copy previous single start spawn and save as a node "Start1" in the new multiple start_spawns.
            Object oldStartSpawnConfigSection = plugin.getConfig().get( configPath + "game_start" );
            if( oldStartSpawnConfigSection != null ) {
                plugin.getConfig().set( configPath + "game_starts.Start1", oldStartSpawnConfigSection );
                plugin.getConfig().set( configPath + "game_start", null );
                plugin.saveConfig();
            }
        }
        ConfigurationSection gameStartsSection = plugin.getConfig().getConfigurationSection( configPath + "game_starts" );
        if( gameStartsSection != null ) {
            for( String startSpawnNode : gameStartsSection.getKeys( false ) ) {
                SpawnLocation startSpawn = new SpawnLocation( plugin, configPath + "game_starts." + startSpawnNode );
                this.startSpawns.add( startSpawn );
                Location spawn = startSpawn.getConfiguredSpawn();
                if( spawn != null ) {
                    if( spawn.getBlockY() <= this.groundLevel && this.groundLevel != -1 ) {
                        plugin.getLogger().warning( "Start spawn " + startSpawn.getStartSpawnNode() + " Y " + spawn.getBlockY() + " should be higher than ground " + this.groundLevel + " for game " + this.name );
                    }
                    if( spawn.getBlockY() >= this.ceilingLevel && this.ceilingLevel != -1 ) {
                        plugin.getLogger().warning( "Start spawn " + startSpawn.getStartSpawnNode() + " Y " + spawn.getBlockY() + " should be lower than ceiling " + this.ceilingLevel + " for game " + this.name );
                    }
                }
            }
        }
        this.startDistributor = new RandomisedDistributor( this.startSpawns.size() );

        this.countdownAnnounceTime = plugin.getConfig().getInt( configPath + "countdown_announce_time", 10 );
        this.winCommands = plugin.getConfig().getStringList( configPath + "win_commands" );

        switch( this.gameType ) {
            case ELIMINATION:
                if( this.groundLevel == -1 ) plugin.getLogger().warning( "Ground must be set for Elimination type game " + this.name );
                if( this.returnToStartAtGround ) plugin.getLogger().warning( "Return to Start should not be set for Elimination type game " + this.name );
                break;
            case ELIMINATION_RACE:
                if( this.groundLevel == -1 ) plugin.getLogger().warning( "Ground must be set for Elimination Race type game " + this.name );
                if( this.ceilingLevel == -1 && this.winBlock == null ) plugin.getLogger().warning( "Ceiling or win block must be set for Elimination Race type game " + this.name );
                if( this.returnToStartAtGround ) plugin.getLogger().warning( "Return to Start should not be set for Elimination Race type game " + this.name );
                if( this.groundLevel >= this.ceilingLevel && this.groundLevel != -1 && this.ceilingLevel != -1 ) {
                    plugin.getLogger().warning( "Ceiling " + this.ceilingLevel + " should be higher than ground " + this.groundLevel + " for game " + this.name );
                }
                break;
            case RACE:
                if( this.ceilingLevel == -1 && this.winBlock == null ) plugin.getLogger().warning( "Ceiling or win block must be set for Race type game " + this.name );
                if( this.returnToStartAtGround && this.groundLevel == -1 ) {
                    plugin.getLogger().warning( "Ground is required since Return to Start is true for Race type game " + this.name );
                }
                if( this.returnToStartAtGround && this.groundLevel >= this.ceilingLevel && this.groundLevel != -1 && this.ceilingLevel != -1 ) {
                    plugin.getLogger().warning( "Ceiling " + this.ceilingLevel + " should be higher than ground " + this.groundLevel + " for game " + this.name );
                }
                break;
            case PARKOUR:
                if( this.ceilingLevel == -1 && this.winBlock == null ) plugin.getLogger().warning( "Ceiling or win block must be set for Parkour type game " + this.name );
                if( this.returnToStartAtGround && this.groundLevel == -1 ) {
                    plugin.getLogger().warning( "Ground is required since Return to Start is true for Parkour type game " + this.name );
                }
                if( this.returnToStartAtGround && this.groundLevel >= this.ceilingLevel && this.groundLevel != -1 && this.ceilingLevel != -1 ) {
                    plugin.getLogger().warning( "Ceiling " + this.ceilingLevel + " should be higher than ground " + this.groundLevel + " for game " + this.name );
                }
                break;
        }

    }

    public void setConfig()
    {
        plugin.getConfig().set( configPath + "name", this.displayName );
        plugin.getConfig().set( configPath + "game_type", this.gameType.toString() );
        plugin.getConfig().set( configPath + "ground", this.groundLevel );
        plugin.getConfig().set( configPath + "ceiling", this.ceilingLevel );
        plugin.getConfig().set( configPath + "return_to_start_at_ground", this.returnToStartAtGround );
        plugin.getConfig().set( configPath + "target_dist", this.targetDist );
        plugin.getConfig().set( configPath + "target_players", this.targetPlayers );

        plugin.getConfig().set( configPath + "countdown", this.countdown );
        plugin.getConfig().set( configPath + "min_players", this.minPlayers );
        plugin.getConfig().set( configPath + "max_players", this.maxPlayers );
        plugin.getConfig().set( configPath + "auto_queue", this.autoQueue );
        plugin.getConfig().set( configPath + "requires_permission", this.requiresPermission );
        plugin.getConfig().set( configPath + "win_block", this.winBlock != null ? this.winBlock.toString() : "" );
        plugin.getConfig().set( configPath + "boost_block", this.boostBlock != null ? this.boostBlock.toString() : "" );
        plugin.getConfig().set( configPath + "gui_item", this.guiItem != null ? this.guiItem.toString() : "" );

        this.lobbySpawn.setConfig();
        this.lossSpawn.setConfig();
        for( SpawnLocation startSpawn : this.startSpawns ) {
            startSpawn.setConfig();
        }

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

    public boolean doesRequirePermission() {
        return requiresPermission;
    }

    public void setRequiresPermission( boolean newRequiresPermission )
    {
        requiresPermission = newRequiresPermission;
        this.setConfig();
        plugin.saveConfig();
    }

    public Material getWinBlock() {
        return winBlock;
    }

    public void setWinBlock( Material material )
    {
        winBlock = material != Material.AIR ? material : null;
        this.setConfig();
        plugin.saveConfig();
    }

    public Material getBoostBlock() {
        return boostBlock;
    }

    public void setBoostBlock( Material material )
    {
        boostBlock = material != Material.AIR ? material : null;
        this.setConfig();
        plugin.saveConfig();
    }

    public Material getGuiItem() {
        return guiItem;
    }

    public void setGuiItem( Material material )
    {
        guiItem = material != Material.AIR ? material : null;
        this.setConfig();
        plugin.saveConfig();
    }

    public GameType getGameType()
    {
        return gameType;
    }

    public void setGameType( GameType gameType )
    {
        this.gameType = gameType;
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

    public int getCeilingLevel()
    {
        return ceilingLevel;
    }

    public void setCeilingLevel( int newCeiling )
    {
        ceilingLevel = newCeiling;
        this.setConfig();
        plugin.saveConfig();
    }

    public boolean isReturnToStartAtGround()
    {
        return returnToStartAtGround;
    }

    public void setReturnToStartAtGround( boolean newReturnToStartAtGround )
    {
        returnToStartAtGround = newReturnToStartAtGround;
        this.setConfig();
        plugin.saveConfig();
    }

    public int getTargetDist()
    {
        return targetDist;
    }

    public boolean canTargetPlayers()
    {
        return targetPlayers;
    }

    public void setTargetPlayers( boolean newTargetPlayers )
    {
        targetPlayers = newTargetPlayers;
        this.setConfig();
        plugin.saveConfig();
    }

    public Location getLobbySpawn() {
        return lobbySpawn.getSpawn();
    }

    public void setLobbySpawn( Location spawn )
    {
        lobbySpawn.setSpawn( spawn );
    }

    public Location getConfiguredLobbySpawn() {
        return lobbySpawn.getConfiguredSpawn();
    }

    public Location getLossSpawn() {
        return lossSpawn.getSpawn();
    }

    public void setLossSpawn( Location spawn )
    {
        lossSpawn.setSpawn( spawn );
    }

    public Location getConfiguredLossSpawn() {
        return lossSpawn.getConfiguredSpawn();
    }

    public Location getStartSpawn()
    {
        if( startSpawns.isEmpty() ) return null;
        return startSpawns.get( startDistributor.getNext() ).getSpawn();
    }

    public void setStartSpawn( Location spawn, String startSpawnNode )
    {
        for( SpawnLocation startSpawn : startSpawns ) {
            if( startSpawn.getStartSpawnNode().equalsIgnoreCase( startSpawnNode ) ) {
                startSpawn.setSpawn( spawn );
                return;
            }
        }
        SpawnLocation startSpawn = new SpawnLocation( plugin, configPath + "game_starts." + startSpawnNode );
        startSpawn.setSpawn( spawn );
        startSpawns.add( startSpawn );
        startDistributor.setRange( startSpawns.size() );
    }

    public void deleteStartSpawn( String startSpawnNode ) throws StartSpawnNodeNotFoundException
    {
        for( int i = 0; i < startSpawns.size(); ++i ) {
            if( startSpawns.get(i).getStartSpawnNode().equalsIgnoreCase( startSpawnNode ) ) {
                startSpawns.get(i).removeConfig();
                startSpawns.remove(i);
                startDistributor.setRange( startSpawns.size() );
                return;
            }
        }
        throw new StartSpawnNodeNotFoundException();
    }

    public Location getConfiguredStartSpawn( String startSpawnNode )
    {
        for( SpawnLocation startSpawn : startSpawns ) {
            if( startSpawn.getStartSpawnNode().equalsIgnoreCase( startSpawnNode ) ) {
                return startSpawn.getConfiguredSpawn();
            }
        }
        return null;
    }

    public boolean hasStartSpawn() {
        return !startSpawns.isEmpty();
    }

    public List<String> getStartSpawnNodes() {
        List<String> nodes = new ArrayList<>();
        for( SpawnLocation startSpawn : startSpawns ) {
            nodes.add( startSpawn.getStartSpawnNode() );
        }
        return nodes;
    }

    public int getStartSpawnsMinY() {
        if( startSpawns.isEmpty() ) return -256;
        int y = startSpawns.get(0).getSpawn().getBlockY();
        for( SpawnLocation startSpawn : startSpawns ) {
            if( startSpawn.getSpawn().getBlockY() < y ) y = startSpawn.getSpawn().getBlockY();
        }
        return y;
    }

    public int getStartSpawnsMaxY() {
        if( startSpawns.isEmpty() ) return -256;
        int y = startSpawns.get(0).getSpawn().getBlockY();
        for( SpawnLocation startSpawn : startSpawns ) {
            if( startSpawn.getSpawn().getBlockY() > y ) y = startSpawn.getSpawn().getBlockY();
        }
        return y;
    }

    public void setSpawnSpread( int spread, String startSpawnNode ) throws StartSpawnNodeNotFoundException
    {
        for( SpawnLocation startSpawn : startSpawns ) {
            if( startSpawn.getStartSpawnNode().equalsIgnoreCase( startSpawnNode ) ) {
                startSpawn.setSpread( spread );
                return;
            }
        }
        throw new StartSpawnNodeNotFoundException();
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
        if( this.startSpawns.isEmpty() ) return false;
        switch( this.gameType )
        {
            case ELIMINATION:
                if( this.getGroundLevel() == -1 ) return false;
                if( this.getLossSpawn() == null ) return false;
                if( this.getStartSpawnsMinY() <= this.getGroundLevel() ) return false;
                break;
            case ELIMINATION_RACE:
                if( this.getGroundLevel() == -1 ) return false;
                if( this.getCeilingLevel() == -1 && this.getWinBlock() == null ) return false;
                if( this.getLossSpawn() == null ) return false;
                if( this.getGroundLevel() >= this.getCeilingLevel() && this.getCeilingLevel() != -1 ) return false;
                if( this.getStartSpawnsMinY() <= this.getGroundLevel() ) return false;
                if( this.getStartSpawnsMaxY() >= this.getCeilingLevel() && this.getCeilingLevel() != -1 ) return false;
                break;
            case RACE:
            case PARKOUR:
                if( this.getCeilingLevel() == -1 && this.getWinBlock() == null ) return false;
                if( this.isReturnToStartAtGround() ) {
                    if( this.getGroundLevel() == -1 ) return false;
                    if( this.getGroundLevel() >= this.getCeilingLevel() && this.getCeilingLevel() != -1 ) return false;
                    if( this.getStartSpawnsMinY() <= this.getGroundLevel() ) return false;
                }
                if( this.getStartSpawnsMaxY() >= this.getCeilingLevel() && this.getCeilingLevel() != -1 ) return false;
                break;
        }
        return true;
    }

    public void displayConfig( CommandSender sender )
    {
        boolean errorCeilingOrWinBlock = ( this.winBlock == null && this.ceilingLevel == -1 && this.gameType != GameType.ELIMINATION );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5Boost game config:" ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Config name: &3" + this.name ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Display name: &3" + this.displayName ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Game type: &3" + this.gameType.toString() ) );
        switch( this.gameType )
        {
            case ELIMINATION:
                if( this.groundLevel == -1 ) {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ground level: &c" + this.groundLevel + " disabled" ) );
                } else {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ground level: &3" + this.groundLevel ) );
                }
                if( this.ceilingLevel == -1 ) {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ceiling level: &3" + this.ceilingLevel + " disabled" ) );
                } else {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ceiling level: &c" + this.ceilingLevel + " but not required" ) );
                }
                sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Return to start at ground: " + ( this.returnToStartAtGround ? "&ctrue" : "&3false" ) ) );
                break;
            case ELIMINATION_RACE:
                if( this.groundLevel == -1 ) {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ground level: &c" + this.groundLevel + " disabled" ) );
                } else {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ground level: &3" + this.groundLevel ) );
                }
                if( this.ceilingLevel == -1 ) {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ceiling level: " + (errorCeilingOrWinBlock?"&c":"&3") + this.ceilingLevel + " disabled" ) );
                } else {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ceiling level: &3" + this.ceilingLevel ) );
                }
                if( errorCeilingOrWinBlock ) sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&c Either a ceiling or a win block is required" ) );
                if( this.groundLevel >= this.ceilingLevel && this.groundLevel != -1 && this.ceilingLevel != -1 ) {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&c Ceiling " + this.ceilingLevel + " should be higher than ground " + this.groundLevel ) );
                }
                sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Return to start at ground: " + ( this.returnToStartAtGround ? "&ctrue" : "&3false" ) ) );
                break;
            case RACE:
            case PARKOUR:
                if( this.groundLevel == -1 ) {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ground level: " + (this.returnToStartAtGround?"&c":"&3") + this.groundLevel + " disabled" ) );
                    if( this.returnToStartAtGround ) sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&c Ground is required since Return to Start is true" ) );
                } else {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ground level: " + (!this.returnToStartAtGround?"&c":"&3") + this.groundLevel + (!this.returnToStartAtGround?" but not required":"") ) );
                }
                if( this.ceilingLevel == -1 ) {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ceiling level: " + (errorCeilingOrWinBlock?"&c":"&3") + this.ceilingLevel + " disabled" ) );
                } else {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Ceiling level: &3" + this.ceilingLevel ) );
                }
                if( errorCeilingOrWinBlock ) sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&c Either a ceiling or a win block is required" ) );
                if( this.returnToStartAtGround && this.groundLevel >= this.ceilingLevel && this.groundLevel != -1 && this.ceilingLevel != -1 ) {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&c Ceiling " + this.ceilingLevel + " should be higher than ground " + this.groundLevel ) );
                }
                sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Return to start at ground: &3" + ( this.returnToStartAtGround ? "true" : "false" ) ) );
                break;
        }
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Target players in air: &3" + ( this.targetPlayers ? "true" : "false" ) ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Target distance: &3" + this.targetDist ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Start countdown: &3" + this.countdown ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Countdown announcement period: &3" + this.countdownAnnounceTime ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Minimum players to start: &3" + this.minPlayers ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Maximum players: &3" + this.maxPlayers ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Auto queue: &3" + ( this.autoQueue ? "&aon" : "&coff" ) ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Requires permission: &3" + ( this.requiresPermission ? "true" : "false" ) ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Win block: " + (errorCeilingOrWinBlock?"&c":"&3") + ( this.winBlock != null ? this.winBlock.toString() : "-" ) ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Boost block: &3" + ( this.boostBlock != null ? this.boostBlock.toString() : "-" ) ) );
        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f GUI item: &3" + ( this.guiItem != null ? this.guiItem.toString() : "-" ) ) );
        if( this.lobbySpawn.getConfiguredSpawn() == null ) {
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Lobby spawn: &cNot configured" ) );
        } else {
            Location spawn = this.lobbySpawn.getConfiguredSpawn();
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Lobby spawn: &3" + spawn.getWorld().getName() + " " + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ() ) );
        }
        if( this.startSpawns.isEmpty() ) {
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Start spawn: &cNot configured" ) );
        } else {
            for( SpawnLocation startSpawn : this.startSpawns ) {
                Location spawn = startSpawn.getConfiguredSpawn();
                boolean errorStartToGround = ( spawn.getBlockY() <= this.groundLevel && this.groundLevel != -1 );
                boolean errorStartToCeiling = ( spawn.getBlockY() >= this.ceilingLevel && this.ceilingLevel != -1 );
                sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Start spawn &3" + startSpawn.getStartSpawnNode() + "&f : " + (errorStartToGround||errorStartToCeiling?"&c":"&3") + spawn.getWorld().getName() + " " + spawn.getBlockX() + ", " + spawn.getBlockY() + ", " + spawn.getBlockZ() + "&f Spread: &3" + startSpawn.getSpread() ) );
                if( errorStartToGround ) sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&c Start spawn Y " + spawn.getBlockY() + " should be higher than ground " + this.groundLevel ) );
                if( errorStartToCeiling ) sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&c Start spawn Y " + spawn.getBlockY() + " should be lower than ceiling " + this.ceilingLevel ) );
            }
        }
        if( this.lossSpawn.getConfiguredSpawn() == null ) {
            switch( this.gameType ) {
                case ELIMINATION:
                case ELIMINATION_RACE:
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Loss spawn: &cNot configured" ) );
                    break;
                case RACE:
                case PARKOUR:
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|&f Loss spawn: &3Not required" ) );
                    break;
            }
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
