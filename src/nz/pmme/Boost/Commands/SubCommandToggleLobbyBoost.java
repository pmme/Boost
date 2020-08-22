package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandToggleLobbyBoost extends AbstractSubCommand
{
    public SubCommandToggleLobbyBoost( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        plugin.getLoadedConfig().setBoostWhileQueuing( !plugin.getLoadedConfig().canBoostWhileQueuing() );
        plugin.messageSender( sender, plugin.getLoadedConfig().canBoostWhileQueuing() ? Messages.BOOST_WHILE_QUEUING : Messages.NO_BOOST_WHILE_QUEUING );
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        return null;
    }
}
