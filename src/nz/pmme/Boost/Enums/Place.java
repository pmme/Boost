package nz.pmme.Boost.Enums;

import java.util.HashMap;
import java.util.Map;

public enum Place
{
    FIRST(0),
    SECOND(1),
    THIRD(2);

    private int index;
    private static Map< Integer, Place > map = new HashMap<>();

    Place( int index ) { this.index = index; }

    static {
        for( Place place : Place.values() ) {
            map.put( place.index, place );
        }
    }

    public int getAsIndex() { return this.index; }

    public static Place valueOf( int index ) {
        return map.get( index );
    }

    public static Place fromString( String place ) {
        try {
            return Place.valueOf( place.toUpperCase() );
        } catch( IllegalArgumentException e ) {
            return null;
        }
    }
};
