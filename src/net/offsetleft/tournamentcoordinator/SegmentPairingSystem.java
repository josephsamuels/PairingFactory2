package net.offsetleft.tournamentcoordinator;

/**
 * Defines the pairing system to be used.
 * <ul>
 * <li>{@link #NONE}</li>
 * <li>{@link #SWISS}</li>
 * <li>{@link #DANISH}</li>
 * </ul>
 * 
 * @author      Joseph W. Samuels
 * @since       2014-11-10
 */
public enum SegmentPairingSystem {
    /**
     * None - Used for participation lists.
     */
    NONE,
    
    /**
     * Swiss - A system where players are paired based on skill without repeating
     * matches.
     */
    SWISS, 
    
    /**
     * Danish - A variation on the Swiss system where a rematch is allowed.
     */
    DANISH;
    
    
    /**************************************************************************
     *  Overridden methods.                                                   *
     **************************************************************************/
    
    /**
     * Returns a string representation of the pairing system.
     * 
     * @return  a string
     */
    @Override
    public final String toString() {
        switch (this) {
            case NONE:
                return "None";
            case SWISS:
                return "Swiss";
            case DANISH:
                return "Danish";
            default:
                return "Error - Invalid pairing system.";
        }
    }
}
