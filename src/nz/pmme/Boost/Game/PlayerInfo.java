package nz.pmme.Boost.Game;

import nz.pmme.Boost.Enums.PlayerGameState;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class PlayerInfo
{
    private Player player;
    private PlayerGameState playerGameState;
    private boolean onCoolDown = false;
    private BukkitTask coolDownTask = null;

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

    public boolean isOnCoolDown() {
        return onCoolDown;
    }

    public void setOnCoolDown( JavaPlugin plugin, long coolDown ) {
        onCoolDown = true;
        coolDownTask = (new BukkitRunnable() {
            @Override
            public void run() { onCoolDown = false; }
        }).runTaskLaterAsynchronously( plugin, coolDown );
    }

    public void resetCoolDown() {
        onCoolDown = false;
        if( coolDownTask != null && !coolDownTask.isCancelled() ) {
            coolDownTask.cancel();
        }
    }
}
