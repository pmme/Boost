package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;
import java.util.List;

public class SubCommandRemoveGameWorld extends AbstractSubCommand
{
    public SubCommandRemoveGameWorld( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    public String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    public boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            World world = plugin.getServer().getWorld( args[1] );
            if( world != null ) {
                if( plugin.getLoadedConfig().isGameWorld( world.getName() ) ) {
                    plugin.getLoadedConfig().delGameWorld( world.getName() );
                    plugin.messageSender( sender, Messages.REMOVED_GAME_WORLD, "%world%", world.getName() );
                } else {
                    plugin.messageSender( sender, Messages.NOT_A_GAME_WORLD, "%world%", world.getName() );
                }
            } else {
                plugin.messageSender( sender, Messages.FAILED_TO_FIND_WORLD, "%world%", args[1] );
            }
            return true;
        }
        if ( args.length == 1 ) {
            if( sender instanceof Player ) {
                World world = ((Player)sender).getLocation().getWorld();
                if( plugin.getLoadedConfig().isGameWorld( world.getName() ) ) {
                    plugin.getLoadedConfig().delGameWorld( world.getName() );
                    plugin.messageSender( sender, Messages.REMOVED_GAME_WORLD, "%world%", world.getName() );
                } else {
                    plugin.messageSender( sender, Messages.NOT_A_GAME_WORLD, "%world%", world.getName() );
                }
            } else {
                plugin.messageSender( sender, Messages.NO_CONSOLE );
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return Arrays.asList( plugin.getLoadedConfig().getGameWorlds() );
        return null;
    }
}
