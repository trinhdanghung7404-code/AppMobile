package com.example.thuoc.model;

import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicineEntry extends Medicine {

    private String dosage;        // Liều lượng thực tế mà user dùng
    private String note;          // Ghi chú cá nhân (ví dụ: uống sau bữa ăn)
    private String expiryDate;    // Hạn sử dụng riêng cho người dùng (nếu có)
    private String medicineId;    // 🔹 Liên kết đến ID thuốc trong collection "Medicines"
    private List<Map<String, String>> times; // { "time": "10:00", "dosage": "1 viên" }

    @Exclude
    private String docId; // ID Firestore của document này (nội bộ app)

    public MedicineEntry() {
        super();
        this.times = new ArrayList<>();
    }

    // 🔹 Constructor đầy đủ
    public MedicineEntry(String id, String name, String description, int quantity, String unit,
                         String dosage, String note, String expiryDate, String medicineId,
                         List<Map<String, String>> times) {
        super(id, name, description, quantity, unit);
        this.dosage = dosage;
        this.note = note;
        this.expiryDate = expiryDate;
        this.medicineId = medicineId;
        this.times = times != null ? times : new ArrayList<>();
    }

    // 🔹 Constructor tiện lợi (chỉ có giờ và liều)
    public MedicineEntry(String name, String dosage, String time, String dose) {
        super(null, name, null, 0, null);
        this.dosage = dosage;
        this.note = "";
        this.expiryDate = null;
        this.medicineId = null;
        this.times = new ArrayList<>();

        if (time != null && dose != null) {
            Map<String, String> timeMap = new HashMap<>();
            timeMap.put("time", time);
            timeMap.put("dosage", dose);
            this.times.add(timeMap);
        }
    }

    // --- Getters & Setters ---
    public String getDosage() { return dosage; }
    public void setDosage(String dosage) { this.dosage = dosage; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getMedicineId() { return medicineId; }  // ✅ getter cho medicineId
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; } // ✅ setter

    public List<Map<String, String>> getTimes() { return times; }
    public void setTimes(List<Map<String, String>> times) { this.times = times; }

    @Exclude
    public String getDocId() { return docId; }
    @Exclude
    public void setDocId(String docId) { this.docId = docId; }
}
