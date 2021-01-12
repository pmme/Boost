package nz.pmme.Boost.Gui;

import nz.pmme.Boost.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIButtonStats implements GUIButton
{
    Main plugin;

    public GUIButtonStats( Main plugin ) {
        this.plugin = plugin;
    }

    @Override
    public ItemStack create() {
        return this.plugin.getLoadedConfig().getGuiButtonConfig( "stats" ).create();
    }

    @Override
    public void onClick( InventoryClickEvent e, Player player ) {
        plugin.getGameManager().displayPlayerStats( player, player, null );
    }
}
