package nz.pmme.Boost.Gui;

import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Game.Game;
import nz.pmme.Boost.Main;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import java.util.HashMap;
import java.util.Map;

public class GUI implements InventoryHolder
{
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

    private void createInventory()
    {
        final int slots = 36;
        final int mainRow = 1;
        final int miscRow = 3;
        final int left = 0;
        final int centre = 4;
        final int right = 8;
        this.inventory = plugin.getServer().createInventory( this, slots, plugin.getLoadedConfig().getSignTitle() );
        if( this.plugin.getGameManager().isPlaying( player ) ) {
            if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "leave" ) ) {
                this.addButton( mainRow*9+centre, new GUIButtonLeave( this.plugin ) );
            }
        } else {
            if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "join" ) ) {
                int queuingGames = 0;
                for( Game game : plugin.getGameManager().getGames() ) {
                    if( game.isQueuing() ) ++queuingGames;
                }
                int gameSlot = mainRow*9+centre - queuingGames / 2;
                for( Game game : plugin.getGameManager().getGames() ) {
                    if( game.isQueuing() ) {
                        this.addButton( gameSlot++, new GUIButtonJoin( this.plugin, game ) );
                    }
                }
            }
        }
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "stats" ) ) {
            this.addButton( miscRow*9+left, new GUIButtonStats( this.plugin ) );
        }
        int leaderboardPos = miscRow*9+right;
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "top_total" ) ) {
            this.addButton( leaderboardPos--, new GUIButtonTop( this.plugin, StatsPeriod.TOTAL ) );
        }
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "top_monthly" ) ) {
            this.addButton( leaderboardPos--, new GUIButtonTop( this.plugin, StatsPeriod.MONTHLY ) );
        }
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "top_weekly" ) ) {
            this.addButton( leaderboardPos--, new GUIButtonTop( this.plugin, StatsPeriod.WEEKLY ) );
        }
        if( this.plugin.getLoadedConfig().isGuiButtonEnabled( "top_daily" ) ) {
            this.addButton( leaderboardPos--, new GUIButtonTop( this.plugin, StatsPeriod.DAILY ) );
        }
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }

    public void openInventory() {
        this.player.openInventory( this.inventory );
    }

    private void addButton( int slot, GUIButton button )
    {
        this.buttons.put( slot, button );
        this.inventory.setItem( slot, button.create() );
    }

    public GUIButton getButton( int slot ) {
        return this.buttons.get(slot);
    }
}
