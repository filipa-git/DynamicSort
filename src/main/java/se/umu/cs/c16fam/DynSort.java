package se.umu.cs.c16fam;

import java.util.*;

/**
 * Class for sorting positive integer data utilizing multiple
 * sorting-functions depending on calculated partial standard deviation
 * @author filipa-git
 * @since 2023-05-07.
 */
public class DynSort {
    private static final int CHOICE_SIZE = 3000000;
    private static final int INS_LIMIT = 10;
    private static final int DEV_LIMIT = 1000000;
    private static final int N_DEV_LIMIT = 1000;
    private int nIns = 0;
    private int nQuick = 0;
    private int nRadix = 0;
    private int nParts = 0;

    /**
     * DynamicSort function. Determine if  list should be split into k sets
     * dependant on CHOICE_SIZE. If split, run k-way merge sort. If not, run
     * dynamic Quicksort.
     * @param list The data to sort
     * @return The sorted data (original reference still works)
     */
    public ArrayList<Integer> dynamicSort(ArrayList<Integer> list) {
        System.err.println("dSort");
        int size = list.size();

        //Split, k-way merge
        if (size > CHOICE_SIZE){
            int k = size / CHOICE_SIZE + ((size % CHOICE_SIZE == 0) ? 0 : 1);
            System.err.println("k: " + k);
            nParts = k;
            kMergeSort(list, k);
        }
        //No split, dynamic Quicksort
        else {
            cQuickSort(list);
        }

        System.err.println("nParts: " + nParts + "; nIns: " + nIns + "; " +
                "nQuick: " + nQuick + "; nRadix: " + nRadix);

        return list;
    }

    /**
     * Private class for nodes used in k-way merging
     */
    private static class KNode implements Comparable<KNode>{
        int i;
        int ind;
        int val;

        KNode (int i, int ind, int val){
            this.i = i;
            this.val = val;
            this.ind= ind;
        }

        @Override
        public int compareTo(KNode n) {
            if (this.val <= n.val)
                return -1;
            else
                return 1;
        }
    }

    /**
     * Split data and merge after sorting using k-way merge sort
     * @param list  The data to sort
     * @param k     The amount of chunks to split into
     * @return The sorted data (original reference still works)
     */
    private ArrayList<Integer> kMergeSort(ArrayList<Integer> list, int
            k) {
        System.err.println("kSort");
        Queue<KNode> pq = new PriorityQueue<>();

        ArrayList<ArrayList<Integer>> parts = new ArrayList<>();

        //Partition data
        for (int i = 0; i < k; i++) {
            ArrayList<Integer> p = new ArrayList<>();
            for (int j = CHOICE_SIZE*i; j < CHOICE_SIZE*(i+1); j++) {
                if (j >= list.size()) //set j to max to break for-loop early
                    j = CHOICE_SIZE*(i+1);
                else
                    p.add(list.get(j));
            }
            parts.add(p);
            System.err.println("Added partition " + p.size());
        }

        //Sort partitions using dynamic Quicksort
        for (ArrayList<Integer> part : parts) {
            cQuickSort(part);
        }

        //Merge using priority queue
        for (int i = 0; i < k; i++) {
            pq.add(new KNode(i, 0, parts.get(i).get(0)));
        }

        KNode curr;
        int ind;
        ArrayList<Integer> p;
        list.clear();
        while (!pq.isEmpty()) {
            curr = pq.poll();
            list.add(curr.val);

            p = parts.get(curr.i);
            ind = (curr.ind)+1;
            if (ind < p.size())
                pq.add(new KNode(curr.i, ind, p.get(ind)));
        }

        return list;
    }

