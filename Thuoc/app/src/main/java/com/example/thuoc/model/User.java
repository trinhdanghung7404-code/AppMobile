package com.example.thuoc.model;

public class User {
    private String id;
    private String name;
    private String phone;
    private String password;
    private String role;       // "manager" hoặc "member"
    private String managerId;  // nếu là member thì lưu managerId

    // 🔹 Bắt buộc: constructor rỗng cho Firebase
    public User() {}

    // 🔹 Constructor 3 tham số: dùng cho đăng ký (fullName, phone, password)
    public User(String name, String phone, String password) {
        this.name = name;
        this.phone = phone;
        this.password = password;
        this.role = "member"; // mặc định member, hoặc bạn set lại trong code
    }

    // 🔹 Constructor 4 tham số: nếu muốn set role/managerId ngay
    public User(String name, String phone, String role, String managerId) {
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.managerId = managerId;
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

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public String getManagerId() { return managerId; }
    public void setManagerId(String managerId) { this.managerId = managerId; }
}
