package nz.pmme.Utils;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

/**
 * Calculate the normalised vector to another player and make available the distance between players.
 * The original un-normalised vector can be retrieved if required.
 *
 * Created by paul on 15/05/16.
 */
public class VectorToOtherPlayer extends Vector
{
    private Vector rawVectorToOtherPlayer;
    private double distanceBetweenPlayers;

    /**
     * Construct a normalised Vector to another player and store the original un-normalised vector and distance
     * between players.
     *
     * @param otherPlayer   The "other" player. The target of the vector.
     * @param thisPlayer    The "current" player. The source of the vector.
     */
    public VectorToOtherPlayer( final LivingEntity otherPlayer, final Player thisPlayer )
    {
        // Get the normalized vector to the other player. Don't use the normalize() method though because we want the distance and it involves Math.sqrt, so we normalize ourselves.
        rawVectorToOtherPlayer = new Vector( otherPlayer.getLocation().getX() - thisPlayer.getLocation().getX(), 0.0, otherPlayer.getLocation().getZ() - thisPlayer.getLocation().getZ() );
        distanceBetweenPlayers = rawVectorToOtherPlayer.length();
        if( distanceBetweenPlayers > 0.0 ) {
            this.copy( rawVectorToOtherPlayer );
            this.multiply( 1.0 / distanceBetweenPlayers );      // this is now a unit vector.
        } else {
            // Players are in the exact same spot and cannot be normalised OR the player is boosting themselves.
            // Set a unit vector in the direction the player is facing.
            rawVectorToOtherPlayer.setX( -Math.sin( Math.toRadians( thisPlayer.getLocation().getYaw() ) ) );
            rawVectorToOtherPlayer.setZ( Math.cos( Math.toRadians( thisPlayer.getLocation().getYaw() ) ) );
            rawVectorToOtherPlayer.setY( 0.0 );
            this.copy( rawVectorToOtherPlayer );
        }
    }

    /**
     * VectorToOtherPlayer stores the original un-normalised vector between the players.
     *
     * @return  The un-normalised vector between the players
     */
    public Vector getRawVectorToOtherPlayer() {
        return rawVectorToOtherPlayer;
    }

    public double getDistanceBetweenPlayers() {
        return distanceBetweenPlayers;
    }
}
