package group4;

import java.util.Comparator;
import java.util.List;

public class OptionComparator implements Comparator<List<Double>> {
    @Override
    public int compare(List<Double> o1, List<Double> o2) {
	    /*
	    if(o1.get(0) > o2.get(0)) return -1;
		if(o1.get(0) < o2.get(0)) return 1;
		return 0;
	     */
        return o2.get(0).compareTo(o1.get(0));
    }
}