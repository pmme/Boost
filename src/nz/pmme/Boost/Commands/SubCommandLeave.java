package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandLeave extends AbstractSubCommand
{
    private static final String[] permissions = { "boost.cmd" };

    public SubCommandLeave( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return permissions;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( sender instanceof Player ) {
            if( args.length == 1 ) {
                plugin.getGameManager().leaveGame( (Player)sender );
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
        return null;
    }
}
