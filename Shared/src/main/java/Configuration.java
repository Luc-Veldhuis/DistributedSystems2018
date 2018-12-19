public class Configuration {
    /**
     * All global configuration parameters should be stored here
     */
    public static final int NUMBER_OF_ERRORS_TO_CORRECT = 2;
    public static final int NUMBER_OF_HEADNODES = NUMBER_OF_ERRORS_TO_CORRECT+1;
    public static final int NUMBER_OF_WORKERS_PER_SYSTEM = 8;
    public static final int MAXIMUM_FAKED_EXECUTION_TIME = 1000;
    public static final int TIMEOUT_DETECTION_TIME = 5000;
    public static final int NUMBER_OF_CONCURRENT_JOBS = 80;

    public enum Policies {LOCK_STEP, MAXIMIZE, SAME_MACHINE};

    public int NUMBER_OF_DUPLICATIONS = NUMBER_OF_ERRORS_TO_CORRECT*2+1;
    public boolean RANDOM_FAILURES = true;
    public double RATE_OF_STOP_FAILURES = 0.1;
    public double RATE_OF_SILENT_FAILURES = 0;
    public double RATE_OF_BYZANTINE_FAILURES = 0.1;
    public Policies policy = Policies.LOCK_STEP;


    public static final int NUMBER_OF_JOBS = 4;

    Configuration(String[] args) {
        if(args.length == 5) {
            NUMBER_OF_DUPLICATIONS = Integer.parseInt(args[0]);
            RATE_OF_STOP_FAILURES = Double.parseDouble(args[1]);
            RATE_OF_SILENT_FAILURES = Double.parseDouble(args[2]);
            RATE_OF_BYZANTINE_FAILURES = Double.parseDouble(args[3]);
            if(RATE_OF_STOP_FAILURES == 0 && RATE_OF_SILENT_FAILURES == 0 && RATE_OF_BYZANTINE_FAILURES == 0) {
                RANDOM_FAILURES = false;
            }
            switch(args[4]) {
                case "LOCK_STEP":
                    policy = Policies.LOCK_STEP;
                    System.out.println("Using LOCK_STEP");
                    break;
                case "MAXIMIZE":
                    policy = Policies.MAXIMIZE;
                    System.out.println("Using MAXIMIZE");
                    break;
                case "SAME_MACHINE":
                    policy = Policies.SAME_MACHINE;
                    System.out.println("Using SAME_MACHINE");
                    break;
                default:
                    policy = Policies.MAXIMIZE;
            }
        }
    }

}
