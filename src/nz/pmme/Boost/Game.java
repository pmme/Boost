package nz.pmme.Boost;

import nz.pmme.Boost.Enums.GameState;
import nz.pmme.Boost.Config.SpawnLocation;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
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
    private int targetDist;
    private int countdown;
    private int minPlayers;
    private int maxPlayers;
    private boolean autoQueue;

    private SpawnLocation lobbySpawn;
    private SpawnLocation startSpawn;
    private SpawnLocation lossSpawn;
    private int spawnSpread;

    private int countdownAnnounceTime;
    private int remainingQueueTime;
    private BukkitTask queueTask;

    private static final String boostJoinMessage = ChatColor.DARK_AQUA + "Joined Boost game";
    private static final String boostNoJoinRunningMessage = ChatColor.RED + "You cannot join that game because it is already running.";
    private static final String boostNoJoinStoppedMessage = ChatColor.RED + "You cannot join that game because it is stopped.";
    private static final String boostNoJoinGameFullMessage = ChatColor.RED + "You cannot join that game because it is already full.";
    private static final String boostLeaveMessage = ChatColor.DARK_AQUA + "Left Boost game";
    private static final String boostLostMessage = ChatColor.DARK_PURPLE + "Your out!";
    private static final String boostGameStarted = ChatColor.LIGHT_PURPLE + "Boost game has started!";
    private static final String boostGameEnded = ChatColor.LIGHT_PURPLE + "Boost game has ended!";
    private static final String boostGameStartsIn = ChatColor.LIGHT_PURPLE + "Game starts in %time% seconds ...";
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
        this.targetDist = plugin.getConfig().getInt( this.configPath( "target_dist" ), 150 );

        this.countdown = plugin.getConfig().getInt( this.configPath( "countdown" ), 30 );
        this.minPlayers = plugin.getConfig().getInt( this.configPath( "min_players" ), 2 );
        this.maxPlayers = plugin.getConfig().getInt( this.configPath( "max_players" ), 0 );
        this.autoQueue = plugin.getConfig().getBoolean( this.configPath( "auto_queue" ), true );

        this.lobbySpawn = new SpawnLocation( plugin, name, "game_lobby" );
        this.startSpawn = new SpawnLocation( plugin, name, "game_start" );
        this.lossSpawn = new SpawnLocation( plugin, name, "game_loss" );
        this.spawnSpread = plugin.getConfig().getInt( this.configPath( "game_start.spread" ), 4 );

        this.countdownAnnounceTime = plugin.getConfig().getInt( this.configPath( "countdown_announce_time" ), 10 );

        if( this.autoQueue ) this.startQueuing();
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

    public int getCountdown() {
        return countdown;
    }

    public int getRemainingQueueTime() {
        return remainingQueueTime;
    }

    public int getMinPlayers() {
        return minPlayers;
    }

    public int getMaxPlayers() {
        return maxPlayers;
    }

    public void startQueuing()
    {
        gameState = GameState.QUEUING;
        remainingQueueTime = this.getCountdown();
        BukkitRunnable queueRunnable = new BukkitRunnable()
        {
            @Override
            public void run() {
                if( !plugin.isBoostEnabled() ) return;
                if( getPlayerCount() < Game.this.getMinPlayers() ) {
                    remainingQueueTime = Game.this.getCountdown();
                } else {
                    if( remainingQueueTime <= 0 ) {
                        this.cancel();
                        Game.this.start();
                    } else {
                        if( remainingQueueTime % countdownAnnounceTime == 0 || remainingQueueTime <= 5 ) {
                            String message = ChatColor.translateAlternateColorCodes( '&', boostGameStartsIn.replaceAll( "%time%", String.valueOf( remainingQueueTime ) ) );
                            for( PlayerInfo playerInfo : players.values() ) {
                                playerInfo.getPlayer().sendMessage( message );
                            }
                        }
                        --remainingQueueTime;
                    }
                }
            }
        };
        queueTask = queueRunnable.runTaskTimer( plugin, 1L, 20L/*ticksPerSecond*/ );
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

    public int getTargetDist()
    {
        return targetDist;
    }

    public void setLobbySpawn( Location spawn )
    {
        lobbySpawn.setSpawn( spawn );
    }

    public void setStartSpawn( Location spawn )
    {
        startSpawn.setSpawn( spawn );
    }

    public void setLossSpawn( Location spawn )
    {
        lossSpawn.setSpawn( spawn );
    }

    public void setSpawnSpread( int spread )
    {
        this.spawnSpread = spread;
        plugin.getConfig().set( configPath("game_start.spread" ), this.spawnSpread );
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
        if( this.getMaxPlayers() > 0 && this.getPlayerCount() >= this.getMaxPlayers() )
        {
            player.sendMessage( boostNoJoinGameFullMessage );
        }
        players.put( player.getUniqueId(), new PlayerInfo( player ) );
        player.teleport( lobbySpawn.getSpawn() );
        player.setGameMode( GameMode.ADVENTURE );
        player.sendMessage( boostJoinMessage );
        return true;
    }

    public void leave( Player player )
    {
        players.remove( player.getUniqueId() );
        player.teleport( lobbySpawn.getSpawn() );
        player.setGameMode( GameMode.ADVENTURE );
        player.sendMessage( boostLeaveMessage );
        player.getInventory().clear();

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
                this.end( false );
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
            player.teleport( lossSpawn.getSpawn() );
            player.setGameMode( GameMode.SPECTATOR );
            player.sendMessage( boostLostMessage );
            player.getInventory().clear();

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
                this.end( false );
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

    public boolean isQueuing()
    {
        return gameState == GameState.QUEUING;
    }

    public String getGameStateText()
    {
        return gameState.toString();
    }

    public boolean start()
    {
        if( queueTask != null && !queueTask.isCancelled() ) queueTask.cancel();
        gameState = GameState.RUNNING;
        for( PlayerInfo playerInfo : players.values() )
        {
            // TODO: Probably worth making PlayerInfo a PlayerControl class, then adding startGame() to it. Needs spawn point and spread.
            Location location = new Location( startSpawn.getSpawn().getWorld(), startSpawn.getSpawn().getX(), startSpawn.getSpawn().getY(), startSpawn.getSpawn().getZ(), (float)( Math.random() * 360.0 ), 0 );
            Vector spreadVector = new Vector( Math.random()*spawnSpread, 0, 0 ).rotateAroundY( Math.random() * 360.0 );
            location.add( spreadVector );
            playerInfo.getPlayer().teleport( location );
            playerInfo.getPlayer().setGameMode( GameMode.ADVENTURE );
            playerInfo.getPlayer().sendMessage( boostGameStarted );
            playerInfo.getPlayer().getInventory().setItemInMainHand( new ItemStack( Material.DIAMOND_HOE ) );
            playerInfo.setActive();
        }
        return true;
    }

    public boolean end( boolean noAutoQueue )
    {
        boolean wasQueuing = ( gameState == GameState.QUEUING || ( queueTask != null && !queueTask.isCancelled() ) );
        if( queueTask != null && !queueTask.isCancelled() ) queueTask.cancel();  // Cancel the queue in case it's queuing rather than running.
        gameState = GameState.STOPPED;
        for( PlayerInfo playerInfo : players.values() )
        {
            playerInfo.getPlayer().teleport( lobbySpawn.getSpawn() );
            playerInfo.getPlayer().setGameMode( GameMode.ADVENTURE );
            playerInfo.getPlayer().sendMessage( boostGameEnded );
            playerInfo.getPlayer().getInventory().clear();
            plugin.getGameManager().removePlayer( playerInfo.getPlayer() );
        }
        players.clear();
        if( autoQueue && !wasQueuing && !noAutoQueue ) this.startQueuing();    // Don't restart queuing if we were in the queuing state when end was called.
        return true;
    }
}
