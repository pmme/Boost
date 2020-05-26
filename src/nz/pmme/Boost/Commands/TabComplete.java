package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TabComplete implements TabCompleter
{
    private Main plugin;
    private static final String[] userCommands = {
            "join",
            "leave",
            "stats",
            "top",
            "status"
    };
    private static final String[] adminCommands = {
            "language",
            "creategame",
            "deletegame",
            "showgameconfig",
            "setdisplayname",
            "setground",
            "setstart",
            "setlobby",
            "setloss",
            "setspread",
            "setminplayers",
            "setmaxplayers",
            "autoqueue",
            "setcountdown",
            "setannouncement",
            "queue",
            "start",
            "end",
            "stop",
            "cleargames",
            "delstats",
            "reload",
            "addgameworld",
            "removegameworld",
            "setmainlobby",
            "setsign",
            "build",
            "nobuild",
            "on",
            "off"
    };
    private static final String[] signCommands = {
            "join",
            "leave",
            "status",
            "stats",
            "top"
    };
    private static final String[] signTopArgs = {
            "daily",
            "weekly",
            "monthly"
    };

    public TabComplete( Main plugin ) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete( CommandSender sender, Command command, String alias, String[] args )
    {
        if( !sender.hasPermission( "boost.cmd" ) ) return Collections.emptyList();

        String arg0lower = args.length > 0 ? args[0].toLowerCase() : null;
        if( args.length == 1 )
        {
            List<String> matchingFirstArguments = new ArrayList<>();
            if( sender.hasPermission( "boost.cmd" ) ) {
                for( String argument : userCommands ) {
                    if( arg0lower.isEmpty() || argument.toLowerCase().startsWith(arg0lower) ) {
                        matchingFirstArguments.add(argument);
                    }
                }
            }
            if( sender.hasPermission( "boost.admin" ) ) {
                for( String argument : adminCommands ) {
                    if( arg0lower.isEmpty() || argument.toLowerCase().startsWith(arg0lower) ) {
                        matchingFirstArguments.add(argument);
                    }
                }
            }
            return matchingFirstArguments;
        }
        else if( args.length == 2 )
        {
            List<String> returnList = new ArrayList<>();
            String arg1lower = args[1].toLowerCase();
            switch( arg0lower )
            {
                case "join":
                    for( String gameName : plugin.getGameManager().getGameNames() ) {
                        returnList.add( ChatColor.stripColor( gameName ) );
                    }
                    return returnList;

                case "delstats":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                case "stats":
                    for( String playerName : plugin.getDataHandler().queryListOfPlayers( StatsPeriod.TOTAL ) ) {
                        String playerNameNoColour = ChatColor.stripColor(playerName);
                        if( arg1lower.isEmpty() || playerNameNoColour.toLowerCase().startsWith(arg1lower) ) {
                            returnList.add( playerNameNoColour );
                        }
                    }
                    return returnList;

                case "showgameconfig":
                case "setdisplayname":
                case "setground":
                case "setstart":
                case "setlobby":
                case "setloss":
                case "setspread":
                case "setminplayers":
                case "setmaxplayers":
                case "autoqueue":
                case "setcountdown":
                case "setannouncement":
                case "queue":
                case "start":
                case "end":
                case "stop":
                case "deletegame":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    for( String gameName : plugin.getGameManager().getGameNames() ) {
                        returnList.add( ChatColor.stripColor( gameName ) );
                    }
                    return returnList;

                case "build":
                case "nobuild":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    for( Player player : plugin.getServer().getOnlinePlayers() ) {
                        if( arg1lower.isEmpty() || player.getName().toLowerCase().startsWith(arg1lower) || ChatColor.stripColor(player.getDisplayName()).toLowerCase().startsWith(arg1lower) ) {
                            returnList.add( player.getName() );
                        }
                    }
                    return returnList;

                case "addgameworld":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    for( World world : plugin.getServer().getWorlds() ) {
                        returnList.add( world.getName() );
                    }
                    return returnList;

                case "removegameworld":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    for( String world : plugin.getLoadedConfig().getGameWorlds() ) {
                        returnList.add( world );
                    }
                    return returnList;

                case "top":
                    returnList.add( plugin.getLoadedConfig().getSignDaily() );
                    returnList.add( plugin.getLoadedConfig().getSignWeekly() );
                    returnList.add( plugin.getLoadedConfig().getSignMonthly() );
                    return returnList;

                case "language":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    return plugin.getLoadedConfig().getLanguagePrefixes();

                case "setsign":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    return java.util.Arrays.asList( signCommands );
            }
        }
        else if( args.length == 3 )
        {
            String arg2lower = args[2].toLowerCase();
            switch( arg0lower ) {
                case "autoqueue":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    return java.util.Arrays.asList( "on", "off" );

                case "setsign":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    String arg1lower = args[1].toLowerCase();
                    if( arg1lower.equals( "join" ) ) {
                        List<String> returnList = new ArrayList<>();
                        for( String gameName : plugin.getGameManager().getGameNames() ) {
                            returnList.add( ChatColor.stripColor( gameName ) );
                        }
                        return returnList;
                    } else if( arg1lower.equals( "top" ) ) {
                        return java.util.Arrays.asList( signTopArgs );
                    }
                    break;
            }
        }
        return Collections.emptyList();
    }

}
