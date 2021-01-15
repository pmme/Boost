package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

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
            String period = args[1].toLowerCase();
            String gameName = args.length == 3 ? args[2] : null;
            if( period.equalsIgnoreCase( "daily" ) ) {
                plugin.getGameManager().displayLeaderBoard( sender, StatsPeriod.DAILY, gameName );
                return true;
            } else if( period.equalsIgnoreCase( "weekly" ) ) {
                plugin.getGameManager().displayLeaderBoard( sender, StatsPeriod.WEEKLY, gameName );
                return true;
            } else if( period.equalsIgnoreCase( "monthly" ) ) {
                plugin.getGameManager().displayLeaderBoard( sender, StatsPeriod.MONTHLY, gameName );
                return true;
            } else if( args.length == 2 ) {
                plugin.getGameManager().displayLeaderBoard( sender, StatsPeriod.TOTAL, period ); // Assume the second parameter was a game name.
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
        if( args.length == 2 ) return StatsPeriod.getStatsPeriods();
        if( args.length == 3 ) return plugin.getGameManager().getGameNames();
        return null;
    }
}
