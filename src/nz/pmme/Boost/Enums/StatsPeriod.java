package nz.pmme.Boost.Enums;

public enum StatsPeriod
{
    DAILY( "player_stats_daily", "Daily", "day", true ),
    WEEKLY( "player_stats_weekly", "Weekly", "week", true ),
    MONTHLY( "player_stats_monthly", "Monthly", "month", true ),
    TOTAL( "player_stats","Total", "total", false );

    private String table;
    private String logName;
    private String configNode;
    private boolean resettable;

    StatsPeriod( String table, String name, String config, boolean resettable )
    {
        this.table = table;
        this.logName = name;
        this.configNode = config;
        this.resettable = resettable;
    }

    public String getTable() { return this.table; }
    public String getLogName() { return this.logName; }
    public String getConfigNode() { return this.configNode; }
    public boolean isResettable() { return this.resettable; }

    public static StatsPeriod fromString( String statsPeriod ) {
        try {
            return StatsPeriod.valueOf( statsPeriod.toUpperCase() );
        } catch( IllegalArgumentException e ) {
            return null;
        }
    }
}
