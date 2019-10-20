package nz.pmme.Boost;

import nz.pmme.Boost.Enums.PlayerGameState;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;

public class PlayerInfo
{
    private Player player;
    private PlayerGameState playerGameState;

    public PlayerInfo( Player player )
    {
        this.player = player;
        this.playerGameState = PlayerGameState.QUEUED;
        player.setGameMode( GameMode.ADVENTURE );
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
        player.setGameMode( GameMode.SPECTATOR );
    }

    public String getPlayerStateText() {
        return playerGameState.toString();
    }
}
