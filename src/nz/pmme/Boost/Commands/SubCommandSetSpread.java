package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Exceptions.StartSpawnNodeNotFoundException;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandSetSpread extends AbstractSubCommand
{
    public SubCommandSetSpread( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 4 ) {
            Game game = plugin.getGameManager().getGame( args[1] );
            if( game == null ) {
                plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                return true;
            }
            try {
                int spread = Integer.parseUnsignedInt( args[3] );
                game.getGameConfig().setSpawnSpread( spread, args[2] );
                String message = plugin.formatMessage( Messages.SPREAD_SET, game.getGameConfig().getDisplayName(), "%spread%", String.valueOf( spread ) )
                        .replaceAll( "%name%", args[2] );
                plugin.messageSender( sender, message );
            } catch( NumberFormatException e ) {
                plugin.messageSender( sender, ChatColor.translateAlternateColorCodes( '&', "&cThe last parameter must be a positive integer number." ) );
            } catch( StartSpawnNodeNotFoundException e ) {
                plugin.messageSender( sender, Messages.START_SPAWN_NOT_FOUND, game.getGameConfig().getDisplayName(), "%name%", args[2] );
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getGameManager().getGameNames();
        if( args.length == 3 ) {
            Game game = plugin.getGameManager().getGame( args[1] );
            if( game != null ) return game.getGameConfig().getStartSpawnNodes();
        }
        return null;
    }
}
