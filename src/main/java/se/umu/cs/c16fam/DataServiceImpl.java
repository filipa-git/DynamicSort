package se.umu.cs.c16fam;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Implementation of data service (server)
 * @author filipa-git
 * @since 2023-05-19.
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

    /**
     * Initialize
     * @param out       Queue for outgoing data to data provider
     * @param nSorters  Number of expected sorters
     * @param cache     Cache limit
     */
    public DataServiceImpl(BlockingQueue<ArrayList<Integer>> out, int
     nSorters, int cache) {
        outQueue = out;
        expectedSorters = nSorters;
        cache_limit = cache/(nSorters+1);
    }

    /**
     * [Remote] Upload data (sorter -> server)
     * @param id    Id of sorter, <0 if first upload
     * @param data  The data
     * @param done  Boolean indicating end-of-upload for sorter
     * @return The id of the sorter for future uploads
     */
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

            //Check if this is the final expected buffer
            //Unlock server when all expected buffers are present
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
            //Aquire lock for buffer
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
                //Signal that there is new data
                bufConds.get(n).signal();
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

    /**
     * Merge sorted data in buffers and send to data provider
     * @throws RemoteException if remote communication fails
     */
    public void sortData() throws RemoteException {
        //Wait for all buffers to be present
        countLock.lock();
        sortLock.lock();
        try {
            while (expectedSorters != bufCount) {
                countLock.unlock();
                //Server waits here until all expected buffers are present
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

        //Create list for keeping track of current buffer index
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

            //Done when no more buffers present
            while (!done) {
                min = -1; //Minimum value of first values in buffers
                bufId = -1;
                ArrayList<Integer> b;

                //Compare first elements in all buffers
                for (int i = 0; i < nBuf; i++) {
                    b = buffers.get(i);
                    if (b != null) {
                        if (bufInds.get(i) >= b.size() || b.isEmpty()) {
                            //Remove if done
                            if (doneSet.contains(i)) {
                                System.err.println("Removing " + i);
                                buffers.remove(i);
                                //Check if all is done
                                if (buffers.isEmpty())
                                    done = true;
                            }
                            else {
                                System.err.println(i + " needs more data");
                                //Allow more data to be uploaded
                                buffers.put(i,new ArrayList<>());
                                bufConds.get(i).signal();
                                bufLocks.get(i).unlock();
                                //Wait for more data
                                bufLocks.get(i).lock();
                                boolean repeat = false;
                                try {
                                    while (buffers.get(i).isEmpty() &&
                                            !repeat) {
                                        repeat = !bufConds.get(i).await(1000,
                                                TimeUnit.MILLISECONDS);
                                        if (repeat)
                                            bufLocks.get(i).lock();
                                    }
                                }
                                catch (Exception e) {
                                    e.printStackTrace();
                                }
                                System.err.println(i + " got more data");
                                bufInds.set(i, 0);
                                i--; //repeat this step in for-loop
                            }
                        }
                        //Update min
                        else if (b.get(bufInds.get(i)) < min || min == -1) {
                            min = b.get(bufInds.get(i));
                            bufId = i;
                        }
                    }
                }

                if (bufId > -1) {
                    int ind = bufInds.get(bufId);
                    //Add minimum value to output buffer and update index
                    outBuf.add(buffers.get(bufId).get(ind));
                    bufInds.set(bufId, ind+1);
                    //Update size of outgoing buffer
                    outCount++;
                }
                //Send data if cache limit reached or merge is done
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
