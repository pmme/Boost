package nz.pmme.Boost;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

/**
 * Created by paul on 24-Apr-16.
 */
public class Command_Boost implements CommandExecutor
{
    private Main plugin;
    private static final String boostEnabledMessage = ChatColor.GREEN + "Boost enabled";
    private static final String boostDisabledMessage = ChatColor.GRAY + "Boost disabled";
    private static final String[] boostCommandUsage = {
            ChatColor.DARK_AQUA + "Boost command usage:",
            ChatColor.WHITE + "/boost on" + ChatColor.DARK_AQUA + " - Turn the boost game and controls on.",
            ChatColor.WHITE + "/boost off" + ChatColor.DARK_AQUA + " - Turn the boost game and controls off."
    };
    private static final String boostNoConsoleMessage = "This boost command must be used by an active player.";

    public Command_Boost( Main plugin ) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        if( args.length == 0 )
        {
            sender.sendMessage( plugin.isBoostEnabled() ? boostEnabledMessage : boostDisabledMessage );
            displayCommandUsage( sender );
            return true;
        }
        else if( args.length > 0 )
        {
            if( args[0].equalsIgnoreCase( "on" ) ) {
                sender.sendMessage( boostEnabledMessage );
                plugin.enableBoost();
                return true;
            }
            else if( args[0].equalsIgnoreCase( "off" ) ) {
                sender.sendMessage( boostDisabledMessage );
                plugin.disableBoost();
                return true;
            }
        }
        displayCommandUsage( sender );
        return true;
    }

    protected void displayCommandUsage( CommandSender sender )
    {
        sender.sendMessage( boostCommandUsage );
    }

    protected void displayNoConsoleMessage( CommandSender sender )
    {
        sender.sendMessage( boostNoConsoleMessage );
    }
}
