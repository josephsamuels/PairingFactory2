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
public class RoundStateException extends TournamentStateException {

    /**
     * Creates a new instance of <code>RoundStateException</code> without detail
     * message.
     */
    public RoundStateException() {
    }

    /**
     * Constructs an instance of <code>RoundStateException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public RoundStateException(String msg) {
        super(msg);
    }
}
