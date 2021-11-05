package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class SubCommandStats extends AbstractSubCommand
{
    private static final String[] permissions = { "boost.cmd" };

    public SubCommandStats( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return permissions;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args )
    {
        // boost stats <player> <period/game> <game>
        if( args.length >= 2 && args.length <= 4 ) {
            UUID playerUuid = plugin.findPlayerByName( args[1] );
            if( playerUuid != null ) {
                OfflinePlayer player = plugin.getServer().getOfflinePlayer( playerUuid );
                if( player != null ) {
                    if( args.length >= 3 ) {
                        StatsPeriod statsPeriod = StatsPeriod.fromString( args[2] );
                        if( statsPeriod != null ) {
                            String gameName = args.length == 4 ? args[3] : null;
                            plugin.getGameManager().displayPlayerStats( sender, player, statsPeriod, gameName );
                            return true;
                        } else if( args.length == 3 ) {
                            plugin.getGameManager().displayPlayerStats( sender, player, StatsPeriod.TOTAL, args[2] ); // Assume the third parameter was a game name.
                            return true;
                        }
                    } else {
                        plugin.getGameManager().displayPlayerStats( sender, player, StatsPeriod.TOTAL, null );
                        return true;
                    }
                    return false;
                }
            }
            plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
            return true;
        } else if( args.length == 1 ) {
            if( sender instanceof Player ) {
                plugin.getGameManager().displayPlayerStats( sender, (Player)sender, StatsPeriod.TOTAL, null );
            } else {
                plugin.messageSender( sender, Messages.NO_CONSOLE );
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getDataHandler().queryListOfPlayers( StatsPeriod.TOTAL );
        if( args.length == 3 ) {
            List<String> returnList = new ArrayList<>();
            returnList.addAll( StatsPeriod.getStatsPeriods() );
            returnList.addAll( plugin.getGameManager().getGameNames() );
            return returnList;
        }
        if( args.length == 4 ) {
            if( null != StatsPeriod.fromString( args[2] ) ) {
                return plugin.getGameManager().getGameNames();
            }
        }
        return null;
    }
}
