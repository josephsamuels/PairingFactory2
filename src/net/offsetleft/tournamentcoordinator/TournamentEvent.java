package net.offsetleft.tournamentcoordinator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.offsetleft.tournamentcoordinator.exceptions.EventStateException;
import net.offsetleft.tournamentcoordinator.exceptions.TournamentStateException;

/**
 * TournamentEvent is a Java class built to organize tournaments with an eye 
 * towards extending the functionality. An instance of the class contains a
 * parallel set of ArrayLists that contain objects that extend the 
 * TournamentParticipant class representing the collection of players in the 
 * event.
 * 
 * <p>An instance of this class also contains an ArrayList populated with 
 * objects that extend the class TournamentSegment. This list can have from 
 * zero to two elements in it. 
 * 
 * <p>Finally, an instance of this class contains three enum values of the types
 * SegmentEliminationStyle, SegmentPairingSystem and SegmentMultiplayerOption.
 * 
 * <p>This class can accept as Type arguments classes that extend the 
 * TournamentSegment, TournamentRound, TournamentMatch and TournamentParticipant
 * classes included in this library.
 * 
 * @param       <S> 
 *              a class that extends TournamentSegement.
 * 
 * @param       <R> 
 *              a class that extends TournamentRound.
 * 
 * @param       <M> 
 *              a class that extends TournamentMatch.
 * 
 * @param       <P> 
 *              a class that extends TournamentParticipant.
 * 
 * @author      Joseph Samuels
 * @since       2014-11-10
 */
