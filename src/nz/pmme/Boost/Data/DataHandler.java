package nz.pmme.Boost.Data;


import java.sql.*;
import java.util.*;

import nz.pmme.Boost.Enums.StatsPeriod;
import org.bukkit.plugin.Plugin;

public class DataHandler
{
    private static final int thisVersion = 1;
    private Plugin plugin;
    private Database database;

    public DataHandler( Plugin plugin, Database database ) {
        this.plugin = plugin;
        this.database = database;
    }

    public void generateTables()
    {
        Connection connection = this.database.getConnection();
        if( connection == null ) return;
        try {
            PreparedStatement preparedStatement;

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS other(id INTEGER PRIMARY KEY,key VARCHAR(255) NOT NULL,value VARCHAR(255) NOT NULL)");
            preparedStatement.executeUpdate();
            preparedStatement.close();

            for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
                PreparedStatement preparedStatsStatement;
                preparedStatsStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + statsPeriod.getTable() + "(id INTEGER PRIMARY KEY,player_id VARCHAR(40) NOT NULL UNIQUE,player_name VARCHAR(255) NOT NULL,games INTEGER NOT NULL,wins INTEGER NOT NULL,losses INTEGER NOT NULL)");
                preparedStatsStatement.executeUpdate();
                preparedStatsStatement.close();
            }
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public boolean checkVersion()
    {
        boolean versionOkay = true;
        Connection connection = this.database.getConnection();
        if( connection == null ) return versionOkay;
        try {
            int version = 0;
            String statement = "SELECT value FROM other WHERE key='VERSION'";
            PreparedStatement preparedStatement = connection.prepareStatement( statement );
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String value = resultSet.getString("value");
                version = Integer.valueOf(value);
            }
            resultSet.close();
            preparedStatement.close();

            if( version == 0 )
            {
                // Set the first version number.
                preparedStatement = connection.prepareStatement( "INSERT INTO other(key, value) VALUES ('VERSION','" + thisVersion + "')" );
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
            else if( version > thisVersion )
            {
                plugin.getLogger().warning( "Database is a future version: " + version + ". This build uses version " + thisVersion + ". This may cause errors." );
                versionOkay = false;
            }
            else if( version < thisVersion )
            {
                plugin.getLogger().info( "Database version " + version + " will be upgraded to " + thisVersion + "." );

                // Upgrade the table structure to this version.

                // Update version number.
                preparedStatement = connection.prepareStatement( "UPDATE other SET value='" + thisVersion + "' WHERE key='VERSION'" );
                preparedStatement.executeUpdate();
                preparedStatement.close();
            }
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return versionOkay;
    }

    public void addPlayer( UUID playerId, String playerName )
    {
        Connection connection = this.database.getConnection();
        if( connection == null ) return;
        for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
            try {
                String insertPlayerSql = "INSERT OR IGNORE INTO " + statsPeriod.getTable() + "(player_id,player_name,games,wins,losses) VALUES (?,?,0,0,0)";
                PreparedStatement insertPlayerStatement = connection.prepareStatement( insertPlayerSql );
                insertPlayerStatement.setString( 1, playerId.toString() );
                insertPlayerStatement.setString( 2, playerName );
                insertPlayerStatement.executeUpdate();
                insertPlayerStatement.close();
            } catch( SQLException sQLException ) {
                plugin.getLogger().severe( "Failed to create or update " + statsPeriod.getTable() + " statistics record for " + playerName + " " + playerId.toString() );
                sQLException.printStackTrace();
            }
        }
    }

