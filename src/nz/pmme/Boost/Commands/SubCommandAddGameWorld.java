package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class SubCommandAddGameWorld extends AbstractSubCommand
{
    public SubCommandAddGameWorld( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return AbstractSubCommand.adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            World world = plugin.getServer().getWorld( args[1] );
            if( world != null ) {
                plugin.getLoadedConfig().addGameWorld( world.getName() );
                plugin.messageSender( sender, Messages.ADDED_GAME_WORLD, "%world%", world.getName() );
            } else {
                plugin.messageSender( sender, Messages.FAILED_TO_FIND_WORLD, "%world%", args[1] );
            }
            return true;
        }
        if ( args.length == 1 ) {
            if( sender instanceof Player ) {
                World world = ((Player)sender).getLocation().getWorld();
                plugin.getLoadedConfig().addGameWorld( world.getName() );
                plugin.messageSender( sender, Messages.ADDED_GAME_WORLD, "%world%", world.getName() );
            } else {
                plugin.messageSender( sender, Messages.NO_CONSOLE );
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) {
            List< String > returnList = new ArrayList<>();
            for( World world : plugin.getServer().getWorlds() ) returnList.add( world.getName() );
            return returnList;
        }
        return null;
    }
}
