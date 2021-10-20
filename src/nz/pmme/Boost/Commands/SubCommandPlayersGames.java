package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;
import java.util.UUID;

public class SubCommandPlayersGames extends AbstractSubCommand
{
    private static final String[] permissions = { "boost.cmd" };

    public SubCommandPlayersGames( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return permissions;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( args.length == 2 || args.length == 3 ) {
            UUID playerUuid = plugin.findPlayerByName( args[1] );
            if( playerUuid != null ) {
                OfflinePlayer player = plugin.getServer().getOfflinePlayer( playerUuid );
                if( player != null ) {
                    StatsPeriod statsPeriod = ( args.length == 3 ) ? StatsPeriod.fromString( args[2] ) : StatsPeriod.TOTAL;
                    if( statsPeriod == null ) return false;
                    plugin.getGameManager().displayPlayersGames( sender, player, statsPeriod );
                    return true;
                }
            }
            plugin.messageSender( sender, Messages.PLAYER_NOT_FOUND, "", "%player%", args[1] );
            return true;
        } else if( args.length == 1 ) {
            if( sender instanceof Player ) {
                plugin.getGameManager().displayPlayersGames( sender, (Player)sender, StatsPeriod.TOTAL );
            } else {
                plugin.messageSender( sender, Messages.NO_CONSOLE );
            }
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return plugin.getDataHandler().queryListOfPlayers( StatsPeriod.TOTAL );
        if( args.length == 3 ) return StatsPeriod.getStatsPeriods();
        return null;
    }
}
