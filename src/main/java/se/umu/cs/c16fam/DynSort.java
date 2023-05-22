package se.umu.cs.c16fam;

import java.lang.reflect.Array;
import java.util.*;

/**
 * @author: filip
 * @since: 2023-05-07.
 */
public class DynSort {
    private static final int CHOICE_SIZE = 3000000;
    private static final int INS_LIMIT = 10;
    private static final int DEV_LIMIT = 1000000;

    public static LinkedList<Integer> dynamicSort(LinkedList<Integer> list) {
        System.err.println("dSort");
        int size = list.size();

        if (size > CHOICE_SIZE){
            int k = (int) Math.ceil(size/CHOICE_SIZE);
            kMergeSort(list, k);
        }
        else {
            cQuickSort(list);
        }

        return list;
    }

    /**
     * Private class for nodes used in k-way merging
     */
    private static class KNode implements Comparable<KNode>{
        int i;
        int val;

        KNode (int i, int val){
            this.i = i;
            this.val = val;
        }

        @Override
        public int compareTo(KNode n) {
            if (this.val <= n.val)
                return -1;
            else
                return 1;
        }
    }

    private static LinkedList<Integer> kMergeSort(LinkedList<Integer> list, int
            k) {
        System.err.println("kSort");
        Queue<KNode> pq = new PriorityQueue<>();

        LinkedList<LinkedList<Integer>> parts = new LinkedList<>();

        //partition
        for (int i = 0; i < k; i++) {
            LinkedList<Integer> p = new LinkedList<>();
            for (int j = 0; j < CHOICE_SIZE; j++) {
                if (list.isEmpty())
                    break;
                p.add(list.poll());
            }
            parts.add(p);
        }

        //sort partitions
        for (LinkedList<Integer> part : parts) {
            cQuickSort(part);
        }

        //merge using priority queue
        for (int i = 0; i < k; i++) {
            pq.add(new KNode(i, parts.get(i).poll()));
        }

        KNode curr;
        LinkedList<Integer> p;
        while (!pq.isEmpty()) {
            curr = pq.poll();
            list.add(curr.val);

            p = parts.get(curr.i);
            if (!p.isEmpty())
                pq.add(new KNode(curr.i, p.poll()));
        }

        return list;
    }

    /**
     * Dynamic quicksort that may choose to sort partitions using other
     * algorithms
     * @param list
     * @return
     */
    private static void cQuickSort(LinkedList<Integer> list) {
        System.err.println("qSort");
        int low = 0;
        int high = list.size()-1;
        int lowSum = 0;
        int highSum = 0;
        //Use insertion sort if list is appropriately small
        if (high+1 <= INS_LIMIT)
            insertionSort(list);
        else {
            int pivot = list.get(high);
            int i = low-1;

            for (int j = low; j <= high; j++) {
                System.err.println(j);
                if (list.get(j) < pivot) {
                    i++;
                    lowSum+=list.get(j);
                    Collections.swap(list, i, j);
                }
                else
                    highSum+=list.get(j);
            }

            Collections.swap(list, i+1, high);

            System.err.println("calculating deviation");
            //Divide into new lists while calculating standard deviation
            LinkedList<Integer> lowNums = new LinkedList<>();
            if (i > -1) {
                double lowMean = lowSum / (i + 1);
                double lowDev = 0;
                while (lowNums.size() < i + 1) {
                    int v = list.poll();
                    lowDev += Math.pow(v - lowMean, 2);
                    lowNums.add(v);
                }
                lowDev = Math.sqrt(lowDev / (i + 1));
                System.err.println("done");

                //Sort lownums
                if (lowDev < DEV_LIMIT)
                    cQuickSort(lowNums);
                else
                    radixSort(lowNums);
            }

            //Remove pivot
            list.poll();

            System.err.println("calculating deviation");
            LinkedList<Integer> highNums = new LinkedList<>();
            if (!list.isEmpty()) {
                double highMean = highSum / (list.size());
                double highDev = 0;
                while (!list.isEmpty()) {
                    int v = list.poll();
                    highDev += Math.pow(v - highMean, 2);
                    highNums.add(v);
                }
                highDev = Math.sqrt(highDev / (i + 1));
                System.err.println("done");

                //sort highnums
                if (highDev < DEV_LIMIT)
                    cQuickSort(highNums);
                else
                    radixSort(highNums);
            }

            //Add all to same list
            list.addAll(lowNums);
            list.add(pivot);
            list.addAll(highNums);
        }
    }

    /**
     * Insertion sort
     * @param list the list to be sorted
     * @return the original list sorted
     */
    public static LinkedList<Integer> insertionSort(LinkedList<Integer> list) {
        System.err.println("iSort");
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
    public static LinkedList<Integer> quickSort(LinkedList<Integer> list) {
        internalQuickSort(list, 0, list.size()-1);
        return list;
    }

    /**
     * Internal quicksort
     * @param list the list to sort
     * @param low lowest index to sort from
     * @param high highest index to sort to
     */
    private static void internalQuickSort(LinkedList<Integer> list, int
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
    private static int partition(LinkedList<Integer> list, int low, int high) {
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
    public static LinkedList<Integer> radixSort(LinkedList<Integer> list) {
        System.err.println("rSort");
        if (list.size() <= INS_LIMIT)
            insertionSort(list);
        else {
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
     * merge sort
     * @param list the list to be sorted
     * @return the original list sorted
     */
    public static LinkedList<Integer> mergeSort(LinkedList<Integer> list) {
        internalMergeSort(list, 0, list.size()-1);
        return list;
    }

    private static void internalMergeSort(LinkedList<Integer> list, int l,
                                          int r) {
        if (l < r) {
            int mid = l + (r - l) / 2;

            internalMergeSort(list, l, mid);
            internalMergeSort(list, mid + 1, r);

            internalMerge(list, l, mid, r);
        }
    }

    private static void internalMerge(LinkedList<Integer> list, int l, int m,
                                      int r) {
        int nl = m-l+1;
        int nr = r-m;

        //copy halves
        LinkedList<Integer> left = new LinkedList<>();
        LinkedList<Integer> right = new LinkedList<>();

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
