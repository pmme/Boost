package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.GameConfig;
import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandSetCeiling extends AbstractSubCommand
{
    public SubCommandSetCeiling( Main plugin, String subCommand ) {
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
            if( args.length == 3 ) {
                try {
                    int y = Integer.parseInt( args[2] );
                    game.getGameConfig().setCeilingLevel(y);
                    plugin.messageSender( sender, Messages.CEILING_SET, game.getGameConfig().getDisplayName(), "%y%", String.valueOf(y) );

                    int startSpawnsMaxY = game.getGameConfig().getStartSpawnsMaxY();
                    if( GameConfig.isValidY(y) && game.getGameConfig().hasStartSpawn() && startSpawnsMaxY >= y ) {
                        String message = plugin.formatMessage( Messages.CEILING_LOWER, game.getGameConfig().getDisplayName(), "%y%", String.valueOf( startSpawnsMaxY ) )
                                .replaceAll( "%ceiling%", String.valueOf( y ) );
                        plugin.messageSender( sender, message );
                    }
                } catch( NumberFormatException e ) {
                    plugin.messageSender( sender, ChatColor.translateAlternateColorCodes( '&', "&cThe last parameter must be an integer number." ) );
                }
                return true;
            }
            if ( args.length == 2 ) {
                if( sender instanceof Player ) {
                    if( !plugin.isInGameWorld(sender) ) {
                        plugin.messageSender( sender, Messages.NOT_IN_GAME_WORLD );
                        return true;
                    }
                    int y = ((Player)sender).getLocation().getBlockY();
                    game.getGameConfig().setCeilingLevel(y);
                    plugin.messageSender( sender, Messages.CEILING_SET, game.getGameConfig().getDisplayName(), "%y%", String.valueOf(y) );

                    int startSpawnsMaxY = game.getGameConfig().getStartSpawnsMaxY();
                    if( GameConfig.isValidY(y) && game.getGameConfig().hasStartSpawn() && startSpawnsMaxY >= y ) {
                        String message = plugin.formatMessage( Messages.CEILING_LOWER, game.getGameConfig().getDisplayName(), "%y%", String.valueOf( startSpawnsMaxY ) )
                                .replaceAll( "%ceiling%", String.valueOf( y ) );
                        plugin.messageSender( sender, message );
                    }
                } else {
                    plugin.messageSender( sender, Messages.NO_CONSOLE );
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
