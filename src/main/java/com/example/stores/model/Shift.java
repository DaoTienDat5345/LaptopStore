package com.example.stores.model;

import java.time.LocalTime;

public class Shift {
    private String shiftID;
    private String shiftName;
    private LocalTime startTime;
    private LocalTime endTime;

    public Shift() {}

    public Shift(String shiftID, String shiftName, LocalTime startTime, LocalTime endTime) {
        this.shiftID = shiftID;
        this.shiftName = shiftName;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    // Getters and Setters
    public String getShiftID() { return shiftID; }
    public void setShiftID(String shiftID) { this.shiftID = shiftID; }
    public String getShiftName() { return shiftName; }
    public void setShiftName(String shiftName) { this.shiftName = shiftName; }
    public LocalTime getStartTime() { return startTime; }
    public void setStartTime(LocalTime startTime) { this.startTime = startTime; }
    public LocalTime getEndTime() { return endTime; }
    public void setEndTime(LocalTime endTime) { this.endTime = endTime; }

    @Override
    public String toString() {
        return shiftName != null ? shiftName : shiftID;
    }
}