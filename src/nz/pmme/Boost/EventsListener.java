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
            Player player = (Player)event.getRightClicked();
            Vector vectorBoost = new Vector(0.0,10.0,0.0);
            player.setVelocity(vectorBoost);
        }
    }
}
