package com.model;

import java.util.Objects;

public class Customer {
	private int CustomerID;
	private String userName;
	private String userPassword;
	private int phone;
	private String fullName;
	private Address userAddress;
	private String userSex;
	private Date userDate;
	public Customer(int customerID, String userName, String userPassword, int phone, String fullName,
			Address userAddress, String userSex, Date userDate) {
		super();
		CustomerID = customerID;
		this.userName = userName;
		this.userPassword = userPassword;
		this.phone = phone;
		this.fullName = fullName;
		this.userAddress = userAddress;
		this.userSex = userSex;
		this.userDate = userDate;
	}
	public int getCustomerID() {
		return CustomerID;
	}
	public void setCustomerID(int customerID) {
		CustomerID = customerID;
	}
	public String getUserName() {
		return userName;
	}
	public void setUserName(String userName) {
		this.userName = userName;
	}
	public String getUserPassword() {
		return userPassword;
	}
	public void setUserPassword(String userPassword) {
		this.userPassword = userPassword;
	}
	public int getPhone() {
		return phone;
	}
	public void setPhone(int phone) {
		this.phone = phone;
	}
	public String getFullName() {
		return fullName;
	}
	public void setFullName(String fullName) {
		this.fullName = fullName;
	}
	public Address getUserAddress() {
		return userAddress;
	}
	public void setUserAddress(Address userAddress) {
		this.userAddress = userAddress;
	}
	public String getUserSex() {
		return userSex;
	}
	public void setUserSex(String userSex) {
		this.userSex = userSex;
	}
	public Date getUserDate() {
		return userDate;
	}
	public void setUserDate(Date userDate) {
		this.userDate = userDate;
	}
	@Override
	public String toString() {
		return "Customer [CustomerID=" + CustomerID + ", userName=" + userName + ", userPassword=" + userPassword
				+ ", phone=" + phone + ", fullName=" + fullName + ", userAddress=" + userAddress + ", userSex="
				+ userSex + ", userDate=" + userDate + "]";
	}
	@Override
	public int hashCode() {
		return Objects.hash(CustomerID, fullName, phone, userAddress, userDate, userName, userPassword, userSex);
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Customer other = (Customer) obj;
		return CustomerID == other.CustomerID && Objects.equals(fullName, other.fullName) && phone == other.phone
				&& Objects.equals(userAddress, other.userAddress) && Objects.equals(userDate, other.userDate)
				&& Objects.equals(userName, other.userName) && Objects.equals(userPassword, other.userPassword)
				&& Objects.equals(userSex, other.userSex);
	}
	
	
	
}
