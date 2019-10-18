package nz.pmme.Boost;

import nz.pmme.Boost.Exceptions.GameAlreadyExistsException;
import org.bukkit.ChatColor;
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
            ChatColor.WHITE + "/boost start Arena1" + ChatColor.DARK_AQUA + " - Force-start the named game.",
            ChatColor.WHITE + "/boost end Arena1" + ChatColor.DARK_AQUA + " - Force-end the named game.",
            ChatColor.WHITE + "/boost on" + ChatColor.DARK_AQUA + " - Turn the Boost game and controls on.",
            ChatColor.WHITE + "/boost off" + ChatColor.DARK_AQUA + " - Turn the Boost game and controls off."
    };
    private static final String boostNoConsoleMessage = "This Boost command must be used by an active player.";
    private static final String boostGameAlreadyExists = ChatColor.RED + "That Boost game already exists.";
    private static final String boostGameDoesNotExist = ChatColor.RED + "No Boost game with that name.";
    private static final String boostGameAlreadyRunning = ChatColor.RED + "That Boost game is already running.";
    private static final String boostGameNotRunning = ChatColor.RED + "That Boost game is not running.";

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
                    plugin.loadConfig();
                    sender.sendMessage( boostConfigReloaded );
                    return true;

                case "creategame":
                    if( args.length == 2 ) {
                        try {
                            plugin.getGameManager().createGame( args[1] );
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
                            int newGround = Integer.valueOf( args[2] );
                            game.setGroundLevel( newGround );
                            return true;
                        } else {
                            if( sender instanceof Player ) {
                                game.setGroundLevel( ((Player)sender).getLocation().getBlockY() );
                            } else {
                                displayNoConsoleMessage( sender );
                            }
                            return true;
                        }
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

                case "start":
                    if( !plugin.isBoostEnabled() ) {
                        sender.sendMessage( boostDisabledMessage );
                        return true;
                    }
                    if( args.length == 2 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            sender.sendMessage( boostGameDoesNotExist );
                            return true;
                        }
                        if( game.isRunning() ) {
                            sender.sendMessage( boostGameAlreadyRunning );
                            return true;
                        }
                        game.start();
                        return true;
                    }
                    break;

                case "end":
                    if( args.length == 2 ) {
                        Game game = plugin.getGameManager().getGame( args[1] );
                        if( game == null ) {
                            sender.sendMessage( boostGameDoesNotExist );
                            return true;
                        }
                        if( !game.isRunning() ) {
                            sender.sendMessage( boostGameNotRunning );
                            return true;
                        }
                        game.end();
                        return true;
                    }
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
