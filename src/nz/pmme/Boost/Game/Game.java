package nz.pmme.Boost.Game;

import nz.pmme.Boost.Config.BoostStick;
import nz.pmme.Boost.Config.GameConfig;
import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.GameState;
import nz.pmme.Boost.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.*;

public class Game implements Comparable<Game>
{
    private Main plugin;
    private GameConfig gameConfig;
    private GameState gameState;
    private Map< UUID, PlayerInfo > players = new HashMap<>();
    private boolean aPlayerHasLost;

    private int remainingQueueTime;
    private BukkitTask queueTask;

    private static Vector VECTOR0 = new Vector();

    public Game( Main plugin, GameConfig gameConfig )
    {
        this.plugin = plugin;
        this.gameConfig = gameConfig;
        this.gameState = GameState.STOPPED;
        this.aPlayerHasLost = false;

        if( this.gameConfig.isAutoQueue() ) this.startQueuing();
    }

    @Override
    public int compareTo( Game other ) {
        return this.gameConfig.getName().compareTo( other.gameConfig.getName() );
    }

    public GameConfig getGameConfig() {
        return gameConfig;
    }

    public int getPlayerCount() {
        return players.size();
    }

    public int getRemainingQueueTime() {
        return remainingQueueTime;
    }

    public void startQueuing()
    {
        gameState = GameState.QUEUING;
        remainingQueueTime = gameConfig.getCountdown();
        BukkitRunnable queueRunnable = new BukkitRunnable()
        {
            @Override
            public void run() {
                if( !plugin.isBoostEnabled() ) return;
                if( getPlayerCount() < gameConfig.getMinPlayers() ) {
                    remainingQueueTime = gameConfig.getCountdown();
                } else {
                    if( remainingQueueTime <= 0 ) {
                        this.cancel();
                        Game.this.start();
                    } else {
                        if( remainingQueueTime % gameConfig.getCountdownAnnounceTime() == 0 || remainingQueueTime <= 5 ) {
                            String message = plugin.formatMessage( Messages.GAME_COUNTDOWN, gameConfig.getDisplayName(), "%time%", String.valueOf( remainingQueueTime ) );
                            for( PlayerInfo playerInfo : players.values() ) {
                                plugin.messageSender( playerInfo.getPlayer(), message );
                                if( remainingQueueTime <= 5 ) {
                                    playerInfo.getPlayer().playSound( playerInfo.getPlayer().getLocation(), plugin.getLoadedConfig().getTickSound(), 1, 1 );
                                }
                            }
                        }
                        --remainingQueueTime;
                    }
                }
            }
        };
        queueTask = queueRunnable.runTaskTimer( plugin, 1L, 20L/*ticksPerSecond*/ );
    }

    public boolean join( Player player )
    {
        if( this.gameConfig.doesRequirePermission() && !player.hasPermission( "boost.join." + gameConfig.getName() ) ) {
            plugin.messageSender( player, Messages.GAME_NOT_AVAILABLE, gameConfig.getDisplayName() );
            return false;
        }
        if( gameState != GameState.QUEUING )
        {
            switch( gameState )
            {
                case RUNNING:
                    plugin.messageSender( player, Messages.NO_JOIN_GAME_RUNNING, gameConfig.getDisplayName() );
                    break;
                case STOPPED:
                    plugin.messageSender( player, Messages.NO_JOIN_GAME_STOPPED, gameConfig.getDisplayName() );
                    break;
            }
            return false;
        }
        if( gameConfig.getMaxPlayers() > 0 && this.getPlayerCount() >= gameConfig.getMaxPlayers() )
        {
            plugin.messageSender( player, Messages.NO_JOIN_GAME_FULL, gameConfig.getDisplayName() );
            return false;
        }

        String message = plugin.formatMessage( Messages.PLAYER_JOINED_GAME, gameConfig.getDisplayName(), "%player%", player.getDisplayName() );
        int playersRequired = gameConfig.getMinPlayers() - this.getPlayerCount() - 1;
        String messagePlayerCount = plugin.formatMessage( Messages.PLAYERS_REQUIRED, gameConfig.getDisplayName(), "%count%", String.valueOf( playersRequired ) );
        for( PlayerInfo playerInfo : players.values() ) {
            plugin.messageSender( playerInfo.getPlayer(), message );
            if( playersRequired > 0 ) plugin.messageSender( playerInfo.getPlayer(), messagePlayerCount );
        }

        players.put( player.getUniqueId(), new PlayerInfo( player ) );
        player.setVelocity( Game.VECTOR0 );
        player.setFlying( false );
        player.teleport( gameConfig.getLobbySpawn() );
        player.setGameMode( plugin.getLoadedConfig().getLobbyGameMode() );
        plugin.messageSender( player, Messages.JOIN_GAME, gameConfig.getDisplayName() );
        if( playersRequired > 0 ) plugin.messageSender( player, messagePlayerCount );
        plugin.getDataHandler().addPlayer( player.getUniqueId(), player.getDisplayName() );
        player.playSound( player.getLocation(), plugin.getLoadedConfig().getJoinSound(), 1, 1 );

        this.giveBoostSticks( player );

        return true;
    }

