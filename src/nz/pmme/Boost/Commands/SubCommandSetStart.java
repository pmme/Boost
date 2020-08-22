package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandSetStart extends AbstractSubCommand
{
    public SubCommandSetStart( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length > 1 ) {
            Game game = plugin.getGameManager().getGame( args[1] );
            if( game == null ) {
                plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                return true;
            }
            Location spawn = null;
            if( args.length == 6 ) {
                World world = plugin.getServer().getWorld( args[2] );
                if( world != null ) {
                    try {
                        spawn = new Location( world, Double.parseDouble( args[3] ), Double.parseDouble( args[4] ), Double.parseDouble( args[5] ) );
                    } catch( NumberFormatException e ) {
                        plugin.messageSender( sender, ChatColor.translateAlternateColorCodes( '&', "&cThe last three parameters must be numbers." ) );
                        return true;
                    }
                } else {
                    plugin.messageSender( sender, Messages.FAILED_TO_FIND_WORLD, "%world%", args[2] );
                    return true;
                }
            }
            if( args.length == 2 ) {
                if( sender instanceof Player ) {
                    if( !plugin.isInGameWorld(sender) ) {
                        plugin.messageSender( sender, Messages.NOT_IN_GAME_WORLD );
                        return true;
                    }
                    spawn = ((Player)sender).getLocation();
                } else {
                    plugin.messageSender( sender, Messages.NO_CONSOLE );
                    return true;
                }
            }
            if( spawn != null ) {
                game.getGameConfig().setStartSpawn( spawn );
                plugin.messageSender( sender, Messages.START_SPAWN_SET, game.getGameConfig().getDisplayName() );

                if( spawn.getBlockY() <= game.getGameConfig().getGroundLevel() && game.getGameConfig().getGroundLevel() != -1 ) {
                    String message = plugin.formatMessage( Messages.GROUND_HIGHER, game.getGameConfig().getDisplayName(), "%y%", String.valueOf( spawn.getBlockY() ) );
                    plugin.messageSender( sender, message.replaceAll( "%ground%", String.valueOf( game.getGameConfig().getGroundLevel() ) ) );
                }
                if( spawn.getBlockY() >= game.getGameConfig().getCeilingLevel() && game.getGameConfig().getCeilingLevel() != -1 ) {
                    String message = plugin.formatMessage( Messages.CEILING_LOWER, game.getGameConfig().getDisplayName(), "%y%", String.valueOf( spawn.getBlockY() ) );
                    plugin.messageSender( sender, message.replaceAll( "%ceiling%", String.valueOf( game.getGameConfig().getCeilingLevel() ) ) );
                }
                return true;
            }
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getGameManager().getGameNames();
        return null;
    }
}
