package com.example.thuoc.dao;

import androidx.annotation.NonNull;

import com.example.thuoc.model.Medicine;
import com.example.thuoc.model.MedicineEntry;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MedicineEntryDAO {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 🔹 Lấy tất cả thuốc của user
    public void getMedicinesByUserId(String userId, Consumer<List<MedicineEntry>> onSuccess, Consumer<Exception> onError) {
        db.collection("UserMedicine").document(userId).collection("Medicines")
                .get()
                .addOnSuccessListener(qs -> {
                    List<MedicineEntry> list = new ArrayList<>();
                    for (DocumentSnapshot doc : qs.getDocuments()) {
                        MedicineEntry e = doc.toObject(MedicineEntry.class);
                        if (e != null) {
                            e.setDocId(doc.getId());
                            list.add(e);
                        }
                    }
                    onSuccess.accept(list);
                })
                .addOnFailureListener(onError::accept);
    }

    // 🔹 Thêm thuốc mới
    public void addMedicine(String userId, Medicine medicine, Runnable onSuccess, Consumer<Exception> onError) {
        db.collection("UserMedicine").document(userId).collection("Medicines")
                .whereEqualTo("name", medicine.getName())
                .get()
                .addOnSuccessListener(qs -> {
                    if (!qs.isEmpty()) {
                        onError.accept(new Exception("Thuốc đã tồn tại"));
                    } else {
                        MedicineEntry entry = new MedicineEntry();
                        entry.setName(medicine.getName());
                        entry.setQuantity(medicine.getQuantity());
                        entry.setExpiryDate(medicine.getDescription());
                        entry.setTimes(new ArrayList<>()); // bắt đầu với danh sách trống

                        db.collection("UserMedicine").document(userId)
                                .collection("Medicines")
                                .add(entry)
                                .addOnSuccessListener(r -> onSuccess.run())
                                .addOnFailureListener(onError::accept);
                    }
                })
                .addOnFailureListener(onError::accept);
    }


    public void addTime(String userId, String entryDocId, String timeStr, String dosageStr, Runnable onSuccess, @NonNull Consumer<Exception> onError) {
        DocumentReference docRef = db.collection("UserMedicine").document(userId)
                .collection("Medicines").document(entryDocId);

        db.runTransaction(transaction -> {
                    DocumentSnapshot snapshot = transaction.get(docRef);
                    if (!snapshot.exists()) {
                        throw new FirebaseFirestoreException("Medicine does not exist", FirebaseFirestoreException.Code.NOT_FOUND);
                    }

                    MedicineEntry med = snapshot.toObject(MedicineEntry.class);
                    if (med.getTimes() == null) med.setTimes(new ArrayList<>());

                    // 🔹 Tạo map mới để lưu giờ + liều lượng
                    boolean exists = false;
                    for (java.util.Map<String, String> t : med.getTimes()) {
                        if (t.get("time").equals(timeStr)) {
                            exists = true;
                            break;
                        }
                    }

                    if (!exists) {
                        java.util.Map<String, String> newTime = new java.util.HashMap<>();
                        newTime.put("time", timeStr);
                        newTime.put("dosage", dosageStr);
                        med.getTimes().add(newTime);
                    }

                    transaction.set(docRef, med, SetOptions.merge());  // merge giữ các field khác
                    return null;
                }).addOnSuccessListener(aVoid -> onSuccess.run())
                .addOnFailureListener(onError::accept);
    }
}
