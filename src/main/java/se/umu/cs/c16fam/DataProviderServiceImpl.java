package se.umu.cs.c16fam;

import java.lang.reflect.Array;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Random;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: filip
 * @since: 2023-05-20.
 */
public class DataProviderServiceImpl implements DataProviderService {
    private final int LIST_SIZE = 1000;
    private final int MAX_VAL = 16000000;
    private final int N_CHUNKS = 1;
    private Integer[] listA = new Integer[]{31,54,81,59,50,9,9,395,338,3};
    private Integer[] listB = new Integer[]{28,67,88,50,3,107,52,395,909,1};
    private BlockingQueue<LinkedList<Integer>> q = new LinkedBlockingQueue<>();
    private LinkedList<Integer> bigList = new LinkedList<>();
    private BlockingQueue<LinkedList<Integer>> resQ;
    private int testSize, currSize;
    private ReentrantLock sizeLock = new ReentrantLock();
    private long endTime;

    public DataProviderServiceImpl(BlockingQueue<LinkedList<Integer>> resQ) {
        this.resQ = resQ;
    }

    public long initData(String cmd) {
        long sTime = 0;
        switch (cmd){
            case "rand":
                Random rand = new Random();
                rand.setSeed(42);
                for (int i=0; i<LIST_SIZE; i++)
                {
                    Integer r = rand.nextInt(MAX_VAL);
                    bigList.add(r);
                }

                sizeLock.lock();
                try {
                    testSize = LIST_SIZE * N_CHUNKS;
                    currSize = 0;
                }
                finally {
                    sizeLock.unlock();
                }

                sTime = System.currentTimeMillis();
                q.add(bigList);
                break;
            default:
                break;
        }
        return sTime;
    }

    public long getEndTime() {
        return endTime;
    }

    @Override
    public void uploadData(LinkedList<Integer> data) throws RemoteException {
        long time = System.currentTimeMillis();
        sizeLock.lock();
        try {
            //update total size
            currSize += data.size();
            //send data to control
            System.err.println("Got data of size " + data.size());
            resQ.add(data);
            //check if test done
            if (currSize >= testSize) {
                resQ.add(new LinkedList<>()); //stop condition for control
                endTime = time;
            }
        }
        finally {
            sizeLock.unlock();
        }
    }

    @Override
    public LinkedList<Integer> getData() throws RemoteException {
        LinkedList<Integer> data;
        try {
            data = q.take();
        }
        catch (Exception e) {
            e.printStackTrace();
            data = new LinkedList<>();
        }
        return data;
    }
}
