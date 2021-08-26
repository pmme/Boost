package nz.pmme.Boost.papi;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import nz.pmme.Boost.Data.PlayerStats;
import nz.pmme.Boost.Enums.GameType;
import nz.pmme.Boost.Enums.StatsPeriod;
import nz.pmme.Boost.Enums.Place;
import nz.pmme.Boost.Game.Game;
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
                return ( playerStats != null ) ? playerStats.getName() : "-";
            case "wins":
                return ( playerStats != null ) ? String.valueOf( playerStats.getWins() ) : "0";
            case "losses":
                return ( playerStats != null ) ? String.valueOf( playerStats.getLosses() ) : "0";
            case "games":
                return ( playerStats != null ) ? String.valueOf( playerStats.getGames() ) : "0";
            case "besttime":
                return ( playerStats != null && playerStats.getBestTime() > 0 ) ? ( (double)playerStats.getBestTime()/1000.0 )+"s" : "-";
            case "lasttime":
                return ( playerStats != null && playerStats.getLastTime() > 0 ) ? ( (double)playerStats.getLastTime()/1000.0 )+"s" : "-";
            case "avgtime":
                return ( playerStats != null && playerStats.getAverageTime() > 0 ) ? ( (double)playerStats.getAverageTime()/1000.0 )+"s" : "-";
            case "rank":
                return ( playerStats != null ) ? String.valueOf( playerStats.getRank() ) : "-";
            default:
                plugin.getLogger().severe( "Error in StatsValue syntax of place holder used with Boost, " + params );
                return null;
        }
    }

    private String getGameValue( Game game, String params, String param )
    {
        switch( param ) {
            case "players":
                return String.valueOf( game.getPlayerCount() );
            case "playing":
                return String.valueOf( game.getActivePlayerCount() );
            case "max":
                return String.valueOf( game.getGameConfig().getMaxPlayers() );
            case "min":
                return String.valueOf( game.getGameConfig().getMinPlayers() );
            case "status":
                return game.getGameStateText();
            case "time":
                if( !game.isQueuing() || game.getPlayerCount() < game.getGameConfig().getMinPlayers() ) return "";
                return String.valueOf( game.getRemainingQueueTime() );
            default:
                plugin.getLogger().severe( "Error in GameValue syntax of place holder used with Boost, " + params );
                return null;
        }
    }

    @Override
    public String onRequest( OfflinePlayer player, String params )
    {
        String[] paramsSplit = params.toLowerCase().split( "_" );
        if( paramsSplit.length >= 4 && paramsSplit.length < 6 && paramsSplit[0].equals( "top" ) )
        {
            // top_daily_first_player
            // top_weekly_second_wins_arena1
            // top_monthly_third_losses
            // top_total_first_rank_arena1
            // top_daily_third_games
            // top_weekly_first_besttime_arena1
            // top_monthly_second_lasttime_arena1
            // top_daily_second_avgtime_arena1
            StatsPeriod period = StatsPeriod.fromString( paramsSplit[1] ); // daily, weekly, monthly, total
            if( period == null ) {
                plugin.getLogger().severe( "Error in StatsPeriod syntax of place holder used with Boost, " + params );
                return null;
            }
            Place place = Place.fromString( paramsSplit[2] ); // first, second, third, fourth, fifth, sixth, seventh, eighth, ninth, tenth
            if( place == null ) {
                plugin.getLogger().severe( "Error in Place syntax of place holder used with Boost, " + params );
                return null;
            }
            String gameName = paramsSplit.length == 5 ? paramsSplit[4] : null; // arena1, arena2
            Game game = gameName != null ? plugin.getGameManager().getGame( gameName ) : null;
            if( paramsSplit.length == 5 && game == null ) {
                plugin.getLogger().severe( "Error in Game syntax of place holder used with Boost, " + params );
                return null;
            }
            boolean isParkour = game != null && game.getGameConfig().getGameType() == GameType.PARKOUR;
            if( paramsSplit[3].equals( "besttime" ) || paramsSplit[3].equals( "lasttime" ) || paramsSplit[3].equals( "avgtime" ) ) {
                if( game == null ) {
                    plugin.getLogger().severe( "Game name required for besttime, lasttime or avgtime place holders used with Boost, " + params );
                    return null;
                }
                if( !isParkour ) {
                    plugin.getLogger().severe( "Must be parkour game type for besttime, lasttime or avgtime place holders used with Boost, " + params );
                    return null;
                }
            }
            List< PlayerStats > topPlayers = plugin.getDataHandler().queryLeaderBoard( period, gameName, 10, true, isParkour );
            PlayerStats topPlayer = place.getAsIndex() < topPlayers.size() ? topPlayers.get( place.getAsIndex() ) : null;
            return this.getStatsValue( topPlayer, params, period, paramsSplit[3] ); // player, wins, losses, games, besttime, lasttime, avgtime, rank
        }
        if( paramsSplit.length >= 3 && paramsSplit.length < 5 && paramsSplit[0].equals( "player" ) )
        {
            // player_daily_player
            // player_total_wins_arena1
            // player_monthly_losses
            // player_weekly_games_arena1
            // player_total_rank
            // player_weekly_besttime_arena1
            // player_monthly_lasttime_arena1
            // player_daily_avgtime_arena1
            StatsPeriod period = StatsPeriod.fromString( paramsSplit[1] ); // daily, weekly, monthly, total
            if( period == null ) {
                plugin.getLogger().severe( "Error in StatsPeriod syntax of place holder used with Boost, " + params );
                return null;
            }
            String gameName = paramsSplit.length == 4 ? paramsSplit[3] : null; // arena1, arena2
            Game game = gameName != null ? plugin.getGameManager().getGame( gameName ) : null;
            if( paramsSplit.length == 4 && game == null ) {
                plugin.getLogger().severe( "Error in Game syntax of place holder used with Boost, " + params );
                return null;
            }
            boolean isParkour = game != null && game.getGameConfig().getGameType() == GameType.PARKOUR;
            if( paramsSplit[2].equals( "besttime" ) || paramsSplit[2].equals( "lasttime" ) || paramsSplit[2].equals( "avgtime" ) ) {
                if( game == null ) {
                    plugin.getLogger().severe( "Game name required for besttime, lasttime or avgtime place holders used with Boost, " + params );
                    return null;
                }
                if( !isParkour ) {
                    plugin.getLogger().severe( "Must be parkour game type for besttime, lasttime or avgtime place holders used with Boost, " + params );
                    return null;
                }
            }
            PlayerStats playerStats = plugin.getDataHandler().queryPlayerStats( period, player.getUniqueId(), gameName, isParkour );
            return this.getStatsValue( playerStats, params, period, paramsSplit[2] ); // player, wins, losses, games, besttime, lasttime, avgtime, rank
        }
        if( paramsSplit.length == 3 && paramsSplit[0].equals( "game" ) )
        {
            // game_arena1_players
            // game_arena2_playing
            // game_arena2_max
            // game_arena1_min
            // game_arena1_status
            // game_arena2_time
            Game game = plugin.getGameManager().getGame( paramsSplit[1] ); // arena1, arena2
            if( game == null ) {
                plugin.getLogger().severe( "Error in Game syntax of place holder used with Boost, " + params );
                return null;
            }
            return this.getGameValue( game, params, paramsSplit[2] ); // players, playing, max, min, status, time
        }
        plugin.getLogger().severe( "Error in syntax of place holder used with Boost, " + params );
        return null;
    }

    @Override
    public String onPlaceholderRequest( Player p, String params ) {
        return onRequest( p, params );
    }
}
