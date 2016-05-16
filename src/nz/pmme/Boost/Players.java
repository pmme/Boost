package nz.pmme.Boost;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by paul on 16/05/16.
 */
public class Players
{
    private Main plugin;
    private List<Player> players = new ArrayList<Player>();
    private List<Player> spectators = new ArrayList<Player>();
    private static final String BOOST_JOIN_MESSAGE = ChatColor.DARK_AQUA + "Joined Boost game";
    private static final String BOOST_LEAVE_MESSAGE = ChatColor.DARK_AQUA + "Left Boost game";
    private static final String BOOST_ALREADY_JOINED_MESSAGE = ChatColor.RED + "You are already in the player list.";
    private static final String BOOST_NOT_IN_PLAYERLIST_MESSAGE = ChatColor.RED + "You are not in the player list.";

    public Players( Main plugin )
    {
        this.plugin = plugin;
    }

    public void join( Player player )
    {
        if( !players.contains( player ) ) {
            players.add( player );
            player.sendMessage( BOOST_JOIN_MESSAGE );
        } else {
            player.sendMessage( BOOST_ALREADY_JOINED_MESSAGE );
        }
    }

    public void leave( Player player )
    {
        if( players.contains( player ) ) {
            players.remove( player );
            player.sendMessage( BOOST_LEAVE_MESSAGE );
        } else {
            player.sendMessage( BOOST_NOT_IN_PLAYERLIST_MESSAGE );
        }
    }
}
