package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Exceptions.GameDisplayNameMustMatchConfigurationException;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandSetDisplayName extends AbstractSubCommand
{
    public SubCommandSetDisplayName( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 3 ) {
            Game game = plugin.getGameManager().getGame( args[1] );
            if( game == null ) {
                plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                return true;
            }
            try {
                game.getGameConfig().setDisplayName( args[2] );
                plugin.messageSender( sender, Messages.DISPLAY_NAME_SET, game.getGameConfig().getDisplayName(), "%gameconfig%", game.getGameConfig().getName() );
            } catch( GameDisplayNameMustMatchConfigurationException e ) {
                plugin.messageSender( sender, Messages.DISPLAY_NAME_MISMATCH, "%gameconfig%", game.getGameConfig().getName() );
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
