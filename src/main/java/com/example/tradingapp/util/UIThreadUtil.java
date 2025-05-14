package com.example.tradingapp.util;

import javafx.application.Platform;
import javafx.concurrent.Task;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Supplier;

// Utility class for managing UI thread operations and background tasks
public class UIThreadUtil {
    
    // Thread pool for background operations
    private static final ExecutorService backgroundExecutor = Executors.newFixedThreadPool(
            Runtime.getRuntime().availableProcessors(), 
            new DaemonThreadFactory("TradingApp-Background-"));
    
    public static void runOnUIThread(Runnable action) {
        if (Platform.isFxApplicationThread()) {
            action.run();
        } else {
            try {
                Platform.runLater(action);
            } catch (Exception e) {
                System.err.println("Error running on UI thread: " + e.getMessage());
                e.printStackTrace();
            }
        }
    }
    
    public static void runInBackground(Runnable task) {
        backgroundExecutor.submit(task);
    }
    
    // Runs a task in the background and then processes the result on the UI thread
    public static <T> void runTaskWithCallback(Supplier<T> backgroundTask, Consumer<T> uiThreadCallback) {
        Task<T> task = new Task<>() {
            @Override
            protected T call() throws Exception {
                return backgroundTask.get();
            }
        };
        
        task.setOnSucceeded(event -> {
            T result = task.getValue();
            Platform.runLater(() -> uiThreadCallback.accept(result));
        });
        
        task.setOnFailed(event -> {
            Throwable exception = task.getException();
            System.err.println("Task failed: " + exception.getMessage());
            exception.printStackTrace();
        });
        
        backgroundExecutor.submit(task);
    }
    
    private static class DaemonThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix;
        
        DaemonThreadFactory(String namePrefix) {
            this.namePrefix = namePrefix;
        }
        
        @Override
        public Thread newThread(Runnable r) {
            Thread t = new Thread(r, namePrefix + threadNumber.getAndIncrement());
            t.setDaemon(true);
            return t;
        }
    }
    
    // Shutdown the executor service
    public static void shutdown() {
        backgroundExecutor.shutdown();
    }
} 