    /**
     * Dynamic Quicksort that may choose to sort partitions using other
     * algorithms depending on partial standard deviation
     * @param list  The data to sort
     */
    private void cQuickSort(ArrayList<Integer> list) {
        int low = 0;
        int high = list.size()-1;
        double lowSum = 0;
        double highSum = 0;
        double lowUsed = 0;
        double highUsed = 0;
        //Use insertion sort if list is appropriately small
        if (high+1 <= INS_LIMIT)
            insertionSort(list);
        else {
            nQuick++;
            int pivot = list.get(high);
            ArrayList<Integer> lowNums = new ArrayList<>();
            ArrayList<Integer> highNums = new ArrayList<>();

            //Divide into low and high lists
            for (int j = low; j < high; j++) {
                Integer v = list.get(j);
                if (v < pivot) {
                    if (lowUsed < N_DEV_LIMIT) {
                        lowSum += v;
                        lowUsed++;
                    }
                    lowNums.add(v);
                }
                else {
                    highNums.add(v);
                    if (highUsed < N_DEV_LIMIT) {
                        highSum += v;
                        highUsed++;
                    }
                }
            }

            //Calculate partial standard deviation
            if (!lowNums.isEmpty()) {
                double lowMean = lowSum / lowUsed;
                double lowDev = 0;
                for (int i = 0; i < lowUsed; i++) {
                    int v = lowNums.get(i);
                    lowDev += Math.pow(v - lowMean, 2);
                }
                lowDev = Math.sqrt(lowDev / (lowUsed-1));

                //Sort low list
                //Use dynamic Quicksort if lower deviation, radix sort if higher
                if (lowDev < DEV_LIMIT)
                    cQuickSort(lowNums);
                else
                    radixSort(lowNums);
            }

            if (!highNums.isEmpty()) {
                double highMean = highSum / highUsed;
                double highDev = 0;
                for (int i = 0; i < highUsed; i++) {
                    int v = highNums.get(i);
                    highDev += Math.pow(v - highMean, 2);
                }
                highDev = Math.sqrt(highDev / highUsed);

                //Sort high list
                //Use dynamic Quicksort if lower deviation, radix sort if higher
                if (highDev < DEV_LIMIT)
                    cQuickSort(highNums);
                else
                    radixSort(highNums);
            }

            //Replace original list with sorted lists and pivot element
            list.clear();
            list.addAll(lowNums);
            list.add(pivot);
            list.addAll(highNums);
        }
    }

    /**
     * Insertion sort
     * @param list The list to be sorted
     * @return The sorted data (original reference still works)
     */
    public ArrayList<Integer> insertionSort(ArrayList<Integer> list) {
        nIns++;
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
     * Regular Quicksort
     * @param list The list to be sorted
     * @return  The sorted data (original reference still works)
     */
    public static ArrayList<Integer> quickSort(ArrayList<Integer> list) {
        internalQuickSort(list, 0, list.size()-1);
        return list;
    }

    /**
     * Internal Quicksort
     * @param list  The list to sort
     * @param low   Lowest index to sort from
     * @param high  Highest index to sort to
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
     * Internal partitioning of list for Quicksort
     * @param list  The list to sort
     * @param low   Lowest index to partition from
     * @param high  Highest index to partition to (index of pivot element)
     * @return Index of pivot element after correct placement
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
     * Radix sort. based on https://www.geeksforgeeks.org/radix-sort/
     * @param list The list to sort
     * @return The sorted data (original reference still works)
     */
    public ArrayList<Integer> radixSort(ArrayList<Integer> list) {
        if (list.size() <= INS_LIMIT)
            insertionSort(list);
        else {
            nRadix++;
            //Get max
            Integer max = Collections.max(list);
            //Get number of digits of max
            int d = max.toString().length();

            //Make buckets
            LinkedList<Integer>[] buckets = new LinkedList[10];
            for (int i = 0; i < 10; i++)
                buckets[i] = new LinkedList<>();

            for (int i = 0; i < d; i++) {
                for (Integer e :
                        list) {
                    int digit = (e / (int) Math.pow(10, i)) % 10;
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
        }
        return list;
    }

    /**
     * Merge sort
     * @param list The list to be sorted
     * @return The sorted data (original reference still works)
     */
    public static ArrayList<Integer> mergeSort(ArrayList<Integer> list) {
        internalMergeSort(list, 0, list.size()-1);
        return list;
    }

    /**
     * Internal function for merge sort. Split list into halves.
     * @param list  The data to sort
     * @param l     The leftmost index to use
     * @param r     The rightmost index to use
     */
    private static void internalMergeSort(ArrayList<Integer> list, int l,
                                          int r) {
        if (l < r) {
            int mid = l + (r - l) / 2;

            internalMergeSort(list, l, mid);
            internalMergeSort(list, mid + 1, r);

            internalMerge(list, l, mid, r);
        }
    }

    /**
     * Internal function for merging a list for merge sort
     * @param list  The data
     * @param l     The leftmost index to merge from
     * @param m     The mid-point index to merge around
     * @param r     The rightmost index to merge to
     */
    private static void internalMerge(ArrayList<Integer> list, int l, int m,
                                      int r) {
        int nl = m-l+1;
        int nr = r-m;

        //Copy halves
        ArrayList<Integer> left = new ArrayList<>();
        ArrayList<Integer> right = new ArrayList<>();

        for (int i = 0; i < nl; i++)
            left.add(list.get(l+i));

        for (int j = 0; j < nr; j++)
            right.add(list.get(m+1+j));

        //Merge
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
