package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Exceptions.GameDoesNotExistException;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandDeleteGame extends AbstractSubCommand
{
    public SubCommandDeleteGame( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 || ( args.length == 3 && !args[2].equalsIgnoreCase( "confirm" ) ) ) {
            plugin.messageSender( sender, Messages.MUST_TYPE_CONFIRM, args[1] );
            return true;
        }
        if( args.length == 3 ) {
            try {
                plugin.getGameManager().deleteGame( args[1] );
                plugin.messageSender( sender, Messages.GAME_DELETED, args[1] );
            } catch( GameDoesNotExistException e ) {
                plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
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
