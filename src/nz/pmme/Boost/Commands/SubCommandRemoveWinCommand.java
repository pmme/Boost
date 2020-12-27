package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Enums.Place;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SubCommandRemoveWinCommand extends AbstractSubCommand
{
    public SubCommandRemoveWinCommand( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length >= 3 )
        {
            StatsPeriod statsPeriod = StatsPeriod.fromString( args[1] );
            if( statsPeriod != null )
            {
                if( args.length == 4 ) {
                    Place place = Place.fromString( args[2] );
                    if( place != null ) {
                        try {
                            plugin.getLoadedConfig().removeWinCommand( statsPeriod, place, Integer.parseUnsignedInt( args[3], 10 ) - 1 );
                            String message = plugin.formatMessage( Messages.PERIODIC_COMMANDS_UPDATED, "", "%period%", statsPeriod.toString() )
                                    .replaceAll( "%winner%", place.toString() )
                                    .replaceAll( "%place%", place.toString() )
                                    .replaceAll( "%count%", String.valueOf( plugin.getLoadedConfig().getWinCommands( statsPeriod, place ).size() ) );
                            plugin.messageSender( sender, message );
                        } catch( NumberFormatException e ) {
                            plugin.messageSender( sender, ChatColor.translateAlternateColorCodes( '&', "&cThe last parameter must be a integer number 1 or more." ) );
                        } catch( IndexOutOfBoundsException e ) {
                            plugin.messageSender( sender, ChatColor.translateAlternateColorCodes( '&', "&cThe last parameter must be the number of the command to remove." ) );
                        }
                        return true;
                    }
                }
            }
            else
            {
                Game game = plugin.getGameManager().getGame( args[1] );
                if( game == null ) {
                    plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                    return true;
                }
                try {
                    game.getGameConfig().removeWinCommand( Integer.parseUnsignedInt( args[2], 10 ) - 1 );
                    plugin.messageSender( sender, Messages.GAME_COMMANDS_UPDATED, game.getGameConfig().getDisplayName(), "%count%", String.valueOf( game.getGameConfig().getWinCommands().size() ) );
                } catch( NumberFormatException e ) {
                    plugin.messageSender( sender, ChatColor.translateAlternateColorCodes( '&', "&cThe last parameter must be a integer number 1 or more." ) );
                } catch( IndexOutOfBoundsException e ) {
                    plugin.messageSender( sender, ChatColor.translateAlternateColorCodes( '&', "&cThe last parameter must be the number of the command to remove." ) );
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            List<String> returnList = new ArrayList<>();
            returnList.addAll( StatsPeriod.getStatsPeriods() );
            returnList.addAll( plugin.getGameManager().getGameNames() );
            return returnList;
        }
        if( args.length == 3 ) {
            StatsPeriod statsPeriod = StatsPeriod.fromString( args[1] );
            if( statsPeriod != null ) {
                List<String> places = new ArrayList<>();
                for( Place place : Place.values() ) {
                    places.add( place.toString().toLowerCase() );
                }
                return places;
            }
        }
        return null;
    }
}
