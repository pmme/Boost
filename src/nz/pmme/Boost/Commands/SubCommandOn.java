package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandOn extends AbstractSubCommand
{
    public SubCommandOn( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    public String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    public boolean executeSubCommand( CommandSender sender, String[] args ) {
        this.plugin.messageSender( sender, Messages.BOOST_ENABLED );
        this.plugin.enableBoost();
        return true;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        return null;
    }

}
