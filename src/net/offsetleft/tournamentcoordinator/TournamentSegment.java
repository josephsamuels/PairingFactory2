package net.offsetleft.tournamentcoordinator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import net.offsetleft.tournamentcoordinator.exceptions.SegementStateException;
import net.offsetleft.tournamentcoordinator.exceptions.TournamentStateException;

/**
 * TODO: Comment.
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
public class TournamentSegment<
        R extends TournamentRound<M, P>, M extends TournamentMatch<P>,
        P extends TournamentParticipant<P>> 
        implements Serializable {
    
    //Segment Properties
    private final SegmentEliminationStyle eliminationStyle;
    private final SegmentPairingSystem pairingSystem;
    private final SegmentMultiplayerOption multiplayerOption;
    
    //Segment Rounds
    private final ArrayList<R> segmentRounds = new ArrayList<>();
    
    //Segment Participants
    private final ArrayList<P> segmentParticipants = new ArrayList<>();
    private final ArrayList<P> activeParticipants = new ArrayList<>();
    
    protected final boolean seeded;
    
    /**
     * TODO: Comment.
     * 
     * @param eliminationStyle
     * @param pairingSystem
     * @param multiplayerOption 
     * @param segmentParticipants 
     * @param seeded 
     */
    public TournamentSegment(
            SegmentEliminationStyle eliminationStyle,
            SegmentPairingSystem pairingSystem,
            SegmentMultiplayerOption multiplayerOption,
            ArrayList<P> segmentParticipants,
            boolean seeded) {
        
        this.eliminationStyle   = eliminationStyle;
        this.pairingSystem      = pairingSystem;
        this.multiplayerOption  = multiplayerOption;
        this.segmentParticipants.addAll(segmentParticipants);
        this.activeParticipants.addAll(segmentParticipants);
        
        this.seeded = seeded;
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to segment properties.                                *
     *   - Getters                                                            *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets the elimination style of the segment.
     * 
     * @return  the SegmentEliminationStyle of the segment.
     */
    public final SegmentEliminationStyle getEliminationStyle() {
        return this.eliminationStyle;
    }

    /**
     * Gets the pairing system of the segment.
     * 
     * @return  the SegmentPairingSystem of the segment.
     */
    public final SegmentPairingSystem getPairingSystem() {
        return this.pairingSystem;
    }
    
    /**
     * Gets the multi-player option of the segment.
     * 
     * @return  the SegmentMultiplayerOption of the segment.
     */
    public final SegmentMultiplayerOption getMultiplayerOption() {
        return this.multiplayerOption;
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to segment rounds.                                    *
     *   - Getters                                                            *
     *   - Creators                                                           *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets a list of all rounds in the segment.
     * 
     * @return  a list of all segment rounds.
     */
    public final ArrayList<R> getSegmentRounds() {
        return segmentRounds;
    }
    
    /**
     * Gets the segment round count.
     * 
     * @return  an integer representing the count of event rounds.
     */
    public final int getSegmentRoundCount() {
        return segmentRounds.size();
    }
    
    /**
     * Gets the current segment round.
     * 
     * @return  the current round of the segment.
     * 
     * @throws  SegementStateException
     *          If the segment has not begun.
     */
    public final R getCurrentSegmentRound() throws SegementStateException {
        if(segmentRounds.isEmpty()) {
            throw new SegementStateException("Segment has not begun yet.");
        }
        
        int lastIndex = segmentRounds.size() - 1;
        return segmentRounds.get(lastIndex);
    }
    
    /**
     * Creates a new tournament round in the segment.
     * 
     * @param   roundNumber
     *          the round number of the round to create
     * 
     * @throws  TournamentStateException
     *          If the round already has matches.
     */
    protected void createNewSegmentRound(int roundNumber) 
            throws TournamentStateException {
        if(!seeded)
            sortForPairing();
        
        dropEliminatedPlayers();
        
        TournamentRound<M, P> tRound = 
                new TournamentRound<>(
                        roundNumber,
                        activeParticipants, 
                        eliminationStyle, 
                        pairingSystem, 
                        multiplayerOption);
        
        tRound.createRoundMatches();
        
        segmentRounds.add((R)tRound);
    }
    
    /**
     * Sorts the active participants for pairing.
     */
    protected final void sortForPairing() {
        if(eliminationStyle == SegmentEliminationStyle.NONE) {
            if(segmentRounds.size() <= 0) {
                Collections.shuffle(activeParticipants);
            } else {
                activeParticipants.sort(new P.StandingsComparator());
            }
        } else {
            if(segmentRounds.size() <= 0) {
                Collections.shuffle(activeParticipants);
                
                activeParticipants.sort(new P.StandingsComparator());
            }
        }
    }
    
    /**
     * TODO: Comment
     * 
     * @throws TournamentStateException 
     */
    protected final void dropEliminatedPlayers() throws TournamentStateException {
        ArrayList<P> toDrop = new ArrayList<>();
        
        switch (getEliminationStyle()) {
            case SINGLE:
                for(P participant : activeParticipants) {
                    if(participant.getParticipantLossCount() > 0) {
                        toDrop.add(participant);
                    }
                }
                break;
            case DOUBLE:
                for(P participant : activeParticipants) {
                    if(participant.getParticipantLossCount() > 1) {
                        toDrop.add(participant);
                    }
                }
                break;
        }
        
        toDrop.stream().forEach((part) -> {
            activeParticipants.remove(part);
        });
    }
    
    /**
     * Removes the current round from the segment.
     * 
     * @throws  TournamentStateException
     *          The segment has not yet begun.
     */
    public final void removeCurrentSegmentRound() throws TournamentStateException {
        if(segmentRounds.isEmpty()) {
            throw new TournamentStateException("Segment has not begun yet.");
        }
        
        int size = segmentRounds.size();
        segmentRounds.remove(size - 1);
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to segment participants.                              *
     *   - Getters                                                            *
     *   - Mutators                                                           *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets the list of participants involved in the segment.
     * 
     * @return  an ArrayList containing segment participants.
     */
    public final ArrayList<P> getSegmentParticipants() {
        return this.segmentParticipants;
    }
    
    /**
     * Gets the list of active participants in the segment.
     * 
     * @return  an ArrayList containing active participants.
     */
    public final ArrayList<P> getActiveParticipants() {
        return this.activeParticipants;
    }
    
    /**
     * Gets the active status of the requested participant
     * 
     * @param   participant
     *          the participant to check.
     * 
     * @return  a boolean value
     */
    public final boolean getParticipantIsActive(P participant) {
        return this.activeParticipants.contains(participant);
    }
    
    /**
     * Removes a participant from active contention.
     * 
     * @param   participant
     *          the participant to deactivate
     * 
     * @throws  TournamentStateException
     *          If the participant is currently inactive.
     */
    public final void deactivateParticipant(P participant) throws TournamentStateException {
        if(!this.activeParticipants.contains(participant)) {
            throw new TournamentStateException("Participant is inactive.");
        }
        
        this.activeParticipants.remove(participant);
    }
    
    /**
     * Moves a participant to active contention.
     * 
     * @param   participant
     *          the participant to activate
     * 
     * @throws  TournamentStateException
     *          If any of the following is true:
     *          <ul>
     *              <li>the participant is currently active.</li>
     *              <li>the participant is not involved in the segment.</li>
     *          </ul>
     */
    public final void reactivateParticipant(P participant) throws TournamentStateException {
        if(this.activeParticipants.contains(participant)) {
            throw new TournamentStateException("Participant is active.");
        }
        
        if(!this.segmentParticipants.contains(participant)) {
            throw new TournamentStateException("Participant is not involed in this segment.");
        }
        
        this.activeParticipants.add(participant);
    }
}
