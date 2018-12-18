import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Used to store handy functions
 */
public class Utils {

    /**
     * Return a list of length x
     * @param x
     * @return
     */
    public static List<Integer> getListOfLength(int x) {
        List<Integer> list = new ArrayList<>();
        for(int i = 0; i < x; i++) {
            list.add(i);
        }
        return list;
    }
}
