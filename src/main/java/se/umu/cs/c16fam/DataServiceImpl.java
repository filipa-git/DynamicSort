package se.umu.cs.c16fam;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * @author: filip
 * @since: 2023-05-19.
 */
public class DataServiceImpl implements DataService {
    private Integer[] listC = new Integer[]{31,54,81,59,50,9,9,395,338,3};

    @Override
    public ArrayList<Integer> getData() {
        return new ArrayList<>(Arrays.asList(listC));
    }
}
