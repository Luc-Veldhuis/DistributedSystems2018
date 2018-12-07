import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static List<Integer> getListOfLength(int x) {
        List<Integer> list = new ArrayList<Integer>();
        for(int i = 0; i < x; i++) {
            list.add(i);
        }
        return list;
    }
}
