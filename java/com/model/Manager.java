package com.model;

import java.sql.Date;

public class Manager {
    private int managerID;
    private String userManager;
    private String userPasswordManager;
    private String imageManager;
    private String managerName;
    private int managerAge;
    private String managerPhone;
    private Date managerDate;

    public Manager(int managerID, String userManager, String userPasswordManager,
                   String imageManager, String managerName, int managerAge,
                   String managerPhone, Date managerDate) {
        this.managerID = managerID;
        this.userManager = userManager;
        this.userPasswordManager = userPasswordManager;
        this.imageManager = imageManager;
        this.managerName = managerName;
        this.managerAge = managerAge;
        this.managerPhone = managerPhone;
        this.managerDate = managerDate;
    }

    public int getManagerID() { return managerID; }
    public String getUserManager() { return userManager; }
    public String getImageManager() { return imageManager; }
    public String getManagerName() { return managerName; }
    public int getManagerAge() { return managerAge; }
    public String getManagerPhone() { return managerPhone; }
    public Date getManagerDate() { return managerDate; }

    public void setManagerID(int managerID) { this.managerID = managerID; }
    public void setUserManager(String userManager) { this.userManager = userManager; }
    public void setUserPasswordManager(String userPasswordManager) { this.userPasswordManager = userPasswordManager; }
    public void setImageManager(String imageManager) { this.imageManager = imageManager; }
    public void setManagerName(String managerName) { this.managerName = managerName; }
    public void setManagerAge(int managerAge) { this.managerAge = managerAge; }
    public void setManagerPhone(String managerPhone) { this.managerPhone = managerPhone; }
    public void setManagerDate(Date managerDate) { this.managerDate = managerDate; }


    @Override
    public String toString() {
        return "Manager [managerID=" + managerID + ", userManager=" + userManager + ", managerName=" + managerName + "]";
    }
}