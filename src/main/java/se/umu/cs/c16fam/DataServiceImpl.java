package se.umu.cs.c16fam;

import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author: filip
 * @since: 2023-05-19.
 */
public class DataServiceImpl implements DataService {
    private Integer[] listC = new Integer[]{31,54,81,59,50,9,9,395,338,3};
    private ReentrantLock lock = new ReentrantLock();

    @Override
    public ArrayList<Integer> getData() {
        System.err.println("Request to get data recieved.");
        lock.lock();
        System.err.println("Lock aquired");
        lock.unlock();
        return new ArrayList<>(Arrays.asList(listC));
    }

    @Override
    public void processData() throws RemoteException {
        System.err.println("Going to sleep");
        lock.lock();
        try {
            Thread.sleep(10000);
            System.err.println("Good morning");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        finally {
            lock.unlock();
        }
    }


}
