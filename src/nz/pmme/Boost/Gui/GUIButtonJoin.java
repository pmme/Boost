package nz.pmme.Boost.Gui;

import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class GUIButtonJoin implements GUIButton
{
    Main plugin;
    Game game;
    Material guiItemOverride;

    GUIButtonJoin( Main plugin, Game game ) {
        this.plugin = plugin;
        this.game = game;
        this.guiItemOverride = game.getGameConfig().getGuiItem();
    }

    @Override
    public ItemStack create() {
        return this.plugin.getLoadedConfig().getGuiButtonConfig( "join" ).create( "%game%", this.game.getGameConfig().getDisplayName(), this.guiItemOverride );
    }

    @Override
    public void onClick( InventoryClickEvent e, Player player ) {
        this.plugin.getGameManager().joinGame( player, this.game.getGameConfig().getName() );
    }
}
