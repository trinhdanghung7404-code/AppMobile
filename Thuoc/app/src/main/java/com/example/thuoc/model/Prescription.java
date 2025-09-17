package com.example.thuoc.model;

import java.util.List;

public class Prescription {
    private String id;
    private String userPhone; // để biết đơn này của user nào
    private List<Medicine> medicines;
    private String note;

    public Prescription() {}

    public Prescription(String id, String userPhone, List<Medicine> medicines, String note) {
        this.id = id;
        this.userPhone = userPhone;
        this.medicines = medicines;
        this.note = note;
    }

    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getUserPhone() { return userPhone; }
    public void setUserPhone(String userPhone) { this.userPhone = userPhone; }

    public List<Medicine> getMedicines() { return medicines; }
    public void setMedicines(List<Medicine> medicines) { this.medicines = medicines; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }
}
