package net.offsetleft.tournamentcoordinator;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import net.offsetleft.tournamentcoordinator.exceptions.MatchStateException;
import net.offsetleft.tournamentcoordinator.exceptions.TournamentStateException;

/**
 * @author      Joseph W. Samuels
 * @param       <P> a class that extends TournamentParticipant.
 * @since       2014-11-10
 */
public class TournamentMatch <P extends TournamentParticipant<P>> implements Filterable, Serializable  {
    
    private final SegmentMultiplayerOption multiplayerOption;
    
    private final ArrayList<P> matchParticipants    = new ArrayList<>();
    private final ArrayList<Integer> matchResults   = new ArrayList<>();
    
    /**
     * TODO: Comment.
     * 
     * @param matchParticipants
     * @param multiplayerOption 
     */
    public TournamentMatch(
            ArrayList<P> matchParticipants, 
            SegmentMultiplayerOption multiplayerOption) {
        this.matchParticipants.addAll(matchParticipants);
        this.multiplayerOption = multiplayerOption;
        
        if(this.matchParticipants.size() == 1 
                && this.multiplayerOption == SegmentMultiplayerOption.HEADSUP) {
            matchResults.addAll(Arrays.asList(2, 0, 0));
        }
    }
    

    /**************************************************************************
     *                                                                        *
     *  Methods related to match participants.                                *
     *   - Getters                                                            *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets a list of the participants in the match.
     * 
     * @return  a list of the match participants.
     */
    public ArrayList<P> getMatchParticipants() {
        return this.matchParticipants;
    }
    
    /**
     * TODO: Comment.
     * 
     * @return 
     */
    public SegmentMultiplayerOption getMultiplayerOption() {
        return this.multiplayerOption;
    }
    
    /**
     * Gets a list of opponents of the requested participant.
     * 
     * @param   participant
     *          participant to check.
     * 
     * @return  the list of opponents.
     * 
     * @throws  TournamentStateException
     *          If the requested participant was not in this match.
     */
    public ArrayList<P> getOpponents(P participant) throws TournamentStateException {
        if(!getWasParticipant(participant)) {
            throw new TournamentStateException("Participant was not in this match.");
        }
        
        ArrayList<P> opponents = new ArrayList<>();
        
        matchParticipants.stream().filter((potentialOp) ->
                (potentialOp != participant)).forEach((potentialOp) -> {
            opponents.add(potentialOp);
        });
        
        return opponents;
    }
    
    /**
     * Counts the number of participants in the match.
     * 
     * @return  an integer count of the participants.
     */
    public int getMatchParticipantCount() {
        return this.matchParticipants.size();
    }
    
    /**
     * Determines if the passed participant was involved in this match.
     * 
     * @param   participant
     *          participant to check.
     * 
     * @return  boolean value.
     */
    public boolean getWasParticipant(P participant) {
        return this.matchParticipants.contains(participant);
    }
    
    /**
     * Gets the participants results from this match.
     * 
     * @param   participant
     *          participant to get results for.
     * 
     * @return  the requested participants results.
     * 
     * @throws  TournamentStateException 
     *          If any of the following is true:
     *          <ul>
     *              <li>the requested participant was not in this match.</li>
     *              <li>the match does not have results.</li>
     *          </ul>
     */
    public int getParticipantResultsByParticipant(P participant) throws TournamentStateException {
        if(!getWasParticipant(participant)) {
            throw new TournamentStateException("Participant was not in this match.");
        }
        
        if(matchResults.isEmpty()) {
            throw new TournamentStateException("Match does not have results.");
        }
        
        int index = matchParticipants.indexOf(participant);
        
        return matchResults.get(index);
    }
    
    /**
     * Gets the results of the participant at the requested index.
     * 
     * @param   index
     *          the index to check results for.
     * 
     * @return  the requested participants results.
     * 
     * @throws  MatchStateException
     *          If the match lacks results.
     */
    public int getParticipantResultsByIndex(int index) throws MatchStateException {
        if(matchResults.isEmpty()) {
            throw new MatchStateException("Match does not have results.");
        }
        
        return matchResults.get(index);
    }
    
    /**
     * Returns the total number of games played in the match.
     * 
     * @return  an integer depending on the following criteria:
     *          <ul>
     *              <li>If there are no results entered - 0</li>
     *              <li>If it was a multi-player match - 1</li>
     *              <li>The sum of all the results entered.</li>
     *          </ul>
     */
    public int getGamesPlayedTotal() {
        if(matchResults.isEmpty()) {
            return 0;
        }
        
        switch (multiplayerOption) {
            case MULTIPLAYER:
                return 1;
            default:
                int totalPlayed = 0;
                
                totalPlayed = 
                        matchResults.stream().map((i) -> i).reduce(totalPlayed, Integer::sum);
            
                return totalPlayed;
        }
    }

    /**
     * TODO: Comment.
     * 
     * @param   participant
     * @return 
     * @throws  TournamentStateException 
     */
    public int getParticipantGamePoints(P participant) throws TournamentStateException {
        if(!getMatchHasResults()) {
            return 0;
        }
        
        switch(multiplayerOption) {
            case HEADSUP:
                return getHeadsUpGamePoints(participant);
            case MULTIPLAYER:
                return getMultiplayerMatchPoints(participant);
        }
        
        return 0;
    }
    
