package com.example.thuoc.dao;

import android.util.Log;

import com.example.thuoc.model.Medicine;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class MedicineDAO {
    private static FirebaseFirestore db = null;

    public MedicineDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // Lắng nghe thay đổi danh sách thuốc (giữ nguyên)
    public ListenerRegistration getAllMedicines(Consumer<List<Medicine>> onSuccess,
                                                Consumer<Exception> onError) {
        return db.collection("Medicine")
                .addSnapshotListener((value, error) -> {
                    if (error != null) {
                        if (onError != null) onError.accept(error);
                        return;
                    }

                    if (value != null) {
                        List<Medicine> list = new ArrayList<>();
                        for (QueryDocumentSnapshot doc : value) {
                            Medicine med = doc.toObject(Medicine.class);
                            med.setId(doc.getId()); // nếu model có setId
                            list.add(med);
                        }
                        if (onSuccess != null) onSuccess.accept(list);
                    }
                });
    }

    // 🔹 Thêm thuốc mới với ID tự tăng (1, 2, 3, ...)
    public void addMedicine(Medicine med,
                            Runnable onSuccess,
                            Consumer<Exception> onError) {

        db.collection("Medicine")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int newId = querySnapshot.size() + 1;
                    String id = String.valueOf(newId);
                    med.setId(id);

                    db.collection("Medicine")
                            .document(id) // documentId = "1", "2", "3", ...
                            .set(med)
                            .addOnSuccessListener(aVoid -> {
                                Log.d("MedicineDAO", "Thêm thuốc thành công, id = " + id);
                                if (onSuccess != null) onSuccess.run();
                            })
                            .addOnFailureListener(e -> {
                                Log.e("MedicineDAO", "Lỗi khi thêm thuốc", e);
                                if (onError != null) onError.accept(e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e("MedicineDAO", "Không thể lấy danh sách thuốc", e);
                    if (onError != null) onError.accept(e);
                });
    }

    public void getMedicines(Consumer<List<Medicine>> onSuccess, Consumer<Exception> onFailure) {
        db.collection("Medicine")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Medicine> medicineList = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Medicine m = doc.toObject(Medicine.class);
                        if (m != null) {
                            m.setId(doc.getId());
                            medicineList.add(m);
                        }
                    }
                    Log.d("MedicineDAO", "✅ Loaded " + medicineList.size() + " medicines");
                    if (onSuccess != null) onSuccess.accept(medicineList);
                })
                .addOnFailureListener(e -> {
                    Log.e("MedicineDAO", "❌ Load failed: " + e.getMessage());
                    if (onFailure != null) onFailure.accept(e);
                });
    }
    public static void subtractMedicineFromUser(String userMedDocId, String medId, String dosage) {
        if (medId == null || medId.isEmpty()) {
            Log.e("MedicineDAO", "❌ medId null/rỗng — không thể trừ thuốc");
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("Medicine").document(medId); // ✅ đúng vị trí Firestore hiện tại

        docRef.get().addOnSuccessListener(doc -> {
            if (doc.exists()) {
                Double qtyDouble = doc.getDouble("quantity");
                double currentQty = (qtyDouble != null) ? qtyDouble : 0.0;

                double dosageValue = extractDosageValue(dosage); // Ví dụ: "1 viên" -> 1.0
                double newQuantity = Math.max(0, currentQty - dosageValue);

                docRef.update("quantity", newQuantity)
                        .addOnSuccessListener(aVoid -> Log.d("MedicineDAO",
                                String.format("✅ Đã trừ %.2f thuốc (ID: %s). Còn lại: %.2f",
                                        dosageValue, medId, newQuantity)))
                        .addOnFailureListener(e ->
                                Log.e("MedicineDAO", "❌ Lỗi khi cập nhật số lượng: " + e.getMessage()));
            } else {
                Log.w("MedicineDAO", "⚠️ Không tìm thấy thuốc: " + medId + " trong collection Medicine");
            }
        }).addOnFailureListener(e ->
                Log.e("MedicineDAO", "🔥 Lỗi Firestore khi đọc thuốc: " + e.getMessage()));
    }
    private static int extractDosageValue(String dosage) {
        try {
            return Integer.parseInt(dosage.replaceAll("[^0-9]", "").trim());
        } catch (Exception e) {
            Log.w("MedicineDAO", "⚠️ Không đọc được số từ dosage: " + dosage + ", mặc định trừ 1");
            return 1;
        }
    }
}
