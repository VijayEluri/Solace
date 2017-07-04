package solace.util;

import java.util.*;
import java.util.concurrent.*;
import solace.util.*;

/**
 * The master game clock. The clock is configured with a tick duration (via
 * configuration in milliseconds), which is used as the fundamental unit of
 * time measurement in the game. Events can be scheduled in the future in terms
 * of ticks. The clock manages the events and ensures that they fire at the
 * appropriate time.
 * @author Ryan Sandor Richards
 */
public class Clock implements Runnable {
  /**
   * An event that can be scheduled on the game clock.
   */
  public class Event {
    private String id;
    private String label;
    private long delay;
    private long initialDelay;
    private Runnable action;
    private boolean isInterval = false;

    /**
     * Creates a new game clock event with the given delay.
     * @param l Label for the event.
     * @param d Delay in game ticks.
     * @param a Action to perform.
     * @param i True if the action is a set interval, false otherwise.
     */
    public Event(String l, long d, Runnable a, boolean i) {
      id = UUID.randomUUID().toString();
      label = l;
      initialDelay = d;
      delay = d;
      action = a;
      isInterval = i;
    }

    /**
     * @return The universally unique id for this event.
     */
    public String getId() { return id; }

    /**
     * Advances the delay clock forward by one tick. If the duration has
     * elapsed, then this also executes the event action.
     * @return <code>true</code> if the event should be removed from the
     *   schedule, <code>false</code> otherwise.
     */
    protected boolean tick() {
      delay--;

      if (delay < 0) {
        return false;
      }

      if (delay == 0) {
        Log.trace(String.format("Running event %s (id: %s).", label, id));
        action.run();
        if (!isInterval) {
          return true;
        }
        delay = initialDelay;
      }

      return false;
    }

    /**
     * Cancels the game event.
     */
    public void cancel() {
      Log.debug(String.format(
        "Clock: cancelling event %s (id: %s).", label, id));
      isInterval = false;
      delay = -1;
    }
  }

  ScheduledExecutorService executor;
  ScheduledFuture tickFuture;
  List<Event> events;
  List<Event> scheduleQueue;
  final Semaphore scheduleLock = new Semaphore(1);

  /**
   * Creates a new clock.
   */
  public Clock() {
    executor = Executors.newScheduledThreadPool(1);
    events = Collections.synchronizedList(new LinkedList<Event>());
    scheduleQueue = Collections.synchronizedList(new LinkedList<Event>());
  }

  /**
   * Starts the game world clock.
   */
  public void start() {
    // Don't start the clock if it is already running
    if (tickFuture != null) {
      return;
    }

    int tickMs = Integer.parseInt(Config.get("world.clock.tick"));
    Log.info("Starting game clock, with tick interval " + tickMs + "ms");
    tickFuture = executor.scheduleAtFixedRate(
      this,
      0,
      tickMs,
      TimeUnit.MILLISECONDS
    );
  }

  /**
   * Pauses the game world clock.
   */
  public void pause() {
    if (tickFuture == null) {
      return;
    }
    tickFuture.cancel(false);
    tickFuture = null;
  }

  /**
   * Stops the game clock entirely.
   */
  public void stop() {
    pause();
    executor.shutdownNow();
  }

  /**
   * Schedules an event on the clock.
   * @param label Label for the event (for ease of human readability).
   * @param delay Delay in ticks to wait before performing the action.
   * @param action Action to perform.
   * @return Clock event that can be cancelled.
   */
  public Event schedule(String label, long delay, Runnable action) {
    Event event = new Event(label, delay, action, false);
    Log.debug(String.format(
      "schedule: Scheduling clock event %s (id: %s) with delay %d.",
      label, event.getId(), delay));
    scheduleClockEvent(event);
    return event;
  }

  /**
   * Sets an event to be repeated periodically for a set interval.
   * @param label Label for the event (for ease of human readability).
   * @param delay Length of the delay between each execution.
   * @param action Action to execute at the set interval.
   * @return Clock event that can be cancelled.
   */
  public Event interval(String label, long delay, Runnable action) {
    Event event = new Event(label, delay, action, true);
    Log.debug(String.format(
      "interval: Scheduling clock interval %s (id: %s) with delay %d.",
      label, event.getId(), delay));
    scheduleClockEvent(event);
    return event;
  }

  /**
   * Thread safe clock event scheduling. Events should _NEVER_ be added directly
   * to the `events` list without using this method as it could cause a deadlock
   * when synchronizing on the `events` list.
   * @param event Event to schedule on the clock.
   */
  private void scheduleClockEvent(Event event) {
    Log.trace("scheduleClockEvent: attempting to acquire schedule lock");
    boolean hasLock = scheduleLock.tryAcquire();
    try {
      if (hasLock) {
        // Need to acquire a lock here in the case where we are scheduling a new
        // event as a result of running an existing event (in this case the )
        Log.trace("scheduleClockEvent: schedule lock acquired");
        synchronized (events) {
          Log.trace("scheduleClockEvent: adding event to schedule");
          events.add(event);
        }
      } else {
        // No need for a lock here because we aren't running abitrary code when
        // dealing with the queue.
        Log.trace("scheduleClockEvent: could not acquire lock");
        synchronized (scheduleQueue) {
          Log.trace("scheduleClockEvent: adding event to schedule queue");
          scheduleQueue.add(event);
        }
      }
    } finally {
      if (hasLock) {
        Log.trace("scheduleClockEvent: releasing schedule lock");
        scheduleLock.release();
      }
    }
  }

  /**
   * Adds any pending events in the scheduling queue on to the clock's main
   * schedule.
   */
  private void addEventsFromQueue () {
    synchronized (scheduleQueue) {
      Log.trace(String.format(
        "addEventsFromQueue: adding %d events", scheduleQueue.size()));
      for (Event event : scheduleQueue) {
        events.add(event);
      }
      scheduleQueue.clear();
    }
  }

  /**
   * Thread safe method to process each event currently scheduled on the clock.
   */
  private void processEvents() throws InterruptedException {
    Log.trace("processEvents: Acquiring event scheduling lock");
    scheduleLock.acquire();
    try {
      Log.trace("processEvents: Processing game clock events");
      synchronized (events) {
        Iterator<Event> iter = events.iterator();
        while (iter.hasNext()) {
          Event event = iter.next();
          if (event.tick()) {
            iter.remove();
          }
        }
      }
    } finally {
      Log.trace("processEvents: Releasing event scheduling lock");
      scheduleLock.release();
    }
  }

  /**
   * Moves the game time clock forward one tick.
   */
  public void run() {
    try {
      processEvents();
      addEventsFromQueue();
    } catch (InterruptedException ie) {
      // This _shouldn't_ happen...
      Log.warn("Game clock event processing interrupted, skipping tick...");
    }
  }

  /**
   * The game wold has but one clock.
   */
  private static final Clock instance = new Clock();

  /**
   * Returns the game clock instance.
   */
  public static Clock getInstance() {
    return instance;
  }
}
