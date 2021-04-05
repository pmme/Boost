package nz.pmme.Utils;

import java.util.ArrayList;
import java.util.List;

public class RandomisedDistributor
{
    private int range;
    private List< Integer > values = new ArrayList<>();

    public RandomisedDistributor( int range )
    {
        this.setRange( range );
    }

    private void resetValues()
    {
        if( this.range > 1 ) {
            for( int i = 0; i < this.range; ++i ) this.values.add( i );
        }
    }

    public void setRange( int range )
    {
        this.range = range;
        this.values.clear();
        this.resetValues();
    }

    public int getNext()
    {
        if( this.range < 2 ) return 0;
        int i = (int)( Math.random() * this.values.size() );
        int chosenValue = this.values.get( i );
        this.values.remove( i );
        if( this.values.size() == 0 ) this.resetValues();
        return chosenValue;
    }
}
