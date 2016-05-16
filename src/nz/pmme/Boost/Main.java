package nz.pmme.Boost;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by paul on 16-Apr-16.
 */
public class Main extends JavaPlugin
{
    private Players players = new Players( this );
    private boolean boostEnabled = false;

    @Override
    public void onEnable() {
        this.getCommand( "boost" ).setExecutor( new Command_Boost(this) );
        this.getServer().getPluginManager().registerEvents( new EventsListener(this), this );
    }

    @Override
    public void onDisable() {
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

    public Players getPlayers() {
        return players;
    }
}
