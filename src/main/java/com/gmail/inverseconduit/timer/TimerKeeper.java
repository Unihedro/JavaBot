package com.gmail.inverseconduit.timer;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * This class is designed to be used as a single application wide instance. The
 * numbering of the Timers is guaranteed to be subsequent and threadsafe.
 * Creating and cancelling timers is also.
 * 
 * @author Clemens
 *
 */
public final class TimerKeeper {

	private final AtomicInteger counter = new AtomicInteger(0);
	private final ScheduledExecutorService executor = Executors
			.newSingleThreadScheduledExecutor();

	private final Map<Integer, ScheduledFuture<?>> waitingTimers = new ConcurrentHashMap<>();

	/**
	 * Schedules a given Runnable for execution after the given number of
	 * minutes and returns a uniquely identifying integer as cancellation token
	 * 
	 * @param sendingInstruction
	 *            The runnable to execute
	 * @param delayInMinutes
	 *            The delay after the sendingInstruction is to be executed.
	 *            Negative (or 0) delays are a request for immediate action
	 * @return an integer that can be used as cancellation token for the
	 *         {@link #cancelTimer(int) cancelTimer} method
	 */
	public int addTimer(final Runnable sendingInstruction,
			final int delayInMinutes) {
		ScheduledFuture<?> task = executor.schedule(sendingInstruction,
				delayInMinutes, TimeUnit.MINUTES);
		int timerNumber = counter.incrementAndGet();
		waitingTimers.put(timerNumber, task);
		return timerNumber;
	}

	/**
	 * Tries to cancel a Timer, which is given by a cancellation Token as
	 * obtained from {@link #addTimer(Runnable, int) addTimer}. The result is a
	 * result in form of a String.
	 * 
	 * @param timerNumber
	 *            the cancellation token of the timer
	 * @return A String indicating non-existance of the task, failure or success
	 *         of cancellation
	 */
	public String cancelTimer(final int timerNumber) {
		ScheduledFuture<?> task = waitingTimers.get(timerNumber);

		if (null == task) {
			return "Task does not exist";
		}
		if (!task.cancel(false)) {
			return "Task could not be cancelled. Most probably it has already been completed";
		}
		return "Task was successfully cancelled";
	}
}
