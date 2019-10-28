package nz.pmme.Boost;

import nz.pmme.Boost.Exceptions.GameAlreadyExistsException;
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
public class Command_Boost implements CommandExecutor
{
    private Main plugin;

    private static final String boostEnabledMessage = ChatColor.GREEN + "Boost enabled";
    private static final String boostDisabledMessage = ChatColor.GRAY + "Boost disabled";
    private static final String boostConfigReloaded = ChatColor.GREEN + "Boost config reloaded";
    private static final String[] boostCommandUsage = {
            ChatColor.DARK_AQUA + "Boost command usage:",
            ChatColor.WHITE + "/boost join Arena1" + ChatColor.DARK_AQUA + " - Join the named game.",
            ChatColor.WHITE + "/boost leave" + ChatColor.DARK_AQUA + " - Leave your current game.",
            ChatColor.WHITE + "/boost creategame Arena1" + ChatColor.DARK_AQUA + " - Create a game with the specified name.",
            ChatColor.WHITE + "/boost setground Arena1 [yPos]" + ChatColor.DARK_AQUA + " - Set the losing ground level for the named game.",
            ChatColor.WHITE + "/boost setstart" + ChatColor.DARK_AQUA + " - Set the start spawn position.",
            ChatColor.WHITE + "/boost setlobby" + ChatColor.DARK_AQUA + " - Set the lobby spawn position.",
            ChatColor.WHITE + "/boost setloss" + ChatColor.DARK_AQUA + " - Set the spawn position for losing players.",
            ChatColor.WHITE + "/boost setspread" + ChatColor.DARK_AQUA + " - Set the spread from the start spawn position.",
            ChatColor.WHITE + "/boost queue Arena1" + ChatColor.DARK_AQUA + " - Start queuing the named game.",
            ChatColor.WHITE + "/boost start Arena1" + ChatColor.DARK_AQUA + " - Force-start the named game.",
            ChatColor.WHITE + "/boost end Arena1" + ChatColor.DARK_AQUA + " - Force-end the named game.",
            ChatColor.WHITE + "/boost cleargames" + ChatColor.DARK_AQUA + " - End all games.",
            ChatColor.WHITE + "/boost status" + ChatColor.DARK_AQUA + " - Get a debug list of games and current player.",
            ChatColor.WHITE + "/boost reload" + ChatColor.DARK_AQUA + " - End all games, reload config, and re-initialise.",
            ChatColor.WHITE + "/boost on" + ChatColor.DARK_AQUA + " - Turn the Boost game and controls on.",
            ChatColor.WHITE + "/boost off" + ChatColor.DARK_AQUA + " - Turn the Boost game and controls off."
    };
    private static final String boostNoConsoleMessage = "This Boost command must be used by an active player.";
    private static final String boostGameAlreadyExists = ChatColor.RED + "That Boost game already exists.";
    private static final String boostGameDoesNotExist = ChatColor.RED + "No Boost game with that name.";
    private static final String boostGamesCleared = ChatColor.DARK_AQUA + "All Boost games cleared.";

    public Command_Boost( Main plugin ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        if( args.length == 0 )
        {
            sender.sendMessage( plugin.isBoostEnabled() ? boostEnabledMessage : boostDisabledMessage );
            displayCommandUsage( sender );
            return true;
        }
        else
        {
            String boostCommand = args[0].toLowerCase();
            switch( boostCommand )
            {
                case "on":
                    sender.sendMessage( boostEnabledMessage );
                    plugin.enableBoost();
                    return true;

                case "off":
                    sender.sendMessage( boostDisabledMessage );
                    plugin.disableBoost();
                    return true;

                case "reload":
                    plugin.getGameManager().clearAllGames();
                    plugin.getLoadedConfig().reload();
                    plugin.getGameManager().createConfiguredGames();
                    sender.sendMessage( boostConfigReloaded );
                    return true;

                case "cleargames":
                    plugin.getGameManager().clearAllGames();
                    sender.sendMessage( boostGamesCleared );
                    return true;

                case "creategame":
                    if( args.length == 2 ) {
                        try {
                            plugin.getGameManager().createNewGame( args[1] );
                        } catch( GameAlreadyExistsException e ) {
                            sender.sendMessage( boostGameAlreadyExists );
                        }
                        return true;
                    }
                    break;

                case "setground":
                    if( args.length > 1 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            sender.sendMessage( boostGameDoesNotExist );
                            return true;
                        }
                        if( args.length == 3 ) {
                            game.getGameConfig().setGroundLevel( Integer.valueOf( args[2] ) );
                            return true;
                        } else {
                            if( sender instanceof Player ) {
                                game.getGameConfig().setGroundLevel( ((Player)sender).getLocation().getBlockY() );
                            } else {
                                displayNoConsoleMessage( sender );
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
                            sender.sendMessage( boostGameDoesNotExist );
                            return true;
                        }
                        if( args.length == 6 )
                        {
                            World world = plugin.getServer().getWorld( args[2] );
                            if( world != null ) {
                                Location spawn = new Location( world, Double.valueOf( args[3] ), Double.valueOf( args[4] ), Double.valueOf( args[5] ) );
                                if( spawn != null ) {
                                    switch( boostCommand ) {
                                        case "setstart": game.getGameConfig().setStartSpawn( spawn ); break;
                                        case "setlobby": game.getGameConfig().setLobbySpawn( spawn ); break;
                                        case "setloss": game.getGameConfig().setLossSpawn( spawn ); break;
                                    }
                                } else {
                                    sender.sendMessage( ChatColor.RED + "Failed to create location to set spawn." );
                                }
                            } else {
                                sender.sendMessage( ChatColor.RED + "Failed to find world named " + args[2] );
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
                            sender.sendMessage( boostGameDoesNotExist );
                            return true;
                        }
                        game.getGameConfig().setSpawnSpread( Integer.valueOf( args[2] ) );
                        return true;
                    }
                    break;

                case "join":
                    if( !plugin.isBoostEnabled() ) {
                        sender.sendMessage( boostDisabledMessage );
                        return true;
                    }
                    if( sender instanceof Player ) {
                        if( args.length == 2 ) {
                            plugin.getGameManager().joinGame( (Player)sender, args[1] );
                            return true;
                        }
                    } else {
                        displayNoConsoleMessage( sender );
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
                        displayNoConsoleMessage( sender );
                        return true;
                    }
                    break;

                case "queue":
                    if( !plugin.isBoostEnabled() ) {
                        sender.sendMessage( boostDisabledMessage );
                        return true;
                    }
                    if( args.length == 2 ) {
                        plugin.getGameManager().queueGame( args[1], sender );
                        return true;
                    }
                    break;

                case "start":
                    if( !plugin.isBoostEnabled() ) {
                        sender.sendMessage( boostDisabledMessage );
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
        displayCommandUsage( sender );
        return true;
    }

    protected void displayCommandUsage( CommandSender sender )
    {
        sender.sendMessage( boostCommandUsage );
    }

    protected void displayNoConsoleMessage( CommandSender sender )
    {
        sender.sendMessage( boostNoConsoleMessage );
    }
}
