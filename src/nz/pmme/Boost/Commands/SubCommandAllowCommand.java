package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandAllowCommand extends AbstractSubCommand
{
    public SubCommandAllowCommand( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            plugin.getLoadedConfig().allowCommandWhilePlaying( args[1].toLowerCase() );
            plugin.messageSender( sender, Messages.COMMAND_NOW_ALLOWED, "%cmd%", args[1].toLowerCase() );
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        return null;
    }
}
