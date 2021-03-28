package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by paul on 24-Apr-16.
 */
public class Commands implements TabExecutor
{
    private Main plugin;
    private final Map< String, SubCommand > commands = new HashMap<>();

    public Commands( Main plugin )
    {
        this.plugin = plugin;
        this.registerSubCommand( new SubCommandOn( plugin, "on" ) );
        this.registerSubCommand( new SubCommandOff( plugin, "off" ) );
        this.registerSubCommand( new SubCommandReload( plugin, "reload" ) );
        this.registerSubCommand( new SubCommandUpdateConfig( plugin, "updateconfig" ) );
        this.registerSubCommand( new SubCommandLanguage( plugin, "language" ) );
        this.registerSubCommand( new SubCommandAddGameWorld( plugin, "addgameworld" ) );
        this.registerSubCommand( new SubCommandRemoveGameWorld( plugin, "removegameworld" ) );
        this.registerSubCommand( new SubCommandSetMainLobby( plugin, "setmainlobby" ) );
        this.registerSubCommand( new SubCommandClearGames( plugin, "cleargames" ) );
        this.registerSubCommand( new SubCommandCreateGame( plugin, "creategame" ) );
        this.registerSubCommand( new SubCommandDeleteGame( plugin, "deletegame" ) );
        this.registerSubCommand( new SubCommandShowGameConfig( plugin, "showgameconfig" ) );
        this.registerSubCommand( new SubCommandSetDisplayName( plugin, "setdisplayname" ) );
        this.registerSubCommand( new SubCommandSetGround( plugin, "setground" ) );
        this.registerSubCommand( new SubCommandSetCeiling( plugin, "setceiling" ) );
        this.registerSubCommand( new SubCommandSetStart( plugin, "setstart" ) );
        this.registerSubCommand( new SubCommandDeleteStart( plugin, "deletestart" ) );
        this.registerSubCommand( new SubCommandSetLobby( plugin, "setlobby" ) );
        this.registerSubCommand( new SubCommandSetLoss( plugin, "setloss" ) );
        this.registerSubCommand( new SubCommandSetSpread( plugin, "setspread" ) );
        this.registerSubCommand( new SubCommandSetReturn( plugin, "setreturn" ) );
        this.registerSubCommand( new SubCommandSetBoostBlock( plugin, "setboostblock" ) );
        this.registerSubCommand( new SubCommandSetGuiItem( plugin, "setguiitem" ) );
        this.registerSubCommand( new SubCommandSetMinPlayers( plugin, "setminplayers" ) );
        this.registerSubCommand( new SubCommandSetMaxPlayers( plugin, "setmaxplayers" ) );
        this.registerSubCommand( new SubCommandAutoQueue( plugin, "autoqueue" ) );
        this.registerSubCommand( new SubCommandRequiresPermission( plugin, "requirespermission" ) );
        this.registerSubCommand( new SubCommandSetCountDown( plugin, "setcountdown" ) );
        this.registerSubCommand( new SubCommandSetAnnouncement( plugin, "setannouncement" ) );
        this.registerSubCommand( new SubCommandAddWinCommand( plugin, "addwincommand" ) );
        this.registerSubCommand( new SubCommandRemoveWinCommand( plugin, "removewincommand" ) );
        this.registerSubCommand( new SubCommandShowWinCommands( plugin, "showwincommands" ) );
        this.registerSubCommand( new SubCommandTestWinCommands( plugin, "testwincommands" ) );
        this.registerSubCommand( new SubCommandToggleLobbyBoost( plugin, "togglelobbyboost" ) );
        this.registerSubCommand( new SubCommandToggleGlow( plugin, "toggleglow" ) );
        this.registerSubCommand( new SubCommandSetCoolDown( plugin, "setcooldown" ) );
        this.registerSubCommand( new SubCommandSetStartDelay( plugin, "setstartdelay" ) );
        this.registerSubCommand( new SubCommandQueue( plugin, "queue" ) );
        this.registerSubCommand( new SubCommandStart( plugin, "start" ) );
        this.registerSubCommand( new SubCommandEnd( plugin, "end" ) );
        this.registerSubCommand( new SubCommandStop( plugin, "stop" ) );
        this.registerSubCommand( new SubCommandJoin( plugin, "join" ) );
        this.registerSubCommand( new SubCommandLeave( plugin, "leave" ) );
        this.registerSubCommand( new SubCommandTop( plugin, "top" ) );
        this.registerSubCommand( new SubCommandStats( plugin, "stats" ) );
        this.registerSubCommand( new SubCommandDelStats( plugin, "delstats" ) );
        this.registerSubCommand( new SubCommandStatus( plugin, "status" ) );
        this.registerSubCommand( new SubCommandSetSign( plugin, "setsign") );
        this.registerSubCommand( new SubCommandBuild( plugin, "build" ) );
        this.registerSubCommand( new SubCommandNoBuild( plugin, "nobuild" ) );
        this.registerSubCommand( new SubCommandAllowCommand( plugin, "allowcommand" ) );
        this.registerSubCommand( new SubCommandBlockCommand( plugin, "blockcommand" ) );
    }

    private void registerSubCommand( SubCommand subCommand ) {
        this.commands.put( subCommand.getSubCommandString(), subCommand );
    }

    @Override
    public boolean onCommand( CommandSender sender, Command command, String label, String[] args )
    {
        if( args.length == 0 )
        {
            plugin.messageSender( sender, plugin.isBoostEnabled() ? Messages.BOOST_ENABLED : Messages.BOOST_DISABLED );
            sender.sendMessage( plugin.getLoadedConfig().getCommandUsage( sender.hasPermission( "boost.admin" ) ) );
            return true;
        }
        else
        {
            String boostCommand = args[0].toLowerCase();
            SubCommand subCommand = commands.get( boostCommand );
            if( subCommand != null && subCommand.onSubCommand( sender, args ) ) return true;
            sender.sendMessage( plugin.getLoadedConfig().getCommandUsage( sender.hasPermission( "boost.admin" ), boostCommand ) );
        }
        return true;
    }

    private List<String> getMatchingStrings( List<String> possibles, String textEnteredThusFar )
    {
        if( possibles == null ) return Collections.emptyList();
        List<String> returnList = new ArrayList<>();
        for( String possible : possibles ) {
            String possibleStripped = ChatColor.stripColor( possible );
            if( textEnteredThusFar.isEmpty() || possibleStripped.toLowerCase().startsWith( textEnteredThusFar ) ) {
                returnList.add( possibleStripped );
            }
        }
        return returnList;
    }

    @Override
    public List<String> onTabComplete( CommandSender sender, Command command, String alias, String[] args )
    {
        if( !sender.hasPermission( "boost.cmd" ) ) return Collections.emptyList();
        if( args.length == 0 ) return Collections.emptyList();

        String[] argsLower = new String[ args.length ];
        for( int i = 0; i < args.length; ++i ) argsLower[i] = args[i].toLowerCase();

        List< String > matchingStrings = new ArrayList<>();
        String nextArgToComplete = argsLower[argsLower.length-1];
        for( SubCommand subCommand : commands.values() ) {
            matchingStrings.addAll( this.getMatchingStrings( subCommand.tabComplete( sender, argsLower ), nextArgToComplete ) );
        }
        return matchingStrings;
    }
}
