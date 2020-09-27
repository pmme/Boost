package nz.pmme.Boost.Gui;

import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public interface GUIButton {
    ItemStack create();
    void onClick( InventoryClickEvent e, Player player );
}
