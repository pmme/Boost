package nz.pmme.Boost.Gui;

import nz.pmme.Boost.Config.GUIButtonConfig;
import nz.pmme.Boost.Enums.GameType;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class GUI implements InventoryHolder
{
    static final int rowSize = 9;

    private Main plugin;
    private Player player;
    private Inventory inventory;
    private Map< Integer, GUIButton > buttons = new HashMap<>();

    public GUI( Main plugin, Player player ) {
        this.plugin = plugin;
        this.player = player;
        this.createInventory();
    }

    public Player getPlayer() {
        return player;
    }

    private int calcSlot( int row, int col ) {
        return (row-1)*rowSize+(col-1);
    }

    private void createInventory()
    {
        final int inventorySize = plugin.getLoadedConfig().getGuiRows() * rowSize;
        boolean[] slotsOccupied = new boolean[inventorySize];
        this.inventory = plugin.getServer().createInventory( this, inventorySize, ChatColor.translateAlternateColorCodes( '&', plugin.getLoadedConfig().getGuiName() ) );
        if( this.plugin.getGameManager().isPlaying( player ) ) {
            if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "leave" ) ) {
                int slot = this.addButton( "leave", new GUIButtonLeave( this.plugin ) );
                slotsOccupied[slot] = true;
            }
        } else {
            GUIButtonConfig joinButtonConfig = this.plugin.getLoadedConfig().getGuiButtonConfig( "join" );
            if( joinButtonConfig.isEnabled() ) {
                int queuingGames = 0;
                for( Game game : plugin.getGameManager().getGames() ) {
                    if( game.isQueuing() || ( game.getGameConfig().getGameType() == GameType.PARKOUR && game.isRunning() ) ) ++queuingGames;
                }
                int gameSlot = this.calcSlot( joinButtonConfig.getRow(), joinButtonConfig.getCol() );
                if( joinButtonConfig.getCol() == rowSize/2+1 ) gameSlot -= queuingGames / 2;
                else if( joinButtonConfig.getCol() == rowSize ) gameSlot -= queuingGames;
                for( Game game : plugin.getGameManager().getGames() ) {
                    if( game.isQueuing() || ( game.getGameConfig().getGameType() == GameType.PARKOUR && game.isRunning() ) ) {
                        int slot = this.addButton( gameSlot++, new GUIButtonJoin( this.plugin, game ) );
                        slotsOccupied[slot] = true;
                    }
                }
            }
        }
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "stats" ) ) {
            int slot = this.addButton( "stats", new GUIButtonStats( this.plugin ) );
            slotsOccupied[slot] = true;
        }
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "top_total" ) ) {
            int slot = this.addButton( "top_total", new GUIButtonTop( this.plugin, StatsPeriod.TOTAL ) );
            slotsOccupied[slot] = true;
        }
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "top_monthly" ) ) {
            int slot = this.addButton( "top_monthly", new GUIButtonTop( this.plugin, StatsPeriod.MONTHLY ) );
            slotsOccupied[slot] = true;
        }
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "top_weekly" ) ) {
            int slot = this.addButton( "top_weekly", new GUIButtonTop( this.plugin, StatsPeriod.WEEKLY ) );
            slotsOccupied[slot] = true;
        }
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "top_daily" ) ) {
            int slot = this.addButton( "top_daily", new GUIButtonTop( this.plugin, StatsPeriod.DAILY ) );
            slotsOccupied[slot] = true;
        }
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "fill" ) ) {
            for( int slot = 0; slot < inventorySize; ++slot ) {
                if( !slotsOccupied[slot] ) {
                    this.addButton( slot, new GUIButtonFill( this.plugin ) );
                }
            }
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void openInventory() {
        this.player.openInventory( this.inventory );
    }

    private int addButton( int slot, GUIButton button )
    {
        this.buttons.put( slot, button );
        this.inventory.setItem( slot, button.create() );
        return slot;
    }

    private int addButton( String buttonName, GUIButton button )
    {
        GUIButtonConfig buttonConfig = this.plugin.getLoadedConfig().getGuiButtonConfig( buttonName );
        return this.addButton( this.calcSlot( buttonConfig.getRow(), buttonConfig.getCol() ), button );
    }

    public GUIButton getButton( int slot ) {
        return this.buttons.get(slot);
    }
}
