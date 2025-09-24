package com.example.thuoc.model;

import java.util.ArrayList;
import java.util.List;
import com.google.firebase.firestore.Exclude;

public class MedicineEntry {
    private String name;
    private String dosage;
    private String note;        // ghi chú (không bắt buộc)
    private int quantity;       // số lượng
    private String expiryDate;  // ngày hết hạn (vd "2025-09-15")
    private List<String> times; // danh sách giờ uống, vd ["08:00", "20:00"]

    // Không lưu vào Firestore (tạm giữ documentId khi cần) - @Exclude để Firestore không map
    @Exclude
    private String docId;

    public MedicineEntry() {}

    // Full constructor
    public MedicineEntry(String name, String dosage, String note,
                         int quantity, String expiryDate, List<String> times) {
        this.name = name;
        this.dosage = dosage;
        this.note = note;
        this.quantity = quantity;
        this.expiryDate = expiryDate;
        this.times = times;
    }

    // Convenience constructor (3 args) — tương thích với chỗ bạn đang gọi
    // timeStr sẽ được thêm vào list times (nếu không rỗng)
    public MedicineEntry(String name, String dosage, String timeStr) {
        this.name = name;
        this.dosage = dosage;
        this.note = "";
        this.quantity = 0;
        this.expiryDate = null;
        this.times = new ArrayList<>();
        if (timeStr != null && !timeStr.isEmpty()) {
            this.times.add(timeStr);
        }
    }

    // getters / setters
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

    public List<String> getTimes() { return times; }
    public void setTimes(List<String> times) { this.times = times; }

    // docId (không lưu lên Firestore)
    @Exclude
    public String getDocId() { return docId; }
    @Exclude
    public void setDocId(String docId) { this.docId = docId; }
}
