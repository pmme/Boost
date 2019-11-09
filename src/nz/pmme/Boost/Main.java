package nz.pmme.Boost;

import nz.pmme.Boost.Commands.Commands;
import nz.pmme.Boost.Config.Config;
import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.GameManager;
import nz.pmme.Data.Database;
import nz.pmme.Data.DataHandler;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
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

    @Override
    public void onEnable() {
        this.config.init();
        this.dataHandler.generateTables();
        this.dataHandler.checkVersion();
        this.getGameManager().createConfiguredGames();
        this.getCommand( "boost" ).setExecutor( new Commands(this) );
        this.getServer().getPluginManager().registerEvents( new EventsListener(this), this );
    }

    @Override
    public void onDisable() {
        this.getGameManager().clearAllGames();
        this.disableBoost();
        this.database.closeConnection();
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

    public DataHandler getDataHandler() { return dataHandler; }

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

    public boolean hasPermission( CommandSender sender, String permission, Messages message ) {
        if( !sender.hasPermission( permission ) ) {
            messageSender( sender, message );
            return false;
        }
        return true;
    }

    public boolean isInBuildMode( UUID playerId ) {
        return this.builders.getOrDefault( playerId, false );
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
