package net.offsetleft.tournamentcoordinator;

/**
 * Defines the elimination system to be used.
 * <ul>
 * <li>{@link #NONE}</li>
 * <li>{@link #SINGLE}</li>
 * <li>{@link #DOUBLE}</li>
 * </ul>
 * 
 * @author      Joseph W. Samuels
 * @since       2014-11-10
 */
public enum SegmentEliminationStyle {
    /**
     * No elimination system will be used - players may participate in all
     * rounds of the segment.
     */
    NONE, 
    
    /**
     * A single elimination system will be used - players will be automatically
     * dropped after a single loss in a segment.
     */
    SINGLE,
    
    /**
     * A double elimination system will be used - players will be automatically
     * dropped after two losses in a segment.
     */
    DOUBLE;
    
    
    /**************************************************************************
     *  Overridden methods.                                                   *
     **************************************************************************/
    
    /**
     * Returns a string representation of the elimination style.
     * 
     * @return  a string
     */
    @Override
    public final String toString() {
        switch (this) {
            case NONE:
                return "None";
            case SINGLE:
                return "Single";
            case DOUBLE:
                return "Double";
            default:
                return "Error - Invalid elimination style.";
        }
    }
}
