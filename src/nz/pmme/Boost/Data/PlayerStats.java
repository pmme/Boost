package nz.pmme.Boost.Data;

public class PlayerStats
{
    private String name;
    private int games;
    private int wins;
    private int losses;
    private int rank;

    public PlayerStats( String name, int games, int wins, int losses, int rank ) {
        this.name = name;
        this.games = games;
        this.wins = wins;
        this.losses = losses;
        this.rank = rank;
    }

    public String getName() { return this.name; }
    public int getGames() { return this.games; }
    public int getWins() { return this.wins; }
    public int getLosses() { return this.losses; }
    public int getRank() { return this.rank; }
}
