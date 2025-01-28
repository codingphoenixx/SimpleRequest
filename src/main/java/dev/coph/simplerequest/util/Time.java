package dev.coph.simplerequest.util;

import java.util.concurrent.TimeUnit;

public class Time {
    private long milliseconds;


    public Time(int duration, TimeUnit unit) {
        milliseconds = unit.toMillis(duration);
    }

    public Time(long duration, TimeUnit unit) {
        milliseconds = unit.toMillis(duration);
    }

    public Time add(int duration, TimeUnit unit) {
        milliseconds += unit.toMillis(duration);
        return this;
    }

    public Time remove(int duration, TimeUnit unit) {
        milliseconds -= unit.toMillis(duration);
        return this;
    }

    public long toMilliseconds() {
        return milliseconds;
    }

    public int toSeconds() {
        return Math.toIntExact(milliseconds / 1000);
    }

    public int toTicks() {
        return Math.toIntExact((milliseconds / 1000) * 20);
    }

    public int toMinutes() {
        return Math.toIntExact(milliseconds / 1000 / 60);
    }

    public int toHours() {
        return Math.toIntExact(milliseconds / 1000 / 60 / 60);
    }

    public int toDays() {
        return Math.toIntExact(milliseconds / 1000 / 60 / 60 / 24);
    }
}
