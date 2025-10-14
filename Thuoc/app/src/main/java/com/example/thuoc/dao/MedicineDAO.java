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
    private final FirebaseFirestore db;

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
                    // Đếm số lượng document hiện tại => gán ID mới
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
}
