/*
 * Copyright 2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package leap.core.schedule;

import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import leap.lang.Args;
import leap.lang.Disposable;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

/**
 * Implementation of {@link Scheduler}, wrapping jdk's {@link ScheduledThreadPoolExecutor}.
 */
@SuppressWarnings("rawtypes")
//Some codes from spring framework in org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler
public class FixedThreadPoolScheduler implements Scheduler, Disposable {
	private static final Log log = LogFactory.get(FixedThreadPoolScheduler.class);
	
	public static final String DEFAULT_NAME = "scheduler";
	
	private String					 name = DEFAULT_NAME;
	private int 					 corePoolSize = 1;
	private boolean					 daemon = true;
	private ThreadFactory			 threadFactory;
	private ScheduledExecutorService scheduledExecutor;
	private RejectedExecutionHandler rejectedHandler;
	
	public FixedThreadPoolScheduler() {
	    super();
    }

	public FixedThreadPoolScheduler(String name) {
		this.name = name;
	}
	
	public FixedThreadPoolScheduler(String name,int poolSize) {
		this.name = name;
		this.corePoolSize = poolSize;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		Args.notEmpty(name,"name");
		this.name = name;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public void setCorePoolSize(int poolSize) {
		Args.assertTrue(poolSize > 1,"poolSize must be 1 or higer");
		this.corePoolSize = poolSize;
	}
	
    public boolean isDaemon() {
		return daemon;
	}

	public void setDaemon(boolean daemon) {
		this.daemon = daemon;
	}

	public void setThreadFactory(ThreadFactory threadFactory) {
		this.threadFactory = threadFactory;
	}

	public ScheduledExecutorService getScheduledExecutor() {
		if(null == scheduledExecutor){
			initScheduledExecutor();
		}
		return scheduledExecutor;
	}

	@Override
	public ScheduledFuture scheduleAtFixedRate(Runnable task, long period) {
		return getScheduledExecutor().scheduleAtFixedRate(task, 0, period, TimeUnit.MILLISECONDS);
	}
	
	@Override
    public ScheduledFuture scheduleAtFixedRate(Runnable task, long period, TimeUnit timeUnit) throws ScheduleRejectedException {
	    return getScheduledExecutor().scheduleAtFixedRate(task, 0, period, timeUnit);
    }

	protected void initScheduledExecutor(){
		if(null == threadFactory){
			threadFactory = createDefaultThreadFactory();
		}
		if(null == rejectedHandler){
			rejectedHandler = createDefaultRejectedHandler();
		}
		this.scheduledExecutor = createDefaultExecutor(threadFactory,rejectedHandler);
	}
	
	protected ThreadFactory createDefaultThreadFactory(){
		return new ThreadFactory() {
			private AtomicLong threadCount = new AtomicLong();
			
			@Override
			public Thread newThread(Runnable r) {
				String threadName = name + "-" + threadCount.incrementAndGet();
				Thread thread = new Thread(r, threadName);
				thread.setDaemon(daemon);
				return thread;
			}
		};
	}
	
	protected RejectedExecutionHandler createDefaultRejectedHandler(){
		return new RejectedExecutionHandler() {
			@Override
			public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
				throw new ScheduleRejectedException("The task '" + r + "' was rejected by executor");
			}
		};
	}
	
	protected ScheduledThreadPoolExecutor createDefaultExecutor(ThreadFactory threadFactory,RejectedExecutionHandler rejectedHandler){
		ScheduledThreadPoolExecutor executor =
		        new ScheduledThreadPoolExecutor(corePoolSize,threadFactory,rejectedHandler);
		
		return executor;
	}

	@Override
    public void dispose() throws Throwable {
		if(null != scheduledExecutor) {
			log.debug("Shutdown scheduler '{}'", name);
		    scheduledExecutor.shutdownNow();
		}
    }
}