package nz.pmme.Boost;

import nz.pmme.Boost.Enums.GameState;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class Game
{
    private Main plugin;
    private String name;
    private GameState gameState;
    private Map< UUID, PlayerInfo > players = new HashMap<>();
    int groundLevel = 64;

    private static final String boostJoinMessage = ChatColor.DARK_AQUA + "Joined Boost game";
    private static final String boostNoJoinRunningMessage = ChatColor.RED + "You cannot join that game because it is already running.";
    private static final String boostNoJoinStoppedMessage = ChatColor.RED + "You cannot join that game because it is stopped.";
    private static final String boostLeaveMessage = ChatColor.DARK_AQUA + "Left Boost game";
    private static final String boostLostMessage = ChatColor.DARK_PURPLE + "Your out!";
    private static final String boostGameStarted = ChatColor.LIGHT_PURPLE + "Boost game has started!";
    private static final String boostGameEnded = ChatColor.LIGHT_PURPLE + "Boost game has ended!";
    private static final String boostWinner = ChatColor.GREEN + "!!Player %player% is the the winner!!";

    public Game( Main plugin, String name )
    {
        this.plugin = plugin;
        this.name = name;
        this.gameState = GameState.STOPPED;
    }

    public String getName() {
        return name;
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
        if( gameState == GameState.RUNNING && players.size() <= 1 )
        {
            this.end();
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
                String message = boostWinner.replaceAll( "%player%", activePlayers.get(0).getDisplayName() );
                for( PlayerInfo playerInfo : players.values() ) {
                    playerInfo.getPlayer().sendMessage( ChatColor.translateAlternateColorCodes( '&', message ) );
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

    // TODO: Should have some other game state checking, potentially called from the commands.

    public boolean start()
    {
        gameState = GameState.RUNNING;
        for( PlayerInfo playerInfo : players.values() )
        {
            playerInfo.setActive();
            playerInfo.getPlayer().sendMessage( boostGameStarted );
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