    public void leave( Player player )
    {
        PlayerInfo playerInfo = players.get( player.getUniqueId() );
        if( playerInfo != null ) playerInfo.resetCoolDown();
        players.remove( player.getUniqueId() );
        player.setVelocity( Game.VECTOR0 );
        player.setFlying( false );
        plugin.messageSender( player, Messages.LEAVE_GAME, gameConfig.getDisplayName() );
        if( plugin.isInGameWorld( player ) ) {
            player.teleport( plugin.getLoadedConfig().getMainLobbySpawn() );
            player.setGameMode( plugin.getLoadedConfig().getLobbyGameMode() );
            player.getInventory().clear();
            ItemStack instructionBook = plugin.getLoadedConfig().createInstructionBook();
            player.getInventory().setItem( 8, instructionBook );
            ItemStack mainGuiItem = plugin.getLoadedConfig().getGuiButtonConfig( "main" ).create();
            player.getInventory().setItem( 0, mainGuiItem );
            player.getInventory().setHeldItemSlot( 0 );
        }
        if( gameState == GameState.RUNNING )
        {
            List<Player> activePlayers = getActivePlayerList();
            if( activePlayers.size() == 0 ) {
                gameState = GameState.STOPPED;
                this.end( false );
                return;
            } else if( activePlayers.size() == 1 && !this.getGameConfig().isReturnToStartAtGround() ) {
                gameState = GameState.STOPPED;
                if( this.aPlayerHasLost ) this.playerWon( activePlayers.get(0) ); // Can only win if at least one player has lost. If everyone else quits the game just ends.
                this.end( false );
                return;
            }
        }
        player.playSound( player.getLocation(), plugin.getLoadedConfig().getLeaveSound(), 1, 1 );
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
            this.aPlayerHasLost = true;
            PlayerInfo payerInfoLost = players.get( player.getUniqueId() );
            player.getWorld().playSound( player.getLocation(), plugin.getLoadedConfig().getLoseSound(), plugin.getLoadedConfig().getWorldSoundRange() / 16f, 1 );
            if( payerInfoLost != null ) payerInfoLost.setLost();
            player.setVelocity( Game.VECTOR0 );
            player.setFlying( false );
            player.teleport( gameConfig.getLossSpawn() );
            player.setGameMode( plugin.getLoadedConfig().getLostGameMode() );
            plugin.messageSender( player, Messages.LOST, gameConfig.getDisplayName() );
            player.getInventory().clear();
            plugin.getDataHandler().logLoss( player.getUniqueId() );
            player.playSound( player.getLocation(), plugin.getLoadedConfig().getLoseSound(), 1, 1 );

            String message = plugin.formatMessage( Messages.PLAYER_LOST, gameConfig.getDisplayName(), "%player%", player.getDisplayName() );
            for( PlayerInfo playerInfo : players.values() ) {
                plugin.messageSender( playerInfo.getPlayer(), message );
            }

            List<Player> activePlayers = getActivePlayerList();
            if( activePlayers.size() <= 1 )
            {
                gameState = GameState.STOPPED;
                if( activePlayers.size() == 1 )
                {
                    this.playerWon( activePlayers.get(0) );
                }
                this.end( false );
            }
        }
    }

    public void setPlayerWon( Player player )
    {
        if( gameState == GameState.RUNNING )
        {
            gameState = GameState.STOPPED;
            this.playerWon( player );
            this.end( false );
        }
    }

    private void playerWon( Player player )
    {
        String message = plugin.formatMessage( Messages.WINNER, gameConfig.getDisplayName(), "%player%", player.getDisplayName() );
        player.playSound( player.getLocation(), plugin.getLoadedConfig().getWinSound(), 0.75f, 1 );
        player.playSound( plugin.getLoadedConfig().getMainLobbySpawn(), plugin.getLoadedConfig().getWinSound(), 0.75f, 1 );     // The player is about to be teleported to the mainLobbySpawn.
        for( PlayerInfo playerInfo : players.values() ) {
            plugin.messageSender( playerInfo.getPlayer(), message );
        }
        plugin.getDataHandler().logWin( player.getUniqueId() );
        plugin.getLoadedConfig().runWinCommands( player.getUniqueId(), this.getGameConfig().getDisplayName(), this.getGameConfig().getWinCommands() );
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

    public boolean isOnCoolDown( Player player )
    {
        PlayerInfo playerInfo = players.get( player.getUniqueId() );
        return playerInfo != null && playerInfo.isOnCoolDown();
    }

    public void coolDown( Player player )
    {
        PlayerInfo playerInfo = players.get( player.getUniqueId() );
        if( playerInfo != null ) {
            playerInfo.setCoolDown( plugin.getLoadedConfig().getCoolDown() );
        }
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

    private void giveBoostSticks( Player player )
    {
        List<BoostStick> sticks = plugin.getLoadedConfig().getBoostSticksAllowedForPlayer( player );
        if( sticks != null && !sticks.isEmpty() ) {
            int firstStickSlot = -1;
            int slot = 0;
            for( BoostStick stick : sticks ) {
                for( ; slot < 36; ++slot ) {
                    if( player.getInventory().getItem( slot ) == null || player.getInventory().getItem( slot ).getType() == Material.AIR ) {
                        player.getInventory().setItem( slot, stick.create() );
                        if( firstStickSlot == -1 ) firstStickSlot = slot;
                        break;
                    }
                }
            }
            if( firstStickSlot >= 0 && firstStickSlot <= 8 ) player.getInventory().setHeldItemSlot( firstStickSlot );
        }
    }

    public boolean start()
    {
        if( queueTask != null && !queueTask.isCancelled() ) queueTask.cancel();
        gameState = GameState.RUNNING;
        this.aPlayerHasLost = false;
        for( PlayerInfo playerInfo : players.values() )
        {
            playerInfo.resetCoolDown();
            playerInfo.getPlayer().setVelocity( Game.VECTOR0 );
            playerInfo.getPlayer().setFlying( false );
            playerInfo.getPlayer().teleport( gameConfig.getStartSpawn() );
            playerInfo.getPlayer().setGameMode( plugin.getLoadedConfig().getPlayingGameMode() );
            plugin.messageSender( playerInfo.getPlayer(), Messages.GAME_STARTED, gameConfig.getDisplayName() );

            playerInfo.setActive();
            plugin.getDataHandler().logGame( playerInfo.getPlayer().getUniqueId() );
        }
        gameConfig.getStartSpawn().getWorld().playSound( gameConfig.getStartSpawn(), plugin.getLoadedConfig().getStartSound(), plugin.getLoadedConfig().getWorldSoundRange() / 16f, 1 );
        return true;
    }

    public boolean end( boolean noAutoQueue )
    {
//        boolean wasQueuing = ( gameState == GameState.QUEUING || ( queueTask != null && !queueTask.isCancelled() ) );
        if( queueTask != null && !queueTask.isCancelled() ) queueTask.cancel();  // Cancel the queue in case it's queuing rather than running.
        gameState = GameState.STOPPED;
        for( PlayerInfo playerInfo : players.values() )
        {
            playerInfo.resetCoolDown();
            playerInfo.getPlayer().setVelocity( Game.VECTOR0 );
            playerInfo.getPlayer().setFlying( false );
            playerInfo.getPlayer().teleport( plugin.getLoadedConfig().getMainLobbySpawn() );
            playerInfo.getPlayer().setGameMode( plugin.getLoadedConfig().getLobbyGameMode() );
            plugin.messageSender( playerInfo.getPlayer(), Messages.GAME_ENDED, gameConfig.getDisplayName() );
            playerInfo.getPlayer().getInventory().clear();
            ItemStack instructionBook = plugin.getLoadedConfig().createInstructionBook();
            playerInfo.getPlayer().getInventory().setItem( 8, instructionBook );
            ItemStack mainGuiItem = plugin.getLoadedConfig().getGuiButtonConfig( "main" ).create();
            playerInfo.getPlayer().getInventory().setItem( 0, mainGuiItem );
            playerInfo.getPlayer().getInventory().setHeldItemSlot( 0 );

            plugin.getGameManager().removePlayer( playerInfo.getPlayer() );
        }
        players.clear();
        this.aPlayerHasLost = false;
        if( gameConfig.isAutoQueue() /*&& !wasQueuing*/ && !noAutoQueue ) this.startQueuing();
        return true;
    }
}
