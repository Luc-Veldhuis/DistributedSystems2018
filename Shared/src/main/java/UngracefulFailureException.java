public class UngracefulFailureException extends Exception {
    /**
     * Throw this exception to stop an actor gracefully
     */
    UngracefulFailureException(String errorMessage) {
        super(errorMessage);
    }
}
