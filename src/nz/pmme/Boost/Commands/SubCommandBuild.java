package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SubCommandBuild extends AbstractSubCommand
{
    public SubCommandBuild( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            for( Player player : plugin.getServer().getOnlinePlayers() ) {
                if( player.getDisplayName().equalsIgnoreCase( args[1] ) || ChatColor.stripColor( player.getDisplayName() ).equalsIgnoreCase( args[1] ) || player.getName().equalsIgnoreCase( args[1] ) ) {
                    plugin.setBuilder( player.getUniqueId() );
                    player.setGameMode( plugin.getLoadedConfig().getBuildGameMode() );
                    plugin.messageSender( player, Messages.BUILD_ENABLED, "", "%player%", player.getName() );
                    plugin.messageSender( sender, Messages.BUILD_ENABLED, "", "%player%", player.getName() );
                    return true;
                }
            }
            plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
            return true;
        } else if( sender instanceof Player ) {
            plugin.setBuilder( ((Player)sender).getUniqueId() );
            ((Player)sender).setGameMode( plugin.getLoadedConfig().getBuildGameMode() );
            plugin.messageSender( sender, Messages.BUILD_ENABLED, "", "%player%", sender.getName() );
            return true;
        } else {
            plugin.messageSender( sender, Messages.NO_CONSOLE );
            return true;
        }
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            List<String> returnList = new ArrayList<>();
            String arg1lower = args[1].toLowerCase();
            for( Player player : plugin.getServer().getOnlinePlayers() ) {
                String playerNameLower = player.getName().toLowerCase();
                if( arg1lower.isEmpty() || playerNameLower.startsWith( arg1lower ) ) {
                    returnList.add( player.getName() );
                }
                String playerDisplayNameStripped = ChatColor.stripColor( player.getDisplayName() );
                if( !playerDisplayNameStripped.toLowerCase().equals( playerNameLower ) ) {
                    if( arg1lower.isEmpty() || playerDisplayNameStripped.toLowerCase().startsWith( arg1lower ) ) {
                        returnList.add( playerDisplayNameStripped );
                    }
                }
            }
            return returnList;
        }
        return null;
    }
}