public class TournamentEvent 
        <S extends TournamentSegment<R, M, P>,
        R extends TournamentRound<M, P>, M extends TournamentMatch<P>,
        P extends TournamentParticipant<P>> implements Serializable {
    
    protected SegmentEliminationStyle eliminationStyle;
    protected SegmentPairingSystem pairingSystem;
    protected SegmentMultiplayerOption multiplayerOption;
    
    protected final ArrayList<S> tournamentSegments       = new ArrayList<>();
    
    private final ArrayList<P> allParticipants          = new ArrayList<>();
    private final ArrayList<P> standingsParticipants    = new ArrayList<>();
    

    /**
     * Constructs a new TournamentEvent object with the default elimination 
     * style, pairing system and multi-player option provided.
     * 
     * @param   eliminationStyle
     *          The default SegmentEliminationStyle to use for this event.
     * 
     * @param   pairingSystem
     *          The default SegmentPairingSystem to use for this event.
     * 
     * @param   multiplayerOption 
     *          The default SegmentMultiplayerOption to use for this event.
     */
    public TournamentEvent(
            SegmentEliminationStyle eliminationStyle, 
            SegmentPairingSystem pairingSystem,
            SegmentMultiplayerOption multiplayerOption) {
        this.eliminationStyle   = eliminationStyle;
        this.pairingSystem      = pairingSystem;
        this.multiplayerOption  = multiplayerOption;
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to event properties.                                  *
     *   - Getters                                                            *
     *   - Setters                                                            *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets the regulation elimination style of the event.
     * 
     * @return  the SegmentEliminationStyle of the event.
     */
    public final SegmentEliminationStyle getEliminationStyle() {
        return this.eliminationStyle;
    }

    /**
     * Gets the regulation pairing system of the event.
     * 
     * @return  the SegmentPairingSystem of the event.
     */
    public final SegmentPairingSystem getPairingSystem() {
        return this.pairingSystem;
    }
    
    /**
     * Gets the regulation multi-player option of the event.
     * 
     * @return  the SegmentMultiplayerOption of the event.
     */
    public final SegmentMultiplayerOption getMultiplayerOption() {
        return this.multiplayerOption;
    }
    
    public final boolean getEventHasStarted() {
        try {
            getRegulationPlaySegment();
            
            return true;
        } catch (TournamentStateException ex) {
            return false;
        }
    }
    
    /**
     * Gets the maximum number of suggested rounds for the event.
     * 
     * @return  the number of suggested rounds as an integer.
     */
    public final int getMaxSuggestedRounds() {
        try {
            switch (tournamentSegments.size()) {
                case 1:
                    return calculateSuggested(getRegulationPlaySegment().getSegmentParticipants());
                case 2:
                    return calculateSuggested(getPlayoffPlaySegment().getSegmentParticipants()) +
                                getRegulationPlaySegment().getSegmentRoundCount();
            }
        } catch (TournamentStateException ex) { }
        
        return calculateSuggested(allParticipants);
    }
    
    /**
     * 
     * @param participants
     * @return 
     */
    private int calculateSuggested(ArrayList<P> participants) {
        if(getMultiplayerOption() == SegmentMultiplayerOption.PARTICIPATION_LIST) {
            return 0;
        }
        
        double suggested = Math.ceil(Math.log(participants.size()) / Math.log(2));
            
        if(getEliminationStyle() == SegmentEliminationStyle.DOUBLE) {
            suggested *= 2;
        }

        if(suggested < 0)
            return 0;
        
        return (int)(suggested);
    }
    
    /**
     * Calculates the number of outstanding match results for the current round.
     * 
     * @return  a count of outstanding results.
     */
    public final int getOutstandingEventMatchResultCount() {
        int outstandingMatchCount = 0;
        
        try {
            R currentEventRound = getCurrentEventRound();
            
            outstandingMatchCount = currentEventRound.getOutstandingRoundResultsCount();
            
            if(outstandingMatchCount == currentEventRound.getRoundMatchCount()) {
                outstandingMatchCount -= currentEventRound.getRoundByeCount();
            }
        } catch (TournamentStateException ex) { }
        
        return outstandingMatchCount;
    }
    
    /**
     * Calculates the number of completed rounds in the event.
     * 
     * @return  a count of completed rounds.
     */
    public final int getCompletedRoundCount() {
        int roundCount = 0;
        
        for(R round : getAllEventRounds()) {
            roundCount += ((round.getOutstandingRoundResultsCount() == 0) ? 1 : 0);
        }
        
        return roundCount;
    }
    
    /**
     * Returns the minimum player count required for the event format.
     * 
     * @return  an integer representing the minimum player count.
     */
    public int getMinimumPlayerCount() {
        return 0;
    }
    
    /**
     * Sets the elimination style of the event.
     * 
     * @param   eliminationStyle 
     *          The SegmentEliminationStyle to be used
     * 
     * @throws  TournamentStateException
     *          If the event has already begun.
     */
    public final void setEliminationStyle(
            SegmentEliminationStyle eliminationStyle) throws TournamentStateException {
        
        if(tournamentSegments.size() > 0) {
            throw new 
                TournamentStateException("Event has begun. Cannot change elimination style.");
        }
        
        this.eliminationStyle = eliminationStyle;
    }
    
    /**
     * Sets the pairing system of the event.
     * 
     * @param   pairingSystem 
     *          The SegmentPairingSystem to be used
     * 
     * @throws  TournamentStateException
     *          If the event has already begun.
     */
    public final void setPairingSystem(
            SegmentPairingSystem pairingSystem) throws TournamentStateException {
        
        if(tournamentSegments.size() > 0) {
            throw new 
                TournamentStateException("Event has begun. Cannot change pairing system.");
        }
        
        this.pairingSystem = pairingSystem;
    }
    
    /**
     * Sets the pairing system of the event.
     * 
     * @param   multiplayerOption  
     *          The SegmentMultiplayerOption to be used
     * 
     * @throws  TournamentStateException
     *          If the event has already begun.
     */
    public final void setMultiplayerOption(
            SegmentMultiplayerOption multiplayerOption) throws TournamentStateException {
        
        if(tournamentSegments.size() > 0) {
            throw new 
                TournamentStateException("Event has begun. Cannot change multiplayer option.");
        }
        
        this.multiplayerOption = multiplayerOption;
    }

    
    /**************************************************************************
     *                                                                        *
     *  Methods relating to event segments.                                   *
     *   - Getters                                                             *
     *   - Creators                                                            *
     **************************************************************************/
    
    /**
     * Gets the current segment.
     * 
     * @return  the current segment of the event.
     */
    public S getCurrentSegment() {
        if(tournamentSegments.size() <= 0) {
            return null;
        }
        
        int lastIndex = tournamentSegments.size() - 1;
        
        return tournamentSegments.get(lastIndex);
    }
    
    /**
     * Gets the regulation play segment.
     * 
     * @return  the regulation play segment.
     * 
     * @throws  TournamentStateException
     *          Regulation play has not begun.
     */
    public S getRegulationPlaySegment() throws TournamentStateException {
        if(!getRegulationHasBegun()) {
            throw new
                TournamentStateException("Regulation play has not begun.");
        }
        
        return tournamentSegments.get(0);
    }
    
    /**
     * Returns a boolean representing if regulation play has begun.
     * 
     * @return  a boolean.
     */
    public boolean getRegulationHasBegun() {
        return tournamentSegments.size() >= 1;
    }
    
    /**
     * Gets the playoff play segment.
     * 
     * @return  the playoff play segment.
     * 
     * @throws  TournamentStateException
     *          Playoff play has not begun.
     */
    public S getPlayoffPlaySegment() throws TournamentStateException {
        if(!getPlayoffHasBegun()) {
            throw new
                TournamentStateException("Playoff play has not begun.");
        }
        
        return tournamentSegments.get(1);
    }
    
    /**
     * Returns a boolean representing if playoff play has begun.
     * 
     * @return  a boolean.
     */
    public boolean getPlayoffHasBegun() {
        return tournamentSegments.size() >= 2;
    }
    
    /**
     * Creates a new regulation play segment.
     * 
     * @throws  TournamentStateException
     *          If the event has already begun.
     */
    public void createRegulationPlaySegment() throws TournamentStateException {
        if(tournamentSegments.size() > 0) {
            throw new
                TournamentStateException("Event has begun. Cannot start regular play a second time.");
        }
        
        createNewSegment(this.eliminationStyle, 
                this.pairingSystem, 
                this.allParticipants,
                false);
    }
    
    /**
     * Creates a new playoff play segment.
     * 
     * This method will throw a TournamentStateException if regulation play has
     * not started or if playoff play has already begun.
     * 
     * @param   cut               
     *          The number of players to keep
     * 
     * @param   eliminationStyle  
     *          The SegmentEliminationStyle to be used
     * 
     * @param   pairingSystem     
     *          the SegmentPairingSystem to be used
     * 
     * @throws  TournamentStateException
     *          If any of the following is true:
     *          <ul>
     *              <li>The event has not begun.</li>
     *              <li>Playoff play has already begun.</li>
     *          </ul>
     */
    public void createPlayoffPlaySegment(
            int cut,
            SegmentEliminationStyle eliminationStyle, 
            SegmentPairingSystem pairingSystem) throws TournamentStateException {
        if(tournamentSegments.size() < 1) {
            throw new
                TournamentStateException("Event has not begun. "
                        + "Cannot start playoffs.");
        }
        
        if(tournamentSegments.size() > 1) {
            throw new
                TournamentStateException("Playoff play has begun. "
                        + "Cannot start playoff play a second time.");
        }
        
        if(cut >= 0) {
            ArrayList<P> topX = new ArrayList<>(standingsParticipants.subList(0, cut));
            
            int playerCount = topX.size();
            
            if(Integer.bitCount(playerCount) == 1) {
                ArrayList<P> seededList = generateSeededList(topX);
                
                createNewSegment(eliminationStyle, 
                        pairingSystem, 
                        seededList, true);
            }
        } else {
            createNewSegment(eliminationStyle, 
                    pairingSystem, 
                    getActiveEventParticipants(), false);
        }
    }
    
    /**
     * TODO: Comment
     * 
     * @param toSeed
     * @return 
     */
    private ArrayList<P> generateSeededList(ArrayList<P> toSeed) {
        int [] seedGreed = getSeedGrid(toSeed.size());
        ArrayList<P> toReturn = new ArrayList<>();
        
        for(int i : seedGreed) {
            toReturn.add(toSeed.get(i - 1));
        }
        
        return toReturn;
    }
    
    /**
     * TODO: Comment
     * 
     * @param partSize
     * @return 
     */
    private int[] getSeedGrid(int partSize) {
        double rounds = Math.log(partSize) / Math.log(2)-1;
        int[] pls = {1, 2};
        
        for(int i = 0; i < rounds; i++) {
            pls = nextLayer(pls);
        }
        
        return pls;
    }
    
    /**
     * TODO: Comment
     * 
     * @param pls
     * @return 
     */
    private int[] nextLayer(int[] pls) {
        ArrayList<Integer> out = new ArrayList<>();
        int length = pls.length * 2 + 1;
        
        for(int i : pls) {
            out.add(i);
            out.add(length - i);
        }
        
        int[] toReturn = new int[out.size()];
        
        for(int i = 0; i < out.size(); i++) {
            toReturn[i] = out.get(i);
        }
        
        return toReturn;
    }
    
    /**
     * Creates a new play segment.
     * 
     * Called internally by both createRegulationPlaySegment and
     * cretePlayoffPlaySegment. Ensures consistent segment creation.
     * 
     * @param   eliminationStyle  
     *          The SegmentEliminationStyle of the segment
     * 
     * @param   pairingSystem     
     *          The SegmentPairingSystem of the segment
     * 
     * @param   participants
     *          The participants participating in this segment.
     * 
     * @param   seeded
     *          If the list of participants is pre-seeded.
     */
    protected void createNewSegment(
            SegmentEliminationStyle eliminationStyle, 
            SegmentPairingSystem pairingSystem,
            ArrayList<P> participants, 
            boolean seeded) {
        
        S newSegment  = (S)(new TournamentSegment<>(
                        eliminationStyle, 
                        pairingSystem, 
                        multiplayerOption, 
                        participants,
                        seeded));
        tournamentSegments.add(newSegment);
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods relating to event rounds.                                    *
     *   - Getters                                                            *
     *   - Creators                                                           *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets a list of all event rounds.
     * 
     * @return  a list of all event rounds.
     */
    public final ArrayList<R> getAllEventRounds() {
        ArrayList<R> eventRounds = new ArrayList<>();
        
        tournamentSegments.stream().forEach((segment) -> {
            eventRounds.addAll(segment.getSegmentRounds());
        });
        
        return eventRounds;
    }
    
    /**
     * Gets the requested round.
     * 
     * @param   round
     *          the round to get.
     * 
     * @return  a TournamentRound object or an object that extends it.
     * 
     * @throws  TournamentStateException
     *          If the requested round is outside acceptable range.
     */
    public final R getRound(int round) throws TournamentStateException {
        ArrayList<R> allRounds = getAllEventRounds();
        
        if(round <= 0 || allRounds.size() < round) {
            throw new TournamentStateException("Requested round is outside "
                    + "acceptable range.");
        }
        
        return getAllEventRounds().get(round - 1);
    }
    
    /**
     * Gets the event round count.
     * 
     * @return  an integer representing the count of event rounds.
     */
    public final int getEventRoundCount() {
        return getAllEventRounds().size();
    }
    
    /**
     * Gets the current event round from the current segment.
     * 
     * @return  the current round of the event.
     * 
     * @throws  TournamentStateException
     *          If any of the following is true:
     *          <ul>
     *              <li>the event has not begun.</li>
     *              <li>the segment has no rounds.</li>
     *          </ul>
     */
    public final R getCurrentEventRound() throws TournamentStateException {
        if(tournamentSegments.isEmpty()) {
            throw new EventStateException(
                    "Event has not begun yet.");
        }
        
        return getCurrentSegment().getCurrentSegmentRound();
    }
    
    /**
     * Gets the round number of the requested round.
     * 
     * @param   round
     *          The round to check.
     * 
     * @return  The round number of the round.
     * 
     * @throws  EventStateException 
     *          If the event does not include the requested round.
     */
    public final int getRoundNumberForRound(R round) throws EventStateException {
        if(!getAllEventRounds().contains(round)) {
            throw new EventStateException("Event does not contain round.");
        }
        
        return getAllEventRounds().indexOf(round);
    }
    
    /**
     * Creates a new tournament round in the current segment.
     * 
     * @throws  TournamentStateException
     *          If any of the following is true:
     *          <ul>
     *              <li>the event has not begun.</li>
     *              <li>the round already has matches.</li>
     *          </ul>
     */
    public void createNewEventRound() throws TournamentStateException {
        if(tournamentSegments.isEmpty()) {
            throw new TournamentStateException("Event has not begun yet. Cannot create new round.");
        }
        
        int roundNumber = getEventRoundCount() + 1;
        
        getCurrentSegment().createNewSegmentRound(roundNumber);
    }
    
    /**
     * Removes the current event round.
     * 
     * @throws  TournamentStateException
     *          If the event has not begun.
     */
    public final void removeCurrentEventRound() throws TournamentStateException {
        if(tournamentSegments.isEmpty()) {
            throw new TournamentStateException("Event has not begun yet. Cannot create new round.");
        }
        
        getCurrentSegment().removeCurrentSegmentRound();
        
        if(getCurrentSegment().getSegmentRoundCount() == 0) {
            int size = tournamentSegments.size();
            tournamentSegments.remove(size - 1);
        }
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to event matches.                                     *
     *   - Getters                                                            *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Returns the matches associated with a tournament round.
     * 
     * @param   roundNumber
     *          The index of 
     * 
     * @return  the list of matches in the requested round.
     * 
     * @throws  TournamentStateException
     *          If any of the following is true:
     *          <ul>
     *              <li>the requested index is out of bounds.</li>
     *              <li>the round does not have matches.</li>
     *          </ul>
     */
    public final ArrayList<M> getRoundMatchesForRound(
            int roundNumber) throws TournamentStateException {
        if(roundNumber < 1 || getAllEventRounds().size() < roundNumber) {
            throw new TournamentStateException("Round index out of bounds. No such round.");
        }
        
        if(!getAllEventRounds().get(roundNumber - 1).getRoundHasMatches()) {
            throw new TournamentStateException("Round does not have matches available.");
        }
        
        return getAllEventRounds().get(roundNumber - 1).getRoundMatches();
    }
    
    /**
     * Returns a list of all event matches in the event.
     * 
     * @return  ArrayList of matches
     */
    public final ArrayList<M> getAllEventMatches() {
        if(getAllEventRounds().size() <= 0) {
            return new ArrayList<>();
        }
        
        ArrayList<M> allMatches = new ArrayList<>();
        
        for(R round : getAllEventRounds()) {
            allMatches.addAll(round.getRoundMatches());
        }
        
        return allMatches;
    }
    
    /**
     * TODO: Comment
     * @param   participant
     * @param   roundNumber
     * @return 
     * @throws net.offsetleft.tournamentcoordinator.exceptions.TournamentStateException 
     */
    public final M getParticipantsMatchForRound(P participant, int roundNumber) throws TournamentStateException {        
        ArrayList<M> roundMatches = getRoundMatchesForRound(roundNumber);
        
        for(M match : roundMatches) {
            if(match.getWasParticipant(participant)) {
                return match;
            }
        }
        
        throw new TournamentStateException("Participant was not in this round.");
    }
    
    /**
     * Returns a list of matches the requested participant was involved in.
     * 
     * @param   participant
     *          The participant to get matches for.
     * 
     * @return  The list of matches the participant was involved in.
     */
    public final ArrayList<M> getAllParticipantMatches(P participant) {
        ArrayList<M> allMatches = getAllEventMatches();
        ArrayList<M> pariticpantMatches = new ArrayList<>();
        
        for(M match : allMatches) {
            if(match.getWasParticipant(participant)) {
                pariticpantMatches.add(match);
            }
        }
        
        return pariticpantMatches;
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to event participants.                                *
     *   - Getters                                                            *
     *   - Mutators                                                           *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets all participants in the event.
     * 
     * @return  all participants
     */
    public final ArrayList<P> getAllParticipants() {
        return this.allParticipants;
    }
    
    public final ArrayList<P> getAllParticipantsStandings() {
        Collections.sort(standingsParticipants, new P.StandingsComparator());
        
        return this.standingsParticipants;
    }
    
    /**
     * Gets if the requested participant is participating in this event.
     * 
     * @param   participant
     *          the participant to check.
     * 
     * @return  a boolean value
     */
    public final boolean getIsParticipant(P participant) {
        return allParticipants.stream().anyMatch((toCheck) ->
                (toCheck.getUUID().equals(participant.getUUID())));
    }
    
    /**
     * Gets the total participant count.
     * 
     * @return  the total participant count
     */
    public final int getAllParticipantCount() {
        return this.allParticipants.size();
    }
    
    /**
     * Gets all active participants in the event.
     * 
     * @return  all active participants
     */
    public final ArrayList<P> getActiveEventParticipants() {
        if(getCurrentSegment() == null) {
            return this.allParticipants;
        }
        
        return getCurrentSegment().getActiveParticipants();
    }
    
    /**
     * Gets the active participant count.
     * 
     * @return  the active participant count
     */
    public final int getActiveParticipantCount() {
        if(getCurrentSegment() == null) {
            return allParticipants.size();
        }
        
        return getActiveEventParticipants().size();
    }
    
    /**
     * Gets all inactive participants in the event.
     * 
     * @return  all inactive participants
     */
    public final ArrayList<P> getInactiveParticipants() {
        ArrayList<P> inactiveParticipants = new ArrayList<>();
        
        if(getCurrentSegment() != null)
            for(P participant : allParticipants) {
                if(!getCurrentSegment().getActiveParticipants().contains(participant))
                    inactiveParticipants.add(participant);
            }
        
        return inactiveParticipants;
    }

    /**
     * Gets the inactive participant count.
     * 
     * @return  the inactive participant count
     */
    public final int getInactiveParticipantCount() {
        return this.getInactiveParticipants().size();
    }
    
    /**
     * Returns a the requested participant's status.
     * 
     * @param   participant
     * 
     * @return  a ParticipantStatus object
     * 
     * @throws  TournamentStateException
     *          If there is no such participant enrolled in the event.
     */
    public final ParticipantStatus getParticipantStatus(P participant) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        if(this.getCurrentSegment() == null || this.getCurrentSegment().getParticipantIsActive(participant)) {
            return ParticipantStatus.ACTIVE; 
        } else {
            return ParticipantStatus.INACTIVE;
        }
    }
    
    /**
     * Gets the current standing of the requested participant.
     * 
     * @param   participant
     *          the participant to check.
     * 
     * @return  the participant's overall standing
     * 
     * @throws  TournamentStateException
     *          If there is no such participant enrolled in the event.
     */
    public final int getParticipantStandings(P participant) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        return standingsParticipants.indexOf(participant) + 1;
    }
    
    /**
     * Gets the current points of the requested participant.
     * 
     * @param   participant
     *          the participant to check.
     * 
     * @return  the participant's points at the current round
     * 
     * @throws  TournamentStateException
     *          If there is no such participant enrolled in the event.
     */
    public final int getParticipantMatchPoints(P participant) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        int currentRound = getAllEventRounds().size();
        
        return getParticipantMatchPointsAtRound(participant, currentRound);
    }
    
    /**
     * Gets the current points of the requested participant at the requested round.
     * 
     * @param   participant
     *          the participant to check.
     * 
     * @param   round
     *          the round cutoff.
     * 
     * @return  the participant's points at the requested round.
     * 
     * @throws  TournamentStateException
     *          If there is no such participant enrolled in the event.
     */
    public final int getParticipantMatchPointsAtRound(P participant, int round) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        ArrayList<R> eventRounds = getAllEventRounds();
        
        if(round <= 0 || eventRounds.size() < round) {
            throw new TournamentStateException("Provided round outside of range.");
        }
        
        int pointTotal = 0;
        
        ArrayList<M> participantMatches = getAllParticipantMatches(participant);
        
        if(participantMatches.size() > 0)
            for(int i = 0; i < round; i++) {
                pointTotal += participantMatches.get(i).getParticipantMatchPoints(participant);
            }
        
        return pointTotal;
    }
    
    /**
     * TODO: Comment
     * 
     * @param   participant
     * @return 
     * @throws  TournamentStateException 
     */
    public final int getParticipantGamePoints(P participant) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        int pointTotal = 0;
        
        ArrayList<M> participantMatches = getAllParticipantMatches(participant);
        
        for(M match : participantMatches) {
            pointTotal += match.getParticipantGamePoints(participant);
        }
        
        return pointTotal;
    }
    
    /**
     * TODO: Comment
     * 
     * @param participant
     * @return
     * @throws TournamentStateException 
     */
    public final int getParticipantGamesPlayed(P participant) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        int gamesPlayed = 0;
        
        ArrayList<M> participantMatches = getAllParticipantMatches(participant);
        
        for(M match : participantMatches) {
            gamesPlayed += match.getGamesPlayedTotal();
        }
        
        return gamesPlayed;
    }
    
    /**
     * Gets the loss count of the requested participant in rounds where the
     * elimination style is Single or Double.
     * 
     * @param   participant
     *          the participant to check.
     * 
     * @return  the participant's loss count
     * 
     * @throws  TournamentStateException
     *          If there is no such participant enrolled in the event.
     */
    public final int getParticipantLossCount(P participant) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        int lossCount = 0;
        
        for(R round : getAllEventRounds()) {
            if(round.getEliminationStyle() == SegmentEliminationStyle.SINGLE
                    || round.getEliminationStyle() == SegmentEliminationStyle.DOUBLE) {
                for(M match : round.getRoundMatches()) {
                    if(match.getWasParticipant(participant) 
                            && match.getMatchHasResults()
                            && match.getParticipantMatchPoints(participant) == 0) {
                        lossCount++;
                    }
                }
            }
        }
        
        return lossCount;
    }
    
    /**
     * Determines if the two participants have played each other.
     * 
     * @param   participantA
     *          the first participant
     * 
     * @param   participantB
     *          the second participant
     * 
     * @return  the result of this test
     * 
     * @throws  TournamentStateException
     *          If one or both of the participants are not enrolled in the event.
     */
    public final boolean getParticipantsHavePlayed(
            P participantA, 
            P participantB) throws TournamentStateException {
        if(!allParticipants.contains(participantA) || !allParticipants.contains(participantB)) {
            throw new TournamentStateException("No such participant.");
        }
        
        for(R round : getAllEventRounds()) {
            for(M match : round.getRoundMatches()) {
                if(match.getWasParticipant(participantA) 
                        && match.getWasParticipant(participantB)) {
                    return true;
                }
            }
        }
        
        return false;
    }
    
    /**
     * Determines if the participant has had a bye.
     * 
     * @param   participant
     *          the participant to check
     * 
     * @return  the result of this test
     * 
     * @throws  TournamentStateException
     *          If the participant is not enrolled in the event.
     */
    public final boolean getParticipantHasHadBye(P participant) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        for(R round : getAllEventRounds()) {
            for(M match : round.getRoundMatches()) {
                if(match.getWasParticipant(participant) 
                        && match.getMatchParticipantCount() == 1) {
                    return true;
                }
            }
        }
        return false;
    }
    
    /**
     * TODO: Comment.
     * 
     * @param   participant
     * @return 
     * @throws  TournamentStateException 
     */
    public final ArrayList<P> getAllOpponentsForParticipant(P participant) throws TournamentStateException {
        ArrayList<M> participantMatches = getAllParticipantMatches(participant);
        ArrayList<P> participantOpponents = new ArrayList<>();
        
        for(M match : participantMatches) {
            participantOpponents.addAll(match.getOpponents(participant));
        }
        
        return participantOpponents;
    }
    
    /**
     * Add a participant to the event.
     * 
     * This method throws a TournamentStateException if the participant is 
     * enrolled in the event.
     * 
     * @param   participant
     *          the participant to add
     * 
     * @throws  TournamentStateException
     *          If the participant is already enrolled in the event.
     */
    public void addParticipant(P participant) throws TournamentStateException {
        for(P toCheck : allParticipants) {
            if(toCheck.getUUID().equals(participant.getUUID()))
                throw new TournamentStateException("Participant already in event.");
        }
        
        allParticipants.add(participant);
        Collections.sort(allParticipants, new P.AlphabeticalComparator());
        standingsParticipants.add(participant);
        
        S segment = getRegulationPlaySegment();
        if(segment != null) {
            segment.getActiveParticipants().add(participant);
            segment.getSegmentParticipants().add(participant);
        }
        
        R round = getCurrentEventRound();
        if(round != null) {
            round.getRoundParticipants().add(participant);
        }
    }
    
    /**
     * Remove a participant from the event.
     * 
     * This method throws a TournamentStateException if the participant is not
     * enrolled in the event.
     * 
     * @param   participant
     *          the participant to remove
     * 
     * @throws  TournamentStateException
     *          If there is no such participant enrolled in the event.
     */
    public final void removeParticipant(P participant) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        allParticipants.remove(participant);
        standingsParticipants.remove(participant);
    }
    
    /**
     * Deactivates a participant.
     * 
     * Moves a player to the inactive list.
     * 
     * @param   participant
     *          the participant to deactivate
     * 
     * @throws  TournamentStateException
     *          If any of the following is true:
     *          <ul>
     *              <li>there is no such participant enrolled in the event.</li>
     *              <li>the participant is currently inactive.</li>
     *          </ul>
     */
    public final void deactivateParticipant(P participant) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        getCurrentSegment().deactivateParticipant(participant);
    }
    
    /**
     * Reactivates a participate.
     * 
     * Moves a player to the active list.
     * 
     * @param   participant
     *          the participant to activate
     * 
     * @throws  TournamentStateException
     *          If any of the following is true:
     *          <ul>
     *              <li>there is no such participant enrolled in the event.</li>
     *              <li>the participant is currently active.</li>
     *              <li>the participant is not involved in the segment.</li>
     *          </ul>
     */
    public final void reactivateParticipant(P participant) throws TournamentStateException {
        if(!allParticipants.contains(participant)) {
            throw new TournamentStateException("No such participant.");
        }
        
        getCurrentSegment().reactivateParticipant(participant);
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Overridden methods.                                                   *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Returns a string representation of the event.
     * 
     * @return  a string
     */
    @Override
    public String toString() {
        return "Elimination Style: " + eliminationStyle 
                + "; Pairing System: " + pairingSystem;
    }
    
    
    /*
    *   (╯°□°)╯︵ ┻━┻
    */
}
