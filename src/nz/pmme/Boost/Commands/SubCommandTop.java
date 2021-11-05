package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;

public class SubCommandTop extends AbstractSubCommand
{
    private static final String[] permissions = { "boost.cmd" };

    public SubCommandTop( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return permissions;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 || args.length == 3 ) {
            StatsPeriod statsPeriod = StatsPeriod.fromString( args[1] );
            String gameName = args.length == 3 ? args[2] : null;
            if( statsPeriod != null ) {
                plugin.getGameManager().displayLeaderBoard( sender, statsPeriod, gameName );
                return true;
            } else if( args.length == 2 ) {
                plugin.getGameManager().displayLeaderBoard( sender, StatsPeriod.TOTAL, args[1] ); // Assume the second parameter was a game name.
                return true;
            }
            return false;
        } else if( args.length == 1 ) {
            plugin.getGameManager().displayLeaderBoard( sender, StatsPeriod.TOTAL, null );
            return true;
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
            if( null != StatsPeriod.fromString( args[1] ) ) {
                return plugin.getGameManager().getGameNames();
            }
        }
        return null;
    }
}
