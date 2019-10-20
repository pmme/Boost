package nz.pmme.Boost;

import nz.pmme.Boost.Enums.GameState;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.*;

public class Game
{
    private Main plugin;
    private String name;
    private String displayName;
    private GameState gameState;
    private Map< UUID, PlayerInfo > players = new HashMap<>();
    private int groundLevel;
    private Location spawn;
    private int spawnSpread;

    private static final String boostJoinMessage = ChatColor.DARK_AQUA + "Joined Boost game";
    private static final String boostNoJoinRunningMessage = ChatColor.RED + "You cannot join that game because it is already running.";
    private static final String boostNoJoinStoppedMessage = ChatColor.RED + "You cannot join that game because it is stopped.";
    private static final String boostLeaveMessage = ChatColor.DARK_AQUA + "Left Boost game";
    private static final String boostLostMessage = ChatColor.DARK_PURPLE + "Your out!";
    private static final String boostGameStarted = ChatColor.LIGHT_PURPLE + "Boost game has started!";
    private static final String boostGameEnded = ChatColor.LIGHT_PURPLE + "Boost game has ended!";
    private static final String boostWinner = ChatColor.GREEN + "!!Player %player% is the the winner!!";

    private String configPath( String subPath ) {
        return "games." + this.name + "." + subPath;
    }

