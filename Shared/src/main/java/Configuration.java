public class Configuration {
    /**
     * All global configuration parameters should be stored here
     */
    public static final int NUMBER_OF_ERRORS_TO_CORRECT = 2;
    public static final int NUMBER_OF_HEADNODES = NUMBER_OF_ERRORS_TO_CORRECT+1;
    public static final int NUMBER_OF_BYZANTIAN_ERRORS = NUMBER_OF_ERRORS_TO_CORRECT*2+1;
}
