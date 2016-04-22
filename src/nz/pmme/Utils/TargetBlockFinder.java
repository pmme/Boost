package nz.pmme.Utils;

import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.util.NumberConversions;
import org.bukkit.util.Vector;

/**
 * Utility class to find and return the block under the cross-hair.
 *
 * Created by paul on 21-Apr-16.
 */
public class TargetBlockFinder
{
    private static final double FINDER_VECTOR_STEP = 0.2;
    private static final double MAXIMUM_FINDER_RANGE = 255;

    private World world;
    private double playerX, playerY, playerZ;
    private Vector playerViewUnitVector;
    private double distanceToTarget;
    private int targetX, targetY, targetZ;

    /**
     * Construct the TargetBlockFinder from the supplied player.
     *
     * Target position is initialised to the player's position until getTargetBlock() is called at which point it
     * iterates along the player's view direction to the first non-air block under the cursor.
     *
     * @param player    The player, most likely the active player.
     */
    public TargetBlockFinder( final Player player )
    {
        this.world = player.getWorld();

        this.playerX = player.getEyeLocation().getX();
        this.playerY = player.getEyeLocation().getY();
        this.playerZ = player.getEyeLocation().getZ();
        this.playerViewUnitVector = player.getLocation().getDirection();
        this.distanceToTarget = 0.0;
        this.targetX = NumberConversions.floor(this.playerX);
        this.targetY = NumberConversions.floor(this.playerY);
        this.targetZ = NumberConversions.floor(this.playerZ);
    }

    /**
     * Find and return the first non-air block under the player's cross-hair.
     *
     * @return The first non-air block under the player's cross-hair. null if out of range or out of Y limits.
     */
    public final Block getTargetBlock()
    {
        Block targetBlock;
        do{
            stepOneBlock();
            targetBlock = this.getCurrentBlock();
        } while( targetBlock != null && targetBlock.getType() == Material.AIR );
        return targetBlock;
    }

    /**
     * Get the block at the current iterated distance.
     *
     * @return The block at the current iterated distance. null if out of range or out of Y limits.
     */
    public final Block getCurrentBlock()
    {
        if( this.distanceToTarget > this.MAXIMUM_FINDER_RANGE || this.targetY < 0 || this.targetY > this.world.getMaxHeight() ) {
            return null;
        }
        return this.world.getBlockAt( this.targetX, this.targetY, this.targetZ );
    }

    /**
     * Trace along the player's view direction one block. Current iterated position is stored in the class instance.
     */
    private final void stepOneBlock()
    {
        int x, y, z;
        do {
            this.distanceToTarget += this.FINDER_VECTOR_STEP;
            x = NumberConversions.floor( this.playerX + this.playerViewUnitVector.getX() * this.distanceToTarget );
            y = NumberConversions.floor( this.playerY + this.playerViewUnitVector.getY() * this.distanceToTarget );
            z = NumberConversions.floor( this.playerZ + this.playerViewUnitVector.getZ() * this.distanceToTarget );
        } while( x == this.targetX && y == this.targetY && z == this.targetZ );

        this.targetX = x;
        this.targetY = y;
        this.targetZ = z;
    }
}
