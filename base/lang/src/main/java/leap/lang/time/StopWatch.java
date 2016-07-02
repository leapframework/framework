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

package leap.lang.time;

/**
 * <p>
 * <code>StopWatch</code> provides a convenient API for timings.
 * </p>
 * 
 * <p>
 * To start the watch, call {@link #start()}. At this point you can:
 * </p>
 * <ul>
 * <li>{@link #suspend()} the watch to pause it. {@link #resume()} allows the watch to continue. Any time between the
 * suspend and resume will not be counted in the total. At this point, these three options are available again.</li>
 * <li>{@link #stop()} the watch to complete the timing session.</li>
 * </ul>
 * 
 * <p>
 * It is intended that the output methods {@link #toString()} and {@link #getElapsedMilliseconds()} should only be called after stop,
 * split or suspend, however a suitable result will be returned at other points.
 * </p>
 * 
 * <p>
 * 1. split(), suspend(), or stop() cannot be invoked twice<br />
 * 2. resume() may only be called if the watch has been suspend()<br />
 * 3. start() cannot be called twice without calling reset()
 * </p>
 * 
 * <p>
 * This class is not thread-safe
 * </p>
 */
public class StopWatch {

	private static final long NANO_2_MILLIS	= 1000000L;

	// running states
	private static final int	STATE_UNSTARTED	= 0;

	private static final int	STATE_RUNNING	= 1;

	private static final int	STATE_STOPPED	= 2;

	private static final int	STATE_SUSPENDED	= 3;

	/**
	 * The current running state of the StopWatch.
	 */
	private int state = STATE_UNSTARTED;

	/**
	 * The start time.
	 */
	private long startTime;

	/**
	 * The start time in Millis - nanoTime is only for elapsed time so we need to also store the currentTimeMillis to
	 * maintain the old getStartTime API.
	 */
	private long startTimeMillis;

	/**
	 * The stop time.
	 */
	private long stopTime;
	
	/**
	 * create {@link StopWatch} instance and call {@link #start()}
	 * 
	 * @return the {@link StopWatch} instance aleady started.
	 */
	public static StopWatch startNew(){
		StopWatch sw = new StopWatch();
		sw.start();
		return sw;
	}

	public StopWatch() {

	}

	/**
	 * <p>
	 * Start the stopwatch.
	 * </p>
	 * 
	 * <p>
	 * This method starts a new timing session, clearing any previous values.
	 * </p>
	 * 
	 * @throws IllegalStateException if the StopWatch is already running.
	 */
	public StopWatch start() {
		if (this.state == STATE_STOPPED) {
			throw new IllegalStateException("Stopwatch must be reset before being restarted. ");
		}
		
		if (this.state != STATE_UNSTARTED) {
			throw new IllegalStateException("Stopwatch already started. ");
		}
		
		this.startTime = System.nanoTime();
		this.startTimeMillis = System.currentTimeMillis();
		this.state = STATE_RUNNING;
		return this;
	}

	/**
	 * <p>
	 * Stop the stopwatch.
	 * </p>
	 * 
	 * <p>
	 * This method ends a new timing session, allowing the time to be retrieved.
	 * </p>
	 * 
	 * @throws IllegalStateException if the StopWatch is not running.
	 */
	public StopWatch stop() {
		if (this.state != STATE_RUNNING && this.state != STATE_SUSPENDED) {
			throw new IllegalStateException("Stopwatch is not running. ");
		}
		
		if (this.state == STATE_RUNNING) {
			this.stopTime = System.nanoTime();
		}
		
		this.state = STATE_STOPPED;
		return this;
	}

	/**
	 * <p>
	 * Resets the stopwatch. Stops it if need be.
	 * </p>
	 * 
	 * <p>
	 * This method clears the internal values to allow the object to be reused.
	 * </p>
	 */
	public StopWatch reset() {
		this.state = STATE_UNSTARTED;
		return this;
	}
	
	/**
	 * Stops time interval measurement, resets the elapsed time to zero, and starts measuring elapsed time
	 */
	public StopWatch restart(){
		reset();
		start();
		return this;
	}

	/**
	 * <p>
	 * Suspend the stopwatch for later resumption.
	 * </p>
	 * 
	 * <p>
	 * This method suspends the watch until it is resumed. The watch will not include time between the suspend and
	 * resume calls in the total time.
	 * </p>
	 * 
	 * @throws IllegalStateException if the StopWatch is not currently running.
	 */
	public StopWatch suspend() {
		if (this.state != STATE_RUNNING) {
			throw new IllegalStateException("Stopwatch must be running to suspend. ");
		}
		this.stopTime = System.nanoTime();
		this.state = STATE_SUSPENDED;
		return this;
	}

	/**
	 * <p>
	 * Resume the stopwatch after a suspend.
	 * </p>
	 * 
	 * <p>
	 * This method resumes the watch after it was suspended. The watch will not include time between the suspend and
	 * resume calls in the total time.
	 * </p>
	 * 
	 * @throws IllegalStateException if the StopWatch has not been suspended.
	 */
	public StopWatch resume() {
		if (this.state != STATE_SUSPENDED) {
			throw new IllegalStateException("Stopwatch must be suspended to resume. ");
		}
		this.startTime += System.nanoTime() - this.stopTime;
		this.state = STATE_RUNNING;
		return this;
	}

	/**
	 * <p>
	 * Get the milliseconds time on the stopwatch.
	 * </p>
	 * 
	 * <p>
	 * This is either the time between the start and the moment this method is called, or the amount of time between
	 * start and stop.
	 * </p>
	 * 
	 * @return the time in milliseconds
	 */
	public long getElapsedMilliseconds() {
		return getElapsedNanoseconds() / NANO_2_MILLIS;
	}

	/**
	 * <p>
	 * Get the time on the stopwatch in nanoseconds.
	 * </p>
	 * 
	 * <p>
	 * This is either the time between the start and the moment this method is called, or the amount of time between
	 * start and stop.
	 * </p>
	 * 
	 * @return the time in nanoseconds
	 */
	public long getElapsedNanoseconds() {
		if (this.state == STATE_STOPPED || this.state == STATE_SUSPENDED) {
			return this.stopTime - this.startTime;
		} else if (this.state == STATE_UNSTARTED) {
			return 0;
		} else if (this.state == STATE_RUNNING) {
			return System.nanoTime() - this.startTime;
		}
		
		throw new IllegalStateException("Illegal running state has occured. ");
	}

	/**
	 * Returns the time this stopwatch was started.
	 * 
	 * @return the time this stopwatch was started
	 * 
	 * @throws IllegalStateException if this StopWatch has not been started
	 */
	public long getStartTime() {
		if (this.state == STATE_UNSTARTED) {
			throw new IllegalStateException("Stopwatch has not been started");
		}
		// System.nanoTime is for elapsed time
		return this.startTimeMillis;
	}

	/**
	 * <p>
	 * Gets a summary of the time that the stopwatch recorded as a string.
	 * </p>
	 * 
	 * @return the time as a String
	 */
	@Override
	public String toString() {
		return getElapsedMilliseconds() + "ms";
	}
}
