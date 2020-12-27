package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Enums.Place;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandShowWinCommands extends AbstractSubCommand
{
    public SubCommandShowWinCommands( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 1 )
        {
            for( StatsPeriod statsPeriod : StatsPeriod.values() )
            {
                if( statsPeriod == StatsPeriod.TOTAL ) break;
                for( Place place : Place.values() )
                {
                    List<String> winCommands = plugin.getLoadedConfig().getWinCommands( statsPeriod, place );
                    if( winCommands == null || winCommands.isEmpty() ) {
                        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5Boost&r win commands for " + statsPeriod.toString() + " " + place.toString() + "&r : &3None configured" ) );
                    } else {
                        sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5Boost&r win commands for " + statsPeriod.toString() + " " + place.toString() + "&r :" ) );
                        for( String winCommand : winCommands ) {
                            sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|   &3- '" ) + winCommand + ChatColor.translateAlternateColorCodes( '&',"&3'." ) );
                        }
                    }
                }
            }
            return true;
        }
        else if( args.length == 2 )
        {
            Game game = plugin.getGameManager().getGame( args[1] );
            if( game == null ) {
                plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                return true;
            }
            List<String> winCommands = game.getGameConfig().getWinCommands();
            if( winCommands == null || winCommands.isEmpty() ) {
                sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5Boost&r win commands for " + game.getGameConfig().getDisplayName() + "&r : &3None configured" ) );
            } else {
                sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5Boost&r win commands for " + game.getGameConfig().getDisplayName() + "&r :" ) );
                for( String winCommand : winCommands ) {
                    sender.sendMessage( ChatColor.translateAlternateColorCodes( '&', "&5|   &3- '" ) + winCommand + ChatColor.translateAlternateColorCodes( '&',"&3'." ) );
                }
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getGameManager().getGameNames();
        return null;
    }
}
