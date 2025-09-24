package com.example.thuoc.model;

public class UserMedicine {
    private String userName;
    private String phone;

    public UserMedicine() {}

    public UserMedicine(String userName, String phone) {
        this.userName = userName;
        this.phone = phone;
    }

    // Getter Setter
    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
}
