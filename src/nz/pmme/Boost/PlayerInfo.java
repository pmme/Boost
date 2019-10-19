package nz.pmme.Boost;

import nz.pmme.Boost.Enums.PlayerGameState;
import org.bukkit.entity.Player;

public class PlayerInfo
{
    Player player;
    PlayerGameState playerGameState;

    public PlayerInfo( Player player )
    {
        this.player = player;
        this.playerGameState = PlayerGameState.QUEUED;
    }

    public Player getPlayer() {
        return player;
    }

    public boolean isActive() {
        return playerGameState == PlayerGameState.ACTIVE;
    }

    public void setActive() {
        playerGameState = PlayerGameState.ACTIVE;
    }

    public void setLost() {
        playerGameState = PlayerGameState.LOST;
    }

    public String getPlayerStateText() {
        return playerGameState.toString();
    }
}
