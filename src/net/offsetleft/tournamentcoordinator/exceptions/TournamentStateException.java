package net.offsetleft.tournamentcoordinator.exceptions;

public class TournamentStateException extends Exception {

    /**
     * Creates a new instance of <code>TournamentStateException</code> without
     * detail message.
     */
    public TournamentStateException() {
    }

    /**
     * Constructs an instance of <code>TournamentStateException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public TournamentStateException(String msg) {
        super(msg);
    }
}
