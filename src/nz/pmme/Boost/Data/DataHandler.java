package nz.pmme.Boost.Data;


import nz.pmme.Boost.Enums.StatsPeriod;
import org.bukkit.plugin.Plugin;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DataHandler
{
    private static final int thisVersion = 2;
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
                preparedStatsStatement = connection.prepareStatement("CREATE TABLE IF NOT EXISTS " + statsPeriod.getTable() + "(id INTEGER PRIMARY KEY,player_id VARCHAR(40) NOT NULL,player_name VARCHAR(255) NOT NULL,games INTEGER NOT NULL,wins INTEGER NOT NULL,losses INTEGER NOT NULL,game_name VARCHAR(255))");
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
        if( connection == null ) return true;
        try {
            int version = 0;
            String statement = "SELECT value FROM other WHERE key='VERSION'";
            PreparedStatement preparedStatement = connection.prepareStatement( statement );
            ResultSet resultSet = preparedStatement.executeQuery();
            if(resultSet.next()) {
                String value = resultSet.getString("value");
                version = Integer.parseInt(value);
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
                plugin.getLogger().warning( "Database version " + version + " will be upgraded to " + thisVersion + "." );

                preparedStatement = connection.prepareStatement( "BEGIN TRANSACTION;" );
                preparedStatement.executeUpdate();
                preparedStatement.close();

                // Upgrade the table structure to this version.
                for( StatsPeriod statsPeriod : StatsPeriod.values() )
                {
                    preparedStatement = connection.prepareStatement( "CREATE TABLE updated_" + statsPeriod.getTable() + "(id INTEGER PRIMARY KEY,player_id VARCHAR(40) NOT NULL,player_name VARCHAR(255) NOT NULL,games INTEGER NOT NULL,wins INTEGER NOT NULL,losses INTEGER NOT NULL,game_name VARCHAR(255));" );
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    preparedStatement = connection.prepareStatement( "INSERT INTO updated_" + statsPeriod.getTable() + "(player_id,player_name,games,wins,losses) SELECT player_id,player_name,games,wins,losses FROM " + statsPeriod.getTable() + ";" );
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    preparedStatement = connection.prepareStatement( "DROP TABLE " + statsPeriod.getTable() + ";" );
                    preparedStatement.executeUpdate();
                    preparedStatement.close();
                    preparedStatement = connection.prepareStatement( "ALTER TABLE updated_" + statsPeriod.getTable() + " RENAME TO " + statsPeriod.getTable() + ";" );
                    preparedStatement.execute();    // ALTER TABLE requires calling execute() not executeUpdate() even though it does not return results.
                    preparedStatement.close();
                }

                // Update version number.
                preparedStatement = connection.prepareStatement( "UPDATE other SET value='" + thisVersion + "' WHERE key='VERSION'" );
                preparedStatement.executeUpdate();
                preparedStatement.close();

                preparedStatement = connection.prepareStatement( "COMMIT TRANSACTION;" );
                preparedStatement.executeUpdate();
                preparedStatement.close();

                plugin.getLogger().info( "Database upgrade completed." );
            }
        }
        catch (SQLException sQLException) {
            sQLException.printStackTrace();
        }
        return versionOkay;
    }

    public void addPlayer( UUID playerId, String playerName, String gameName )
    {
        Connection connection = this.database.getConnection();
        if( connection == null ) return;
        for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
            try {
                String checkForPlayerSql = "SELECT 1 FROM " + statsPeriod.getTable() + " WHERE player_id=? AND game_name" + ( gameName != null ? "=?" : " IS NULL" );
                PreparedStatement checkForPlayerStatement = connection.prepareStatement( checkForPlayerSql );
                checkForPlayerStatement.setString( 1, playerId.toString() );
                if( gameName != null ) checkForPlayerStatement.setString( 2, gameName.toLowerCase() );
                ResultSet resultSet = checkForPlayerStatement.executeQuery();
                boolean playerRecordExists = resultSet.next();
                resultSet.close();
                checkForPlayerStatement.close();

                if( !playerRecordExists )
                {
                    String insertPlayerSql = "INSERT INTO " + statsPeriod.getTable() + "(player_id,player_name,games,wins,losses,game_name) VALUES (?,?,0,0,0,?)";
                    PreparedStatement insertPlayerStatement = connection.prepareStatement( insertPlayerSql );
                    insertPlayerStatement.setString( 1, playerId.toString() );
                    insertPlayerStatement.setString( 2, playerName );
                    if( gameName != null ) {
                        insertPlayerStatement.setString( 3, gameName.toLowerCase() );
                    } else {
                        insertPlayerStatement.setNull( 3, java.sql.Types.VARCHAR );
                    }
                    insertPlayerStatement.executeUpdate();
                    insertPlayerStatement.close();
                }
            } catch( SQLException sQLException ) {
                plugin.getLogger().severe( "Failed to create or update " + statsPeriod.getTable() + " statistics record for " + playerName + " " + playerId.toString() + " in game " + gameName );
                sQLException.printStackTrace();
            }
        }
    }

    private void updateCountColumn( StatsPeriod statsPeriod, UUID playerId, String playerName, String gameName, String countColumn )
    {
        Connection connection = this.database.getConnection();
        if( connection == null ) return;
        try {
            String updateSql = "UPDATE " + statsPeriod.getTable() + " SET player_name=?, " + countColumn + "=" + countColumn + "+1 WHERE player_id=? AND game_name" + ( gameName != null ? "=?" : " IS NULL" );
            PreparedStatement updateGamesStatement = connection.prepareStatement( updateSql );
            updateGamesStatement.setString( 1, playerName );
            updateGamesStatement.setString( 2, playerId.toString() );
            if( gameName != null ) updateGamesStatement.setString( 3, gameName.toLowerCase() );
            updateGamesStatement.executeUpdate();
            updateGamesStatement.close();
        } catch( SQLException sQLException ) {
            plugin.getLogger().severe( "Failed to update " + statsPeriod.getTable() + " " + countColumn + " count for player " + playerId.toString() + " in game " + gameName );
            sQLException.printStackTrace();
        }
    }

    public void logGame( UUID playerId, String playerName, String gameName )
    {
        for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
            this.updateCountColumn( statsPeriod, playerId, playerName, gameName, "games" );
        }
    }

    public void logLoss( UUID playerId, String playerName, String gameName )
    {
        for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
            this.updateCountColumn( statsPeriod, playerId, playerName, gameName, "losses" );
        }
    }

    public void logWin( UUID playerId, String playerName, String gameName )
    {
        for( StatsPeriod statsPeriod : StatsPeriod.values() ) {
            this.updateCountColumn( statsPeriod, playerId, playerName, gameName, "wins" );
        }
    }

    public PlayerStats queryPlayerStats( StatsPeriod statsPeriod, UUID playerId, String gameName )
    {
        PlayerStats playerStats = null;
        Connection connection = this.database.getConnection();
        if( connection == null ) return null;
        try {
            String queryStatsSql = "SELECT * FROM " + statsPeriod.getTable() + " WHERE player_id=? AND game_name" + ( gameName != null ? "=?" : " IS NULL" );
            PreparedStatement queryStatsStatement = connection.prepareStatement( queryStatsSql );
            queryStatsStatement.setString( 1, playerId.toString() );
            if( gameName != null ) queryStatsStatement.setString( 2, gameName.toLowerCase() );
            ResultSet resultSetStats = queryStatsStatement.executeQuery();
            if( resultSetStats.next() )
            {
                String queryRankSql = "SELECT COUNT(*) FROM " + statsPeriod.getTable() + " WHERE wins>? AND game_name" + ( gameName != null ? "=?" : " IS NULL" );
                PreparedStatement queryRankStatement = connection.prepareStatement( queryRankSql );
                queryRankStatement.setInt( 1, resultSetStats.getInt( "wins" ) );
                if( gameName != null ) queryRankStatement.setString( 2, gameName.toLowerCase() );
                ResultSet resultSetRank = queryRankStatement.executeQuery();
                int rank = resultSetRank.next() ? resultSetRank.getInt(1)+1 : 0;
                resultSetRank.close();
                queryRankStatement.close();

                playerStats = new PlayerStats( resultSetStats.getString( "player_name" ), playerId, gameName, resultSetStats.getInt( "games" ), resultSetStats.getInt( "wins" ), resultSetStats.getInt( "losses" ), rank );
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
            String queryPlayersSql = "SELECT DISTINCT player_name FROM " + statsPeriod.getTable() + " ORDER BY player_name";
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

    public List<PlayerStats> queryLeaderBoard( StatsPeriod statsPeriod, String gameName, int numberToFetch, boolean mustHaveWon )
    {
        List<PlayerStats> leaderBoard = new ArrayList<>();
        Connection connection = this.database.getConnection();
        if( connection == null ) return leaderBoard;
        try {
            String queryLeaderBoardSql = "SELECT * FROM " + statsPeriod.getTable() + " WHERE " + ( mustHaveWon ? "wins>0 AND game_name" : "game_name" ) + ( gameName != null ? "=?" : " IS NULL" ) + " ORDER BY wins DESC, losses ASC, games DESC LIMIT " + numberToFetch;
            PreparedStatement queryLeaderBoardStatement = connection.prepareStatement( queryLeaderBoardSql );
            if( gameName != null ) queryLeaderBoardStatement.setString( 1, gameName.toLowerCase() );
            ResultSet resultSet = queryLeaderBoardStatement.executeQuery();
            while( resultSet.next() ) {
                try {
                    leaderBoard.add( new PlayerStats( resultSet.getString( "player_name" ), UUID.fromString( resultSet.getString( "player_id" ) ), gameName, resultSet.getInt( "games" ), resultSet.getInt( "wins" ), resultSet.getInt( "losses" ), 0 ) );
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

    public void deleteStats( StatsPeriod statsPeriod, UUID playerId, String gameName )
    {
        Connection connection = this.database.getConnection();
        if( connection == null ) return;
        try {
            StringBuilder deleteStatsSql = new StringBuilder();
            deleteStatsSql.append( "DELETE FROM " ).append( statsPeriod.getTable() );
            if( playerId != null || gameName != null ) {
                deleteStatsSql.append( " WHERE " );
                if( playerId != null ) deleteStatsSql.append( "player_id=?" );
                if( playerId != null && gameName != null ) deleteStatsSql.append( " AND " );
                if( gameName != null ) deleteStatsSql.append( "game_name=?" );
            }
            PreparedStatement deleteStatsStatement = connection.prepareStatement( deleteStatsSql.toString() );
            int parameterIndex = 1;
            if( playerId != null ) {
                deleteStatsStatement.setString( parameterIndex++, playerId.toString() );
            }
            if( gameName != null ) {
                deleteStatsStatement.setString( parameterIndex++, gameName.toLowerCase() );
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

