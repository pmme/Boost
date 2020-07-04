package nz.pmme.Boost.Commands;

import com.sun.org.glassfish.external.statistics.Stats;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Enums.Winner;
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
            "setceiling",
            "setstart",
            "setlobby",
            "setloss",
            "setspread",
            "setminplayers",
            "setmaxplayers",
            "autoqueue",
            "requirespermission",
            "setcountdown",
            "setannouncement",
            "addwincommand",
            "removewincommand",
            "showwincommands",
            "testwincommands",
            "togglelobbyboost",
            "setcooldown",
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
    private static final String[] statsPeriods = {
            "daily",
            "weekly",
            "monthly"
    };
    private static final String[] onOff = {
            "on",
            "off"
    };

    public TabComplete( Main plugin ) {
        this.plugin = plugin;
    }

    private List<String> getMatchingStrings( List<String> possibles, String textEnteredThusFar )
    {
        List<String> returnList = new ArrayList<>();
        for( String possible : possibles ) {
            String possibleStripped = ChatColor.stripColor( possible );
            if( textEnteredThusFar.isEmpty() || possibleStripped.toLowerCase().startsWith( textEnteredThusFar ) ) {
                returnList.add( possibleStripped );
            }
        }
        return returnList;
    }

    private List<String> getMatchingStrings( String[] possibles, String textEnteredThusFar )
    {
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

        String arg0lower = args.length > 0 ? args[0].toLowerCase() : null;
        if( args.length == 1 )
        {
            List<String> matchingFirstArguments = new ArrayList<>();
            if( sender.hasPermission( "boost.cmd" ) ) {
                matchingFirstArguments.addAll( this.getMatchingStrings( userCommands, arg0lower ) );
            }
            if( sender.hasPermission( "boost.admin" ) ) {
                matchingFirstArguments.addAll( this.getMatchingStrings( adminCommands, arg0lower ) );
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
                    return getMatchingStrings( plugin.getGameManager().getGameNames(), arg1lower );

                case "delstats":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                case "stats":
                    return getMatchingStrings( plugin.getDataHandler().queryListOfPlayers( StatsPeriod.TOTAL ), arg1lower );

                case "showgameconfig":
                case "setdisplayname":
                case "setground":
                case "setceiling":
                case "setstart":
                case "setlobby":
                case "setloss":
                case "setspread":
                case "setminplayers":
                case "setmaxplayers":
                case "autoqueue":
                case "requirespermission":
                case "setcountdown":
                case "setannouncement":
                case "queue":
                case "start":
                case "end":
                case "stop":
                case "deletegame":
                case "showwincommands":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    return getMatchingStrings( plugin.getGameManager().getGameNames(), arg1lower );

                case "testwincommands":
                case "addwincommand":
                case "removewincommand":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
                        if( statsPeriod == StatsPeriod.TOTAL ) break;
                        returnList.add( statsPeriod.toString().toLowerCase() );
                    }
                    returnList.addAll( plugin.getGameManager().getGameNames() );
                    return getMatchingStrings( returnList, arg1lower );

                case "build":
                case "nobuild":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    for( Player player : plugin.getServer().getOnlinePlayers() ) {
                        String playerNameLower = player.getName().toLowerCase();
                        if( arg1lower.isEmpty() || playerNameLower.startsWith( arg1lower ) ) {
                            returnList.add( player.getName() );
                        }
                        String playerDisplayNameStripped = ChatColor.stripColor( player.getDisplayName() );
                        if( !playerDisplayNameStripped.toLowerCase().equals( playerNameLower ) ) {
                            if( arg1lower.isEmpty() || playerDisplayNameStripped.toLowerCase().startsWith( arg1lower ) ) {
                                returnList.add( playerDisplayNameStripped );
                            }
                        }
                    }
                    return returnList;

                case "addgameworld":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    for( World world : plugin.getServer().getWorlds() ) returnList.add( world.getName() );
                    return getMatchingStrings( returnList, arg1lower );

                case "removegameworld":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    return getMatchingStrings( plugin.getLoadedConfig().getGameWorlds(), arg1lower );

                case "top":
                    return getMatchingStrings( statsPeriods, arg1lower );

                case "language":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    return getMatchingStrings( plugin.getLoadedConfig().getLanguagePrefixes(), arg1lower );

                case "setsign":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    return getMatchingStrings( signCommands, arg1lower );
            }
        }
        else if( args.length == 3 )
        {
            String arg2lower = args[2].toLowerCase();
            switch( arg0lower ) {
                case "autoqueue":
                case "requirespermission":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    return getMatchingStrings( onOff, arg2lower );

                case "setsign":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    String arg1lower = args[1].toLowerCase();
                    if( arg1lower.equals( "join" ) ) {
                        return getMatchingStrings( plugin.getGameManager().getGameNames(), arg2lower );
                    } else if( arg1lower.equals( "top" ) ) {
                        return getMatchingStrings( statsPeriods, arg2lower );
                    }
                    break;

                case "testwincommands":
                case "addwincommand":
                case "removewincommand":
                    if( !sender.hasPermission( "boost.admin" ) ) break;
                    StatsPeriod statsPeriod = StatsPeriod.fromString( args[1] );
                    if( statsPeriod != null ) {
                        List<String> winners = new ArrayList<>();
                        for( Winner winner : Winner.values() ) {
                            winners.add( winner.toString().toLowerCase() );
                        }
                        return getMatchingStrings( winners, arg2lower );
                    }
                    break;
            }
        }
        return Collections.emptyList();
    }

}
