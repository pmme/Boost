package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

public class SubCommandSetSign extends AbstractSubCommand
{
    private static final List<String> signCommands = java.util.Arrays.asList(
            "join",
            "leave",
            "status",
            "stats",
            "top"
    );

    public SubCommandSetSign( Main plugin, String subCommand ) {
        super( plugin, subCommand );
    }

    @Override
    protected String[] getRequiredPermissions() {
        return adminPermission;
    }

    @Override
    protected boolean executeSubCommand( CommandSender sender, String[] args ) {
        if( sender instanceof Player ) {
            Player player = (Player)sender;
            Block targetBlock = player.getTargetBlock( null,5 );
            if( targetBlock == null || !(targetBlock.getState() instanceof Sign ) ) {
                plugin.messageSender( sender, Messages.NOT_FACING_A_SIGN );
                return true;
            }
            if( args.length > 1 ) {
                String args1lower = args[1].toLowerCase();
                if( args.length != 3 && args1lower.equals( "join" ) ) return false;
                Sign sign = (Sign)( targetBlock.getState() );
                int line = 0;
                sign.setLine( line++, plugin.getLoadedConfig().getSignTitle() );
                switch( args1lower ) {
                    case "join":
                        sign.setLine( line++, plugin.getLoadedConfig().getSignJoin() );
                        String gameNameForSign = null;
                        if( args[2].contains( "&" ) ) {
                            gameNameForSign = ChatColor.translateAlternateColorCodes( '&', args[2] );
                        } else {
                            for( String game : plugin.getGameManager().getGameNames() ) {
                                if( ChatColor.stripColor( game ).equalsIgnoreCase( args[2] ) ) {
                                    gameNameForSign = game;
                                }
                            }
                            if( gameNameForSign == null ) gameNameForSign = args[2];
                        }
                        sign.setLine( line++, gameNameForSign );
                        break;
                    case "leave":
                        sign.setLine( line++, plugin.getLoadedConfig().getSignLeave() );
                        break;
                    case "status":
                        sign.setLine( line++, plugin.getLoadedConfig().getSignStatus() );
                        break;
                    case "stats":
                        sign.setLine( line++, plugin.getLoadedConfig().getSignStats() );
                        break;
                    case "top":
                        sign.setLine( line++, plugin.getLoadedConfig().getSignTop() );
                        if( args.length == 3 ) {
                            switch( args[2].toLowerCase() ) {
                                case "daily":
                                    sign.setLine( line++, plugin.getLoadedConfig().getSignDaily() );
                                    break;
                                case "weekly":
                                    sign.setLine( line++, plugin.getLoadedConfig().getSignWeekly() );
                                    break;
                                case "monthly":
                                    sign.setLine( line++, plugin.getLoadedConfig().getSignMonthly() );
                                    break;
                            }
                        }
                        break;
                    default:
                        String arg1Coloured = ChatColor.translateAlternateColorCodes( '&', args[1] );
                        sign.setLine( line++, arg1Coloured );
                        plugin.messageSender( sender, Messages.SIGN_COMMAND_NOT_RECOGNISED, "%command%", arg1Coloured );
                        break;
                }
                for( ; line < 4; ++line ) sign.setLine( line, "" );
                sign.update();
                plugin.messageSender( sender, Messages.SIGN_SET );
                return true;
            }
        } else {
            plugin.messageSender( sender, Messages.NO_CONSOLE );
            return true;
        }
        return false;
    }

    @Override
    protected List< String > tabCompleteArgs( CommandSender sender, String[] args ) {
        if( args.length == 2 ) return signCommands;
        if( args.length == 3 ) {
            String arg1lower = args[1].toLowerCase();
            if( arg1lower.equals( "join" ) ) {
                return plugin.getGameManager().getGameNames();
            } else if( arg1lower.equals( "top" ) ) {
                return StatsPeriod.getStatsPeriods();
            }
        }
        return null;
    }
}
