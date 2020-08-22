package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandJoin extends AbstractSubCommand
{
    private static final String[] permissions = { "boost.cmd", "boost.join" };

    public SubCommandJoin( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return permissions;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( !plugin.isBoostEnabled() ) {
            plugin.messageSender( sender, Messages.BOOST_DISABLED );
            return true;
        }
        if( sender instanceof Player ) {
            if( !plugin.isInGameWorld(sender) ) {
                plugin.messageSender( sender, Messages.NOT_IN_GAME_WORLD );
                return true;
            }
            if( args.length == 2 ) {
                plugin.getGameManager().joinGame( (Player)sender, args[1] );
                return true;
            }
        } else {
            plugin.messageSender( sender, Messages.NO_CONSOLE );
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
