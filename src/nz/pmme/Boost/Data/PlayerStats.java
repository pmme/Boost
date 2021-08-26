package nz.pmme.Boost.Data;

import java.util.UUID;

public class PlayerStats
{
    private String name;
    private UUID uuid;
    private String gameName;
    private int games;
    private int wins;
    private int losses;
    private int bestTime;
    private int lastTime;
    private int totalTime;
    private int averageTime;
    private int rank;

    public PlayerStats( String name, UUID uuid, String gameName, int games, int wins, int losses, int bestTime, int lastTime, int totalTime, int rank ) {
        this.name = name;
        this.uuid = uuid;
        this.gameName = gameName;
        this.games = games;
        this.wins = wins;
        this.losses = losses;
        this.bestTime = bestTime;
        this.lastTime = lastTime;
        this.totalTime = totalTime;
        this.averageTime = wins > 0 ? totalTime / wins : 0;
        this.rank = rank;
    }

    public String getName() { return this.name; }
    public UUID getUuid() { return uuid; }
    public String getGameName() { return gameName; }
    public int getGames() { return this.games; }
    public int getWins() { return this.wins; }
    public int getLosses() { return this.losses; }
    public int getBestTime() { return this.bestTime; }
    public int getLastTime() { return this.lastTime; }
    public int getTotalTime() { return this.totalTime; }
    public int getAverageTime() { return this.averageTime; }
    public int getRank() { return this.rank; }
}
