package nz.pmme.Boost.Game;

import nz.pmme.Boost.Config.GameConfig;
import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.GameState;
import nz.pmme.Boost.Enums.GameType;
import nz.pmme.Boost.Main;
import org.bukkit.Location;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

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

        if( this.gameConfig.isAutoQueue() && this.gameConfig.isProperlyConfigured() ) {
            if( this.gameConfig.getGameType() != GameType.PARKOUR ) {
                this.startQueuing();
            } else {
                this.start();
            }
        }
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

    public int getActivePlayerCount()
    {
        int activePlayers = 0;
        for( PlayerInfo playerInfo : players.values() ) {
            if( playerInfo.isActive() ) ++activePlayers;
        }
        return activePlayers;
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
                if( getPlayerCount() < gameConfig.getMinPlayers() || getPlayerCount() == 0 ) {
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
        if( this.gameConfig.getGameType() != GameType.PARKOUR && gameState != GameState.QUEUING )
        {
            switch( gameState )
            {
                case RUNNING:
                    plugin.messageSender( player, Messages.NO_JOIN_GAME_RUNNING, gameConfig.getDisplayName() );
                    break;
                case ENDING:
                case STOPPED:
                    plugin.messageSender( player, Messages.NO_JOIN_GAME_STOPPED, gameConfig.getDisplayName() );
                    break;
            }
            return false;
        }
        else if( this.gameConfig.getGameType() == GameType.PARKOUR && gameState != GameState.RUNNING )
        {
            plugin.messageSender( player, Messages.NO_JOIN_GAME_STOPPED, gameConfig.getDisplayName() );
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

        PlayerInfo playerInfo = new PlayerInfo( player );
        if( gameConfig.getGameType() == GameType.PARKOUR ) playerInfo.setActive();
        players.put( player.getUniqueId(), playerInfo );
        player.setVelocity( Game.VECTOR0 );
        player.setFlying( false );
        if( gameConfig.getGameType() != GameType.PARKOUR ) {
            player.teleport( gameConfig.getLobbySpawn() );
            player.setGameMode( plugin.getLoadedConfig().getLobbyGameMode() );
        } else {
            player.teleport( gameConfig.getStartSpawn() );
            player.setGameMode( plugin.getLoadedConfig().getPlayingGameMode() );
        }
        plugin.messageSender( player, Messages.JOIN_GAME, gameConfig.getDisplayName() );
        if( playersRequired > 0 ) plugin.messageSender( player, messagePlayerCount );
        plugin.getDataHandler().addPlayer( player.getUniqueId(), player.getDisplayName(), null );
        plugin.getDataHandler().addPlayer( player.getUniqueId(), player.getDisplayName(), gameConfig.getName() );
        if( gameConfig.getGameType() == GameType.PARKOUR ) {
            plugin.getDataHandler().logGame( player.getUniqueId(), player.getDisplayName(), null );
            plugin.getDataHandler().logGame( player.getUniqueId(), player.getDisplayName(), gameConfig.getName() );
        }
        player.playSound( player.getLocation(), plugin.getLoadedConfig().getJoinSound(), 1, 1 );

        plugin.getInventoryManager().giveBoostSticks( player );

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
            plugin.getInventoryManager().removeBoostItems( player );
            plugin.getInventoryManager().giveInstructionBook( player );
            plugin.getInventoryManager().giveMainGuiItem( player, true );
        }
        if( gameState == GameState.RUNNING )
        {
            List<Player> activePlayers = getActivePlayerList();
            if( activePlayers.size() == 0 ) {
                this.end( false );
                return;
            } else if( activePlayers.size() == 1 && gameConfig.getGameType() != GameType.PARKOUR ) {
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
            plugin.getInventoryManager().removeBoostItems( player );
            plugin.getDataHandler().logLoss( player.getUniqueId(), player.getDisplayName(), null );
            plugin.getDataHandler().logLoss( player.getUniqueId(), player.getDisplayName(), gameConfig.getName() );
            player.playSound( player.getLocation(), plugin.getLoadedConfig().getLoseSound(), 1, 1 );

            String message = plugin.formatMessage( Messages.PLAYER_LOST, gameConfig.getDisplayName(), "%player%", player.getDisplayName() );
            for( PlayerInfo playerInfo : players.values() ) {
                plugin.messageSender( playerInfo.getPlayer(), message );
            }

            List<Player> activePlayers = getActivePlayerList();
            if( activePlayers.size() <= 1 )
            {
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
        if( gameState == GameState.RUNNING ) {
            if( gameConfig.getGameType() != GameType.PARKOUR ) {
                this.playerWon( player );
                this.end( false );
            } else {
                this.playerWon( player );

                PlayerInfo playerInfo = players.get( player.getUniqueId() );
                if( playerInfo != null ) playerInfo.resetCoolDown();
                new BukkitRunnable()
                {
                    @Override
                    public void run() {
                        player.setVelocity( Game.VECTOR0 );
                        player.setFlying( false );
                        player.teleport( plugin.getLoadedConfig().getMainLobbySpawn() );
                        player.setGameMode( plugin.getLoadedConfig().getLobbyGameMode() );
                        plugin.getInventoryManager().removeBoostItems( player );
                        plugin.getInventoryManager().giveInstructionBook( player );
                        plugin.getInventoryManager().giveMainGuiItem( player, true );
                        players.remove( player.getUniqueId() );
                        plugin.getGameManager().removePlayer( player );
                    }
                }.runTaskLater( plugin, ( 20 * plugin.getConfig().getLong( "physics.game_end_cooldown" ) / 1000 ) );
            }
        }
    }

    private void playerWon( Player player )
    {
        PlayerInfo winningPlayerInfo = players.get( player.getUniqueId() );
        long time = winningPlayerInfo != null ? winningPlayerInfo.getTimePlaying() : 0L;

        String message = plugin.formatMessage( Messages.WINNER, gameConfig.getDisplayName(), "%player%", player.getDisplayName() );
        player.playSound( player.getLocation(), plugin.getLoadedConfig().getWinSound(), 0.75f, 1 );
        if( gameConfig.getGameType() != GameType.PARKOUR ) {
            for( PlayerInfo playerInfo : players.values() ) {
                plugin.messageSender( playerInfo.getPlayer(), message );
            }
        } else {
            plugin.messageSender( player, message );
        }
        plugin.getDataHandler().logWin( player.getUniqueId(), player.getDisplayName(), null );
        plugin.getDataHandler().logWin( player.getUniqueId(), player.getDisplayName(), gameConfig.getName() );
        plugin.getDataHandler().logTimeToWin( player.getUniqueId(), player.getDisplayName(), time, gameConfig.getName() );
        plugin.getLoadedConfig().runWinCommands( player.getUniqueId(), this.getGameConfig().getDisplayName(), this.getGameConfig().getWinCommands() );
    }

    public boolean isActiveInGame( Player player )
    {
        if( gameState != GameState.RUNNING ) return false;
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

    public boolean isEnding()
    {
        return gameState == GameState.ENDING;
    }

    public String getGameStateText()
    {
        return gameState.toString();
    }

    public boolean start()
    {
        if( queueTask != null && !queueTask.isCancelled() ) queueTask.cancel();
        gameState = GameState.RUNNING;
        this.aPlayerHasLost = false;

        long coolDownEndMillis;
        if( plugin.getLoadedConfig().getGameStartBoostDelay() > 0L )
        {
            coolDownEndMillis = System.currentTimeMillis() + plugin.getLoadedConfig().getGameStartBoostDelay() * 1000L;
            String barTitle = plugin.formatMessage( Messages.START_DELAY_TITLE, "", "%time%", Long.toString( plugin.getLoadedConfig().getGameStartBoostDelay() ) );
            BossBar bossBar = plugin.getServer().createBossBar( barTitle, plugin.getLoadedConfig().getGameStartBoostDelayBarColor(), plugin.getLoadedConfig().getGameStartBoostDelayBarStyle() );
            bossBar.setProgress( 1.0 );
            for( PlayerInfo playerInfo : players.values() ) bossBar.addPlayer( playerInfo.getPlayer() );
            bossBar.setVisible( true );
            new BukkitRunnable()
            {
                long boostDelayCountdown = plugin.getLoadedConfig().getGameStartBoostDelay();
                @Override
                public void run() {
                    --boostDelayCountdown;
                    if( boostDelayCountdown >= 0L ) {
                        bossBar.setProgress( boostDelayCountdown / (double)plugin.getLoadedConfig().getGameStartBoostDelay() );
                    } else {
                        this.cancel();
                        bossBar.removeAll();
                        bossBar.setVisible( false );
                    }
                }
            }.runTaskTimer( plugin, 0L, 20L/*ticksPerSecond*/ );
        }
        else coolDownEndMillis = 0L;

        for( PlayerInfo playerInfo : players.values() )
        {
            playerInfo.setCoolDownEnd( coolDownEndMillis );
            playerInfo.getPlayer().setVelocity( Game.VECTOR0 );
            playerInfo.getPlayer().setFlying( false );
            playerInfo.getPlayer().removePotionEffect( PotionEffectType.GLOWING );
            Location startSpawn = gameConfig.getStartSpawn();
            playerInfo.getPlayer().teleport( startSpawn );
            playerInfo.getPlayer().setGameMode( plugin.getLoadedConfig().getPlayingGameMode() );
            plugin.messageSender( playerInfo.getPlayer(), Messages.GAME_STARTED, gameConfig.getDisplayName() );

            playerInfo.setActive();
            plugin.getDataHandler().logGame( playerInfo.getPlayer().getUniqueId(), playerInfo.getPlayer().getDisplayName(), null );
            plugin.getDataHandler().logGame( playerInfo.getPlayer().getUniqueId(), playerInfo.getPlayer().getDisplayName(), gameConfig.getName() );
        }

        List<Location> startSpawns = gameConfig.getConfiguredStartSpawns();
        for( Location startSpawn : startSpawns ) {
            startSpawn.getWorld().playSound( startSpawn, plugin.getLoadedConfig().getStartSound(), plugin.getLoadedConfig().getWorldSoundRange() / 16f, 1 );
        }

        return true;
    }

    public boolean end( boolean noAutoQueue )
    {
//        boolean wasQueuing = ( gameState == GameState.QUEUING || ( queueTask != null && !queueTask.isCancelled() ) );
        if( queueTask != null && !queueTask.isCancelled() ) queueTask.cancel();  // Cancel the queue in case it's queuing rather than running.
        gameState = GameState.ENDING;
        Game thisGame = this;
        BukkitRunnable endRunnable = new BukkitRunnable()
        {
            @Override
            public void run() {
                thisGame.gameState = GameState.STOPPED;
                for( PlayerInfo playerInfo : thisGame.players.values() )
                {
                    playerInfo.resetCoolDown();
                    playerInfo.getPlayer().setVelocity( Game.VECTOR0 );
                    playerInfo.getPlayer().setFlying( false );
                    playerInfo.getPlayer().teleport( plugin.getLoadedConfig().getMainLobbySpawn() );
                    playerInfo.getPlayer().setGameMode( plugin.getLoadedConfig().getLobbyGameMode() );
                    plugin.messageSender( playerInfo.getPlayer(), Messages.GAME_ENDED, thisGame.gameConfig.getDisplayName() );
                    plugin.getInventoryManager().removeBoostItems( playerInfo.getPlayer() );
                    plugin.getInventoryManager().giveInstructionBook( playerInfo.getPlayer() );
                    plugin.getInventoryManager().giveMainGuiItem( playerInfo.getPlayer(), true );

                    plugin.getGameManager().removePlayer( playerInfo.getPlayer() );
                }
                thisGame.players.clear();
                thisGame.aPlayerHasLost = false;
                if( thisGame.gameConfig.isAutoQueue() /*&& !wasQueuing*/ && !noAutoQueue ) {
                    if( thisGame.gameConfig.getGameType() != GameType.PARKOUR ) {
                        thisGame.startQueuing();
                    } else {
                        thisGame.start();
                    }
                }
            }
        };
        endRunnable.runTaskLater( plugin, ( 20L * (long)( (double)plugin.getConfig().getLong( "physics.game_end_cooldown" ) / 1000.0 ) ) );
        return true;
    }
}
