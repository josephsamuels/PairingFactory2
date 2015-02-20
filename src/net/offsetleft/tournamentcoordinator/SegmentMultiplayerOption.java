package net.offsetleft.tournamentcoordinator;

/**
 * Defines the elimination system to be used.
 * <ul>
 * <li>{@link #HEADSUP}</li>
 * <li>{@link #MULTIPLAYER}</li>
 * </ul>
 * 
 * @author      Joseph W. Samuels
 * @since       2014-11-10
 */
public enum SegmentMultiplayerOption {
    /**
     * Participation list - Players are not matched at all.
     */
    PARTICIPATION_LIST,
    
    /**
     * Heads-up - Players are matched up in one-on-one games.
     */
    HEADSUP, 
    
    /**
     * Multi-player - Players are matched up in groups of up to 4.
     */
    MULTIPLAYER;
    
    
    /**************************************************************************
     *  Overridden methods.                                                   *
     **************************************************************************/
    
    /**
     * Returns a string representation of the multi-player option.
     * 
     * @return  a string
     */
    @Override
    public final String toString() {
        switch (this) {
            case PARTICIPATION_LIST:
                return "Participation List";
            case HEADSUP:
                return "Swiss";
            case MULTIPLAYER:
                return "Danish";
            default:
                return "Error - Invalid multiplayer option.";
        }
    }
}
