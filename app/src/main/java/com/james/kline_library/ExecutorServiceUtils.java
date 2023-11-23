package com.james.kline_library;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class ExecutorServiceUtils {
    private static ExecutorServiceUtils instance;
    private ExecutorService executorService;

    private ExecutorServiceUtils() {
    }

    public synchronized static ExecutorServiceUtils getInstance() {
        if (instance == null) {
            instance = new ExecutorServiceUtils();
        }
        return instance;
    }

    public synchronized ExecutorService getExecutorService() {
        if (executorService == null
                || executorService.isShutdown()) {
            int size = Runtime.getRuntime().availableProcessors();
            if (size < 1) {
                size = 1;
            }
            size *= 2;
            executorService = Executors.newFixedThreadPool(size);
        }
        return executorService;
    }
}
