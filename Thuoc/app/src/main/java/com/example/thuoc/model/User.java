package com.example.thuoc.model;

public class User {
    private String id;
    private String name;
    private String phone;
    private String password;

    // Constructor rỗng cho Firebase
    public User() {}

    public User(String name, String phone, String password) {
        this.name = name;
        this.phone = phone;
        this.password = password;
    }

    // Constructor đăng ký
    public User(String id, String name, String phone, String password) {
        this.id = id;
        this.name = name;
        this.phone = phone;
        this.password = password;
    }

    // Getter & Setter
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

}
