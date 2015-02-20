package net.offsetleft.tournamentcoordinator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import net.offsetleft.tournamentcoordinator.exceptions.TournamentStateException;

/**
 *
 * @param       <P>
 *
 * @author      Joseph W. Samuels
 * @since       2014-11-10
 */
public class TournamentParticipant <P extends TournamentParticipant> implements 
        Cloneable, Filterable, Serializable  {
    
    protected String participantFirstName, participantLastName;
    protected final String participantUUID;
    
    protected TournamentEvent enrolledEvent;
    
    /**
     * TODO: Comment.
     * 
     * @param participantFirstName
     * @param participantLastName 
     */
    public TournamentParticipant(
            String participantFirstName, 
            String participantLastName) {
        this.participantFirstName = participantFirstName;
        this.participantLastName = participantLastName;
        this.participantUUID = "" + Calendar.getInstance().getTimeInMillis();
    }
    
    /**
     * TODO: Comment.
     * 
     * @param participant
     * @param enrolledEvent 
     */
    public TournamentParticipant(
            P participant, 
            TournamentEvent enrolledEvent) {
        this.participantFirstName = participant.getFirstName();
        this.participantLastName = participant.getLastName();
        this.participantUUID = participant.getUUID();
        this.enrolledEvent = enrolledEvent;
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to participant properties.                            *
     *   - Getters                                                            *
     *   - Setters                                                            *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets the participant's first name.
     * 
     * @return  the first name of the participant.
     */
    public final String getFirstName() {
        return this.participantFirstName;
    }
    
    /**
     * Gets the participant's last name.
     * 
     * @return  the last name of the participant.
     */
    public final String getLastName() {
        return this.participantLastName;
    }
    
    /**
     * Gets the participant's UUID.
     * 
     * @return  the first name of the participant.
     */
    public final String getUUID() {
        return this.participantUUID;
    }
    
    /**
     * Gets the participants status.
     * 
     * @return  a ParticipantStatus object.
     */
    public final ParticipantStatus getParticipantStatus() {
        ParticipantStatus status = ParticipantStatus.INACTIVE;
        
        try {
            status = enrolledEvent.getParticipantStatus(this);
        } catch (TournamentStateException ex) { }
        
        return status;
    }
    
    /**
     * Gets the participant's standing in the event.
     * 
     * @return  the participants standing as an integer.
     * 
     * @throws  TournamentStateException
     *          If this participant is not enrolled in the event.
     */
    public final int getParticipantStanding() throws TournamentStateException {
        return enrolledEvent.getParticipantStandings(this);
    }
    
    /**
     * Gets the participant's point total.
     * 
     * @return  the total points the participant has accrued as an integer.
     */
    public final int getParticipantMatchPoints() {
        try {
            return enrolledEvent.getParticipantMatchPoints(this);
        } catch (TournamentStateException ex) { 
            return 0;
        }
    }
    
    /**
     * TODO: Comment.
     * 
     * @return 
     */
    public final int getParticipantGamePoints() {
        try {
            return enrolledEvent.getParticipantGamePoints(this);
        } catch (TournamentStateException ex) {
            return 0;
        }
    }
    
    /**
     * Gets the participant's point total.
     * 
     * @param   round
     *          the round cutoff.
     * 
     * @return  the total points the participant has accrued by the requested 
     *          round as an integer.
     * 
     * @throws  TournamentStateException
     *          If this participant is not enrolled in the event.
     */
    public final int getParticipantPointsAtRound(int round) throws TournamentStateException { 
        return enrolledEvent.getParticipantMatchPointsAtRound(this, round);
    }
    
    /**
     * Gets the participant's total number of losses.
     * 
     * @return  the total loss count as an integer.
     * 
     * @throws  TournamentStateException
     *          If this participant is not enrolled in the event.
     */
    public final int getParticipantLossCount() throws TournamentStateException {
        return enrolledEvent.getParticipantLossCount(this);
    }
    
    /**
     * Determines if this participant has played the passed participant.
     * 
     * @param   participant
     *          the participant to check.
     * 
     * @return  boolean value
     * 
     * @throws  TournamentStateException
     *          If this participant is not enrolled in the event.
     */
    public final boolean getHasPlayedParticipant(P participant) throws TournamentStateException {
        return enrolledEvent.getParticipantsHavePlayed(this, participant);
    }
    
    /**
     * Determines if this participant has had a bye this event.
     * 
     * @return  a boolean value
     * 
     * @throws  TournamentStateException
     *          If this participant is not enrolled in the event.
     */
    public final boolean getHasHadBye() throws TournamentStateException {
        return enrolledEvent.getParticipantHasHadBye(this);
    }
    
    /**
     * TODO: Comment.
     * 
     * @return 
     */
    public final double getMatchWinPercentage() {
        double matchesPlayed = enrolledEvent.getAllParticipantMatches(this).size();
        double totalPoints = getParticipantMatchPoints();

        double winPct = totalPoints / (matchesPlayed * 3);
        
        if(winPct > .33) {
            return winPct;
        } else {
            return .33;
        }
    }
    
    /**
     * TODO: Comment
     * 
     * @return 
     */
    public final double getOpponentsMatchWinPercentage() {
        ArrayList<P> opponents = new ArrayList<>();
        
        try {
            opponents = enrolledEvent.getAllOpponentsForParticipant(this);
        } catch (TournamentStateException ex) { }
        
        double opponentCount = opponents.size();
        
        if(opponentCount < 1) {
            return 0;
        }
        
        double total = 0;
        
        for(P opponent : opponents) {
            total += opponent.getMatchWinPercentage();
        }
        
        return total / opponentCount;
    }
    
    /**
     * TODO: Comment.
     * 
     * @return
     */
    public final double getGameWinPercentage() {        
        try {
            double gamesPlayed = enrolledEvent.getParticipantGamesPlayed(this);
            if(gamesPlayed < 1) {
                return 0;
            }
            
            double gamePoints = enrolledEvent.getParticipantGamePoints(this);
            
            return gamePoints / (gamesPlayed * 3);
        } catch (TournamentStateException ex) {
            return 0;
        }
        
    }
    
    /**
     * TODO: Comment.
     * 
     * @return 
     */
    public final double getOpponentsGameWinPercentage() {
        ArrayList<P> opponents = new ArrayList<>();
        
        try {
            opponents = enrolledEvent.getAllOpponentsForParticipant(this);
        } catch (TournamentStateException ex) { }
        
        double opponentCount = opponents.size();
        
        if(opponentCount < 1) {
            return 0;
        }
        
        double total = 0;
        
        for(P opponent : opponents) {
            double opponentGWP = opponent.getGameWinPercentage();

            if(opponentGWP < .33)
                total += .33;
            else 
                total += opponent.getGameWinPercentage();
        }
        
        return total / opponentCount;
    }
    
    /**
     * Sets the participants first name.
     * 
     * @param participantFirstName the new first name to use
     */
    public final void setFirstName(String participantFirstName) {
        this.participantFirstName = participantFirstName;
    }
    
    /**
     * Sets the participants last name.
     * 
     * @param participantLastName  the new last name to use
     */
    public final void setLastName(String participantLastName) {
        this.participantLastName = participantLastName;
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Overridden methods.                                                   *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Returns a string representation of the participant.
     * 
     * @return  a string
     */
    @Override
    public String toString() {
        return "First Name: " + participantFirstName 
                + "; Last Name: " + participantLastName 
                + "; UUID: " + participantUUID;
    }

    /**
     * Returns a boolean that determines if this item should be filtered out.
     * 
     * @param   filterValue
     *          Value to check.
     * 
     * @return  a boolean.
     */
    @Override
    public boolean filtered(String filterValue) {
        if(getFirstName().toLowerCase().contains(filterValue)) {
            return true;
        } else if(getLastName().toLowerCase().contains(filterValue)) {
            return true;
        }
        
        return false;
    }

    @Override
    protected Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
    
    

    /**************************************************************************
     *                                                                        *
     *  AlphabeticalComparator helper class.                                  *
     *                                                                        *
     **************************************************************************/
    
    /**
     * TODO: Comment.
     */
    public static class AlphabeticalComparator implements Comparator<TournamentParticipant> {

        /**
         * TODO: Comment. Implement.
         * @param p1
         * @param p2
         */
        @Override
        public int compare(TournamentParticipant p1, TournamentParticipant p2) {
            if(p1.getLastName().compareTo(p2.getLastName()) != 0) {
                return p1.getLastName().compareTo(p2.getLastName());
            } else if (p1.getFirstName().compareTo(p2.getFirstName()) != 0) {
                return p1.getFirstName().compareTo(p2.getFirstName());
            }
            
            return 0;
        }
        
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  StandingsComparator helper class.                                     *
     *                                                                        *
     **************************************************************************/
    
    /**
     * TODO: Comment.
     */
    public static class StandingsComparator implements Comparator<TournamentParticipant> {
        
        /**
         * TODO: Comment.
         * 
         * @param p1
         * @param p2
         */
        @Override
        public int compare(TournamentParticipant p1, TournamentParticipant p2) {
            if(p1.getParticipantMatchPoints() > p2.getParticipantMatchPoints()) {
                return -1;
            } else if(p1.getParticipantMatchPoints() < p2.getParticipantMatchPoints()) {
                return 1;
            }
                
            if(p1.getOpponentsMatchWinPercentage() > p2.getOpponentsMatchWinPercentage()) {
                return -1;
            } else if(p1.getOpponentsMatchWinPercentage() < p2.getOpponentsMatchWinPercentage()) {
                return 1;
            }
            
            if(p1.getGameWinPercentage() > p2.getGameWinPercentage()) {
                return -1;
            } else if(p1.getGameWinPercentage() < p2.getGameWinPercentage()) {
                return 1;
            }
            
            if(p1.getOpponentsGameWinPercentage() > p2.getOpponentsGameWinPercentage()) {
                return -1;
            } else if(p1.getOpponentsGameWinPercentage() < p2.getOpponentsGameWinPercentage()) {
                return 1;
            }
            
            return 0;
        }
        
    }
}
