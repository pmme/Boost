package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Exceptions.GameAlreadyExistsException;
import nz.pmme.Boost.Exceptions.GameDisplayNameMustMatchConfigurationException;
import nz.pmme.Boost.Exceptions.GameDoesNotExistException;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.OfflinePlayer;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Created by paul on 24-Apr-16.
 */
public class Commands implements CommandExecutor
{
    private Main plugin;
    static final List<String> trueValues = java.util.Arrays.asList( "on", "true", "t", "1", "yes", "y" );

    public Commands( Main plugin ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        if( args.length == 0 )
        {
            plugin.messageSender( sender, plugin.isBoostEnabled() ? Messages.BOOST_ENABLED : Messages.BOOST_DISABLED );
            sender.sendMessage( plugin.getLoadedConfig().getCommandUsage( sender.hasPermission( "boost.admin" ) ) );
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
                    plugin.reload();
                    plugin.messageSender( sender, Messages.CONFIG_RELOADED );
                    return true;

                case "language":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        plugin.getConfig().set( "language", args[1].toLowerCase() );
                        plugin.saveConfig();
                        plugin.reload();
                        plugin.messageSender( sender, Messages.CONFIG_RELOADED );
                        return true;
                    }
                    break;

                case "addgameworld":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        World world = plugin.getServer().getWorld( args[1] );
                        if( world != null ) {
                            plugin.getLoadedConfig().addGameWorld( world.getName() );
                            plugin.messageSender( sender, Messages.ADDED_GAME_WORLD, "%world%", world.getName() );
                        } else {
                            plugin.messageSender( sender, Messages.FAILED_TO_FIND_WORLD, "%world%", args[1] );
                        }
                        return true;
                    }
                    if ( args.length == 1 ) {
                        if( sender instanceof Player ) {
                            World world = ((Player)sender).getLocation().getWorld();
                            plugin.getLoadedConfig().addGameWorld( world.getName() );
                            plugin.messageSender( sender, Messages.ADDED_GAME_WORLD, "%world%", world.getName() );
                        } else {
                            plugin.messageSender( sender, Messages.NO_CONSOLE );
                        }
                        return true;
                    }
                    break;

