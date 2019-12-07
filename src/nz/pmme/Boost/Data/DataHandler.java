package nz.pmme.Data;


import java.sql.*;
import java.util.*;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;

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
        try {
            PreparedStatement preparedStatement;

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS other(id INTEGER PRIMARY KEY,key VARCHAR(255) NOT NULL,value VARCHAR(255) NOT NULL)");
            preparedStatement.execute();
            preparedStatement.close();

            preparedStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS player_stats(id INTEGER PRIMARY KEY,player_id VARCHAR(40) NOT NULL UNIQUE,player_name VARCHAR(255) NOT NULL,games INTEGER NOT NULL,wins INTEGER NOT NULL,losses INTEGER NOT NULL)");
            preparedStatement.execute();
            preparedStatement.close();
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
    }

    public boolean checkVersion()
    {
        boolean versionOkay = true;
        Connection connection = this.database.getConnection();
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
                preparedStatement.execute();
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
                preparedStatement.execute();
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
        try {
            String insertPlayerSql = "INSERT OR IGNORE INTO player_stats(player_id,player_name,games,wins,losses) VALUES (?,?,0,0,0)";
            PreparedStatement insertPlayerStatement = connection.prepareStatement( insertPlayerSql );
            insertPlayerStatement.setString( 1, playerId.toString() );
            insertPlayerStatement.setString( 2, playerName );
            insertPlayerStatement.execute();
            insertPlayerStatement.close();
        }
        catch (SQLException sQLException) {
            plugin.getLogger().severe( "Failed to create or update statistics record for " + playerName + " " + playerId.toString() );
            sQLException.printStackTrace();
        }
    }

    public void logGame( UUID playerId )
    {
        Connection connection = this.database.getConnection();
        try {
            String updateGamesSql = "UPDATE player_stats SET games=games+1 WHERE player_id=?";
            PreparedStatement updateGamesStatement = connection.prepareStatement( updateGamesSql );
            updateGamesStatement.setString( 1, playerId.toString() );
            updateGamesStatement.execute();
            updateGamesStatement.close();
        }
        catch (SQLException sQLException) {
            plugin.getLogger().severe( "Failed to update games count for player " + playerId.toString() );
            sQLException.printStackTrace();
        }
    }

    public void logLoss( UUID playerId )
    {
        Connection connection = this.database.getConnection();
        try {
            String updateLossesSql = "UPDATE player_stats SET losses=losses+1 WHERE player_id=?";
            PreparedStatement updateLossesStatement = connection.prepareStatement( updateLossesSql );
            updateLossesStatement.setString( 1, playerId.toString() );
            updateLossesStatement.execute();
            updateLossesStatement.close();
        }
        catch (SQLException sQLException) {
            plugin.getLogger().severe( "Failed to update losses count for player " + playerId.toString() );
            sQLException.printStackTrace();
        }
    }

    public void logWin( UUID playerId )
    {
        Connection connection = this.database.getConnection();
        try {
            String updateWinsSql = "UPDATE player_stats SET wins=wins+1 WHERE player_id=?";
            PreparedStatement updateWinsStatement = connection.prepareStatement( updateWinsSql );
            updateWinsStatement.setString( 1, playerId.toString() );
            updateWinsStatement.execute();
            updateWinsStatement.close();
        }
        catch (SQLException sQLException) {
            plugin.getLogger().severe( "Failed to update wins count for player " + playerId.toString() );
            sQLException.printStackTrace();
        }
    }

    public PlayerStats queryPlayerStats( UUID playerId )
    {
        Connection connection = this.database.getConnection();
        try {
            PlayerStats playerStats = null;
            String queryStatsSql = "SELECT * FROM player_stats WHERE player_id=?";
            PreparedStatement queryStatsStatement = connection.prepareStatement( queryStatsSql );
            queryStatsStatement.setString( 1, playerId.toString() );
            ResultSet resultSetStats = queryStatsStatement.executeQuery();
            if( resultSetStats.next() )
            {
                String queryRankSql = "SELECT COUNT(*) FROM player_stats WHERE wins>?";
                PreparedStatement queryRankStatement = connection.prepareStatement( queryRankSql );
                queryRankStatement.setInt( 1, resultSetStats.getInt( "wins" ) );
                ResultSet resultSetRank = queryRankStatement.executeQuery();
                int rank = resultSetRank.next() ? resultSetRank.getInt(1)+1 : 0;
                resultSetRank.close();
                queryRankStatement.close();

                playerStats = new PlayerStats( resultSetStats.getString( "player_name" ), resultSetStats.getInt( "games" ), resultSetStats.getInt( "wins" ), resultSetStats.getInt( "losses" ), rank );
            }
            resultSetStats.close();
            queryStatsStatement.close();
            return playerStats;
        }
        catch (SQLException sQLException) {
            plugin.getLogger().severe( "Failed to query game statistics for player " + playerId.toString() );
            sQLException.printStackTrace();
        }
        return null;
    }

    public ArrayList<String> queryListOfPlayers()
    {
        Connection connection = this.database.getConnection();
        try {
            ArrayList<String> results = new ArrayList<>();
            String queryPlayersSql = "SELECT player_name FROM player_stats ORDER BY player_name";
            PreparedStatement queryPlayersStatement = connection.prepareStatement(queryPlayersSql);
            ResultSet resultSet = queryPlayersStatement.executeQuery();
            while( resultSet.next() ) {
                results.add( resultSet.getString("player_name") );
            }
            resultSet.close();
            queryPlayersStatement.close();
            return results;
        }
        catch (SQLException sQLException) {
            plugin.getLogger().severe( "Failed to query list of players" );
            sQLException.printStackTrace();
        }
        return null;
    }

    public List<PlayerStats> queryLeaderBoard()
    {
        List<PlayerStats> leaderBoard = new ArrayList<>();
        Connection connection = this.database.getConnection();
        try {
            String queryLeaderBoardSql = "SELECT * FROM player_stats ORDER BY wins DESC LIMIT 10";
            PreparedStatement queryLeaderBoardStatement = connection.prepareStatement( queryLeaderBoardSql );
            ResultSet resultSet = queryLeaderBoardStatement.executeQuery();
            while( resultSet.next() ) {
                leaderBoard.add( new PlayerStats( resultSet.getString( "player_name" ), resultSet.getInt( "games" ), resultSet.getInt( "wins" ), resultSet.getInt( "losses" ), 0 ) );
            }
        }
        catch (SQLException sQLException) {
            plugin.getLogger().severe( "Failed to query leader board" );
            sQLException.printStackTrace();
        }
        return leaderBoard;
    }

    public void deleteStats( UUID playerId )
    {
        Connection connection = this.database.getConnection();
        try {
            StringBuilder deleteStatsSql = new StringBuilder();
            deleteStatsSql.append( "DELETE FROM player_stats" );
            if( playerId != null ) {
                deleteStatsSql.append( " WHERE player_id=?" );
            }
            PreparedStatement deleteStatsStatement = connection.prepareStatement( deleteStatsSql.toString() );
            if( playerId != null ) {
                deleteStatsStatement.setString( 1, playerId.toString() );
            }
            deleteStatsStatement.execute();
            deleteStatsStatement.close();
        }
        catch (SQLException sQLException) {
            if( playerId != null ) {
                plugin.getLogger().severe( "Failed to delete game statistics for player " + playerId.toString() );
            } else {
                plugin.getLogger().severe( "Failed to delete game statistics." );
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

