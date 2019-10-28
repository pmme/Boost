package nz.pmme.Boost;

import nz.pmme.Boost.Config.GameConfig;
import nz.pmme.Boost.Exceptions.GameAlreadyExistsException;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;


public class GameManager
{
    private Main plugin;
    private Map< String, Game > games = new HashMap<>();
    private Map< UUID, Game > playersInGames = new HashMap<>();

    private static final String boostAlreadyInGameMessage = ChatColor.RED + "You are already in a Boost game.";
    private static final String boostNotInGameMessage = ChatColor.RED + "You are not in a Boost game.";
    private static final String boostGameDoesNotExistMessage = ChatColor.RED + "No Boost game with that name.";
    private static final String boostGameAlreadyQueuingMessage = ChatColor.RED + "That Boost game is already queuing.";
    private static final String boostGameAlreadyRunningMessage = ChatColor.RED + "That Boost game is already running.";
    private static final String boostGameNotRunningMessage = ChatColor.RED + "That Boost game is not running.";

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

    public Game getGame( String gameName )
    {
        return games.get( gameName.toLowerCase() );
    }

    public List<Game> getGames() {
        return new ArrayList<>( games.values() );
    }

    public void joinGame( Player player, String gameName )
    {
        if( this.isPlaying( player ) ) {
            player.sendMessage( boostAlreadyInGameMessage );
            return;
        }
        Game game = this.getGame( gameName );
        if( game == null ) {
            player.sendMessage( boostGameDoesNotExistMessage );
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
            player.sendMessage( boostNotInGameMessage );
            return;
        }
        this.removePlayer( player );
        game.leave( player );
    }

    public void removePlayer( Player player )
    {
        playersInGames.remove( player.getUniqueId() );
    }

    public void queueGame( String gameName, CommandSender sender )
    {
        Game game = this.getGame( gameName );
        if( game == null ) {
            sender.sendMessage( boostGameDoesNotExistMessage );
            return;
        }
        if( game.isRunning() ) {
            sender.sendMessage( boostGameAlreadyRunningMessage );
            return;
        }
        if( game.isQueuing() ) {
            sender.sendMessage( boostGameAlreadyQueuingMessage );
            return;
        }
        game.startQueuing();
    }

    public void startGame( String gameName, CommandSender sender )
    {
        Game game = this.getGame( gameName );
        if( game == null ) {
            sender.sendMessage( boostGameDoesNotExistMessage );
            return;
        }
        if( game.isRunning() ) {
            sender.sendMessage( boostGameAlreadyRunningMessage );
            return;
        }
        game.start();
    }

    public void endGame( String gameName, CommandSender sender )
    {
        Game game = this.getGame( gameName );
        if( game == null ) {
            sender.sendMessage( boostGameDoesNotExistMessage );
            return;
        }
        if( !game.isRunning() && !game.isQueuing() ) {
            sender.sendMessage( boostGameNotRunningMessage );
            return;
        }
        game.end( false );
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
        for( Game game : plugin.getGameManager().getGames() )
        {
            String message = "- " + game.getGameConfig().getDisplayName() + ChatColor.RESET + ", which is " + game.getGameStateText() + ", with " + game.getPlayerCount() + " players.";
            if( game.isQueuing() ) message += " " + game.getRemainingQueueTime() + "s before start.";
            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', message ) );
        }
        if( sender instanceof Player ) {
            Game game = plugin.getGameManager().getPlayersGame( (Player)sender );
            if( game != null ) {
                String message = "You are " + game.getPlayerStateText( (Player)sender ) + " in game " + game.getGameConfig().getDisplayName() + ChatColor.RESET + ", which is " + game.getGameStateText() + " with " + game.getPlayerCount() + " players.";
                sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', message ) );
            } else {
                sender.sendMessage( "Not currently in a game." );
            }
        }
    }
}
