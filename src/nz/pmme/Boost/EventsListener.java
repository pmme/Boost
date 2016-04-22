package nz.pmme.Boost;

import nz.pmme.Utils.TargetBlockFinder;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.util.Vector;


/**
 * Created by paul on 19-Apr-16.
 */
public class EventsListener implements Listener
{
    private Main plugin;

    public EventsListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract( PlayerInteractEvent event )
    {
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
        if( targetBlock != null ) targetBlock.setType( event.getMaterial() );
    }

    @EventHandler
    public void onPlayerInteractEntity( PlayerInteractEntityEvent event )
    {
        if( event.getRightClicked() instanceof LivingEntity )
        {
            LivingEntity otherPlayer = (LivingEntity)event.getRightClicked();
//            Player otherPlayer = (Player)event.getRightClicked();
            Player thisPlayer = event.getPlayer();

            double deltaX = otherPlayer.getLocation().getX() - thisPlayer.getLocation().getX();
            double deltaZ = otherPlayer.getLocation().getZ() - thisPlayer.getLocation().getZ();
            Vector vectorBoost = new Vector( deltaX, 3.0, deltaZ );
            otherPlayer.setVelocity( vectorBoost );
        }
    }
}
