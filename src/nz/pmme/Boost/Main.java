package nz.pmme.Boost;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by paul on 16-Apr-16.
 */
public class Main extends JavaPlugin
{
    private GameManager gameManager = new GameManager( this );
    private boolean boostEnabled = true;

    @Override
    public void onEnable() {
        this.saveDefaultConfig();
        this.loadConfig();

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

    public void loadConfig()
    {
        this.reloadConfig();
        // TODO: load config stuff like...
        //messageWinner = this.getConfig().getString( "message.winner" );
    }
}
