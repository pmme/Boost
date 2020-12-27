package nz.pmme.Boost.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import nz.pmme.Boost.Data.PlayerStats;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Enums.Place;
import nz.pmme.Boost.Main;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.util.List;

public class BoostExpansion extends PlaceholderExpansion {
    private Main plugin;

    public BoostExpansion( Main plugin ) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public String getIdentifier() {
        return "boost";
    }

    @Override
    public String getVersion() {
        return plugin.getDescription().getVersion();
    }

    private String getStatsValue( PlayerStats playerStats, String params, StatsPeriod period, String param )
    {
        switch( param ) {
            case "player":
                return ( playerStats != null ) ? String.valueOf( playerStats.getName() ) : "-";
            case "wins":
                return ( playerStats != null ) ? String.valueOf( playerStats.getWins() ) : "0";
            case "losses":
                return ( playerStats != null ) ? String.valueOf( playerStats.getLosses() ) : "0";
            case "games":
                return ( playerStats != null ) ? String.valueOf( playerStats.getGames() ) : "0";
            case "rank":
                return ( playerStats != null ) ? String.valueOf( playerStats.getRank() ) : "0";
            default:
                plugin.getLogger().severe( "Error in syntax of PlaceholdAPI used with Boost, " + params );
                return "";
        }
    }

    @Override
    public String onRequest( OfflinePlayer player, String params )
    {
        String paramsLower = params.toLowerCase();
        int firstUnderscore = paramsLower.indexOf( '_' );
        int secondUnderscore = ( firstUnderscore != -1 ) ? paramsLower.indexOf( '_', firstUnderscore + 1 ) : -1;
        int thirdUnderscore = ( secondUnderscore != -1 ) ? paramsLower.indexOf( '_', secondUnderscore + 1 ) : -1;
        if( paramsLower.startsWith( "top" ) )
        {
            // top_daily_first_player
            // top_weekly_second_wins
            // top_monthly_third_losses
            // top_total_first_rank
            // top_daily_third_games
            try {
                String periodParam = paramsLower.substring( firstUnderscore+1, secondUnderscore );
                StatsPeriod period = StatsPeriod.fromString( periodParam );
                if( period == null ) {
                    plugin.getLogger().severe( "Error in syntax of PlaceholdAPI used with Boost, " + params );
                    return "";
                }
                String positionParam = paramsLower.substring( secondUnderscore+1, thirdUnderscore );
                Place place = Place.fromString( positionParam );
                if( place == null ) {
                    plugin.getLogger().severe( "Error in syntax of PlaceholdAPI used with Boost, " + params );
                    return "";
                }

                List< PlayerStats > top3 = plugin.getDataHandler().queryLeaderBoard( period, 3, true );
                PlayerStats topPlayer = place.getAsIndex() < top3.size() ? top3.get( place.getAsIndex() ) : null;

                String param = paramsLower.substring( thirdUnderscore+1 );
                return getStatsValue( topPlayer, params, period, param );
            } catch( NumberFormatException | StringIndexOutOfBoundsException e ) {
                plugin.getLogger().severe( "Error in syntax of PlaceholdAPI used with Boost, " + params );
                return "";
            }
        }
        if( paramsLower.startsWith( "player" ) )
        {
            // player_daily_player
            // player_total_wins
            // player_monthly_losses
            // player_weekly_games
            // player_total_rank
            try {
                String periodParam = paramsLower.substring( firstUnderscore+1, secondUnderscore );
                StatsPeriod period = StatsPeriod.fromString( periodParam );
                if( period == null ) {
                    plugin.getLogger().severe( "Error in syntax of PlaceholdAPI used with Boost, " + params );
                    return "";
                }
                String param = paramsLower.substring( secondUnderscore+1 );
                PlayerStats playerStats = plugin.getDataHandler().queryPlayerStats( period, player.getUniqueId() );
                return getStatsValue( playerStats, params, period, param );
            } catch( NumberFormatException | StringIndexOutOfBoundsException e ) {
                plugin.getLogger().severe( "Error in syntax of PlaceholdAPI used with Boost, " + params );
                return "";
            }
        }
        return null;
    }

    @Override
    public String onPlaceholderRequest( Player p, String params ) {
        return onRequest( p, params );
    }
}
