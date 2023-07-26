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
 * Implementation of data provider service
 * @author filipa-git
 * @since 2023-05-20.
 */
public class DataProviderServiceImpl implements DataProviderService {
    private int list_size = 500000;
    private int n_chunks = 1;
    private final int MAX_VAL = 16000000;
    private BlockingQueue<ArrayList<Integer>> q = new LinkedBlockingQueue<>();
    private BlockingQueue<ArrayList<Integer>> resQ;
    private int testSize, currSize;
    private ReentrantLock sizeLock = new ReentrantLock();
    private long endTime;

    /**
     * Initialize DataProviderServiceImpl
     * @param resQ Queue to put results into for control by Main
     * @param size Max size of data lists (int)
     * @param chunks Number of data lists to create, one per sorter (int)
     */
    public DataProviderServiceImpl(BlockingQueue<ArrayList<Integer>> resQ,
                                   int size, int chunks) {
        this.resQ = resQ;
        this.list_size = size;
        this.n_chunks = chunks;
    }

    /**
     * Initialize BlockingQueue 'q' and start a sorting test.
     * @param cmd String determining the data to create
     * @return The start time of the test in milliseconds (long)
     */
    public long initData(String cmd) {
        long sTime;
        ArrayList<ArrayList<Integer>> tempList = new ArrayList<>();
        Random rand = new Random();
        //Create data depending on cmd
        switch (cmd){
            case "srand": //Seeded random
                rand.setSeed(42);
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r = rand.nextInt(MAX_VAL);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "sgaus3": //Seeded gaussian, standard deviation of 1000
                rand.setSeed(42);
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r;
                        do {
                            r = (int) (rand.nextGaussian() * 1000 + 8000000);
                        } while (r < 0);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "sgaus4": //Seeded gaussian, standard deviation of 10000
                rand.setSeed(42);
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r;
                        do {
                            r = (int) (rand.nextGaussian() * 10000 + 8000000);
                        } while (r < 0);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "sgaus5": //Seeded gaussian, standard deviation of 100000
                rand.setSeed(42);
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r;
                        do {
                            r = (int) (rand.nextGaussian() * 100000 + 8000000);
                        } while (r < 0);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "sgaus6": //Seeded gaussian, standard deviation of 1000000
                rand.setSeed(42);
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r;
                        do {
                            r = (int) (rand.nextGaussian() * 1000000 + 8000000);
                        } while (r < 0);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "sgaus7": //Seeded gaussian, standard deviation of 10000000
                rand.setSeed(42);
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r;
                        do {
                            r = (int) (rand.nextGaussian() * 10000000 +
                                    30000000);
                        } while (r < 0);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "rand": //Random
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r = rand.nextInt(MAX_VAL);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "gaus3": //Gaussian, standard deviation of 1000
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r;
                        do {
                            r = (int) (rand.nextGaussian() * 1000 + 8000000);
                        } while (r < 0);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "gaus4": //Gaussian, standard deviation of 10000
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r;
                        do {
                            r = (int) (rand.nextGaussian() * 10000 + 8000000);
                        } while (r < 0);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "gaus5": //Gaussian, standard deviation of 100000
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r;
                        do {
                            r = (int) (rand.nextGaussian() * 100000 + 8000000);
                        } while (r < 0);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "gaus6": //Gaussian, standard deviation of 1000000
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r;
                        do {
                            r = (int) (rand.nextGaussian() * 1000000 + 8000000);
                        } while (r < 0);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "gaus7": //Gaussian, standard deviation of 10000000
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        Integer r;
                        do {
                            r = (int) (rand.nextGaussian() * 10000000 +
                                    30000000);
                        } while (r < 0);
                        l.add(r);
                    }
                    tempList.add(l);
                }
                break;
            case "sorted": //Pre-sorted lists
                System.err.println(n_chunks);
                for (int j = 0; j < n_chunks; j++) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = 0; i < list_size; i++) {
                        l.add(i+(list_size*j));
                    }
                    tempList.add(l);
                }
                break;
            case "rsorted": //Reverse-sorted lists
                System.err.println(n_chunks);
                for (int j = n_chunks-1; j >= 0; j--) {
                    ArrayList<Integer> l = new ArrayList<>();
                    System.err.println(list_size);
                    for (int i = list_size-1; i >= 0; i--) {
                        l.add(i+(list_size*j));
                    }
                    tempList.add(l);
                }
                break;

            default:
                return 0;
        }

        //Handle concurrency of size
        sizeLock.lock();
        try {
            testSize = list_size * n_chunks;
            currSize = 0;
        }
        finally {
            sizeLock.unlock();
        }

        //Start test
        sTime = System.currentTimeMillis();
        try {
            for (ArrayList<Integer> e : tempList) {
                System.err.println("Adding data " + e.size());
                q.put(e);
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return sTime;
    }

    /**
     * Get stored end time of test
     * @return End time of test (long)
     */
    public long getEndTime() {
        return endTime;
    }

    /**
     * [Remote] Send data (server -> data provider)
     * @param data The sent data (ArrayList<Integer)
     * @throws RemoteException if remote communication fails
     */
    @Override
    public void uploadData(ArrayList<Integer> data) throws RemoteException {
        //Store time of upload
        long time = System.currentTimeMillis();

        sizeLock.lock();
        try {
            //Update total size
            currSize += data.size();
            //Send data to control by Main
            resQ.add(data);
            //Check if test done
            if (currSize >= testSize) {
                resQ.add(new ArrayList<>()); //Stop-condition for control
                endTime = time;
            }
        }
        finally {
            sizeLock.unlock();
        }
    }

    /**
     * [Remote] Download data (data provider -> sorter)
     * @return The downloaded data
     * @throws RemoteException if remote communication fails
     */
    @Override
    public ArrayList<Integer> getData() throws RemoteException {
        ArrayList<Integer> data;
        try {
            data = q.take();
        }
        catch (Exception e) {
            e.printStackTrace();
            //Send empty list if failed
            data = new ArrayList<>();
        }
        return data;
    }
}
