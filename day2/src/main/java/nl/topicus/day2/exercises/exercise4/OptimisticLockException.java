package nl.topicus.day2.exercises.exercise4;

/**
 * Custom exception for optimistic locking conflicts.
 * Thrown when an update detects that the data was modified
 * by another user (version mismatch → 0 rows updated).
 */
public class OptimisticLockException extends RuntimeException {

    public OptimisticLockException(String message) {
        super(message);
    }
}
