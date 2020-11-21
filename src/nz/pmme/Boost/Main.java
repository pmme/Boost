package nz.pmme.Boost;

import nz.pmme.Boost.Commands.Commands;
import nz.pmme.Boost.Config.Config;
import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Data.DataHandler;
import nz.pmme.Boost.Data.Database;
import nz.pmme.Boost.Game.GameManager;
import nz.pmme.Boost.Gui.GUIListener;
import nz.pmme.Boost.Tasks.StatsResetTask;
import nz.pmme.Boost.inventory.InventoryManager;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Created by paul on 16-Apr-16.
 */
public class Main extends JavaPlugin
{
    private GameManager gameManager = new GameManager( this );
    private boolean boostEnabled = true;
    private Config config = new Config( this );
    private Database database = new Database( this );
    private DataHandler dataHandler = new DataHandler( this, this.database );
    private Map< UUID, Boolean > builders = new HashMap<>();
    private StatsResetTask statsResetTask = new StatsResetTask( this );
    private Commands commands = new Commands(this);
    private InventoryManager inventoryManager = new InventoryManager( this );

    @Override
    public void onEnable() {
        this.getLoadedConfig().init();
        this.getDataHandler().generateTables();
        this.getDataHandler().checkVersion();
        this.getGameManager().createConfiguredGames();
        this.getCommand( "boost" ).setExecutor( this.commands );
        this.getCommand( "boost" ).setTabCompleter( this.commands );
        this.getServer().getPluginManager().registerEvents( new EventsListener(this), this );
        this.getServer().getPluginManager().registerEvents( new GUIListener(), this );
        this.statsResetTask.startTask();

        if( this.getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new nz.pmme.Boost.papi.BoostExpansion(this).register();
        }
    }

    @Override
    public void onDisable() {
        try { this.statsResetTask.cancel(); } catch( IllegalStateException ignored ) {}
        this.getGameManager().clearAllGames();
        this.disableBoost();
        this.getDatabase().closeConnection();
    }

    public void reload() {
        this.getGameManager().clearAllGames();
        this.getDatabase().closeConnection();
        this.getLoadedConfig().reload();
        this.getDataHandler().generateTables();
        this.getDataHandler().checkVersion();
        this.getGameManager().createConfiguredGames();
        this.getLogger().info( "Boost reloaded." );
    }

    public boolean isBoostEnabled() {
        return boostEnabled;
    }

    public void enableBoost() {
        boostEnabled = true;
    }

    public void disableBoost() {
        boostEnabled = false;
    }

    public GameManager getGameManager() {
        return gameManager;
    }

    public Config getLoadedConfig() {
        return config;
    }

    public Database getDatabase() {
        return database;
    }

    public DataHandler getDataHandler() {
        return dataHandler;
    }

    public InventoryManager getInventoryManager() { return inventoryManager; }

    public UUID findPlayerByName( String name )
    {
        for( Player player : this.getServer().getOnlinePlayers() ) {
            if( ChatColor.stripColor( player.getDisplayName() ).equalsIgnoreCase( name ) || player.getName().equalsIgnoreCase( name ) ) {
                return player.getUniqueId();
            }
        }
        for( OfflinePlayer player : this.getServer().getOfflinePlayers() ) {
            String playerName = player.getName();
            if( playerName != null && playerName.equalsIgnoreCase( name ) ) {
                return player.getUniqueId();
            }
        }
        return null;
    }

    public void messageSender( CommandSender sender, String message ) {
        sender.sendMessage( this.getLoadedConfig().getPrefix() + message );
    }

    public void messageSender( CommandSender sender, Messages message ) {
        sender.sendMessage( this.getLoadedConfig().getPrefix() + this.getLoadedConfig().getMessage( message ) );
    }

    public void messageSender( CommandSender sender, Messages message, String gameName ) {
        String gameNameColoured = ChatColor.translateAlternateColorCodes( '&', gameName );
        sender.sendMessage( this.getLoadedConfig().getPrefix() + this.getLoadedConfig().getMessage( message ).replaceAll( "%game%", gameNameColoured ) );
    }

    public void messageSender( CommandSender sender, Messages message, String placeHolder, String replacementValue ) {
        String replacementValueColoured = ChatColor.translateAlternateColorCodes( '&', replacementValue );
        sender.sendMessage( this.getLoadedConfig().getPrefix() + this.getLoadedConfig().getMessage( message ).replaceAll( placeHolder, replacementValueColoured ) );
    }

    public void messageSender( CommandSender sender, Messages message, String gameName, String placeHolder, String replacementValue ) {
        String gameNameColoured = ChatColor.translateAlternateColorCodes( '&', gameName );
        String replacementValueColoured = ChatColor.translateAlternateColorCodes( '&', replacementValue );
        sender.sendMessage( this.getLoadedConfig().getPrefix() + this.getLoadedConfig().getMessage( message ).replaceAll( "%game%", gameNameColoured ).replaceAll( placeHolder, replacementValueColoured ) );
    }

    public String formatMessage( Messages message, String gameName, String placeHolder, String replacementValue ) {
        String gameNameColoured = ChatColor.translateAlternateColorCodes( '&', gameName );
        String replacementValueColoured = ChatColor.translateAlternateColorCodes( '&', replacementValue );
        return this.getLoadedConfig().getMessage( message ).replaceAll( "%game%", gameNameColoured ).replaceAll( placeHolder, replacementValueColoured );
    }

    public boolean checkPermission( CommandSender sender, String permission, Messages message ) {
        if( !sender.hasPermission( permission ) ) {
            messageSender( sender, message );
            return false;
        }
        return true;
    }

    public boolean isInBuildMode( UUID playerId ) {
        return this.builders.getOrDefault( playerId, false );
    }

    public boolean isInGameWorld( Entity entity ) {
        return this.getLoadedConfig().isGameWorld( entity.getWorld().getName() );
    }

    public boolean isInGameWorld( CommandSender sender ) {
        if( sender instanceof Player ) {
            return this.getLoadedConfig().isGameWorld( ((Player)sender).getWorld().getName() );
        }
        return false;
    }

    public boolean isGameWorld( String world ) {
        return this.getLoadedConfig().isGameWorld( world );
    }

    public void setBuilder( UUID playerId ) {
        this.builders.put( playerId, true );
    }

    public void setNotBuilder( UUID playerId ) {
        this.builders.put( playerId, false );
    }

    public void removeBuilder( UUID playerId ) {
        this.builders.remove( playerId );
    }
}
