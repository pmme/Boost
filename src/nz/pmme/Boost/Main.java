package nz.pmme.Boost;

import nz.pmme.Boost.Config.Config;
import nz.pmme.Boost.Config.Messages;
import nz.pmme.Boost.Game.GameManager;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by paul on 16-Apr-16.
 */
public class Main extends JavaPlugin
{
    private GameManager gameManager = new GameManager( this );
    private boolean boostEnabled = true;
    private Config config = new Config( this );

    @Override
    public void onEnable() {
        config.init();
        this.getGameManager().createConfiguredGames();
        this.getCommand( "boost" ).setExecutor( new Command_Boost(this) );
        this.getServer().getPluginManager().registerEvents( new EventsListener(this), this );
    }

    @Override
    public void onDisable() {
        this.getGameManager().clearAllGames();
        this.disableBoost();
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
}
