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
public class SegementStateException extends TournamentStateException {

    /**
     * Creates a new instance of <code>SegementStateException</code> without
     * detail message.
     */
    public SegementStateException() {
    }

    /**
     * Constructs an instance of <code>SegementStateException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public SegementStateException(String msg) {
        super(msg);
    }
}
