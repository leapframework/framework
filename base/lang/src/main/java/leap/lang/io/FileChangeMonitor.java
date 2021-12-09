/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.lang.io;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ThreadFactory;
import java.util.function.Predicate;
import leap.lang.Exceptions;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

/**
 * A runnable that spawns a monitoring thread triggering any
 * registered {@link FileChangeObserver} at a specified interval.
 * 
 * @see FileChangeObserver
 */
public class FileChangeMonitor implements Runnable {
	
	private static final Log log = LogFactory.get(FileChangeMonitor.class);
	
	private static final long ERROR_SLEEP_INTERVAL = 10000;

    protected final long interval;
    protected final List<FileChangeObserver> observers = new CopyOnWriteArrayList<FileChangeObserver>();
    protected Thread thread = null;
    protected ThreadFactory threadFactory;
    protected boolean daemon = true;
    protected volatile boolean running = false;
    protected boolean errorStop;

    /**
     * Construct a monitor with a default interval of 10 seconds.
     */
    public FileChangeMonitor() {
        this(10000);
    }

    /**
     * Construct a monitor with the specified interval.
     *
     * @param interval The amount of time in miliseconds to wait between
     * checks of the file system
     */
    public FileChangeMonitor(long interval) {
        this.interval = interval;
    }

    /**
     * Construct a monitor with the specified interval and set of observers.
     *
     * @param interval The amount of time in miliseconds to wait between
     * checks of the file system
     * @param observers The set of observers to add to the monitor.
     */
    public FileChangeMonitor(long interval, FileChangeObserver... observers) {
        this(interval);
        if (observers != null) {
            for (FileChangeObserver observer : observers) {
                addObserver(observer);
            }
        }
    }

    /**
     * Return the interval.
     *
     * @return the interval
     */
    public long getInterval() {
        return interval;
    }
    
    public void setDaemon(boolean daemon){
    	this.daemon = daemon;
    }
    
    public void setErrorStop(boolean errorStop){
    	this.errorStop = errorStop;
    }

    /**
     * Set the thread factory.
     *
     * @param threadFactory the thread factory
     */
    public synchronized void setThreadFactory(ThreadFactory threadFactory) {
        this.threadFactory = threadFactory;
    }

    /**
     * Add a file system observer to this monitor.
     *
     * @param observer The file system observer to add
     */
    public void addObserver(final FileChangeObserver observer) {
        if (observer != null) {
            observers.add(observer);
        }
    }

    /**
     * Remove a file system observer from this monitor.
     *
     * @param observer The file system observer to remove
     */
    public void removeObserver(final FileChangeObserver observer) {
        if (observer != null) {
            while (observers.remove(observer)) {
            }
        }
    }

    /**
     * Remove file system observer from this monitor.
     */
    public void removeObserver(final Predicate<FileChangeObserver> func) {
        if (func != null) {
            observers.removeIf(func);
        }
    }

    /**
     * Returns the set of {@link FileChangeObserver} registered with
     * this monitor. 
     *
     * @return The set of {@link FileChangeObserver}
     */
    public Iterable<FileChangeObserver> getObservers() {
        return observers;
    }

    /**
     * Start monitoring.
     *
     * @throws Exception if an error occurs initializing the observer
     */
    public synchronized void start() throws Exception {
        if (running) {
            throw new IllegalStateException("Monitor is already running");
        }
        for (FileChangeObserver observer : observers) {
            observer.initialize();
        }
        running = true;
        if (threadFactory != null) {
            thread = threadFactory.newThread(this);
        } else {
            thread = new Thread(this);
        }
        thread.setDaemon(daemon);
        thread.start();
    }

    /**
     * Stop monitoring.
     *
     * @throws Exception if an error occurs initializing the observer
     */
    public synchronized void stop() throws Exception {
        stop(interval);
    }

    /**
     * Stop monitoring.
     *
     * @param stopInterval the amount of time in milliseconds to wait for the thread to finish.
     * A value of zero will wait until the thread is finished (see {@link Thread#join(long)}).
     * @throws Exception if an error occurs initializing the observer
     * @since 2.1
     */
    public synchronized void stop(long stopInterval) throws Exception {
        if (running == false) {
            throw new IllegalStateException("Monitor is not running");
        }
        running = false;
        try {
            thread.join(stopInterval);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        for (FileChangeObserver observer : observers) {
            observer.destroy();
        }
    }

    /**
     * Try.
     */
    public void run() {
        while (running) {
            try {
	            for (FileChangeObserver observer : observers) {
	                observer.checkAndNotify();
	            }
            } catch (Throwable e) {
            	if(errorStop){
            		throw Exceptions.uncheck(e);	
            	}else{
            		log.error("Error invoking observer.checkAndNotify",e);
            		
                    try {
                        Thread.sleep(ERROR_SLEEP_INTERVAL);
                    } catch (final InterruptedException ignored) {
                    }            	
                 }
            } 
            if (!running) {
                break;
            }
            try {
                Thread.sleep(interval);
            } catch (final InterruptedException ignored) {
            }
        }
    }
}
