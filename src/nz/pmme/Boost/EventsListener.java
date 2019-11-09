package nz.pmme.Boost;

import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Utils.VectorToOtherPlayer;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.Sign;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;
import org.bukkit.util.Vector;

import java.util.List;


/**
 * Created by paul on 19-Apr-16.
 */
public class EventsListener implements Listener
{
    private static final Vector VECTOR_UP = new Vector( 0, 1, 0 );
    private Main plugin;

    public EventsListener(Main plugin) {
        this.plugin = plugin;
    }

    private boolean inTargetBox( Vector targetPosition, Location playerPosition )
    {
        int yDelta = playerPosition.getBlockY() - targetPosition.getBlockY();
        if(     Math.abs( playerPosition.getBlockX() - targetPosition.getBlockX() ) <= plugin.getLoadedConfig().getTargetBoxH()
            &&  yDelta >= 0 && yDelta <= plugin.getLoadedConfig().getTargetBoxV()
            &&  Math.abs( playerPosition.getBlockZ() - targetPosition.getBlockZ() ) <= plugin.getLoadedConfig().getTargetBoxH() )
        {
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract( PlayerInteractEvent event )
    {
        if( plugin.isBoostEnabled() && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) )
        {
            final Player thisPlayer = event.getPlayer();

            if( event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK )
            {
                if( event.getClickedBlock().getState() instanceof Sign )
                {
                    Sign sign = (Sign)event.getClickedBlock().getState();
                    String[] lines = sign.getLines();
                    if( lines.length > 1 && ChatColor.stripColor( lines[0] ).equalsIgnoreCase( plugin.getLoadedConfig().getSignTitle() ) )
                    {
                        String strippedLine1 = ChatColor.stripColor( lines[1] );
                        if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignJoin() ) && lines.length >= 3 ) {
                            if( !plugin.hasPermission( thisPlayer, "boost.join", Messages.NO_PERMISSION_USE ) ) return;
                            plugin.getGameManager().joinGame( thisPlayer, ChatColor.stripColor( lines[2] ) );
                            return;
                        } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignLeave() ) ) {
                            plugin.getGameManager().leaveGame( thisPlayer );
                            return;
                        } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignStatus() ) ) {
                            if( !plugin.hasPermission( thisPlayer, "boost.status", Messages.NO_PERMISSION_USE ) ) return;
                            plugin.getGameManager().displayStatus( thisPlayer );
                            return;
                        } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignStats() ) ) {
                            plugin.getGameManager().displayPlayerStats( thisPlayer, thisPlayer );
                            return;
                        } else if( strippedLine1.equalsIgnoreCase( plugin.getLoadedConfig().getSignTop() ) ) {
                            plugin.getGameManager().displayLeaderBoard( thisPlayer );
                            return;
                        }
                        plugin.messageSender( thisPlayer, Messages.ERROR_IN_SIGN );
                        return;
                    }
                }
            }

            final Game playersGame = plugin.getGameManager().getPlayersGame( thisPlayer );
            if( playersGame == null ) return;
            if( !playersGame.isActiveInGame( thisPlayer ) ) return;

            Block targetBlock = null;
            switch( event.getAction() ) {
                case LEFT_CLICK_BLOCK:
                case RIGHT_CLICK_BLOCK:
                    targetBlock = event.getClickedBlock();
                    break;
                case LEFT_CLICK_AIR:
                case RIGHT_CLICK_AIR:
                    targetBlock = event.getPlayer().getTargetBlock( null, playersGame.getGameConfig().getTargetDist() );
                    break;
                case PHYSICAL:
                    break;
            }
            if( targetBlock != null ) {
                // Check if there is a player standing on the target block.
                final Vector targetPotentialPlayerPosition = targetBlock.getLocation().toVector().add( VECTOR_UP );

                List< Player > otherPlayers = playersGame.getActivePlayerList();
                for( Player otherPlayer : otherPlayers ) {
                    if( inTargetBox( targetPotentialPlayerPosition, otherPlayer.getLocation() ) ) {
                        VectorToOtherPlayer vectorToOtherPlayer = new VectorToOtherPlayer( otherPlayer, thisPlayer );

                        // Set a horizontal boost velocity of medium magnitude, a medium gain from hitting their block.
                        vectorToOtherPlayer.multiply( plugin.getLoadedConfig().getBlock_hit_horizontal_velocity() );

                        // Final boost vector is our calculated horizontal velocity and our constant vertical velocity.
                        Vector vectorBoost = new Vector( vectorToOtherPlayer.getX(), plugin.getLoadedConfig().getVertical_velocity(), vectorToOtherPlayer.getZ() );
                        otherPlayer.setVelocity( vectorBoost );
                    }
                }
            }
        }
    }

    @EventHandler
    public void onPlayerInteractEntity( PlayerInteractEntityEvent event )
    {
        if( plugin.isBoostEnabled() && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) ) {
            if( event.getRightClicked() instanceof Player )
            {
                final Player thisPlayer = event.getPlayer();
                final Player otherPlayer = (Player)event.getRightClicked();
                if( plugin.getGameManager().activeInSameGame( thisPlayer, otherPlayer ) )
                {
                    VectorToOtherPlayer vectorToOtherPlayer = new VectorToOtherPlayer( otherPlayer, thisPlayer );

                    // Calculate a horizontal boost velocity that is greater the closer you are to the target.
                    double horizontalVelocity = plugin.getLoadedConfig().getMax_horizontal_velocity() - vectorToOtherPlayer.getDistanceBetweenPlayers();
                    if( horizontalVelocity < plugin.getLoadedConfig().getMin_horizontal_velocity() )
                        horizontalVelocity = plugin.getLoadedConfig().getMin_horizontal_velocity();
                    vectorToOtherPlayer.multiply( horizontalVelocity );

                    // Final boost vector is our calculated horizontal velocity and our constant vertical velocity.
                    Vector vectorBoost = new Vector( vectorToOtherPlayer.getX(), plugin.getLoadedConfig().getVertical_velocity(), vectorToOtherPlayer.getZ() );
                    otherPlayer.setVelocity( vectorBoost );
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak( BlockBreakEvent event )
    {
        if( plugin.isBoostEnabled() && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) ) event.setCancelled( true );
    }

    @EventHandler
    public void onBlockPlace( BlockPlaceEvent event )
    {
        if( plugin.isBoostEnabled() && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) ) event.setCancelled( true );
    }

    @EventHandler
    public void onEntityDamage( EntityDamageEvent event )
    {
        if( plugin.isBoostEnabled() ) {
//            if( event.getEntity() instanceof Player ) {
//                if( plugin.getGameManager().isPlaying( (Player)event.getEntity() ) ) {
                    event.setCancelled( true );
//                }
//            }
        }
    }

    @EventHandler
    public void onPlayerMove( PlayerMoveEvent event )
    {
        if( plugin.isBoostEnabled() && !plugin.isInBuildMode( event.getPlayer().getUniqueId() ) ) {
            Game game = plugin.getGameManager().getPlayersGame( event.getPlayer() );
            if( game == null ) return;
            if( game.isActiveInGame( event.getPlayer() ) ) {
                if( event.getTo() != null && event.getTo().getBlockY() <= game.getGameConfig().getGroundLevel() ) {
                    game.setPlayerLost( event.getPlayer() );
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit( PlayerQuitEvent event )
    {
        plugin.getGameManager().leaveGame( event.getPlayer() );
        plugin.removeBuilder( event.getPlayer().getUniqueId() );
    }

    @EventHandler
    public void onPlayerJoin( PlayerJoinEvent event )
    {
        if( plugin.isBoostEnabled() ) {
            event.getPlayer().teleport( event.getPlayer().getWorld().getSpawnLocation() );
        }
    }
}
