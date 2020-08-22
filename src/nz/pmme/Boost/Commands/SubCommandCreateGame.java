package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Exceptions.GameAlreadyExistsException;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandCreateGame extends AbstractSubCommand
{
    public SubCommandCreateGame( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            try {
                plugin.getGameManager().createNewGame( args[1] );
                plugin.messageSender( sender, Messages.GAME_CREATED, args[1] );
            } catch( GameAlreadyExistsException e ) {
                plugin.messageSender( sender, Messages.GAME_ALREADY_EXISTS, args[1] );
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        return null;
    }
}