    public void logGame( UUID playerId )
    {
        Connection connection = this.database.getConnection();
        if( connection == null ) return;
        for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
            try {
                String updateGamesSql = "UPDATE " + statsPeriod.getTable() + " SET games=games+1 WHERE player_id=?";
                PreparedStatement updateGamesStatement = connection.prepareStatement( updateGamesSql );
                updateGamesStatement.setString( 1, playerId.toString() );
                updateGamesStatement.executeUpdate();
                updateGamesStatement.close();
            } catch( SQLException sQLException ) {
                plugin.getLogger().severe( "Failed to update " + statsPeriod.getTable() + " games count for player " + playerId.toString() );
                sQLException.printStackTrace();
            }
        }
    }

    public void logLoss( UUID playerId )
    {
        Connection connection = this.database.getConnection();
        if( connection == null ) return;
        for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
            try {
                String updateLossesSql = "UPDATE " + statsPeriod.getTable() + " SET losses=losses+1 WHERE player_id=?";
                PreparedStatement updateLossesStatement = connection.prepareStatement( updateLossesSql );
                updateLossesStatement.setString( 1, playerId.toString() );
                updateLossesStatement.executeUpdate();
                updateLossesStatement.close();
            } catch( SQLException sQLException ) {
                plugin.getLogger().severe( "Failed to update " + statsPeriod.getTable() + " losses count for player " + playerId.toString() );
                sQLException.printStackTrace();
            }
        }
    }

    public void logWin( UUID playerId )
    {
        Connection connection = this.database.getConnection();
        if( connection == null ) return;
        for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
            try {
                String updateWinsSql = "UPDATE " + statsPeriod.getTable() + " SET wins=wins+1 WHERE player_id=?";
                PreparedStatement updateWinsStatement = connection.prepareStatement( updateWinsSql );
                updateWinsStatement.setString( 1, playerId.toString() );
                updateWinsStatement.executeUpdate();
                updateWinsStatement.close();
            } catch( SQLException sQLException ) {
                plugin.getLogger().severe( "Failed to update " + statsPeriod.getTable() + " wins count for player " + playerId.toString() );
                sQLException.printStackTrace();
            }
        }
    }

    public PlayerStats queryPlayerStats( StatsPeriod statsPeriod, UUID playerId )
    {
        PlayerStats playerStats = null;
        Connection connection = this.database.getConnection();
        if( connection == null ) return playerStats;
        try {
            String queryStatsSql = "SELECT * FROM " + statsPeriod.getTable() + " WHERE player_id=?";
            PreparedStatement queryStatsStatement = connection.prepareStatement( queryStatsSql );
            queryStatsStatement.setString( 1, playerId.toString() );
            ResultSet resultSetStats = queryStatsStatement.executeQuery();
            if( resultSetStats.next() )
            {
                String queryRankSql = "SELECT COUNT(*) FROM " + statsPeriod.getTable() + " WHERE wins>?";
                PreparedStatement queryRankStatement = connection.prepareStatement( queryRankSql );
                queryRankStatement.setInt( 1, resultSetStats.getInt( "wins" ) );
                ResultSet resultSetRank = queryRankStatement.executeQuery();
                int rank = resultSetRank.next() ? resultSetRank.getInt(1)+1 : 0;
                resultSetRank.close();
                queryRankStatement.close();

                playerStats = new PlayerStats( resultSetStats.getString( "player_name" ), playerId, resultSetStats.getInt( "games" ), resultSetStats.getInt( "wins" ), resultSetStats.getInt( "losses" ), rank );
            }
            resultSetStats.close();
            queryStatsStatement.close();
        }
        catch (SQLException sQLException) {
            plugin.getLogger().severe( "Failed to query " + statsPeriod.getTable() + " game statistics for player " + playerId.toString() );
            sQLException.printStackTrace();
        }
        return playerStats;
    }

    public ArrayList<String> queryListOfPlayers( StatsPeriod statsPeriod )
    {
        ArrayList<String> results = new ArrayList<>();
        Connection connection = this.database.getConnection();
        if( connection == null ) return results;
        try {
            String queryPlayersSql = "SELECT player_name FROM " + statsPeriod.getTable() + " ORDER BY player_name";
            PreparedStatement queryPlayersStatement = connection.prepareStatement(queryPlayersSql);
            ResultSet resultSet = queryPlayersStatement.executeQuery();
            while( resultSet.next() ) {
                results.add( resultSet.getString("player_name") );
            }
            resultSet.close();
            queryPlayersStatement.close();
        }
        catch (SQLException sQLException) {
            plugin.getLogger().severe( "Failed to query " + statsPeriod.getTable() + " list of players" );
            sQLException.printStackTrace();
        }
        return results;
    }

    public List<PlayerStats> queryLeaderBoard( StatsPeriod statsPeriod, int numberToFetch, boolean mustHaveWon )
    {
        List<PlayerStats> leaderBoard = new ArrayList<>();
        Connection connection = this.database.getConnection();
        if( connection == null ) return leaderBoard;
        try {
            String queryLeaderBoardSql = "SELECT * FROM " + statsPeriod.getTable() + ( mustHaveWon ? " WHERE wins>0" : "" ) + " ORDER BY wins DESC, losses ASC, games DESC LIMIT " + numberToFetch;
            PreparedStatement queryLeaderBoardStatement = connection.prepareStatement( queryLeaderBoardSql );
            ResultSet resultSet = queryLeaderBoardStatement.executeQuery();
            while( resultSet.next() ) {
                try {
                    leaderBoard.add( new PlayerStats( resultSet.getString( "player_name" ), UUID.fromString( resultSet.getString( "player_id" ) ), resultSet.getInt( "games" ), resultSet.getInt( "wins" ), resultSet.getInt( "losses" ), 0 ) );
                } catch( IllegalArgumentException e ) {
                    plugin.getLogger().warning( "Failed to convert player_id '" + resultSet.getString( "player_id" ) + "' to UUID when querying leader board." );
                }
            }
            resultSet.close();
            queryLeaderBoardStatement.close();
        }
        catch (SQLException sQLException) {
            plugin.getLogger().severe( "Failed to query " + statsPeriod.getTable() + " leader board" );
            sQLException.printStackTrace();
        }
        return leaderBoard;
    }

    public void deleteStats( StatsPeriod statsPeriod, UUID playerId )
    {
        Connection connection = this.database.getConnection();
        if( connection == null ) return;
        try {
            StringBuilder deleteStatsSql = new StringBuilder();
            deleteStatsSql.append( "DELETE FROM " ).append( statsPeriod.getTable() );
            if( playerId != null ) {
                deleteStatsSql.append( " WHERE player_id=?" );
            }
            PreparedStatement deleteStatsStatement = connection.prepareStatement( deleteStatsSql.toString() );
            if( playerId != null ) {
                deleteStatsStatement.setString( 1, playerId.toString() );
            }
            deleteStatsStatement.executeUpdate();
            deleteStatsStatement.close();
        }
        catch (SQLException sQLException) {
            if( playerId != null ) {
                plugin.getLogger().severe( "Failed to delete " + statsPeriod.getTable() + " game statistics for player " + playerId.toString() );
            } else {
                plugin.getLogger().severe( "Failed to delete " + statsPeriod.getTable() + " game statistics." );
            }
            sQLException.printStackTrace();
        }
    }

//    private static void asyncExecute(final PreparedStatement preparedStatement) {
//        new BukkitRunnable(){
//
//            public void run() {
//                try {
//                    preparedStatement.execute();
//                }
//                catch (SQLException sQLException) {
//                    sQLException.printStackTrace();
//                }
//            }
//        }.runTaskAsynchronously(plugin);
//    }
//
//    private static void asyncExecuteBatch(final PreparedStatement preparedStatement) {
//        new BukkitRunnable(){
//
//            public void run() {
//                try {
//                    preparedStatement.executeBatch();
//                }
//                catch (SQLException sQLException) {
//                    sQLException.printStackTrace();
//                }
//            }
//        }.runTaskAsynchronously(plugin);
//    }
}

