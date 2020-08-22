package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandAutoQueue extends AbstractSubCommand
{
    private static final List<String> trueValues = java.util.Arrays.asList( "on", "true", "t", "1", "yes", "y" );
    private static final List<String> onOff = java.util.Arrays.asList( "on", "off" );

    public SubCommandAutoQueue( Main plugin, String subCommand ) {
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
            boolean autoQueue = trueValues.contains( args[2].toLowerCase() );
            game.getGameConfig().setAutoQueue( autoQueue );
            plugin.messageSender( sender, autoQueue ? Messages.AUTO_QUEUE_ENABLED : Messages.AUTO_QUEUE_DISABLED, game.getGameConfig().getDisplayName() );
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getGameManager().getGameNames();
        if( args.length == 3 ) return onOff;
        return null;
    }
}
