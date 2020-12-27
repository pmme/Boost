package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Enums.Place;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubCommandTestWinCommands extends AbstractSubCommand
{
    public SubCommandTestWinCommands( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length >= 2 )
        {
            StatsPeriod statsPeriod = StatsPeriod.fromString( args[1] );
            if( statsPeriod != null )
            {
                if( args.length >= 3 ) {
                    Place place = Place.fromString( args[2] );
                    if( place != null ) {
                        UUID playerUuid = null;
                        if( args.length == 4 ) {
                            playerUuid = plugin.findPlayerByName( args[3] );
                            if( playerUuid == null ) {
                                plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[3] );
                                return true;
                            }
                        } else {
                            if( sender instanceof Player ) {
                                playerUuid = ((Player)sender).getUniqueId();
                            } else {
                                plugin.messageSender( sender, Messages.NO_CONSOLE );
                                return true;
                            }
                        }
                        plugin.getLoadedConfig().runWinCommands( playerUuid, null, plugin.getLoadedConfig().getWinCommands( statsPeriod, place ) );
                        String message = plugin.formatMessage( Messages.PERIODIC_COMMANDS_TESTED, "", "%period%", statsPeriod.toString() )
                                .replaceAll( "%winner%", place.toString() )
                                .replaceAll( "%place%", place.toString() )
                                .replaceAll( "%count%", String.valueOf( plugin.getLoadedConfig().getWinCommands( statsPeriod, place ).size() ) );
                        plugin.messageSender( sender, message );
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
                UUID playerUuid = null;
                if( args.length == 3 ) {
                    playerUuid = plugin.findPlayerByName( args[2] );
                    if( playerUuid == null ) {
                        plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[2] );
                        return true;
                    }
                } else {
                    if( sender instanceof Player ) {
                        playerUuid = ((Player)sender).getUniqueId();
                    } else {
                        plugin.messageSender( sender, Messages.NO_CONSOLE );
                        return true;
                    }
                }
                plugin.getLoadedConfig().runWinCommands( playerUuid, game.getGameConfig().getDisplayName(), game.getGameConfig().getWinCommands() );
                plugin.messageSender( sender, Messages.GAME_COMMANDS_TESTED, game.getGameConfig().getDisplayName(), "%count%", String.valueOf( game.getGameConfig().getWinCommands().size() ) );
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
                for( Place place : Place.getTop3places() ) {
                    places.add( place.toString().toLowerCase() );
                }
                return places;
            }
        }
        return null;
    }
}
