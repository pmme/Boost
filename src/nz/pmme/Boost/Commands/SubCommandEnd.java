package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandEnd extends AbstractSubCommand
{
    public SubCommandEnd( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 1 ) {
            if( sender instanceof Player ) {
                Game game = plugin.getGameManager().getPlayersGame( (Player)sender );
                if( game == null ) return false;
                plugin.getGameManager().endGame( game, sender );
                return true;
            }
        }
        if( args.length == 2 ) {
            if( args[1].equals( "*" ) ) {
                for( Game game : plugin.getGameManager().getGames() ) {
                    plugin.getGameManager().endGame( game, sender );
                }
            } else {
                plugin.getGameManager().endGame( args[1], sender );
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
