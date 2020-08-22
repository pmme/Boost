package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandLanguage extends AbstractSubCommand
{
    public SubCommandLanguage( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    public String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    public boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            plugin.getConfig().set( "language", args[1].toLowerCase() );
            plugin.saveConfig();
            plugin.reload();
            plugin.messageSender( sender, Messages.CONFIG_RELOADED );
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getLoadedConfig().getLanguagePrefixes();
        return null;
    }
}
