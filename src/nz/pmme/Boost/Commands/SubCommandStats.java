package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class SubCommandStats extends AbstractSubCommand
{
    private static final String[] permissions = { "boost.cmd" };

    public SubCommandStats( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return permissions;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length >= 2 ) {
            String gameName = args.length == 3 ? args[2] : null;
            UUID playerUuid = plugin.findPlayerByName( args[1] );
            if( playerUuid != null ) {
                OfflinePlayer player = plugin.getServer().getOfflinePlayer( playerUuid );
                if( player != null ) {
                    plugin.getGameManager().displayPlayerStats( sender, player, gameName );
                    return true;
                }
            }
            plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
            return true;
        } else if( sender instanceof Player ) {
            plugin.getGameManager().displayPlayerStats( sender, (Player)sender, null );
            return true;
        } else {
            plugin.messageSender( sender, Messages.NO_CONSOLE );
            return true;
        }
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getDataHandler().queryListOfPlayers( StatsPeriod.TOTAL );
        if( args.length == 3 ) return plugin.getGameManager().getGameNames();
        return null;
    }
}
