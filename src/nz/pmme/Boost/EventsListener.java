package nz.pmme.Boost;

import org.bukkit.entity.Entity;
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
    public void onPlayerInteract(PlayerInteractEvent event) {
    }

    @EventHandler
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event)
    {
        if(event.getRightClicked() instanceof Player) {
            Player otherPlayer = (Player)event.getRightClicked();
            Player thisPlayer = event.getPlayer();
            double deltaX = otherPlayer.getLocation().getX() - thisPlayer.getLocation().getX();
            double deltaZ = otherPlayer.getLocation().getZ() - thisPlayer.getLocation().getZ();
            Vector vectorBoost = new Vector( deltaX, 3.0, deltaZ );
            otherPlayer.setVelocity(vectorBoost);
        }
    }
}
