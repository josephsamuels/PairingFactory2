/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.offsetleft.tournamentcoordinator.exceptions;

/**
 *
 * @author josephsamuels
 */
public class MatchStateException extends TournamentStateException {

    /**
     * Creates a new instance of <code>MatchStateException</code> without detail
     * message.
     */
    public MatchStateException() {
    }

    /**
     * Constructs an instance of <code>MatchStateException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public MatchStateException(String msg) {
        super(msg);
    }
}
