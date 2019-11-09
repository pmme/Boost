package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Exceptions.GameAlreadyExistsException;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Created by paul on 24-Apr-16.
 */
public class Commands implements CommandExecutor
{
    private Main plugin;

    public Commands( Main plugin ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        if( args.length == 0 )
        {
            plugin.messageSender( sender, plugin.isBoostEnabled() ? Messages.BOOST_ENABLED : Messages.BOOST_DISABLED );
            sender.sendMessage( plugin.getLoadedConfig().getCommandUsage() );
            return true;
        }
        else
        {
            String boostCommand = args[0].toLowerCase();
            switch( boostCommand )
            {
                case "on":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    plugin.messageSender( sender, Messages.BOOST_ENABLED );
                    plugin.enableBoost();
                    return true;

                case "off":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    plugin.messageSender( sender, Messages.BOOST_DISABLED );
                    plugin.disableBoost();
                    return true;

                case "reload":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    plugin.getGameManager().clearAllGames();
                    plugin.getLoadedConfig().reload();
                    plugin.getGameManager().createConfiguredGames();
                    plugin.messageSender( sender, Messages.CONFIG_RELOADED );
                    return true;

                case "cleargames":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    plugin.getGameManager().clearAllGames();
                    plugin.messageSender( sender, Messages.GAMES_CLEARED );
                    return true;

                case "creategame":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        try {
                            plugin.getGameManager().createNewGame( args[1] );
                        } catch( GameAlreadyExistsException e ) {
                            plugin.messageSender( sender, Messages.GAME_ALREADY_EXISTS, args[1] );
                        }
                        return true;
                    }
                    break;

                case "setground":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length > 1 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                            return true;
                        }
                        if( args.length == 3 ) {
                            game.getGameConfig().setGroundLevel( Integer.valueOf( args[2] ) );
                            return true;
                        } else {
                            if( sender instanceof Player ) {
                                game.getGameConfig().setGroundLevel( ((Player)sender).getLocation().getBlockY() );
                            } else {
                                plugin.messageSender( sender, Messages.NO_CONSOLE );
                            }
                            return true;
                        }
                    }
                    break;

                case "setstart":
                case "setlobby":
                case "setloss":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length > 1 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
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
                                plugin.messageSender( sender, Messages.FAILED_TO_FIND_WORLD, args[1],"%world%", args[2] );
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
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 3 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                            return true;
                        }
                        game.getGameConfig().setSpawnSpread( Integer.valueOf( args[2] ) );
                        return true;
                    }
                    break;

                case "join":
                    if( !plugin.hasPermission( sender, "boost.join", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( !plugin.isBoostEnabled() ) {
                        plugin.messageSender( sender, Messages.BOOST_DISABLED );
                        return true;
                    }
                    if( sender instanceof Player ) {
                        if( args.length == 2 ) {
                            plugin.getGameManager().joinGame( (Player)sender, args[1] );
                            return true;
                        }
                    } else {
                        plugin.messageSender( sender, Messages.NO_CONSOLE );
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
                        plugin.messageSender( sender, Messages.NO_CONSOLE );
                        return true;
                    }
                    break;

                case "queue":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( !plugin.isBoostEnabled() ) {
                        plugin.messageSender( sender, Messages.BOOST_DISABLED );
                        return true;
                    }
                    if( args.length == 2 ) {
                        plugin.getGameManager().queueGame( args[1], sender );
                        return true;
                    }
                    break;

                case "start":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( !plugin.isBoostEnabled() ) {
                        plugin.messageSender( sender, Messages.BOOST_DISABLED );
                        return true;
                    }
                    if( args.length == 2 ) {
                        plugin.getGameManager().startGame( args[1], sender );
                        return true;
                    }
                    break;

                case "end":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        plugin.getGameManager().endGame( args[1], sender );
                        return true;
                    }
                    break;

                case "top":
                    plugin.getGameManager().displayLeaderBoard( sender );
                    return true;

                case "stats":
                    if( args.length == 2 ) {
                        for( Player player : plugin.getServer().getOnlinePlayers() ) {
                            if( player.getDisplayName().equalsIgnoreCase( args[1] ) || ChatColor.stripColor( player.getDisplayName() ).equalsIgnoreCase( args[1] ) || player.getName().equalsIgnoreCase( args[1] ) ) {
                                plugin.getGameManager().displayPlayerStats( sender, player );
                                return true;
                            }
                        }
                        plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
                        return true;
                    } else if( sender instanceof Player ) {
                        plugin.getGameManager().displayPlayerStats( sender, (Player)sender );
                        return true;
                    } else {
                        plugin.messageSender( sender, Messages.NO_CONSOLE );
                        return true;
                    }

                case "delstats":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        for( Player player : plugin.getServer().getOnlinePlayers() ) {
                            if( player.getDisplayName().equalsIgnoreCase( args[1] ) || ChatColor.stripColor( player.getDisplayName() ).equalsIgnoreCase( args[1] ) || player.getName().equalsIgnoreCase( args[1] ) ) {
                                plugin.getGameManager().deletePlayerStats( sender, player );
                                return true;
                            }
                        }
                        plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
                        return true;
                    } else if( sender instanceof Player ) {
                        plugin.getGameManager().deletePlayerStats( sender, (Player)sender );
                        return true;
                    } else {
                        plugin.messageSender( sender, Messages.NO_CONSOLE );
                        return true;
                    }

                case "status":
                    if( !plugin.hasPermission( sender, "boost.cmd", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( !plugin.hasPermission( sender, "boost.status", Messages.NO_PERMISSION_CMD ) ) return true;
                    plugin.getGameManager().displayStatus( sender );
                    return true;

                case "build":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        for( Player player : plugin.getServer().getOnlinePlayers() ) {
                            if( player.getDisplayName().equalsIgnoreCase( args[1] ) || ChatColor.stripColor( player.getDisplayName() ).equalsIgnoreCase( args[1] ) || player.getName().equalsIgnoreCase( args[1] ) ) {
                                plugin.setBuilder( player.getUniqueId() );
                                return true;
                            }
                        }
                        plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
                        return true;
                    } else if( sender instanceof Player ) {
                        plugin.setBuilder( ((Player)sender).getUniqueId() );
                        return true;
                    } else {
                        plugin.messageSender( sender, Messages.NO_CONSOLE );
                        return true;
                    }

                case "nobuild":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        for( Player player : plugin.getServer().getOnlinePlayers() ) {
                            if( player.getDisplayName().equalsIgnoreCase( args[1] ) || ChatColor.stripColor( player.getDisplayName() ).equalsIgnoreCase( args[1] ) || player.getName().equalsIgnoreCase( args[1] ) ) {
                                plugin.setNotBuilder( player.getUniqueId() );
                                return true;
                            }
                        }
                        plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
                        return true;
                    } else if( sender instanceof Player ) {
                        plugin.setNotBuilder( ((Player)sender).getUniqueId() );
                        return true;
                    } else {
                        plugin.messageSender( sender, Messages.NO_CONSOLE );
                        return true;
                    }
            }
        }
        sender.sendMessage( plugin.getLoadedConfig().getCommandUsage() );
        return true;
    }
}
