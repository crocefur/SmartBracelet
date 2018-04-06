package com.softdev.smarttechx.smartbracelet.util;


public class Stopwatch {

    public int id = 0;
    public long startTime = 0;
    public long stopTime = 0;
    public boolean running = false;
    public boolean resume = false;
    public String title = null;

    public void start() {
        if (startTime == 0)
            //startTime = System.currentTimeMillis()-86340000;
            startTime = System.currentTimeMillis();
        else
            startTime = System.currentTimeMillis() - (stopTime - startTime);
        running = true;
    }

    public void reset() {
        startTime = 0;
        stopTime = 0;
        running = false;
        resume = false;
    }

    public void stop() {
        stopTime = System.currentTimeMillis();
        running = false;
    }

    public long getElapsedTime() {
        long elapsed;
        if (running) {
            elapsed = (System.currentTimeMillis() - startTime);
        } else {
            elapsed = (stopTime - startTime);
        }
        return elapsed;
    }

}