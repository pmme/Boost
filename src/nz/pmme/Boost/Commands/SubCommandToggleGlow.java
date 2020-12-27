package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandToggleGlow extends AbstractSubCommand
{
    public SubCommandToggleGlow( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        plugin.getLoadedConfig().setGlowAfterBoost( !plugin.getLoadedConfig().shouldGlowAfterBoost() );
        plugin.messageSender( sender, plugin.getLoadedConfig().shouldGlowAfterBoost() ? Messages.GLOW_AFTER_BOOST_ENABLED : Messages.GLOW_AFTER_BOOST_DISABLED );
        return true;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        return null;
    }
}
