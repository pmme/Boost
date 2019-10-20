package nz.pmme.Boost;

import nz.pmme.Boost.Config.Config;
import nz.pmme.Boost.Exceptions.GameAlreadyExistsException;
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
        try {
            this.getGameManager().createConfiguredGames();
        } catch( GameAlreadyExistsException e ) {}

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
}
