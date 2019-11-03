package nz.pmme.Boost;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Exceptions.GameAlreadyExistsException;
import nz.pmme.Boost.Game.Game;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by paul on 24-Apr-16.
 */
public class Command_Boost implements CommandExecutor
{
    private Main plugin;

    public Command_Boost( Main plugin ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        if( args.length == 0 )
        {
            sender.sendMessage( plugin.isBoostEnabled() ? plugin.getLoadedConfig().getMessage( Messages.BOOST_ENABLED ) : plugin.getLoadedConfig().getMessage( Messages.BOOST_DISABLED ) );
            sender.sendMessage( plugin.getLoadedConfig().getCommandUsage() );
            return true;
        }
        else
        {
            String boostCommand = args[0].toLowerCase();
            switch( boostCommand )
            {
                case "on":
                    sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.BOOST_ENABLED ) );
                    plugin.enableBoost();
                    return true;

                case "off":
                    sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.BOOST_DISABLED ) );
                    plugin.disableBoost();
                    return true;

                case "reload":
                    plugin.getGameManager().clearAllGames();
                    plugin.getLoadedConfig().reload();
                    plugin.getGameManager().createConfiguredGames();
                    sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.CONFIG_RELOADED ) );
                    return true;

                case "cleargames":
                    plugin.getGameManager().clearAllGames();
                    sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.GAMES_CLEARED ) );
                    return true;

                case "creategame":
                    if( args.length == 2 ) {
                        try {
                            plugin.getGameManager().createNewGame( args[1] );
                        } catch( GameAlreadyExistsException e ) {
                            sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.GAME_ALREADY_EXISTS ).replaceAll( "%game%", args[1] ) );
                        }
                        return true;
                    }
                    break;

                case "setground":
                    if( args.length > 1 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.GAME_DOES_NOT_EXIST ).replaceAll( "%game%", args[1] ) );
                            return true;
                        }
                        if( args.length == 3 ) {
                            game.getGameConfig().setGroundLevel( Integer.valueOf( args[2] ) );
                            return true;
                        } else {
                            if( sender instanceof Player ) {
                                game.getGameConfig().setGroundLevel( ((Player)sender).getLocation().getBlockY() );
                            } else {
                                sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.NO_CONSOLE ) );
                            }
                            return true;
                        }
                    }
                    break;

                case "setstart":
                case "setlobby":
                case "setloss":
                    if( args.length > 1 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.GAME_DOES_NOT_EXIST ).replaceAll( "%game%", args[1] ) );
                            return true;
                        }
                        if( args.length == 6 )
                        {
                            World world = plugin.getServer().getWorld( args[2] );
                            if( world != null ) {
                                Location spawn = new Location( world, Double.valueOf( args[3] ), Double.valueOf( args[4] ), Double.valueOf( args[5] ) );
                                switch( boostCommand ) {
                                    case "setstart": game.getGameConfig().setStartSpawn( spawn ); break;
                                    case "setlobby": game.getGameConfig().setLobbySpawn( spawn ); break;
                                    case "setloss": game.getGameConfig().setLossSpawn( spawn ); break;
                                }
                            } else {
                                sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.FAILED_TO_FIND_WORLD ).replaceAll( "%world%", args[2] ) );
                            }
                            return true;
                        }
                        else if( args.length == 2 )
                        {
                            if( sender instanceof Player ) {
                                switch( boostCommand ) {
                                    case "setstart": game.getGameConfig().setStartSpawn( ((Player)sender).getLocation() ); break;
                                    case "setlobby": game.getGameConfig().setLobbySpawn( ((Player)sender).getLocation() ); break;
                                    case "setloss": game.getGameConfig().setLossSpawn( ((Player)sender).getLocation() ); break;
                                }
                            }
                            return true;
                        }
                    }
                    break;

                case "setspread":
                    if( args.length == 3 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.GAME_DOES_NOT_EXIST ).replaceAll( "%game%", args[1] ) );
                            return true;
                        }
                        game.getGameConfig().setSpawnSpread( Integer.valueOf( args[2] ) );
                        return true;
                    }
                    break;

                case "join":
                    if( !plugin.isBoostEnabled() ) {
                        sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.BOOST_DISABLED ) );
                        return true;
                    }
                    if( sender instanceof Player ) {
                        if( args.length == 2 ) {
                            plugin.getGameManager().joinGame( (Player)sender, args[1] );
                            return true;
                        }
                    } else {
                        sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.NO_CONSOLE ) );
                        return true;
                    }
                    break;

                case "leave":
                    if( sender instanceof Player ) {
                        if( args.length == 1 ) {
                            plugin.getGameManager().leaveGame( (Player)sender );
                            return true;
                        }
                    } else {
                        sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.NO_CONSOLE ) );
                        return true;
                    }
                    break;

                case "queue":
                    if( !plugin.isBoostEnabled() ) {
                        sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.BOOST_DISABLED ) );
                        return true;
                    }
                    if( args.length == 2 ) {
                        plugin.getGameManager().queueGame( args[1], sender );
                        return true;
                    }
                    break;

                case "start":
                    if( !plugin.isBoostEnabled() ) {
                        sender.sendMessage( plugin.getLoadedConfig().getMessage( Messages.BOOST_DISABLED ) );
                        return true;
                    }
                    if( args.length == 2 ) {
                        plugin.getGameManager().startGame( args[1], sender );
                        return true;
                    }
                    break;

                case "end":
                    if( args.length == 2 ) {
                        plugin.getGameManager().endGame( args[1], sender );
                        return true;
                    }
                    break;

                case "status":
                    plugin.getGameManager().displayStatus( sender );
                    return true;
            }
        }
        sender.sendMessage( plugin.getLoadedConfig().getCommandUsage() );
        return true;
    }
}