    /**
     * Gets the participants points from this match.
     * 
     * @param   participant
     *          participant to get points for.
     * 
     * @return  the requested participants points.
     * 
     * @throws  TournamentStateException 
     *          If any of the following is true:
     *          <ul>
     *              <li>the requested participant was not in this match.</li>
     *              <li>the match does not have results.</li>
     *          </ul>
     */
    public int getParticipantMatchPoints(P participant) throws TournamentStateException {      
        if(!getMatchHasResults()) {
            return 0;
        }
        
        switch(multiplayerOption) {
            case HEADSUP:
                return getHeadsUpMatchPoints(participant);
            case MULTIPLAYER:
                return getMultiplayerMatchPoints(participant);
            default:
                return 0;
        }
    }
    
    /**
     * Gets the participants points for a heads-up match.
     * 
     * @param   participant
     *          participant to get points for.
     * 
     * @return  the requested participants points.
     * 
     * @throws  TournamentStateException 
     *          If any of the following is true:
     *          <ul>
     *              <li>the requested participant was not in this match.</li>
     *              <li>the match does not have results.</li>
     *          </ul>
     */
    protected int getHeadsUpMatchPoints(P participant) throws TournamentStateException {
        if(matchParticipants.size() == 1) {
            if(getParticipantGameWins(participant) == 2) {
                return 3;
            } else {
                return 0;
            }
        } else {
            if(getParticipantGameWins(participant) > getParticipantGameLosses(participant)) {
                return 3;
            } else if(getParticipantGameWins(participant) == getParticipantGameLosses(participant)) {
                return 1;
            } else {
                return 0;
            }
        }
    }
    
    /**
     * Gets the participants points for a multi-player match.
     * 
     * @param   participant
     *          participant to get points for.
     * 
     * @return  the requested participants points.
     * 
     * @throws  TournamentStateException 
     *          If any of the following is true:
     *          <ul>
     *              <li>the requested participant was not in this match.</li>
     *              <li>the match does not have results.</li>
     *          </ul>
     */
    protected int getMultiplayerMatchPoints(P participant) throws TournamentStateException {
        int participantResult   = getParticipantResultsByParticipant(participant);
        
        switch(participantResult) {
            case 1:
                return 6;
            case 2:
                return 3;
            case 3:
                return 1;
            default:
                return 0;
        }
    }
    
    /**
     * TODO: Comment.
     * 
     * @param   participant
     * @return 
     * @throws  TournamentStateException 
     */
    protected int getHeadsUpGamePoints(P participant) throws TournamentStateException {
        if(matchParticipants.size() == 1) {
            return 6;
        } else {
            return getParticipantGameWins(participant) * 3 + getMatchDraws();
        }
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to match results.                                     *
     *   - Getters                                                            *
     *   - Setters                                                            *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets a list of the match results.
     * 
     * @return  a list of results.
     * 
     * @throws  MatchStateException
     *          If the match has no results.
     */
    public ArrayList<Integer> getMatchResults() throws MatchStateException {
        if(!getMatchHasResults()) {
            throw new MatchStateException("Match lacks results.");
        }
        
        return this.matchResults;
    }
    
    /**
     * TODO: Comment.
     * 
     * @param   participant
     * 
     * @return  
     * 
     * @throws  TournamentStateException 
     */
    public int getParticipantGameWins(P participant) throws TournamentStateException {
        return getParticipantResultsByParticipant(participant);
    }
    
    /**
     * TODO: Comment.
     * 
     * @param participant
     * @return 
     * @throws net.offsetleft.tournamentcoordinator.exceptions.MatchStateException 
     */
    public int getParticipantGameLosses(P participant) throws MatchStateException {
        if(matchParticipants.size() == 1) {
            return 0;
        }
        
        int index = matchParticipants.indexOf(participant);
        
        int opponentIndex = (index + 1) % 2;
        
        return getParticipantResultsByIndex(opponentIndex);
    }
    
    /**
     * TODO: Comment.
     * 
     * @return 
     */
    public int getMatchDraws() {
        if(matchResults.size() > 2) {
            return matchResults.get(2);
        }
        
        return 0;
    }
    
    /**
     * Gets if the match has result data.
     * 
     * @return  a boolean.
     */
    public boolean getMatchHasResults() {
        return this.matchResults.size() > 0;
    }
    
    /**
     * Sets the results for the match.
     * 
     * @param   matchResults 
     *          sets the results for the match.
     * 
     * @throws  TournamentStateException
     *          If the size of the provided list is different from the number
     *          of match participants.
     */
    public void setMatchResults(ArrayList<Integer> matchResults) throws TournamentStateException {
        if(((multiplayerOption == SegmentMultiplayerOption.MULTIPLAYER
                && matchResults.size() != matchParticipants.size())
            || multiplayerOption == SegmentMultiplayerOption.HEADSUP 
                && matchResults.size() != 3)
            && !matchResults.isEmpty()) {
            throw new TournamentStateException("Result size mismatch.");
        }
        
        this.matchResults.clear();
        this.matchResults.addAll(matchResults);
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Overridden methods.                                                   *
     *                                                                        *
     **************************************************************************/
    
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
//        try {
//            int i = Integer.parseInt(filterValue);
//            
//            return getTableNumber() == i;
//        } catch (NumberFormatException ex) { }
        
        for(P p : matchParticipants) {
            if(p.filtered(filterValue)) {
                return true;
            }
        }
        
        return false;
    }
}
