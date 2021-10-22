package nz.pmme.Boost.Data;

public class GameStats
{
    private String name;
    private int games;
    private int wins;
    private int losses;
    private int bestTime;
    private int totalTime;
    private int averageTime;

    public GameStats( String name, int games, int wins, int losses, int bestTime, int totalTime )
    {
        this.name = name;
        this.games = games;
        this.wins = wins;
        this.losses = losses;
        this.bestTime = bestTime;
        this.totalTime = totalTime;
        this.averageTime = wins > 0 ? totalTime / wins : 0;
    }

    public String getName() { return this.name; }
    public int getGames() { return this.games; }
    public int getWins() { return this.wins; }
    public int getLosses() { return this.losses; }
    public int getBestTime() { return this.bestTime; }
    public int getTotalTime() { return this.totalTime; }
    public int getAverageTime() { return this.averageTime; }
}
