package nz.pmme.Boost.Enums;

import java.util.ArrayList;
import java.util.List;

public enum GameType
{
    ELIMINATION,
    ELIMINATION_RACE,
    RACE,
    PARKOUR;

    public static GameType fromString( String gameType ) {
        if( gameType == null ) return null;
        try {
            return GameType.valueOf( gameType.toUpperCase() );
        } catch( IllegalArgumentException e ) {
            return null;
        }
    }

    public static List< String > getGameTypes() {
        List< String > gameTypes = new ArrayList<>();
        for( GameType gameType : GameType.values() ) {
            gameTypes.add( gameType.toString() );
        }
        return gameTypes;
    }
}
