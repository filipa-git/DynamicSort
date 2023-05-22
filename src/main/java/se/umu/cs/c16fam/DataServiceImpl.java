package se.umu.cs.c16fam;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: filip
 * @since: 2023-05-19.
 */
public class DataServiceImpl implements DataService {
    private int cache_limit = 16000000;

    private BlockingQueue<ArrayList<Integer>> outQueue;
    private ArrayList<Integer> outBuf = new ArrayList<>();
    private ReentrantLock countLock = new ReentrantLock();
    private int expectedSorters = 0;
    private ReentrantLock sortLock = new ReentrantLock();
    private Condition sortCond = sortLock.newCondition();
    private int bufCount = 0;
    private ConcurrentHashMap<Integer, ArrayList<Integer>> buffers = new
            ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, ReentrantLock> bufLocks = new
            ConcurrentHashMap<>();
    private ConcurrentHashMap<Integer, Condition> bufConds = new
            ConcurrentHashMap<>();
    private Set<Integer> doneSet = ConcurrentHashMap.newKeySet();

    public DataServiceImpl(BlockingQueue<ArrayList<Integer>> out, int
     nSorters, int cache) {
        outQueue = out;
        expectedSorters = nSorters;
        cache_limit = cache/(nSorters+1);
    }

    @Override
    public int uploadData(int id, ArrayList<Integer> data, boolean done) {
        int n = id;
        //Add data for first time
        if (id < 0) {
            //Increase buffer count
            countLock.lock();
            try {
                n = bufCount;
                bufCount++;
            }
            finally {
                countLock.unlock();
            }

            //Add new buffer, lock and condition
            buffers.put(n, data);
            ReentrantLock l = new ReentrantLock();
            bufLocks.put(n, l);
            bufConds.put(n, l.newCondition());

            //Unlock when all expected buffers are present
            sortLock.lock();
            try {
                if (bufCount == expectedSorters)
                    sortCond.signal();
            }
            finally {
                sortLock.unlock();
            }
        }
        else {
            //Aquire lock
            ReentrantLock lock = bufLocks.get(n);
            if (lock == null) {
                System.err.println("Could not find lock " + n);
                return -1;
            }
            lock.lock();

            try {
                while (!buffers.get(n).isEmpty())
                    bufConds.get(n).await();
                //Add data
                buffers.put(n, data);
            }
            catch (Exception e) {
                e.printStackTrace();
            }
            finally {
                lock.unlock();
            }
        }

        //Check if done
        if (done)
            doneSet.add(n);

        return n;
    }

    public void sortData() throws RemoteException {
        //Wait for all buffers to be present
        countLock.lock();
        sortLock.lock();
        try {
            while (expectedSorters != bufCount) {
                countLock.unlock();
                sortCond.await();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            sortLock.unlock();
        }
        System.err.println("Beginning final sorting");

        //Aquire all buffer locks
        int nBuf;
        countLock.lock();
        try {
            nBuf = bufCount;
        }
        finally {
            countLock.unlock();
        }

        ArrayList<Integer> bufInds = new ArrayList<>();

        for (int i = 0; i < nBuf; i++) {
            try {
                bufLocks.get(i).lock();
                bufInds.add(i,0);
            }
            catch (Exception e) {
                e.printStackTrace();
                return;
            }
        }

        System.err.println("Beginning merge");
        try {
            //K-way merge sort
            int min;
            int bufId;
            int outCount = 0;
            boolean done = false;
            boolean repeat = false;//failsafe if sorter crashes

            while (!done) {
                min = -1;
                bufId = -1;
                ArrayList<Integer> b;

                //Compare first elements in all buffers
                for (int i = 0; i < nBuf; i++) {
                    b = buffers.get(i);
                    if (b != null) {
                        if (bufInds.get(i) >= b.size() || b.isEmpty()) {
                            //remove if done or repeated
                            if (doneSet.contains(i) || repeat) {
                                buffers.remove(i);
                                //check if all is done
                                if (buffers.isEmpty())
                                    done = true;
                                repeat = false;
                            }
                            else {
                                //allow more data
                                buffers.put(i,new ArrayList<>());
                                bufConds.get(i).signal();
                                bufLocks.get(i).unlock();
                                try {
                                    Thread.sleep(1000);
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                bufLocks.get(i).lock();
                                bufInds.set(i, 0);
                                i--; //repeat this step in for-loop
                                repeat = true;
                            }
                        }
                        else if (b.get(bufInds.get(i)) < min || min == -1) {
                            min = b.get(bufInds.get(i));
                            bufId = i;
                            repeat = false;
                        }
                    }
                }

                if (bufId > -1) {
                    int ind = bufInds.get(bufId);
                    //Move min from original buffer to output buffer
                    outBuf.add(buffers.get(bufId).get(ind));
                    bufInds.set(bufId, ind+1);
                    outCount++;
                }
                //Send data if cache limit reached or done
                if (outCount >= cache_limit || done) {
                    System.err.println("Sending data");
                    if (!outBuf.isEmpty())
                        outQueue.add(outBuf);
                    outBuf = new ArrayList<>();
                    outCount = 0;
                }
            }
        }
        finally {
            //Release all locks
            for (int i = 0; i < nBuf; i++) {
                try {
                    bufLocks.get(i).unlock();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
