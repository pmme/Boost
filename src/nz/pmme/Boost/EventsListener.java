package nz.pmme.Boost;

import nz.pmme.Utils.TargetBlockFinder;
import nz.pmme.Utils.VectorToOtherPlayer;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
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
        if(     Math.abs( playerPosition.getBlockX() - targetPosition.getBlockX() ) <= plugin.getLoadedConfig().getTargetDistanceH()
            &&  yDelta >= 0 && yDelta <= plugin.getLoadedConfig().getTargetDistanceV()
            &&  Math.abs( playerPosition.getBlockZ() - targetPosition.getBlockZ() ) <= plugin.getLoadedConfig().getTargetDistanceH() )
        {
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerInteract( PlayerInteractEvent event )
    {
        if( plugin.isBoostEnabled() )
        {
            final Player thisPlayer = event.getPlayer();
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
                    TargetBlockFinder targetBlockFinder = new TargetBlockFinder( event.getPlayer() );
                    targetBlock = targetBlockFinder.getTargetBlock();
                    break;
                case PHYSICAL:
                    break;
            }
            if( targetBlock != null )
            {
                // Check if there is a player standing on the target block.
                final Vector targetPotentialPlayerPosition = targetBlock.getLocation().toVector().add( VECTOR_UP );

                List<Player> otherPlayers = playersGame.getActivePlayerList();
                for( Player otherPlayer : otherPlayers )
                {
                    if( inTargetBox( targetPotentialPlayerPosition, otherPlayer.getLocation() ) )
                    {
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
        if( plugin.isBoostEnabled() ) {
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
        if( plugin.isBoostEnabled() ) event.setCancelled( true );
    }

    @EventHandler
    public void onBlockPlace( BlockPlaceEvent event )
    {
        if( plugin.isBoostEnabled() ) event.setCancelled( true );
    }

    @EventHandler
    public void onEntityDamage( EntityDamageEvent event )
    {
        if( plugin.isBoostEnabled() ) {
            if( event.getEntity() instanceof Player ) {
                if( plugin.getGameManager().isPlaying( (Player)event.getEntity() ) ) {
                    event.setCancelled( true );
                }
            }
        }
    }

    @EventHandler
    public void onPlayerMove( PlayerMoveEvent event )
    {
        if( plugin.isBoostEnabled() ) {
            Game game = plugin.getGameManager().getPlayersGame( event.getPlayer() );
            if( game == null ) return;
            if( game.isActiveInGame( event.getPlayer() ) ) {
                if( event.getTo() != null && event.getTo().getBlockY() <= game.getGroundLevel() ) {
                    game.setPlayerLost( event.getPlayer() );
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit( PlayerQuitEvent event )
    {
        plugin.getGameManager().leaveGame( event.getPlayer() );
    }
}
