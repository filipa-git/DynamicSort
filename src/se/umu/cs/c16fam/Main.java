package se.umu.cs.c16fam;

import java.util.ArrayList;
import java.util.Arrays;

public class Main {

    public static void main(String[] args) {
	    Integer[] listA = new Integer[]{1,2,3,4,5,6,7,8,9,10};
        Integer[] listB = new Integer[]{10,9,8,7,6,5,4,3,2,1};
        Integer[] listC = new Integer[]{31,54,81,59,50,9,9,395,338,3};

        ArrayList<Integer> list = new ArrayList<>(Arrays.asList(listC));
        System.out.println("List: " + list.toString());
        dynSort.insertionSort(list);
        System.out.println("Insertion sort: " + list.toString());

        list = new ArrayList<>(Arrays.asList(listC));
        System.out.println("List: " + list.toString());
        dynSort.quickSort(list);
        System.out.println("Quicksort: " + list.toString());

        list = new ArrayList<>(Arrays.asList(listC));
        System.out.println("List: " + list.toString());
        dynSort.radixSort(list);
        System.out.println("Radix sort: " + list.toString());

        list = new ArrayList<>(Arrays.asList(listC));
        System.out.println("List: " + list.toString());
        dynSort.mergeSort(list);
        System.out.println("Merge sort: " + list.toString());
    }
}
