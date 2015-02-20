package net.offsetleft.tournamentcoordinator;

import java.io.Serializable;
import java.util.ArrayList;
import net.offsetleft.tournamentcoordinator.exceptions.TournamentStateException;

/**
 * TODO: Comment.
 * 
 * @param       <M> 
 *              a class that extends TournamentMatch.
 * 
 * @param       <P> 
 *              a class that extends TournamentParticipant.
 * 
 * @author      Joseph W. Samuels
 * @since       2014-11-10
 */
public class TournamentRound <M extends TournamentMatch<P>, 
        P extends TournamentParticipant<P>> 
        implements Serializable {
    
    private final int roundNumber;
    
    private final SegmentEliminationStyle eliminationStyle;
    private final SegmentPairingSystem pairingSystem;
    private final SegmentMultiplayerOption multiplayerOption;
    
    protected final ArrayList<M> roundMatches = new ArrayList<>();
    protected final ArrayList<P> roundParticipants = new ArrayList<>();
    
    /**
     * TODO: Comment.
     * 
     * @param roundNumber
     * @param roundParticipants
     * @param eliminationStyle
     * @param pairingSystem
     * @param multiplayerOption 
     */
    public TournamentRound(int roundNumber,
                ArrayList<P> roundParticipants, 
                SegmentEliminationStyle eliminationStyle, 
                SegmentPairingSystem pairingSystem,
                SegmentMultiplayerOption multiplayerOption) {
        this.roundNumber = roundNumber;
        
        this.roundParticipants.addAll(roundParticipants);
        
        this.eliminationStyle = eliminationStyle;
        this.pairingSystem = pairingSystem;
        this.multiplayerOption = multiplayerOption;
    }
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to round properties.                                  *
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
    
    /**
     * TODO: Comment.
     * 
     * @return 
     */
    public final int getRoundNumber() {
        return this.roundNumber;
    }
    
    /**
     * Calculates the number of outstanding match results for this round.
     * 
     * @return  an integer.
     */
    public final int getOutstandingRoundResultsCount() {
        int outstandingCount = 0;
        
        for(M match : roundMatches) {
            outstandingCount += (match.getMatchHasResults() ? 0 : 1);
        }
        
        if(outstandingCount == getRoundMatchCount() - getRoundByeCount()) {
            outstandingCount += getRoundByeCount();
        }
        
        return outstandingCount;
    }
    
    public final int getRoundByeCount() {
        int byeCount = 0;
        
        for(M match : roundMatches) {
            if(match.getMatchParticipantCount() == 1) {
                byeCount++;
            }
        }
        
        return byeCount;
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to round matches.                                     *
     *   - Getters                                                            *
     *   - Setters                                                            *
     *   - Creators                                                           *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets a list of all matches in the round.
     * 
     * @return  a list of all round matches.
     */
    public ArrayList<M> getRoundMatches() {
        return this.roundMatches;
    }
    
    /**
     * Gets the round match count.
     * 
     * @return  an integer representing the count of round matches.
     */
    public int getRoundMatchCount() {
        return this.roundMatches.size();
    }
    
    /**
     * Gets if the round has any matches in it.
     * 
     * @return  a boolean
     */
    public boolean getRoundHasMatches() {
        return !roundMatches.isEmpty();
    }
    
    /**
     * Creates a matches for the round.
     * 
     * @throws  TournamentStateException
     *          If the round already has matches.
     */
    public void createRoundMatches() throws TournamentStateException {
        if(this.roundMatches.size() > 0) {
            throw new TournamentStateException("Round already has matches.");
        }
        
        int minNodeSize = 0;
        int maxNodeSize = 0;
        
        switch (multiplayerOption) {
            case HEADSUP:
                minNodeSize = 1;
                maxNodeSize = 2;
                break;
                
            case MULTIPLAYER:
                minNodeSize = 3;
                maxNodeSize = 4;
        }

        if(eliminationStyle != SegmentEliminationStyle.DOUBLE 
                || multiplayerOption == SegmentMultiplayerOption.HEADSUP) {
            roundMatches.addAll( 
                    generateRoundMatches(roundParticipants, minNodeSize, maxNodeSize));
        } else {
            ArrayList<P> winnersBracket = new ArrayList<>();
            ArrayList<P> losersBracket = new ArrayList<>();
            
            for(P p : roundParticipants) {
                if(p.getParticipantLossCount()== 0) {
                    winnersBracket.add(p);
                } else {
                    losersBracket.add(p);
                }
            }
            
            ArrayList<M> winnersPairings = 
                    generateRoundMatches(winnersBracket, minNodeSize, maxNodeSize);
            roundMatches.addAll(winnersPairings);

            if(losersBracket.size() > 0) {
                ArrayList<M> losersPairings = 
                        generateRoundMatches(losersBracket, minNodeSize, maxNodeSize);
                roundMatches.addAll(losersPairings);
            }
        }
    }
    
    /**
     * Subroutine reducing code redundancy in createRoundMatches().
     * 
     * @param   players
     *          The players to match.
     * 
     * @param   minNodeSize
     *          The minimum size of a PairingNode.
     * 
     * @param   maxNodeSize
     *          The maximum size of a PairingNode.
     * 
     * @return  an ArrayList of matches.
     */
    private ArrayList<M> generateRoundMatches(
                ArrayList<P> players, 
                int minNodeSize, int maxNodeSize) {
        ArrayList<M> pairings = new ArrayList<>();

        PairingNode root = new PairingNode(null, minNodeSize, maxNodeSize);

        for(P p : players) {
            root.addPlayer(p);
        }
        
        root.cleanupNodes();

        pairings.add(root.getMatch());
        
        while(root.hasNextNode()) {
            root = root.getNextNode();
            
            pairings.add(root.getMatch());
        }
        
        return pairings;
    }
    
    /**
     * TODO: Comment
     * 
     * @param participants
     * @param mulitplayerOption
     * @return 
     */
    public M createRoundMatch(ArrayList<P> participants, 
            SegmentMultiplayerOption mulitplayerOption) {
        TournamentMatch match = new TournamentMatch(participants, multiplayerOption);
        
        return (M)match;
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Methods related to round participants.                                *
     *   - Getters                                                            *
     *                                                                        *
     **************************************************************************/
    
    /**
     * Gets the list of participants in the round.
     * 
     * @return  an ArrayList of participants.
     */
    public ArrayList<P> getRoundParticipants() {
        return this.roundParticipants;
    }

    /**
     * TODO: Comment.
     * 
     * @param participant
     * @return 
     */
    public boolean getParticipantHasMatch(P participant) {
        if (roundMatches.stream().anyMatch((match) ->
                (match.getWasParticipant(participant)))) {
            return true;
        }
        
        return false;
    }
    
    
    /**************************************************************************
     *                                                                        *
     *  Overridden methods.                                                   *
     *                                                                        *
     **************************************************************************/
    
    /**
     * TODO: Comment
     * 
     * @return 
     */
    @Override
    public String toString() {
        return "" + roundNumber;
    }

    /**************************************************************************
     *                                                                        *
     *  PairingNode helper class.                                             *
     *                                                                        *
     **************************************************************************/
    
    /**
     * 
     */
    private class PairingNode {
            private final ArrayList<P> nodePlayers = new ArrayList<>();
            
            private final int minNodeSize, maxNodeSize;
            
            private final PairingNode previous;
            private PairingNode next;
            
            /**
             * TODO: Comment.
             * 
             * @param previous
             * @param minNodeSize
             * @param maxNodeSize 
             */
            private PairingNode(PairingNode previous,
                    int minNodeSize,
                    int maxNodeSize) {
                this.previous = previous;
                this.minNodeSize = minNodeSize;
                this.maxNodeSize = maxNodeSize;
            }
            
            
            /**
             * TODO: Comment.
             * 
             * @param p 
             */
            private void addPlayer(P p) {
                if (this.getNodeSize() < maxNodeSize 
                        && pairingSystem == SegmentPairingSystem.SWISS) {
                    for (P opponent : nodePlayers) {
                        try {
                            if (p.getHasPlayedParticipant(opponent)) {
                                passToNext(p);
                                return;
                            }
                        } catch (TournamentStateException ex) {
                            System.err.println(ex);
                        }
                    }
                    
                    nodePlayers.add(p);
                } else if (getNodeSize() < maxNodeSize) {
                    nodePlayers.add(p);
                } else {
                    passToNext(p);
                }
            }
            
            /**
             * TODO: Comment.
             * 
             * @param p 
             */
            private void passToNext(P p) {
                if (!this.hasNextNode()) {
                    next = new PairingNode(this, 
                            minNodeSize, 
                            maxNodeSize);
                }
                
                next.addPlayer(p);
            }
            
            /**
             * TODO: Comment.
             * 
             * @return 
             */
            private boolean hasNextNode() {
                return next != null;
            }
            
            /**
             * TODO: Comment.
             * 
             * @return 
             */
            private PairingNode getNextNode() {
                return next;
            }
            
            /**
             * TODO: Comment.
             * 
             * @return 
             */
            private M getMatch() {
                M match = createRoundMatch(nodePlayers, multiplayerOption);
                
                return match;
            }
            
            /**
             * TODO: Comment.
             * 
             * @return 
             */
            private int getNodeSize() {
                return nodePlayers.size();
            }
            
            /**
             * TODO: Comment.
             * 
             */
            private void cleanupNodes() {
                if (next != null) {
                    next.cleanupNodes();
                }
                
                if(maxNodeSize > 2) {
                    while (previous != null
                            && getNodeSize() < minNodeSize
                            && previous.getNodeSize() > minNodeSize) {
                        nodePlayers.add(previous.surrenderLast());
                    }
                } else if(getNodeSize() == 1) {
                    P toCheck = nodePlayers.get(0);
                    
                    try {
                        while(toCheck.getHasHadBye()
                                || (previous != null && previous.getNodeSize() == 1)) {
                            this.nodePlayers.remove(toCheck);

                            if(!previous.findNewOpponent(toCheck)) {
                                this.nodePlayers.add(toCheck);
                                break;
                            }

                            if(nodePlayers.size() > 0)
                                toCheck = nodePlayers.get(0);
                            else
                                break;
                        }
                    } catch (TournamentStateException ex) {
                        System.err.println(ex);
                    }
                }
                
                if(next != null && next.getNodeSize() == 0) {
                    next = null;
                }
            }
            
            /**
             * TODO: Comment.
             * 
             * @return 
             */
            private P surrenderLast() {
                return surrenderPlayer(nodePlayers.size() - 1);
            }
            
            /**
             * TODO: Comment.
             * 
             * @param index
             * @return 
             */
            private P surrenderPlayer(int index) {
                return nodePlayers.remove(index);
            }
            
            /**
             * TODO: Comment.
             * 
             * @param toPair
             * @return 
             */
            private boolean findNewOpponent(P toPair) {
                try {
                    if(this.getNodeSize() > minNodeSize) {
                        P p1 = this.nodePlayers.get(0);
                        P p2 = this.nodePlayers.get(1);
                        int start = (int)(Math.random() * 2);

                        if(start == 0) {
                            if(!toPair.getHasPlayedParticipant(p1)) {
                                this.nodePlayers.remove(p2);
                                this.nodePlayers.add(toPair);
                                this.addPlayer(p2);

                                return true;
                            }

                            if(!toPair.getHasPlayedParticipant(p2)) {
                                this.nodePlayers.remove(p1);
                                this.nodePlayers.add(toPair);
                                this.addPlayer(p1);

                                return true;
                            }
                        } else {
                            if(!toPair.getHasPlayedParticipant(p2)) {
                                this.nodePlayers.remove(p1);
                                this.nodePlayers.add(toPair);
                                this.addPlayer(p1);

                                return true;
                            }

                            if(!toPair.getHasPlayedParticipant(p1)) {
                                this.nodePlayers.remove(p2);
                                this.nodePlayers.add(toPair);
                                this.addPlayer(p2);

                                return true;
                            }
                        }
                    } else if (this.getNodeSize() == minNodeSize) {
                        P p1 = this.nodePlayers.get(0);

                        if(!toPair.getHasPlayedParticipant(p1)) {
                            this.nodePlayers.add(toPair);

                            return true;
                        }
                    }
                } catch (TournamentStateException ex) {
                    System.err.println(ex);
                }
                
                if(previous != null)
                    return previous.findNewOpponent(toPair);
                
                return false;
            }
            
            
            /******************************************************************
             *  Overridden methods.                                           *
             ******************************************************************/
            
            /**
             * Returns a string representation of the node.
             * 
             * @return  a string
             */
            @Override
            public String toString() {
                return nodePlayers.toString();
            }
        }
}
