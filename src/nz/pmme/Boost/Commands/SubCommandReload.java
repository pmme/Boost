package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandReload extends AbstractSubCommand
{
    public SubCommandReload( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    public String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    public boolean executeSubCommand( CommandSender sender, String[] args ) {
        plugin.reload();
        plugin.messageSender( sender, Messages.CONFIG_RELOADED );
        return true;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        return null;
    }
}
