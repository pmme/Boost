package nz.pmme.Boost.Tasks;

import nz.pmme.Boost.Data.PlayerStats;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Main;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Calendar;
import java.util.List;

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

        for( StatsPeriod statsPeriod : StatsPeriod.values() )
        {
            if( statsPeriod.isResettable() )
            {
                int periodCounter = 0;
                switch( statsPeriod ) {
                    case DAILY:
                        periodCounter = cal.get( Calendar.DAY_OF_WEEK );
                        break;
                    case WEEKLY:
                        periodCounter = cal.get( Calendar.WEEK_OF_YEAR );
                        break;
                    case MONTHLY:
                        periodCounter = cal.get( Calendar.MONTH );
                        break;
                }
                if( periodCounter != plugin.getLoadedConfig().getPeriodTracker( statsPeriod ) )
                {
                    List<PlayerStats> top3 = plugin.getDataHandler().queryLeaderBoard( statsPeriod, null, 3, true );
                    new BukkitRunnable() {
                        @Override
                        public void run() { plugin.getLoadedConfig().runPeriodicWinCommandsForTop3( statsPeriod, top3 ); }
                    }.runTask( this.plugin );

                    plugin.getDataHandler().deleteStats( statsPeriod, null, null );
                    plugin.getLoadedConfig().setPeriodTracker( statsPeriod, periodCounter );
                    plugin.getLogger().info( statsPeriod.getLogName() + " player stats reset." );
                }
            }
        }
    }

}
