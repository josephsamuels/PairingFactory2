/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.offsetleft.tournamentcoordinator;

/**
 * Used to represent the active state of the participant.
 * <ul>
 * <li>{@link #ACTIVE}</li>
 * <li>{@link #INACTIVE}</li>
 * </ul>
 * 
 * @author      Joseph Samuels
 * @since       2014-11-10
 */
public enum ParticipantStatus {
    /**
     * The participant is currently active.
     */
    ACTIVE, 
    
    /**
     * The participant is inactive.
     */
    INACTIVE;
    
    
    /**************************************************************************
     *                                                                        *
     *  Overridden methods.                                                   *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Returns a string representation of the elimination style.
     * 
     * @return  a string
     */
    @Override
    public final String toString() {
        switch (this) {
            case ACTIVE:
                return "Active";
            case INACTIVE:
                return "Inactive";
            default:
                return "Error - Invalid participant state.";
        }
    }
}
