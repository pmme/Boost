package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandQueue extends AbstractSubCommand
{
    public SubCommandQueue( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( !plugin.isBoostEnabled() ) {
            plugin.messageSender( sender, Messages.BOOST_DISABLED );
            return true;
        }
        if( args.length == 2 ) {
            if( args[1].equals( "*" ) ) {
                for( Game game : plugin.getGameManager().getGames() ) {
                    plugin.getGameManager().queueGame( game, sender );
                }
            } else {
                plugin.getGameManager().queueGame( args[1], sender );
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
