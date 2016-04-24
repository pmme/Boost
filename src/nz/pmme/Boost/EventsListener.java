package nz.pmme.Boost;

import nz.pmme.Utils.TargetBlockFinder;
import org.bukkit.block.Block;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;


/**
 * Created by paul on 19-Apr-16.
 */
public class EventsListener implements Listener
{
    private static final double BOOST_VERTICAL_VELOCITY = 2.0;
    private static final double BOOST_MAX_HORIZONTAL_VELOCITY = 2.5;    // NOTE: Horizontal velocity is scaled much smaller than vertical in Minecraft.
    public static final double BOOST_MIN_HORIZONTAL_VELOCITY = 0.1;
    private Main plugin;

    public EventsListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract( PlayerInteractEvent event )
    {
        if( plugin.isBoostEnabled() ) {
            Block targetBlock = null;
            switch( event.getAction() ) {
                case LEFT_CLICK_BLOCK:
                    break;
                case RIGHT_CLICK_BLOCK:
                    targetBlock = event.getClickedBlock();
                    break;
                case LEFT_CLICK_AIR:
                    break;
                case RIGHT_CLICK_AIR:
                    TargetBlockFinder targetBlockFinder = new TargetBlockFinder( event.getPlayer() );
                    targetBlock = targetBlockFinder.getTargetBlock();
                    break;
                case PHYSICAL:
                    break;
            }
//            if( targetBlock != null ) targetBlock.setType( event.getMaterial() );
        }
    }

    @EventHandler
    public void onPlayerInteractEntity( PlayerInteractEntityEvent event )
    {
        if( plugin.isBoostEnabled() ) {
            if( event.getRightClicked() instanceof LivingEntity/*Player*/ )     // Enabled animal testing for Boost.
            {
                LivingEntity otherPlayer = (LivingEntity)event.getRightClicked();
                Player thisPlayer = event.getPlayer();

                // Get the normalized vector to the other player. Don't use the normalize() method though because we want the distance and it involves Math.sqrt, so we normalize ourselves.
                Vector vectorToOtherPlayer = new Vector( otherPlayer.getLocation().getX() - thisPlayer.getLocation().getX(), 0.0, otherPlayer.getLocation().getZ() - thisPlayer.getLocation().getZ() );
                double distanceBetweenPlayers = vectorToOtherPlayer.length();
                vectorToOtherPlayer.multiply( 1.0 / distanceBetweenPlayers );      // vectorToOtherPlayer is now a unit vector.

                // Calculate a horizontal boost velocity that is greater the closer you are to the target.
                double horizontalVelocity = BOOST_MAX_HORIZONTAL_VELOCITY - distanceBetweenPlayers;
                if( horizontalVelocity < BOOST_MIN_HORIZONTAL_VELOCITY ) horizontalVelocity = BOOST_MIN_HORIZONTAL_VELOCITY;
                vectorToOtherPlayer.multiply( horizontalVelocity );

                // Final boost vector is our calculated horizontal velocity and our constant vertical velocity.
                Vector vectorBoost = new Vector( vectorToOtherPlayer.getX(), BOOST_VERTICAL_VELOCITY, vectorToOtherPlayer.getZ() );
                otherPlayer.setVelocity( vectorBoost );
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
            if( event.getEntity() instanceof LivingEntity/*Player*/ )
            {
                event.setCancelled( true );
            }
        }
    }
}
