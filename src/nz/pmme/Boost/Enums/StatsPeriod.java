package nz.pmme.Boost.Enums;

public enum StatsPeriod
{
    DAILY("player_stats_daily"),
    WEEKLY("player_stats_weekly"),
    MONTHLY("player_stats_monthly"),
    TOTAL("player_stats");

    private String table;

    StatsPeriod( String table ) { this.table = table; }

    public String getTable() { return this.table; }
}
