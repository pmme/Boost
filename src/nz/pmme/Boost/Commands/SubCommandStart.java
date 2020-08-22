package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandStart extends AbstractSubCommand
{
    public SubCommandStart( Main plugin, String subCommand ) {
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
        if( args.length == 1 ) {
            if( sender instanceof Player ) {
                Game game = plugin.getGameManager().getPlayersGame( (Player)sender );
                if( game == null ) return false;
                plugin.getGameManager().startGame( game, sender );
                return true;
            }
        }
        if( args.length == 2 ) {
            plugin.getGameManager().startGame( args[1], sender );
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
