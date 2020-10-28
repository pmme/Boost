package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandUpdateConfig extends AbstractSubCommand
{
    public SubCommandUpdateConfig( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        boolean somethingWasAdded = plugin.getLoadedConfig().updateConfigFiles();
        plugin.messageSender( sender, somethingWasAdded ? Messages.CONFIG_UPDATED : Messages.CONFIG_UP_TO_DATE );
        return true;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        return null;
    }
}
