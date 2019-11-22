package nz.pmme.Boost.Commands;

import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
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
            "creategame",
            "setground",
            "setstart",
            "setlobby",
            "setloss",
            "setspread",
            "queue",
            "start",
            "end",
            "stop",
            "cleargames",
            "delstats",
            "reload",
            "build",
            "nobuild",
            "on",
            "off"
    };

    public TabComplete( Main plugin ) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete( CommandSender sender, Command command, String alias, String[] args )
    {
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
                    for( String playerName : plugin.getDataHandler().queryListOfPlayers() ) {
                        String playerNameNoColour = ChatColor.stripColor(playerName);
                        if( arg1lower.isEmpty() || playerNameNoColour.toLowerCase().startsWith(arg1lower) ) {
                            returnList.add( playerNameNoColour );
                        }
                    }
                    return returnList;

                case "setground":
                case "setstart":
                case "setlobby":
                case "setloss":
                case "setspread":
                case "queue":
                case "start":
                case "end":
                case "stop":
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
            }
        }
        return Collections.emptyList();
    }

}
