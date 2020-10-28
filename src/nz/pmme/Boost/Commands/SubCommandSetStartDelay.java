package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandSetStartDelay extends AbstractSubCommand
{
    public SubCommandSetStartDelay( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            try {
                plugin.getLoadedConfig().setGameStartBoostDelay( Long.parseUnsignedLong( args[1] ) );
                plugin.messageSender( sender, Messages.START_DELAY_SET, "%count%", String.valueOf( plugin.getLoadedConfig().getGameStartBoostDelay() ) );
            } catch( NumberFormatException e ) {
                plugin.messageSender( sender, ChatColor.translateAlternateColorCodes( '&', "&cThe last parameter must be a positive integer number." ) );
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        return null;
    }
}
