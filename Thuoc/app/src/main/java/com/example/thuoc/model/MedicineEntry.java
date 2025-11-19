package com.example.thuoc.model;

import com.google.firebase.firestore.Exclude;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicineEntry extends Medicine {
    private String expiryDate;    // Hạn sử dụng riêng cho người dùng (nếu có)
    private String medicineId;    // 🔹 Liên kết đến ID thuốc trong collection "Medicines"
    private List<Map<String, String>> times; // { "time": "10:00", "dosage": "1 viên" }

    @Exclude
    private String docId; // ID Firestore của document này (nội bộ app)

    public MedicineEntry() {
        super();
        this.times = new ArrayList<>();
    }

    public String getExpiryDate() { return expiryDate; }
    public void setExpiryDate(String expiryDate) { this.expiryDate = expiryDate; }

    public String getMedicineId() { return medicineId; }
    public void setMedicineId(String medicineId) { this.medicineId = medicineId; }

    public List<Map<String, String>> getTimes() { return times; }
    public void setTimes(List<Map<String, String>> times) { this.times = times; }

    @Exclude
    public String getDocId() { return docId; }
    @Exclude
    public void setDocId(String docId) { this.docId = docId; }
}
