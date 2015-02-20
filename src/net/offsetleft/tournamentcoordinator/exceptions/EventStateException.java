package net.offsetleft.tournamentcoordinator.exceptions;

public class EventStateException extends TournamentStateException {

    /**
     * Creates a new instance of <code>EventStateException</code> without detail
     * message.
     */
    public EventStateException() {
    }

    /**
     * Constructs an instance of <code>EventStateException</code> with the
     * specified detail message.
     *
     * @param msg the detail message.
     */
    public EventStateException(String msg) {
        super(msg);
    }
}
