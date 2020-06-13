package nz.pmme.Boost.Enums;

public enum Winner {
    FIRST(0),
    SECOND(1),
    THIRD(2);

    private int top3Listing;

    Winner( int top3Listing ) { this.top3Listing = top3Listing; }

    public int getTop3Listing() { return this.top3Listing; }

    public static Winner fromString( String winner ) {
        try {
            return Winner.valueOf( winner.toUpperCase() );
        } catch( IllegalArgumentException e ) {
            return null;
        }
    }
};
