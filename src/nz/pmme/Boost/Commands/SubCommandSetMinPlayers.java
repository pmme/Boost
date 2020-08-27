package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;

import java.util.List;

public class SubCommandSetMinPlayers extends AbstractSubCommand
{
    public SubCommandSetMinPlayers( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 3 ) {
            Game game = plugin.getGameManager().getGame( args[1] );
            if( game == null ) {
                plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                return true;
            }
            try {
                int minPlayers = Integer.parseUnsignedInt( args[2] );
                game.getGameConfig().setMinPlayers( minPlayers );
                plugin.messageSender( sender, Messages.MIN_PLAYERS_SET, game.getGameConfig().getDisplayName(), "%count%", String.valueOf( minPlayers ) );
            } catch( NumberFormatException e ) {
                plugin.messageSender( sender, ChatColor.translateAlternateColorCodes( '&', "&cThe last parameter must be a positive integer number." ) );
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getGameManager().getGameNames();
        return null;
    }
}
