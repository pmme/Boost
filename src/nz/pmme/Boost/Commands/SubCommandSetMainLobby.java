package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandSetMainLobby extends AbstractSubCommand
{
    public SubCommandSetMainLobby( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    public String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    public boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 5 ) {
            World world = plugin.getServer().getWorld( args[1] );
            if( world != null ) {
                try {
                    Location spawn = new Location( world, Double.parseDouble( args[2] ), Double.parseDouble( args[3] ), Double.parseDouble( args[4] ) );
                    plugin.getLoadedConfig().setMainLobbySpawn( spawn );
                    plugin.messageSender( sender, Messages.MAIN_SPAWN_SET );
                } catch( NumberFormatException e ) {
                    plugin.messageSender( sender, ChatColor.translateAlternateColorCodes( '&', "&cThe last three parameters must be numbers." ) );
                }
            } else {
                plugin.messageSender( sender, Messages.FAILED_TO_FIND_WORLD, "%world%", args[1] );
            }
            return true;
        }
        if( args.length == 1 ) {
            if( sender instanceof Player ) {
                if( !plugin.isInGameWorld(sender) ) {
                    plugin.messageSender( sender, Messages.NOT_IN_GAME_WORLD );
                    return true;
                }
                plugin.getLoadedConfig().setMainLobbySpawn( ( (Player)sender ).getLocation() );
                plugin.messageSender( sender, Messages.MAIN_SPAWN_SET );
            } else {
                plugin.messageSender( sender, Messages.NO_CONSOLE );
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
