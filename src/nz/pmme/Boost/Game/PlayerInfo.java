package nz.pmme.Boost.Game;

import nz.pmme.Boost.Enums.PlayerGameState;
import org.bukkit.entity.Player;

public class PlayerInfo
{
    private Player player;
    private PlayerGameState playerGameState;
    private long coolDownEndMillis = 0L;
    private long startMillis = 0L;

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
        startMillis = System.currentTimeMillis();
    }

    public void setLost() {
        playerGameState = PlayerGameState.LOST;
    }

    public String getPlayerStateText() {
        return playerGameState.toString();
    }

    public boolean isOnCoolDown() {
        return ( System.currentTimeMillis() < this.coolDownEndMillis );
    }

    public void setCoolDown( long coolDownMillis ) {
        this.coolDownEndMillis = ( coolDownMillis > 0L ) ? System.currentTimeMillis() + coolDownMillis : 0L;
    }

    public void setCoolDownEnd( long coolDownEndMillis ) {
        if( coolDownEndMillis > 0L ) this.coolDownEndMillis = coolDownEndMillis;
    }

    public void resetCoolDown() {
        this.coolDownEndMillis = 0L;
    }

    public long getTimePlaying() {
        return ( System.currentTimeMillis() - this.startMillis );
    }
}
