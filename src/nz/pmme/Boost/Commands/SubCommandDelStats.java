package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

import java.util.List;
import java.util.UUID;

public class SubCommandDelStats extends AbstractSubCommand
{
    public SubCommandDelStats( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 || args.length == 3 ) {
            String gameName = args.length == 3 ? args[2] : null;
            if( args[1].equals( "*" ) ) {
                plugin.getGameManager().deletePlayerStats( sender, null, gameName );
            } else {
                UUID playerUuid = plugin.findPlayerByName( args[1] );
                if( playerUuid != null ) {
                    OfflinePlayer player = plugin.getServer().getOfflinePlayer( playerUuid );
                    if( player != null ) {
                        plugin.getGameManager().deletePlayerStats( sender, player, gameName );
                        return true;
                    }
                }
                plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getDataHandler().queryListOfPlayers( StatsPeriod.TOTAL );
        if( args.length == 3 ) return plugin.getGameManager().getGameNames();
        return null;
    }
}