    public Game( Main plugin, String name )
    {
        this.plugin = plugin;
        this.name = name;
        this.gameState = GameState.STOPPED;

        this.displayName = plugin.getConfig().getString( this.configPath( "name" ), this.name );
        this.groundLevel = plugin.getConfig().getInt( this.configPath( "ground" ), 64 );
        World spawnWorld = plugin.getServer().getWorld( plugin.getConfig().getString( this.configPath( "spawn.world" ), "world" ) );
        if( spawnWorld != null ) {
            int spawnX = plugin.getConfig().getInt( this.configPath( "spawn.x" ), 0 );
            int spawnY = plugin.getConfig().getInt( this.configPath( "spawn.y" ), 64 );
            int spawnZ = plugin.getConfig().getInt( this.configPath( "spawn.z" ), 0 );
            this.spawn = new Location( spawnWorld, spawnX, spawnY, spawnZ );
        }
        this.spawnSpread = plugin.getConfig().getInt( this.configPath( "spawn.spread" ), 4 );
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public void setQueuing() {
        gameState = GameState.QUEUING;
    }

    public int getGroundLevel()
    {
        return groundLevel;
    }

    public void setGroundLevel( int newGround )
    {
        groundLevel = newGround;
        plugin.getConfig().set( configPath( "ground" ), groundLevel );
        plugin.saveConfig();
    }

    public void setSpawn( Location spawn )
    {
        this.spawn = spawn;
        plugin.getConfig().set( configPath("spawn.world" ), this.spawn.getWorld().getName() );
        plugin.getConfig().set( configPath("spawn.x" ), this.spawn.getBlockX() );
        plugin.getConfig().set( configPath("spawn.y" ), this.spawn.getBlockY() );
        plugin.getConfig().set( configPath("spawn.z" ), this.spawn.getBlockZ() );
        plugin.saveConfig();
    }

    public void setSpawnSpread( int spread )
    {
        this.spawnSpread = spread;
        plugin.getConfig().set( configPath("spawn.spread" ), this.spawnSpread );
        plugin.saveConfig();
    }

    public boolean join( Player player )
    {
        if( gameState != GameState.QUEUING )
        {
            switch( gameState )
            {
                case RUNNING:
                    player.sendMessage( boostNoJoinRunningMessage );
                    break;
                case STOPPED:
                    player.sendMessage( boostNoJoinStoppedMessage );
                    break;
            }
            return true;
        }
        players.put( player.getUniqueId(), new PlayerInfo( player ) );
        player.sendMessage( boostJoinMessage );
        return true;
    }

    public void leave( Player player )
    {
        players.remove( player.getUniqueId() );
        player.sendMessage( boostLeaveMessage );
        if( gameState == GameState.RUNNING )
        {
            List<Player> activePlayers = getActivePlayerList();
            if( activePlayers.size() <= 1 )
            {
                gameState = GameState.STOPPED;
                if( activePlayers.size() == 1 )
                {
                    String message = boostWinner.replaceAll( "%player%", activePlayers.get( 0 ).getDisplayName() );
                    for( PlayerInfo playerInfo : players.values() ) {
                        playerInfo.getPlayer().sendMessage( ChatColor.translateAlternateColorCodes( '&', message ) );
                    }
                }
                this.end();
            }
        }
    }

    public List<Player> getPlayerList()
    {
        List<Player> playerList = new ArrayList<>();
        for( PlayerInfo playerInfo : players.values() )
        {
            playerList.add( playerInfo.getPlayer() );
        }
        return playerList;
    }

    public List<Player> getActivePlayerList()
    {
        List<Player> playerList = new ArrayList<>();
        for( PlayerInfo playerInfo : players.values() )
        {
            if( playerInfo.isActive() )
            {
                playerList.add( playerInfo.getPlayer() );
            }
        }
        return playerList;
    }

    public void setPlayerLost( Player player )
    {
        if( gameState == GameState.RUNNING )
        {
            PlayerInfo payerInfoLost = players.get( player.getUniqueId() );
            if( payerInfoLost != null ) payerInfoLost.setLost();
            player.sendMessage( boostLostMessage );

            List<Player> activePlayers = getActivePlayerList();
            if( activePlayers.size() <= 1 )
            {
                gameState = GameState.STOPPED;
                if( activePlayers.size() == 1 )
                {
                    String message = boostWinner.replaceAll( "%player%", activePlayers.get( 0 ).getDisplayName() );
                    for( PlayerInfo playerInfo : players.values() ) {
                        playerInfo.getPlayer().sendMessage( ChatColor.translateAlternateColorCodes( '&', message ) );
                    }
                }
                this.end();
            }
        }
    }

    public boolean isActiveInGame( Player player )
    {
        PlayerInfo playerInfo = players.get( player.getUniqueId() );
        if( playerInfo == null ) return false;
        return playerInfo.isActive();
    }

    public String getPlayerStateText( Player player )
    {
        PlayerInfo playerInfo = players.get( player.getUniqueId() );
        if( playerInfo == null ) return "Not in game";
        return playerInfo.getPlayerStateText();
    }

    public boolean isRunning()
    {
        return gameState == GameState.RUNNING;
    }

    public String getGameStateText()
    {
        return gameState.toString();
    }

    public boolean start()
    {
        gameState = GameState.RUNNING;
        for( PlayerInfo playerInfo : players.values() )
        {
            Location location = new Location( spawn.getWorld(), spawn.getX(), spawn.getY(), spawn.getZ(), (float)( Math.random() * 360.0 ), 0 );
            Vector spreadVector = new Vector( Math.random()*spawnSpread, 0, 0 ).rotateAroundY( Math.random() * 360.0 );
            location.add( spreadVector );
            playerInfo.getPlayer().teleport( location );
            playerInfo.setActive();
            playerInfo.getPlayer().sendMessage( boostGameStarted );
            playerInfo.getPlayer().getInventory().setItemInMainHand( new ItemStack( Material.DIAMOND_HOE ) );
            playerInfo.getPlayer().setGameMode( GameMode.ADVENTURE );
        }
        return true;
    }

    public boolean end()
    {
        gameState = GameState.STOPPED;
        for( PlayerInfo playerInfo : players.values() )
        {
            playerInfo.getPlayer().sendMessage( boostGameEnded );
            plugin.getGameManager().removePlayer( playerInfo.getPlayer() );
        }
        players.clear();
        gameState = GameState.QUEUING;
        return true;
    }
}
