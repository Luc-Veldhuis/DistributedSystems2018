public class Configuration {
    /**
     * All global configuration parameters should be stored here
     */
    public static final int NUMBER_OF_ERRORS_TO_CORRECT = 2;
    public static final int NUMBER_OF_HEADNODES = NUMBER_OF_ERRORS_TO_CORRECT+1;
    public static final int NUMBER_OF_BYZANTIAN_ERRORS = NUMBER_OF_ERRORS_TO_CORRECT*2+1;
    public static final int NUMBER_OF_WORKERS_PER_SYSTEM = 8;
    public static final int MAXIMUM_WAITING_TIME = 1000;
    public static final int TIMEOUT_DETECTION_TIME = 5000;
    public static final boolean RANDOM_FAILURES = true;
    public static final double RATE_OF_STOP_FAILURES = 0.1;
    public static final double RATE_OF_SILENT_FAILURES = 0;
    public static final double RATE_OF_BYZANTINE_FAILURES = 0.1;
    public enum Policies {LOCK_STEP, MAXIMIZE, SAME_MACHINE};
    public static final Policies policy = Policies.LOCK_STEP;
}
