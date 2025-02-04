package dev.coph.simplerequest.util;

import java.util.concurrent.TimeUnit;

/**
 * A utility class for handling and converting time representations.
 * The class internally stores time as milliseconds and provides methods
 * for constructing, modifying, and converting time values across various units.
 */
public class Time {

    /**
     * Represents the internal time value in milliseconds.
     * This field is used to store the time internally in the smallest unit of milliseconds,
     * enabling precise time calculations and conversions to various time units.
     */
    private long milliseconds;

    /**
     * Constructs a Time object with the specified duration and unit of time.
     *
     * @param duration the amount of time to be represented
     * @param unit the unit of time for the duration (e.g., seconds, minutes, hours)
     */
    public Time(int duration, TimeUnit unit) {
        milliseconds = unit.toMillis(duration);
    }

    /**
     * Constructs a Time object with the specified duration and unit of time.
     *
     * @param duration the amount of time to be represented
     * @param unit the unit of time for the duration (e.g., seconds, minutes, hours)
     */
    public Time(long duration, TimeUnit unit) {
        milliseconds = unit.toMillis(duration);
    }

    /**
     * Adds the specified duration to the current time value.
     *
     * @param duration the amount of time to add
     * @param unit the unit of time for the duration (e.g., seconds, minutes, hours)
     * @return the updated Time object after adding the specified duration
     */
    public Time add(int duration, TimeUnit unit) {
        milliseconds += unit.toMillis(duration);
        return this;
    }

    /**
     * Decreases the stored time value by the specified duration in the given time unit.
     *
     * @param duration the amount of time to subtract
     * @param unit the unit of time for the duration (e.g., seconds, minutes, hours)
     * @return the updated Time object after subtracting the specified duration
     */
    public Time remove(int duration, TimeUnit unit) {
        milliseconds -= unit.toMillis(duration);
        return this;
    }

    /**
     * Retrieves the stored time value in milliseconds.
     * This method provides direct access to the internal millisecond representation
     * of the time stored in the object.
     *
     * @return the time value in milliseconds as a long
     */
    public long toMilliseconds() {
        return milliseconds;
    }

    /**
     * Converts the stored time value from milliseconds to an equivalent number of ticks.
     * A tick is defined as 1/20th of a second.
     *
     * @return the time value in ticks as a long
     */
    public long toTicks() {
        return Math.toIntExact((milliseconds / 1000) * 20);
    }

    /**
     * Converts the stored time value from milliseconds to an equivalent number of seconds.
     *
     * @return the time value in seconds as an integer
     */
    public int toSeconds() {
        return Math.toIntExact(milliseconds / 1000);
    }

    /**
     * Converts the stored time value from milliseconds to an equivalent number of minutes.
     *
     * @return the time value in minutes as an integer
     */
    public int toMinutes() {
        return Math.toIntExact(milliseconds / 1000 / 60);
    }


    /**
     * Converts the stored time value from milliseconds to an equivalent number of hours.
     *
     * @return the time value in hours as an integer
     */
    public int toHours() {
        return Math.toIntExact(milliseconds / 1000 / 60 / 60);
    }

    /**
     * Converts the stored time value from milliseconds to an equivalent number of days.
     *
     * @return the time value in days as an integer
     */
    public int toDays() {
        return Math.toIntExact(milliseconds / 1000 / 60 / 60 / 24);
    }
}
