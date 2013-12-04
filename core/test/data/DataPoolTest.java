package data;

import com.parentoop.core.data.Datum;
import com.parentoop.core.data.NetworkDataPool;
import org.junit.*;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static org.junit.Assert.assertEquals;

public class DataPoolTest {

    private static final int THREAD_POOL_SIZE = 10;
    private static final int IO_TIMOUT = 1000;
    private static ScheduledExecutorService mExecutorService;
    private NetworkDataPool mNetworkDataPool;
    private List<ConsumerRunnable> mConsumerTasks;
    private List<ProducerRunnable> mProducerTasks;


    @Before
    public void setUp() throws InterruptedException {
        mExecutorService = Executors.newScheduledThreadPool(THREAD_POOL_SIZE);
        mNetworkDataPool = new NetworkDataPool();
        mConsumerTasks = Arrays.asList(new ConsumerRunnable[5]);
        mProducerTasks = Arrays.asList(new ProducerRunnable[5]);
        Iterator<Datum> it = mNetworkDataPool.iterator();
        for(int i = 0; i < 5; i++){
            mConsumerTasks.set(i, new ConsumerRunnable("TestTask", it));
            mProducerTasks.set(i, new ProducerRunnable("TestTask"));
        }
    }

    @After
    public void tearDown() throws InterruptedException {
        mNetworkDataPool.close();
        mExecutorService.shutdown();
        mExecutorService.awaitTermination(Integer.MAX_VALUE,TimeUnit.MILLISECONDS);
    }


    @Test(timeout = 5*IO_TIMOUT)
    public void testConsumerCanWrite() throws InterruptedException {
        mExecutorService.submit(mProducerTasks.get(0));
        Thread.sleep(IO_TIMOUT);
        mExecutorService.submit(mConsumerTasks.get(0));

        assertEquals(mProducerTasks.get(0).getDatum(), mConsumerTasks.get(0).getDatum());
    }

    @Test
    public void testMultipleThreadsCanWrite() throws InterruptedException {
        mExecutorService.scheduleAtFixedRate(mProducerTasks.get(0), 0, 100, TimeUnit.MILLISECONDS);
        mExecutorService.scheduleAtFixedRate(mProducerTasks.get(1), 0, 100, TimeUnit.MILLISECONDS);
        mExecutorService.scheduleAtFixedRate(mProducerTasks.get(2), 0, 100, TimeUnit.MILLISECONDS);

        Thread.sleep(5*IO_TIMOUT - 20);
        assertEquals(150, mNetworkDataPool.getBufferSize());
    }

    @Test
    public void testSimulataneousIO() throws InterruptedException {
        for(int i = 0; i < 5; i++){
            mExecutorService.scheduleWithFixedDelay(mProducerTasks.get(i), 0, 100, TimeUnit.MILLISECONDS);
            mExecutorService.scheduleWithFixedDelay(mConsumerTasks.get(i), 1000, 100, TimeUnit.MILLISECONDS);
        }

        Thread.sleep(2*IO_TIMOUT - 20);

        assertEquals(50, mNetworkDataPool.getBufferSize());
        for(int i = 0; i < 5; i++){
            assertEquals(10, mConsumerTasks.get(i).mActions);
        }

    }




    private class TaskRunnable implements Runnable {
        protected String mName;
        protected Datum mDatum;
        protected int mActions;

        public Datum getDatum() {
            return mDatum;
        }

        protected TaskRunnable(String taskName) {
            mName = taskName;
            mActions = 0;
            mDatum = new Datum(mName, mActions);
        }

        @Override
        public void run() {
            mDatum = new Datum(mName, ++mActions);
            mNetworkDataPool.yield(mDatum);
        }
    }

    private class ConsumerRunnable extends TaskRunnable {
        private Iterator<Datum> mDatumIterator;

        public ConsumerRunnable(String taskName, Iterator<Datum> iterator) {
            super(taskName);
            mDatumIterator = iterator;
        }

        @Override
        public void run() {
           mActions++;
           mDatum = mDatumIterator.next();
        }
    }

    private class ProducerRunnable extends TaskRunnable {

        protected ProducerRunnable(String taskName) {
            super(taskName);
        }

        @Override
        public void run() {
            mDatum = new Datum(mName, mActions++);
            mNetworkDataPool.yield(mDatum);
        }
    }


}
