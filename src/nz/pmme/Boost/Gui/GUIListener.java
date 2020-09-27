package nz.pmme.Boost.Gui;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class GUIListener implements Listener
{
    @EventHandler
    public void onInventoryClickEvent( InventoryClickEvent e )
    {
        if( e.getInventory().getHolder() != null && e.getInventory().getHolder() instanceof GUI )
        {
            e.setCancelled(true);
            GUI gui = (GUI)e.getInventory().getHolder();
            GUIButton button = gui.getButton( e.getSlot() );
            if( button != null ) {
                button.onClick( e, gui.getPlayer() );
                gui.getPlayer().closeInventory();
            }
        }
    }
}
