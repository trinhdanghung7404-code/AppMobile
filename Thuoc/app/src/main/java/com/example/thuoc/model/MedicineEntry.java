package com.example.thuoc.model;

import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicineEntry {
    private String name;
    private String dosage;
    private String note;
    private int quantity;
    private String expiryDate;

    // times: Danh sách các map chứa { "time": "10:00", "dosage": "1 viên" }
    private List<Map<String, String>> times;

    @Exclude
    private String docId;

    public MedicineEntry() {
        this.times = new ArrayList<>();
    }

    // 🔹 Full constructor
    public MedicineEntry(String name, String dosage, String note,
                         int quantity, String expiryDate,
                         List<Map<String, String>> times) {
        this.name = name;
        this.dosage = dosage;
        this.note = note;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.times = times != null ? times : new ArrayList<>();
    }

    // 🔹 Convenience constructor (giờ + liều lượng)
    public MedicineEntry(String name, String dosage, String time, String dose) {
        this.name = name;
        this.dosage = dosage;
        this.note = "";
        this.quantity = 0;
        this.expiryDate = null;
        this.times = new ArrayList<>();

        if (time != null && dose != null) {
            Map<String, String> timeMap = new HashMap<>();
            timeMap.put("time", time);
            timeMap.put("dosage", dose);
            this.times.add(timeMap);
        }
    }

    // --- Getters & Setters ---
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) { this.quantity = quantity; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public List<Map<String, String>> getTimes() { return times; }
    public void setTimes(List<Map<String, String>> times) { this.times = times; }

    @Exclude
    public String getDocId() { return docId; }
    @Exclude
    public void setDocId(String docId) { this.docId = docId; }
}
