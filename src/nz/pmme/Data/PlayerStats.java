package nz.pmme.Data;

import java.util.UUID;

public class PlayerStats
{
    private String name;
    private int games;
    private int wins;
    private int losses;

    public PlayerStats( String name, int games, int wins, int losses ) {
        this.name = name;
        this.games = games;
        this.wins = wins;
        this.losses = losses;
    }

    public String getName() { return this.name; }
    public int getGames() { return this.games; }
    public int getWins() { return this.wins; }
    public int getLosses() { return this.losses; }
}
