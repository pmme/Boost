package nz.pmme.Boost;

import org.bukkit.plugin.java.JavaPlugin;

/**
 * Created by paul on 16-Apr-16.
 */
public class Main extends JavaPlugin
{
    @Override
    public void onEnable() {
        this.getServer().getPluginManager().registerEvents(new EventsListener(this), this);
    }

    @Override
    public void onDisable() {
    }
}
