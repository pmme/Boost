package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandTesttp extends AbstractSubCommand
{
    private static final List<String> spawns = java.util.Arrays.asList( "lobby", "loss", "start" );

    public SubCommandTesttp( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args )
    {
        if( sender instanceof Player ) {
            Player player = (Player)sender;
            if( args.length >= 3 ) {
                Game game = plugin.getGameManager().getGame( args[1] );
                if( game == null ) {
                    plugin.messageSender( sender, Messages.GAME_DOES_NOT_EXIST, args[1] );
                    return true;
                }
                String spawnType = args[2].toLowerCase();
                switch( spawnType ) {
                    case "lobby":
                        Location lobbySpawn = game.getGameConfig().getConfiguredLobbySpawn();
                        if( lobbySpawn != null ) {
                            player.teleport( lobbySpawn );
                        } else {
                            plugin.messageSender( player, Messages.SPAWN_NOT_SET );
                        }
                        return true;
                    case "loss":
                        Location lossSpawn = game.getGameConfig().getConfiguredLossSpawn();
                        if( lossSpawn != null ) {
                            player.teleport( lossSpawn );
                        } else {
                            plugin.messageSender( player, Messages.SPAWN_NOT_SET );
                        }
                        return true;
                    case "start":
                        if( args.length == 4 ) {
                            Location startSpawn = game.getGameConfig().getConfiguredStartSpawn( args[3] );
                            if( startSpawn != null ) {
                                player.teleport( startSpawn );
                            } else {
                                plugin.messageSender( player, Messages.SPAWN_NOT_SET );
                            }
                            return true;
                        }
                        break;
                }
            }
        } else {
            plugin.messageSender( sender, Messages.NO_CONSOLE );
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args )
    {
        if( args.length == 2 ) return plugin.getGameManager().getGameNames();
        if( args.length == 3 ) return spawns;
        if( args.length == 4 && args[2].equalsIgnoreCase( "start" ) ) {
            Game game = plugin.getGameManager().getGame( args[1] );
            if( game != null ) return game.getGameConfig().getStartSpawnNodes();
        }
        return null;
    }
}
