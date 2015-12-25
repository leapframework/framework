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
package leap.core.scheduling;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicInteger;

import leap.core.junit.AppTestBase;
import leap.core.schedule.Scheduler;
import leap.core.schedule.FixedThreadPoolScheduler;
import leap.lang.logging.Log;
import leap.lang.logging.LogFactory;

import org.junit.Test;

public class ThreadPoolSchedulerTest extends AppTestBase {
	
	private static final Log log = LogFactory.get(ThreadPoolSchedulerTest.class);
	
	@Test
	public void testScheduleAtFixedRate() throws Exception{
		Scheduler scheduler = new FixedThreadPoolScheduler();
		
		final AtomicInteger counter = new AtomicInteger();
		
		final ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(new Runnable() {
			@Override
			public void run() {
				try {
	                Thread.sleep(10);
                } catch (InterruptedException e) {
	                e.printStackTrace();
                }
				counter.incrementAndGet();
				log.info("Increase counter to " + counter.get());
			}
		}, 1);
		
		for(;;){
			Thread.sleep(10);
			if(counter.get() > 1){
				future.cancel(false);
				break;
			}
		}
	}
}