                case "removegameworld":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        World world = plugin.getServer().getWorld( args[1] );
                        if( world != null ) {
                            if( plugin.getLoadedConfig().isGameWorld( world.getName() ) ) {
                                plugin.getLoadedConfig().delGameWorld( world.getName() );
                                plugin.messageSender( sender, Messages.REMOVED_GAME_WORLD, "%world%", world.getName() );
                            } else {
                                plugin.messageSender( sender, Messages.NOT_A_GAME_WORLD, "%world%", world.getName() );
                            }
                        } else {
                            plugin.messageSender( sender, Messages.FAILED_TO_FIND_WORLD, "%world%", args[1] );
                        }
                        return true;
                    }
                    if ( args.length == 1 ) {
                        if( sender instanceof Player ) {
                            World world = ((Player)sender).getLocation().getWorld();
                            if( plugin.getLoadedConfig().isGameWorld( world.getName() ) ) {
                                plugin.getLoadedConfig().delGameWorld( world.getName() );
                                plugin.messageSender( sender, Messages.REMOVED_GAME_WORLD, "%world%", world.getName() );
                            } else {
                                plugin.messageSender( sender, Messages.NOT_A_GAME_WORLD, "%world%", world.getName() );
                            }
                        } else {
                            plugin.messageSender( sender, Messages.NO_CONSOLE );
                        }
                        return true;
                    }
                    break;

                case "setmainlobby":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 5 ) {
                        World world = plugin.getServer().getWorld( args[1] );
                        if( world != null ) {
                            Location spawn = new Location( world, Double.parseDouble( args[2] ), Double.parseDouble( args[3] ), Double.parseDouble( args[4] ) );
                            plugin.getLoadedConfig().setMainLobbySpawn( spawn );
                            plugin.messageSender( sender, Messages.MAIN_SPAWN_SET );
                        } else {
                            plugin.messageSender( sender, Messages.FAILED_TO_FIND_WORLD, "%world%", args[1] );
                        }
                        return true;
                    }
                    if( args.length == 1 ) {
                        if( sender instanceof Player ) {
                            if( !plugin.isInGameWorld(sender) ) {
                                plugin.messageSender( sender, Messages.NOT_IN_GAME_WORLD );
                                return true;
                            }
                            plugin.getLoadedConfig().setMainLobbySpawn( ( (Player)sender ).getLocation() );
                            plugin.messageSender( sender, Messages.MAIN_SPAWN_SET );
                        } else {
                            plugin.messageSender( sender, Messages.NO_CONSOLE );
                        }
                        return true;
                    }
                    break;

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
                            plugin.messageSender( sender, Messages.GAME_CREATED, args[1] );
                        } catch( GameAlreadyExistsException e ) {
                            plugin.messageSender( sender, Messages.GAME_ALREADY_EXISTS, args[1] );
                        }
                        return true;
                    }
                    break;

                case "deletegame":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 || ( args.length == 3 && !args[2].equalsIgnoreCase( "confirm" ) ) ) {
                        plugin.messageSender( sender, Messages.MUST_TYPE_CONFIRM, args[1] );
                        return true;
                    }
                    if( args.length == 3 ) {
                        try {
                            plugin.getGameManager().deleteGame( args[1] );
                            plugin.messageSender( sender, Messages.GAME_DELETED, args[1] );
                        } catch( GameDoesNotExistException e ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                        }
                        return true;
                    }
                    break;

                case "showgameconfig":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                            return true;
                        }
                        game.getGameConfig().displayConfig( sender );
                        return true;
                    }
                    break;

                case "setdisplayname":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 3 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                            return true;
                        }
                        try {
                            game.getGameConfig().setDisplayName( args[2] );
                            plugin.messageSender( sender, Messages.DISPLAY_NAME_SET, game.getGameConfig().getDisplayName(), "%gameconfig%", game.getGameConfig().getName() );
                        } catch( GameDisplayNameMustMatchConfigurationException e ) {
                            plugin.messageSender( sender, Messages.DISPLAY_NAME_MISMATCH, "%gameconfig%", game.getGameConfig().getName() );
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
                            int y = Integer.parseInt( args[2] );
                            game.getGameConfig().setGroundLevel(y);
                            plugin.messageSender( sender, Messages.GROUND_SET, game.getGameConfig().getDisplayName(), "%y%", String.valueOf(y) );

                            Location spawn = game.getGameConfig().getStartSpawn();
                            if( spawn != null && spawn.getBlockY() <= y ) {
                                String message = plugin.formatMessage( Messages.GROUND_HIGHER, game.getGameConfig().getDisplayName(), "%y%", String.valueOf( spawn.getBlockY() ) );
                                plugin.messageSender( sender, message.replaceAll( "%ground%", String.valueOf( y ) ) );
                            }
                            return true;
                        }
                        if ( args.length == 2 ) {
                            if( sender instanceof Player ) {
                                if( !plugin.isInGameWorld(sender) ) {
                                    plugin.messageSender( sender, Messages.NOT_IN_GAME_WORLD );
                                    return true;
                                }
                                int y = ((Player)sender).getLocation().getBlockY();
                                game.getGameConfig().setGroundLevel(y);
                                plugin.messageSender( sender, Messages.GROUND_SET, game.getGameConfig().getDisplayName(), "%y%", String.valueOf(y) );

                                Location spawn = game.getGameConfig().getStartSpawn();
                                if( spawn != null && spawn.getBlockY() <= y ) {
                                    String message = plugin.formatMessage( Messages.GROUND_HIGHER, game.getGameConfig().getDisplayName(), "%y%", String.valueOf( spawn.getBlockY() ) );
                                    plugin.messageSender( sender, message.replaceAll( "%ground%", String.valueOf( y ) ) );
                                }
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
                        Location spawn = null;
                        if( args.length == 6 ) {
                            World world = plugin.getServer().getWorld( args[2] );
                            if( world != null ) {
                                spawn = new Location( world, Double.parseDouble( args[3] ), Double.parseDouble( args[4] ), Double.parseDouble( args[5] ) );
                            } else {
                                plugin.messageSender( sender, Messages.FAILED_TO_FIND_WORLD, "%world%", args[2] );
                                return true;
                            }
                        }
                        if( args.length == 2 ) {
                            if( sender instanceof Player ) {
                                if( !plugin.isInGameWorld(sender) ) {
                                    plugin.messageSender( sender, Messages.NOT_IN_GAME_WORLD );
                                    return true;
                                }
                                spawn = ((Player)sender).getLocation();
                            } else {
                                plugin.messageSender( sender, Messages.NO_CONSOLE );
                                return true;
                            }
                        }
                        if( spawn != null ) {
                            switch( boostCommand ) {
                                case "setstart":
                                    game.getGameConfig().setStartSpawn( spawn );
                                    plugin.messageSender( sender, Messages.START_SPAWN_SET, game.getGameConfig().getDisplayName() );

                                    if( spawn.getBlockY() <= game.getGameConfig().getGroundLevel() ) {
                                        String message = plugin.formatMessage( Messages.GROUND_HIGHER, game.getGameConfig().getDisplayName(), "%y%", String.valueOf( spawn.getBlockY() ) );
                                        plugin.messageSender( sender, message.replaceAll( "%ground%", String.valueOf( game.getGameConfig().getGroundLevel() ) ) );
                                    }
                                    break;
                                case "setlobby":
                                    game.getGameConfig().setLobbySpawn( spawn );
                                    plugin.messageSender( sender, Messages.LOBBY_SPAWN_SET, game.getGameConfig().getDisplayName() );
                                    break;
                                case "setloss":
                                    game.getGameConfig().setLossSpawn( spawn );
                                    plugin.messageSender( sender, Messages.LOSS_SPAWN_SET, game.getGameConfig().getDisplayName() );
                                    break;
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
                        int spread = Integer.parseInt( args[2] );
                        game.getGameConfig().setSpawnSpread( spread );
                        plugin.messageSender( sender, Messages.SPREAD_SET, game.getGameConfig().getDisplayName(), "%spread%", String.valueOf( spread ) );
                        return true;
                    }
                    break;

                case "setminplayers":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 3 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                            return true;
                        }
                        int minPlayers = Integer.parseInt( args[2] );
                        game.getGameConfig().setMinPlayers( minPlayers );
                        plugin.messageSender( sender, Messages.MIN_PLAYERS_SET, game.getGameConfig().getDisplayName(), "%count%", String.valueOf( minPlayers ) );
                        return true;
                    }
                    break;

                case "setmaxplayers":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 3 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                            return true;
                        }
                        int maxPlayers = Integer.parseInt( args[2] );
                        game.getGameConfig().setMaxPlayers( maxPlayers );
                        plugin.messageSender( sender, Messages.MAX_PLAYERS_SET, game.getGameConfig().getDisplayName(), "%count%", String.valueOf( maxPlayers ) );
                        return true;
                    }
                    break;

                case "autoqueue":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 3 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                            return true;
                        }
                        boolean autoQueue = trueValues.contains( args[2].toLowerCase() );
                        game.getGameConfig().setAutoQueue( autoQueue );
                        plugin.messageSender( sender, autoQueue ? Messages.AUTO_QUEUE_ENABLED : Messages.AUTO_QUEUE_DISABLED, game.getGameConfig().getDisplayName() );
                        return true;
                    }
                    break;

                case "setcountdown":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 3 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                            return true;
                        }
                        int countdown = Integer.parseInt( args[2] );
                        game.getGameConfig().setCountdown( countdown );
                        plugin.messageSender( sender, Messages.COUNTDOWN_SET, game.getGameConfig().getDisplayName(), "%count%", String.valueOf( countdown ) );
                        return true;
                    }
                    break;

                case "setannouncement":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 3 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                            return true;
                        }
                        int countdownAnnouncementTime = Integer.parseInt( args[2] );
                        game.getGameConfig().setCountdownAnnounceTime( countdownAnnouncementTime );
                        plugin.messageSender( sender, Messages.COUNTDOWN_ANNOUNCEMENT_SET, game.getGameConfig().getDisplayName(), "%count%", String.valueOf( countdownAnnouncementTime ) );
                        return true;
                    }
                    break;

                case "join":
                    if( !plugin.hasPermission( sender, "boost.cmd", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( !plugin.hasPermission( sender, "boost.join", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( !plugin.isBoostEnabled() ) {
                        plugin.messageSender( sender, Messages.BOOST_DISABLED );
                        return true;
                    }
                    if( sender instanceof Player ) {
                        if( !plugin.isInGameWorld(sender) ) {
                            plugin.messageSender( sender, Messages.NOT_IN_GAME_WORLD );
                            return true;
                        }
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
                    if( !plugin.hasPermission( sender, "boost.cmd", Messages.NO_PERMISSION_CMD ) ) return true;
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
                        if( args[1].equals( "*" ) ) {
                            for( Game game : plugin.getGameManager().getGames() ) {
                                plugin.getGameManager().queueGame( game, sender );
                            }
                        } else {
                            plugin.getGameManager().queueGame( args[1], sender );
                        }
                        return true;
                    }
                    break;

                case "start":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( !plugin.isBoostEnabled() ) {
                        plugin.messageSender( sender, Messages.BOOST_DISABLED );
                        return true;
                    }
                    if( args.length == 1 ) {
                        if( sender instanceof Player ) {
                            Game game = plugin.getGameManager().getPlayersGame( (Player)sender );
                            if( game == null ) break;
                            plugin.getGameManager().startGame( game, sender );
                            return true;
                        }
                    }
                    if( args.length == 2 ) {
                        plugin.getGameManager().startGame( args[1], sender );
                        return true;
                    }
                    break;

                case "end":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 1 ) {
                        if( sender instanceof Player ) {
                            Game game = plugin.getGameManager().getPlayersGame( (Player)sender );
                            if( game == null ) break;
                            plugin.getGameManager().endGame( game, sender );
                            return true;
                        }
                    }
                    if( args.length == 2 ) {
                        if( args[1].equals( "*" ) ) {
                            for( Game game : plugin.getGameManager().getGames() ) {
                                plugin.getGameManager().endGame( game, sender );
                            }
                        } else {
                            plugin.getGameManager().endGame( args[1], sender );
                        }
                        return true;
                    }
                    break;

                case "stop":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 1 ) {
                        if( sender instanceof Player ) {
                            Game game = plugin.getGameManager().getPlayersGame( (Player)sender );
                            if( game == null ) break;
                            plugin.getGameManager().stopGame( game, sender );
                            return true;
                        }
                    }
                    if( args.length == 2 ) {
                        if( args[1].equals( "*" ) ) {
                            for( Game game : plugin.getGameManager().getGames() ) {
                                plugin.getGameManager().stopGame( game, sender );
                            }
                        } else {
                            plugin.getGameManager().stopGame( args[1], sender );
                        }
                        return true;
                    }
                    break;

                case "top":
                    if( !plugin.hasPermission( sender, "boost.cmd", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        String period = args[1].toLowerCase();
                        if( period.equalsIgnoreCase( "daily" ) ) {
                            plugin.getGameManager().displayLeaderBoard( sender, StatsPeriod.DAILY );
                        } else if( period.equalsIgnoreCase( "weekly" ) ) {
                            plugin.getGameManager().displayLeaderBoard( sender, StatsPeriod.WEEKLY );
                        } else if( period.equalsIgnoreCase( "monthly" ) ) {
                            plugin.getGameManager().displayLeaderBoard( sender, StatsPeriod.MONTHLY );
                        } else break;
                        return true;
                    } else if( args.length == 1 ) {
                        plugin.getGameManager().displayLeaderBoard( sender, StatsPeriod.TOTAL );
                        return true;
                    }
                    break;

                case "stats":
                    if( !plugin.hasPermission( sender, "boost.cmd", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        for( Player player : plugin.getServer().getOnlinePlayers() ) {
                            if( player.getDisplayName().equalsIgnoreCase( args[1] ) || ChatColor.stripColor( player.getDisplayName() ).equalsIgnoreCase( args[1] ) || player.getName().equalsIgnoreCase( args[1] ) ) {
                                plugin.getGameManager().displayPlayerStats( sender, player );
                                return true;
                            }
                        }
                        for( OfflinePlayer player : plugin.getServer().getOfflinePlayers() ) {
                            if( player.getName().equalsIgnoreCase( args[1] ) ) {
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
                        if( args[1].equals( "*" ) ) {
                            plugin.getGameManager().deletePlayerStats( sender, null );
                        } else {
                            for( Player player : plugin.getServer().getOnlinePlayers() ) {
                                if( player.getDisplayName().equalsIgnoreCase( args[1] ) || ChatColor.stripColor( player.getDisplayName() ).equalsIgnoreCase( args[1] ) || player.getName().equalsIgnoreCase( args[1] ) ) {
                                    plugin.getGameManager().deletePlayerStats( sender, player );
                                    return true;
                                }
                            }
                            for( OfflinePlayer player : plugin.getServer().getOfflinePlayers() ) {
                                if( player.getName().equalsIgnoreCase( args[1] ) ) {
                                    plugin.getGameManager().deletePlayerStats( sender, player );
                                    return true;
                                }
                            }
                            plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
                        }
                        return true;
                    }
                    break;

                case "status":
                    if( !plugin.hasPermission( sender, "boost.cmd", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( !plugin.hasPermission( sender, "boost.status", Messages.NO_PERMISSION_CMD ) ) return true;
                    plugin.getGameManager().displayStatus( sender );
                    return true;

                case "setsign":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( sender instanceof Player ) {
                        Player player = (Player)sender;
                        Block targetBlock = player.getTargetBlock( null,5 );
                        if( targetBlock == null || !(targetBlock.getState() instanceof Sign ) ) {
                            plugin.messageSender( sender, Messages.NOT_FACING_A_SIGN );
                            return true;
                        }
                        if( args.length > 1 ) {
                            String args1lower = args[1].toLowerCase();
                            if( args.length != 3 && args1lower.equals( "join" ) ) break;
                            Sign sign = (Sign)( targetBlock.getState() );
                            int line = 0;
                            sign.setLine( line++, plugin.getLoadedConfig().getSignTitle() );
                            switch( args1lower ) {
                                case "join":
                                    sign.setLine( line++, plugin.getLoadedConfig().getSignJoin() );
                                    String gameNameForSign = null;
                                    if( args[2].contains( "&" ) ) {
                                        gameNameForSign = ChatColor.translateAlternateColorCodes( '&', args[2] );
                                    } else {
                                        for( String game : plugin.getGameManager().getGameNames() ) {
                                            if( ChatColor.stripColor( game ).equalsIgnoreCase( args[2] ) ) {
                                                gameNameForSign = game;
                                            }
                                        }
                                        if( gameNameForSign == null ) gameNameForSign = args[2];
                                    }
                                    sign.setLine( line++, gameNameForSign );
                                    break;
                                case "leave":
                                    sign.setLine( line++, plugin.getLoadedConfig().getSignLeave() );
                                    break;
                                case "status":
                                    sign.setLine( line++, plugin.getLoadedConfig().getSignStatus() );
                                    break;
                                case "stats":
                                    sign.setLine( line++, plugin.getLoadedConfig().getSignStats() );
                                    break;
                                case "top":
                                    sign.setLine( line++, plugin.getLoadedConfig().getSignTop() );
                                    if( args.length == 3 ) {
                                        switch( args[2].toLowerCase() ) {
                                            case "daily":
                                                sign.setLine( line++, plugin.getLoadedConfig().getSignDaily() );
                                                break;
                                            case "weekly":
                                                sign.setLine( line++, plugin.getLoadedConfig().getSignWeekly() );
                                                break;
                                            case "monthly":
                                                sign.setLine( line++, plugin.getLoadedConfig().getSignMonthly() );
                                                break;
                                        }
                                    }
                                    break;
                                default:
                                    String arg1Coloured = ChatColor.translateAlternateColorCodes( '&', args[1] );
                                    sign.setLine( line++, arg1Coloured );
                                    plugin.messageSender( sender, Messages.SIGN_COMMAND_NOT_RECOGNISED, "%command%", arg1Coloured );
                                    break;
                            }
                            for( ; line < 4; ++line ) sign.setLine( line, "" );
                            sign.update();
                            plugin.messageSender( sender, Messages.SIGN_SET );
                            return true;
                        }
                    } else {
                        plugin.messageSender( sender, Messages.NO_CONSOLE );
                        return true;
                    }
                    break;

                case "build":
                    if( !plugin.hasPermission( sender, "boost.admin", Messages.NO_PERMISSION_CMD ) ) return true;
                    if( args.length == 2 ) {
                        for( Player player : plugin.getServer().getOnlinePlayers() ) {
                            if( player.getDisplayName().equalsIgnoreCase( args[1] ) || ChatColor.stripColor( player.getDisplayName() ).equalsIgnoreCase( args[1] ) || player.getName().equalsIgnoreCase( args[1] ) ) {
                                plugin.setBuilder( player.getUniqueId() );
                                player.setGameMode( plugin.getLoadedConfig().getBuildGameMode() );
                                plugin.messageSender( player, Messages.BUILD_ENABLED, "", "%player%", player.getName() );
                                plugin.messageSender( sender, Messages.BUILD_ENABLED, "", "%player%", player.getName() );
                                return true;
                            }
                        }
                        plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
                        return true;
                    } else if( sender instanceof Player ) {
                        plugin.setBuilder( ((Player)sender).getUniqueId() );
                        ((Player)sender).setGameMode( plugin.getLoadedConfig().getBuildGameMode() );
                        plugin.messageSender( sender, Messages.BUILD_ENABLED, "", "%player%", sender.getName() );
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
                                player.setGameMode( plugin.getLoadedConfig().getLobbyGameMode() );
                                plugin.messageSender( player, Messages.BUILD_DISABLED, "", "%player%", player.getName() );
                                plugin.messageSender( sender, Messages.BUILD_DISABLED, "", "%player%", player.getName() );
                                return true;
                            }
                        }
                        plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
                        return true;
                    } else if( sender instanceof Player ) {
                        plugin.setNotBuilder( ((Player)sender).getUniqueId() );
                        ((Player)sender).setGameMode( plugin.getLoadedConfig().getLobbyGameMode() );
                        plugin.messageSender( sender, Messages.BUILD_DISABLED, "", "%player%", sender.getName() );
                        return true;
                    } else {
                        plugin.messageSender( sender, Messages.NO_CONSOLE );
                        return true;
                    }
            }
        }
        sender.sendMessage( plugin.getLoadedConfig().getCommandUsage( sender.hasPermission( "boost.admin" ) ) );
        return true;
    }
}
