package nz.pmme.Boost.Enums;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum Place
{
    FIRST(0),
    SECOND(1),
    THIRD(2),
    FOURTH(3),
    FIFTH(4),
    SIXTH(5),
    SEVENTH(6),
    EIGHTH(7),
    NINTH(8),
    TENTH(9);

    private int index;
    private static Map< Integer, Place > map = new HashMap<>();
    private static List< Place > top3places = new ArrayList<>();

    Place( int index ) { this.index = index; }

    static {
        for( Place place : Place.values() ) {
            map.put( place.index, place );
        }
        top3places.add( Place.FIRST );
        top3places.add( Place.SECOND );
        top3places.add( Place.THIRD );
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

    public static List< Place > getTop3places() { return Place.top3places; }
};
