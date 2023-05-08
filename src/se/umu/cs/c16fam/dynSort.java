package se.umu.cs.c16fam;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;

/**
 * @author: filip
 * @since: 2023-05-07.
 */
public class dynSort {
    /**
     * Insertion sort
     * @param list the list to be sorted
     * @return the original list sorted
     */
    public static ArrayList<Integer> insertionSort(ArrayList<Integer> list) {
        int n = list.size();
        int j;
        for (int i = 1; i < n; i++) {
            j = i;
            while (j > 0 && list.get(j - 1) > list.get(j)) {
                Collections.swap(list, j-1, j);
                j = j - 1;
            }
        }

        return list;
    }

    /**
     * quicksort
     * @param list the list to be sorted
     * @return the original list sorted
     */
    public static ArrayList<Integer> quickSort(ArrayList<Integer> list) {
        internalQuickSort(list, 0, list.size()-1);
        return list;
    }

    /**
     * Internal quicksort
     * @param list the list to sort
     * @param low lowest index to sort from
     * @param high highest index to sort to
     */
    private static void internalQuickSort(ArrayList<Integer> list, int
            low, int high) {
        if (low < high) {
            int pivot = partition(list, low, high);
            internalQuickSort(list, low, pivot-1);
            internalQuickSort(list, pivot+1, high);
        }
    }

    /**
     * internal partitioning of list for quicksort
     * @param list the list to sort
     * @param low lowest index to partition from
     * @param high highest index to partition to (index of pivot element)
     * @return index of pivot element after correct placement
     */
    private static int partition(ArrayList<Integer> list, int low, int high) {
        int pivot = list.get(high);
        int i = low-1;

        for (int j = low; j <= high; j++) {
            if (list.get(j) < pivot) {
                i++;
                Collections.swap(list, i, j);
            }
        }

        Collections.swap(list, i+1, high);
        return i+1;
    }

    /**
     * radix sort. based on https://www.geeksforgeeks.org/radix-sort/
     * @param list the list to sort
     * @return the original list sorted
     */
    public static ArrayList<Integer> radixSort(ArrayList<Integer> list) {
        //Get max
        Integer max = Collections.max(list);
        //Get number of digits of max
        int d = max.toString().length();

        //Make buckets
        LinkedList<Integer>[] buckets = new LinkedList[10];
        for (int i = 0; i < 10; i++)
            buckets[i] = new LinkedList<>();

        for (int i = 0; i < d; i++) {
            for (Integer e:
                 list) {
                int digit = (e / (int)Math.pow(10,i))%10;
                buckets[digit].add(e);
            }

            int x = 0, y = 0;
            while (x < 10) {
                while (!buckets[x].isEmpty()) {
                    list.set(y, buckets[x].remove());
                    y++;
                }
                x++;
            }
        }
        return list;
    }

    /**
     * merge sort
     * @param list the list to be sorted
     * @return the original list sorted
     */
    public static ArrayList<Integer> mergeSort(ArrayList<Integer> list) {
        internalMergeSort(list, 0, list.size()-1);
        return list;
    }

    private static void internalMergeSort(ArrayList<Integer> list, int l,
                                          int r) {
        if (l < r) {
            int mid = l + (r - l) / 2;

            internalMergeSort(list, l, mid);
            internalMergeSort(list, mid + 1, r);

            internalMerge(list, l, mid, r);
        }
    }

    private static void internalMerge(ArrayList<Integer> list, int l, int m,
                                      int r) {
        int nl = m-l+1;
        int nr = r-m;

        //copy halves
        ArrayList<Integer> left = new ArrayList<>();
        ArrayList<Integer> right = new ArrayList<>();

        for (int i = 0; i < nl; i++)
            left.add(list.get(l+i));

        for (int j = 0; j < nr; j++)
            right.add(list.get(m+1+j));

        //merge
        int i = 0, j = 0, k = l;
        while (i < nl && j < nr) {
            int lt = left.get(i);
            int rt = right.get(j);
            if (lt <= rt) {
                list.set(k, lt);
                i++;
            }
            else {
                list.set(k, rt);
                j++;
            }
            k++;
        }

        while (i < nl) {
            list.set(k, left.get(i));
            i++;
            k++;
        }

        while (j < nr) {
            list.set(k, right.get(j));
            j++;
            k++;
        }
    }
}
