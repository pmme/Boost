package nz.pmme.Boost.Game;

import nz.pmme.Boost.Config.GameConfig;
import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Exceptions.GameAlreadyExistsException;
import nz.pmme.Boost.Exceptions.GameDoesNotExistException;
import nz.pmme.Boost.Main;
import nz.pmme.Boost.Data.PlayerStats;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;


public class GameManager
{
    private Main plugin;
    private Map< String, Game > games = new HashMap<>();
    private Map< UUID, Game > playersInGames = new HashMap<>();


    public GameManager( Main plugin )
    {
        this.plugin = plugin;
    }

    public void createConfiguredGames()
    {
        for( GameConfig gameConfig : plugin.getLoadedConfig().getGameList() )
        {
            this.createGame( gameConfig );
        }
    }

    public Game createNewGame( String gameName ) throws GameAlreadyExistsException
    {
        if( games.containsKey( gameName.toLowerCase() ) ) {
            throw new GameAlreadyExistsException();
        }
        GameConfig gameConfig = plugin.getLoadedConfig().createNewGameConfig( gameName );
        return this.createGame( gameConfig );
    }

    public Game createGame( GameConfig gameConfig )
    {
        Game game = new Game( plugin, gameConfig );
        games.put( gameConfig.getName().toLowerCase(), game );
        return game;
    }

    public void deleteGame( String gameName ) throws GameDoesNotExistException
    {
        String gameNameLower = gameName.toLowerCase();
        Game game = games.get( gameNameLower );
        if( game == null ) {
            throw new GameDoesNotExistException();
        }
        game.end( true );
        games.remove( gameNameLower );
        plugin.getLoadedConfig().deleteGameConfig( game.getGameConfig() );
    }

    public Game getGame( String gameName )
    {
        return games.get( gameName.toLowerCase() );
    }

    public List<Game> getGames()
    {
        List<Game> gameList = new ArrayList<>( games.values() );
        gameList.sort(null);
        return gameList;
    }

    public List<String> getGameNames()
    {
        List<String> gameNames = new ArrayList<>();
        List<Game> gamesList = this.getGames();
        for( Game game : gamesList ) {
            gameNames.add( ChatColor.translateAlternateColorCodes( '&', game.getGameConfig().getDisplayName() ) );
        }
        return gameNames;
    }

    public void joinGame( Player player, String gameName )
    {
        if( this.isPlaying( player ) ) {
            plugin.messageSender( player, Messages.ALREADY_IN_GAME, this.getPlayersGame( player ).getGameConfig().getDisplayName() );
            return;
        }
        Game game = this.getGame( gameName );
        if( game == null ) {
            plugin.messageSender( player, Messages.GAME_DOES_NOT_EXIST, gameName );
            return;
        }
        if( game.join( player ) ) {
            playersInGames.put( player.getUniqueId(), game );
        }
    }

    public Game getPlayersGame( Player player )
    {
        return playersInGames.get( player.getUniqueId() );
    }

    public boolean isPlaying( Player player )
    {
        return playersInGames.containsKey( player.getUniqueId() );
    }

    public boolean activeInSameGame( Player player1, Player player2 )
    {
        Game game1 = this.getPlayersGame( player1 );
        if( game1 == null ) return false;
        Game game2 = this.getPlayersGame( player2 );
        if( game2 == null ) return false;
        return ( game1 == game2 && game1.isActiveInGame( player1 ) && game2.isActiveInGame( player2 ) );
    }

    public void leaveGame( Player player )
    {
        Game game = this.getPlayersGame( player );
        if( game == null ) {
            plugin.messageSender( player, Messages.NOT_IN_GAME );
            return;
        }
        this.removePlayer( player );
        game.leave( player );
    }

    public void removePlayer( Player player )
    {
        playersInGames.remove( player.getUniqueId() );
    }

    public void queueGame( Game game, CommandSender sender )
    {
        if( game.isRunning() ) {
            plugin.messageSender( sender, Messages.GAME_ALREADY_RUNNING, game.getGameConfig().getDisplayName() );
            return;
        }
        if( game.isQueuing() ) {
            plugin.messageSender( sender, Messages.GAME_ALREADY_QUEUING, game.getGameConfig().getDisplayName() );
            return;
        }
        if( !game.getGameConfig().isProperlyConfigured() ) {
            plugin.messageSender( sender, Messages.GAME_NOT_CONFIGURED, game.getGameConfig().getDisplayName() );
            return;
        }
        plugin.messageSender( sender, Messages.STARTED_QUEUING, game.getGameConfig().getDisplayName() );
        game.startQueuing();
    }

