package com.example.stores.model;

// import java.time.LocalDate; // Không cần nữa

public class WorkShiftSchedule {
    private int scheduleID;
    private int employeeID;
    private String shiftID;
    private String dayOfWeek;
    private String notes;
    private String employeeFullName; // Getter: getEmployeeFullName()
    private String shiftDetails;   // Getter: getShiftDetails()

    public WorkShiftSchedule() {}

    public WorkShiftSchedule(int employeeID, String shiftID, String dayOfWeek, String notes) {
        this.employeeID = employeeID;
        this.shiftID = shiftID;
        this.dayOfWeek = dayOfWeek;
        this.notes = notes;
    }

    // Getters and Setters
    public int getScheduleID() { return scheduleID; }
    public void setScheduleID(int scheduleID) { this.scheduleID = scheduleID; }
    public int getEmployeeID() { return employeeID; }
    public void setEmployeeID(int employeeID) { this.employeeID = employeeID; }
    public String getShiftID() { return shiftID; }
    public void setShiftID(String shiftID) { this.shiftID = shiftID; }
    public String getDayOfWeek() { return dayOfWeek; } // Getter mới
    public void setDayOfWeek(String dayOfWeek) { this.dayOfWeek = dayOfWeek; } // Setter mới
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public String getEmployeeFullName() { return employeeFullName; }
    public void setEmployeeFullName(String employeeFullName) { this.employeeFullName = employeeFullName; }
    public String getShiftDetails() { return shiftDetails; } // Đổi tên từ shiftNameDetails
    public void setShiftDetails(String shiftDetails) { this.shiftDetails = shiftDetails; } // Đổi tên

    @Override
    public String toString() {
        return "WorkShiftSchedule{" +
                "scheduleID=" + scheduleID +
                ", employeeID=" + employeeID +
                ", shiftID='" + shiftID + '\'' +
                ", dayOfWeek='" + dayOfWeek + '\'' +
                '}';
    }
}