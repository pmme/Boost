package nz.pmme.Boost.Gui;

import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import nz.pmme.Utils.PairedValue;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

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
        List< PairedValue<String,String> > placeHolders = new ArrayList<>();
        placeHolders.add( new PairedValue<String,String>( "%game%", this.game.getGameConfig().getDisplayName() ) );
        placeHolders.add( new PairedValue<String,String>( "%players%", String.valueOf( this.game.getPlayerCount() ) ) );
        placeHolders.add( new PairedValue<String,String>( "%min%", String.valueOf( this.game.getGameConfig().getMinPlayers() ) ) );
        placeHolders.add( new PairedValue<String,String>( "%max%", String.valueOf( this.game.getGameConfig().getMaxPlayers() ) ) );
        return this.plugin.getLoadedConfig().getGuiButtonConfig( "join" ).create( placeHolders, this.guiItemOverride );
    }

    @Override
    public void onClick( InventoryClickEvent e, Player player ) {
        this.plugin.getGameManager().joinGame( player, this.game.getGameConfig().getName() );
    }
}