    public void queueGame( String gameName, CommandSender sender )
    {
        Game game = this.getGame( gameName );
        if( game == null ) {
            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, gameName );
            return;
        }
        this.queueGame( game, sender );
    }

    public void startGame( Game game, CommandSender sender )
    {
        if( game.isRunning() ) {
            plugin.messageSender( sender, Messages.GAME_ALREADY_RUNNING, game.getGameConfig().getDisplayName() );
            return;
        }
        if( !game.getGameConfig().isProperlyConfigured() ) {
            plugin.messageSender( sender, Messages.GAME_NOT_CONFIGURED, game.getGameConfig().getDisplayName() );
            return;
        }
        game.start();
        plugin.messageSender( sender, Messages.GAME_STARTED, game.getGameConfig().getDisplayName() );
    }

    public void startGame( String gameName, CommandSender sender )
    {
        Game game = this.getGame( gameName );
        if( game == null ) {
            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, gameName );
            return;
        }
        this.startGame( game, sender );
    }

    public void endGame( Game game, CommandSender sender )
    {
        if( !game.isRunning() && !game.isQueuing() ) {
            plugin.messageSender( sender, Messages.GAME_NOT_RUNNING, game.getGameConfig().getDisplayName() );
            return;
        }
        game.end( false );
        plugin.messageSender( sender, Messages.GAME_ENDED, game.getGameConfig().getDisplayName() );
    }

    public void endGame( String gameName, CommandSender sender )
    {
        Game game = this.getGame( gameName );
        if( game == null ) {
            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, gameName );
            return;
        }
        this.endGame( game, sender );
    }

    public void stopGame( Game game, CommandSender sender )
    {
        if( !game.isRunning() && !game.isQueuing() ) {
            plugin.messageSender( sender, Messages.GAME_NOT_RUNNING, game.getGameConfig().getDisplayName() );
            return;
        }
        game.end( true );
        plugin.messageSender( sender, Messages.GAME_ENDED, game.getGameConfig().getDisplayName() );
    }

    public void stopGame( String gameName, CommandSender sender )
    {
        Game game = this.getGame( gameName );
        if( game == null ) {
            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, gameName );
            return;
        }
        this.stopGame( game, sender );
    }

    public void clearAllGames()
    {
        for( Game game : games.values() ) {
            game.end( true );
        }
        games.clear();
        playersInGames.clear();
    }

    public void displayStatus( CommandSender sender )
    {
        sender.sendMessage( "Boost games:" );
        for( Game game : this.getGames() )
        {
            String message = "- " + game.getGameConfig().getDisplayName() + ChatColor.RESET + " is " + game.getGameStateText() + " with " + game.getPlayerCount() + " players";
            if( game.getPlayerCount() < game.getGameConfig().getMinPlayers() ) {
                message += " " + ( game.getGameConfig().getMinPlayers() - game.getPlayerCount() ) + " more required";
            } else if( game.isQueuing() ) {
                message += " " + game.getRemainingQueueTime() + "s before start";
            }
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', message ) );
        }
        if( sender instanceof Player ) {
            Game game = plugin.getGameManager().getPlayersGame( (Player)sender );
            if( game != null ) {
                String message = "You are " + game.getPlayerStateText( (Player)sender ) + " in game " + game.getGameConfig().getDisplayName();
                sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', message ) );
            } else {
                plugin.messageSender( sender, Messages.NOT_IN_GAME );
            }
        }
    }

    public void displayPlayerStats( CommandSender sender, OfflinePlayer player )
    {
        List<String> playerStatsMessage = new ArrayList<>();
        PlayerStats playerStats = plugin.getDataHandler().queryPlayerStats( StatsPeriod.TOTAL, player.getUniqueId() );
        if( playerStats == null ) {
            plugin.messageSender( sender, Messages.PLAYER_NO_STATS, "", "%player%", player.getName() );
        } else {
            for( String string : plugin.getLoadedConfig().getPlayerStatsTemplate() ) {
                playerStatsMessage.add( ChatColor.translateAlternateColorCodes( '&', string
                        .replaceAll( "%player%", player.getName() )
                        .replaceAll( "%games%", String.valueOf( playerStats.getGames() ) )
                        .replaceAll( "%wins%", String.valueOf( playerStats.getWins() ) )
                        .replaceAll( "%losses%", String.valueOf( playerStats.getLosses() ) )
                        .replaceAll( "%rank%", String.valueOf( playerStats.getRank() ) )
                ) );
            }
            sender.sendMessage( playerStatsMessage.toArray( new String[0] ) );
            if( sender instanceof Player ) {
                ((Player)sender).playSound( ((Player)sender).getLocation(), plugin.getLoadedConfig().getStatsSound(), 1, 1 );
            }
        }
    }

    public void displayLeaderBoard( CommandSender sender, StatsPeriod statsPeriod )
    {
        List<String> leaderBoardMessage = new ArrayList<>();
        leaderBoardMessage.add( ChatColor.translateAlternateColorCodes( '&', plugin.getLoadedConfig().getMessage( Messages.LEADER_BOARD_TITLE ) ) );
        List<PlayerStats> playerStatsList = plugin.getDataHandler().queryLeaderBoard( statsPeriod );
        if( playerStatsList != null ) {
            int index = 1;
            for( PlayerStats playerStats : playerStatsList ) {
                leaderBoardMessage.add( ChatColor.translateAlternateColorCodes( '&', plugin.getLoadedConfig().getMessage( Messages.LEADER_BOARD_ENTRY )
                        .replaceAll( "%index%", String.valueOf( index++ ) )
                        .replaceAll( "%player%", playerStats.getName() )
                        .replaceAll( "%games%", String.valueOf( playerStats.getGames() ) )
                        .replaceAll( "%wins%", String.valueOf( playerStats.getWins() ) )
                        .replaceAll( "%losses%", String.valueOf( playerStats.getLosses() ) )
                ) );
            }
        }
        sender.sendMessage( leaderBoardMessage.toArray( new String[0] ) );
        if( sender instanceof Player ) {
            ((Player)sender).playSound( ((Player)sender).getLocation(), plugin.getLoadedConfig().getLeaderSound(), 1, 1 );
        }
    }

    public void deletePlayerStats( CommandSender sender, OfflinePlayer player )
    {
        for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
            plugin.getDataHandler().deleteStats( statsPeriod, player != null ? player.getUniqueId() : null );
        }
        plugin.messageSender( sender, Messages.DELETED_STATS );
    }
}
