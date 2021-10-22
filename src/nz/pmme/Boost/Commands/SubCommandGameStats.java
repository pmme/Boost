package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandGameStats extends AbstractSubCommand
{
    public SubCommandGameStats( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 1 || args.length == 2 ) {
            StatsPeriod statsPeriod = ( args.length == 2 ) ? StatsPeriod.fromString( args[1] ) : StatsPeriod.TOTAL;
            if( statsPeriod == null ) return false;
            plugin.getGameManager().displayGameStats( sender, statsPeriod );
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return StatsPeriod.getStatsPeriods();
        return null;
    }
}
