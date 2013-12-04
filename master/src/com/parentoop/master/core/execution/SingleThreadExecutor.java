package com.parentoop.master.core.execution;

import java.util.concurrent.*;

public class SingleThreadExecutor implements Executor {

    private SingleThreadFactory mThreadFactory;
    private ExecutorService mExecutorService;
    private Thread.UncaughtExceptionHandler mUncaughtExceptionHandler;

    public SingleThreadExecutor() {
        mThreadFactory = new SingleThreadFactory();
        mExecutorService = new ExceptionProxyExecutorService(mThreadFactory);
    }

    public void setUncaughtExceptionHandler(Thread.UncaughtExceptionHandler uncaughtExceptionHandler) {
        mUncaughtExceptionHandler = uncaughtExceptionHandler;
    }

    @Override
    public void execute(Runnable command) {
        if (Thread.currentThread() == mThreadFactory.mThread) {
            command.run();
            return;
        }
        mExecutorService.execute(command);
    }

    public void shutdown() {
        mExecutorService.shutdown();
    }

    public void shutdownNow() {
        mExecutorService.shutdownNow();
    }

    private class ExceptionProxyExecutorService extends ThreadPoolExecutor {

        public ExceptionProxyExecutorService(ThreadFactory threadFactory) {
            super(1, 1, 0L, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>(), threadFactory);
        }

        @Override
        protected void afterExecute(Runnable r, Throwable t) {
            super.afterExecute(r, t);
            if (t == null && r instanceof Future<?>) {
                try {
                    Future<?> future = (Future<?>) r;
                    if (future.isDone())
                        future.get();
                } catch (CancellationException ce) {
                    t = ce;
                } catch (ExecutionException ee) {
                    t = ee.getCause();
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt(); // ignore/reset
                }
            }
            if (t != null && mUncaughtExceptionHandler != null) {
                mUncaughtExceptionHandler.uncaughtException(mThreadFactory.mThread, t);
            }
        }
    }

    private class SingleThreadFactory implements ThreadFactory {

        private Thread mThread;

        @Override
        public Thread newThread(Runnable r) {
            return mThread = new Thread(r);
        }
    }
}
