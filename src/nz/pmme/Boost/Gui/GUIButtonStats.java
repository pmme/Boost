package nz.pmme.Boost.Gui;

import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIButtonStats implements GUIButton
{
    Main plugin;
    StatsPeriod statsPeriod;
    String configNode;

    public GUIButtonStats( Main plugin, StatsPeriod statsPeriod ) {
        this.plugin = plugin;
        this.statsPeriod = statsPeriod;
        this.configNode = statsPeriod == StatsPeriod.TOTAL ? "stats" : "stats_" + statsPeriod.toString().toLowerCase();
    }

    @Override
    public ItemStack create() {
        return this.plugin.getLoadedConfig().getGuiButtonConfig( this.configNode ).create();
    }

    @Override
    public void onClick( InventoryClickEvent e, Player player ) {
        plugin.getGameManager().displayPlayerStats( player, player, this.statsPeriod, null );
    }
}
