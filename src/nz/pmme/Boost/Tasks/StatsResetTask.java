package nz.pmme.Boost.Tasks;

import nz.pmme.Boost.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;

public class StatsResetTask extends BukkitRunnable
{
    private Main plugin;
    private static long UPDATE_INTERVAL = 1 * 60 * 20;

    public StatsResetTask( Main plugin )
    {
        this.plugin = plugin;
    }

    public void startTask()
    {
        this.runTaskTimer( this.plugin, StatsResetTask.UPDATE_INTERVAL, StatsResetTask.UPDATE_INTERVAL );
    }

    @Override
    public void run()
    {
        final Calendar cal = Calendar.getInstance();
        cal.setFirstDayOfWeek(Calendar.SUNDAY);

        int day = cal.get(Calendar.DAY_OF_WEEK);
        int week = cal.get(Calendar.WEEK_OF_YEAR);
        int month = cal.get(Calendar.MONTH);

        if( day != plugin.getLoadedConfig().getTrackedDay() )
        {
            plugin.getLoadedConfig().setTrackedDay( day );
        }
        if( week != plugin.getLoadedConfig().getTrackedWeek() )
        {
            plugin.getLoadedConfig().setTrackedWeek( week );
        }
        if( month != plugin.getLoadedConfig().getTrackedMonth() )
        {
            plugin.getLoadedConfig().setTrackedMonth( month );
        }
    }

}
