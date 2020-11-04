package nz.pmme.Boost.inventory;

import nz.pmme.Boost.Config.BoostStick;
import nz.pmme.Boost.Main;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.List;

public class InventoryManager
{
    private final Main plugin;
    private static final ItemStack stackOfNothing = new ItemStack( Material.AIR, 0 );

    public InventoryManager( Main plugin )
    {
        this.plugin = plugin;
    }

    public void giveInstructionBook( Player player )
    {
        ItemStack instructionBook = plugin.getLoadedConfig().createInstructionBook();
        for( int slot = 8; slot >= 0; --slot ) {
            if( player.getInventory().getItem( slot ) == null || player.getInventory().getItem( slot ).getType() == Material.AIR ) {
                player.getInventory().setItem( slot, instructionBook );
                return;
            }
        }
        for( int slot = 9; slot < 36; ++slot ) {
            if( player.getInventory().getItem( slot ) == null || player.getInventory().getItem( slot ).getType() == Material.AIR ) {
                player.getInventory().setItem( slot, instructionBook );
                return;
            }
        }
    }

    public void giveMainGuiItem( Player player, boolean holdItem )
    {
        ItemStack mainGuiItem = plugin.getLoadedConfig().getGuiButtonConfig( "main" ).create();
        for( int slot = 0; slot < 36; ++slot ) {
            if( player.getInventory().getItem( slot ) == null || player.getInventory().getItem( slot ).getType() == Material.AIR ) {
                player.getInventory().setItem( slot, mainGuiItem );
                if( holdItem ) player.getInventory().setHeldItemSlot( slot );
                break;
            }
        }
    }

    public void giveBoostSticks( Player player )
    {
        List< BoostStick > sticks = plugin.getLoadedConfig().getBoostSticksAllowedForPlayer( player );
        if( sticks != null && !sticks.isEmpty() ) {
            int firstStickSlot = -1;
            int slot = 0;
            for( BoostStick stick : sticks ) {
                for( ; slot < 36; ++slot ) {
                    if( player.getInventory().getItem( slot ) == null || player.getInventory().getItem( slot ).getType() == Material.AIR ) {
                        player.getInventory().setItem( slot, stick.create() );
                        if( firstStickSlot == -1 ) firstStickSlot = slot;
                        break;
                    }
                }
            }
            if( firstStickSlot >= 0 && firstStickSlot <= 8 ) player.getInventory().setHeldItemSlot( firstStickSlot );
        }
    }

    public void removeBoostSticks( Player player )
    {
        ItemStack[] contents = player.getInventory().getContents();
        for( int slot = 0; slot < contents.length; ++slot )
        {
            for ( BoostStick boostStick : plugin.getLoadedConfig().getBoostSticks() ) {
                if( boostStick.isBoostStick( contents[slot] ) ) {
                    contents[slot] = InventoryManager.stackOfNothing;
                    break;
                }
            }
        }
        player.getInventory().setContents( contents );
    }

    public void removeBoostItems( Player player )
    {
        ItemStack[] contents = player.getInventory().getContents();
        for( int slot = 0; slot < contents.length; ++slot )
        {
            if( plugin.getLoadedConfig().getInstructionBookConfig().isInstructionBook( contents[slot] ) )
                contents[slot] = InventoryManager.stackOfNothing;
            else if( plugin.getLoadedConfig().getGuiButtonConfig( "main" ).isGUIItem( contents[slot] ) )
                contents[slot] = InventoryManager.stackOfNothing;
            else {
                for ( BoostStick boostStick : plugin.getLoadedConfig().getBoostSticks() ) {
                    if( boostStick.isBoostStick( contents[slot] ) ) {
                        contents[slot] = InventoryManager.stackOfNothing;
                        break;
                    }
                }
            }
        }
        player.getInventory().setContents( contents );
    }
}
