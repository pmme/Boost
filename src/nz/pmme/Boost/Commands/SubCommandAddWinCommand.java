package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Enums.Winner;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SubCommandAddWinCommand extends AbstractSubCommand
{
    public SubCommandAddWinCommand( Main plugin, String subCommand ) {
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
                if( args.length >= 4 ) {
                    Winner winner = Winner.fromString( args[2] );
                    if( winner != null ) {
                        StringBuilder winCommand = new StringBuilder();
                        winCommand.append( args[3] );
                        for( int i = 4; i < args.length; ++i ) {
                            winCommand.append( " " );
                            winCommand.append( args[i] );
                        }
                        plugin.getLoadedConfig().addWinCommand( statsPeriod, winner, winCommand.toString() );
                        String message = plugin.formatMessage( Messages.PERIODIC_COMMANDS_UPDATED, "", "%period%", statsPeriod.toString() )
                                .replaceAll( "%winner%", winner.toString() )
                                .replaceAll( "%count%", String.valueOf( plugin.getLoadedConfig().getWinCommands( statsPeriod, winner ).size() ) );
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
                StringBuilder winCommand = new StringBuilder();
                winCommand.append( args[2] );
                for( int i = 3; i < args.length; ++i ) {
                    winCommand.append( " " );
                    winCommand.append( args[i] );
                }
                game.getGameConfig().addWinCommand( winCommand.toString() );
                plugin.messageSender( sender, Messages.GAME_COMMANDS_UPDATED, game.getGameConfig().getDisplayName(), "%count%", String.valueOf( game.getGameConfig().getWinCommands().size() ) );
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
                List<String> winners = new ArrayList<>();
                for( Winner winner : Winner.values() ) {
                    winners.add( winner.toString().toLowerCase() );
                }
                return winners;
            }
        }
        return null;
    }
}
