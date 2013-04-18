package com.example.helpers;

public class Timer {
	public long startTime = 0;
    public long endTime = 0;

    public long getDuration() {
        return endTime - startTime;
    }
    public void start() {
        startTime = System.currentTimeMillis();
    }
    public void stop() {
         endTime = System.currentTimeMillis();
     }
}
