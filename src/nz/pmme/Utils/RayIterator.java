package nz.pmme.Utils;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;

public class RayIterator
{
    private final Vector origin;
    private final Vector direction;
    private final double maximumDistance;
    private final double stepDistance;
    private double currentDistance;

    public RayIterator( final Player player, final Location target, final double stepDistance )
    {
        this.origin = player.getEyeLocation().toVector();
        this.direction = player.getEyeLocation().getDirection();
        this.maximumDistance = player.getEyeLocation().toVector().distance( target.toVector() );
        this.stepDistance = stepDistance > 0.0 ? stepDistance : 1.0;
        this.currentDistance = 0.0;
    }

    public RayIterator( final Player player, final double range, final double stepDistance )
    {
        this.origin = player.getEyeLocation().toVector();
        this.direction = player.getEyeLocation().getDirection();
        this.maximumDistance = range;
        this.stepDistance = stepDistance > 0.0 ? stepDistance : 1.0;
        this.currentDistance = 0.0;
    }

    public final void step()
    {
        this.currentDistance += this.stepDistance;
    }

    public final boolean end() {
        return this.currentDistance > this.maximumDistance;
    }

    public Location toLocation( World world )
    {
        Vector currentPosition = new Vector().copy( this.direction );
        currentPosition.multiply( this.currentDistance );
        currentPosition.add( this.origin );
        return currentPosition.toLocation( world );
    }
}
