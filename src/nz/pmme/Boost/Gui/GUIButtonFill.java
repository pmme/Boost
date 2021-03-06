package nz.pmme.Boost.Gui;

import nz.pmme.Boost.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIButtonFill implements GUIButton
{
    Main plugin;

    GUIButtonFill( Main plugin ) {
        this.plugin = plugin;
    }

    @Override
    public ItemStack create() {
        return this.plugin.getLoadedConfig().getGuiButtonConfig( "fill" ).create();
    }

    @Override
    public void onClick( InventoryClickEvent e, Player player ) {}
}
