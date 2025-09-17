package com.example.thuoc.model;

public class Schedule {
    private String id;          // id lịch
    private String userPhone;   // user nào tạo lịch này
    private String medicineId;  // thuốc nào
    private String time;        // giờ uống (ví dụ "08:00")
    private String date;        // ngày uống (ví dụ "2025-09-15")
    private int dose;           // liều lượng (số viên)

    public Schedule() {} // Firebase cần constructor rỗng

    public Schedule(String id, String userPhone, String medicineId, String time, String date, int dose) {
        this.id = id;
        this.userPhone = userPhone;
        this.medicineId = medicineId;
        this.time = time;
        this.date = date;
        this.dose = dose;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public String getTime() { return time; }
    public void setTime(String time) { this.time = time; }

    public String getDate() { return date; }
    public void setDate(String date) { this.date = date; }

    public int getDose() { return dose; }
    public void setDose(int dose) { this.dose = dose; }
}
