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

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/**
 * An {@link Scheduler} that can schedule tasks to run after a given
 * delay, or to execute periodically.
 */
//Some codes from spring framework in org.springframework.scheduling.TaskScheduler
@SuppressWarnings("rawtypes")
public interface Scheduler {

	/**
	 * Schedule the given {@link Runnable}, starting as soon as possible and
	 * invoking it with the given period.
	 * 
	 * <p>
	 * Execution will end once the scheduler shuts down or the returned
	 * {@link ScheduledFuture} gets cancelled.
	 * 
	 * @param task the Runnable to execute whenever the trigger fires
	 * @param period the interval between successive executions of the task (in milliseconds)
	 * 
	 * @return a {@link ScheduledFuture} representing pending completion of the task
	 * 
	 * @throws ScheduleRejectedException if the task was not accpted.
	 */
    ScheduledFuture scheduleAtFixedRate(Runnable task, long period) throws ScheduleRejectedException;
    
	/**
	 * Schedule the given {@link Runnable}, starting as soon as possible and
	 * invoking it with the given period.
	 * 
	 * <p>
	 * Execution will end once the scheduler shuts down or the returned
	 * {@link ScheduledFuture} gets cancelled.
	 * 
	 * @param task the Runnable to execute whenever the trigger fires
	 * @param period the interval between successive executions of the task
	 * @param timeUnit the unit of period.
	 * 
	 * @return a {@link ScheduledFuture} representing pending completion of the task
	 * 
	 * @throws ScheduleRejectedException if the task was not accpted.
	 */
    ScheduledFuture scheduleAtFixedRate(Runnable task, long period, TimeUnit timeUnit) throws ScheduleRejectedException;
    
}