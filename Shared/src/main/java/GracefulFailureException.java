public class GracefulFailureException extends Exception {
    /**
     * Throw this exception to stop an actor gracefully
     */
    GracefulFailureException(String errorMessage) {
        super(errorMessage);
    }
}
