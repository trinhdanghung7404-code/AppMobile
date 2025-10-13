package com.example.thuoc.dao;

import com.example.thuoc.model.Medicine;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class MedicineDAO {
    private FirebaseFirestore db;
    public MedicineDAO() {
        db = FirebaseFirestore.getInstance();
    }
    // Lắng nghe thay đổi danh sách thuốc
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

    // Thêm thuốc mới
    public void addMedicine(Medicine med,
                            Runnable onSuccess,
                            Consumer<Exception> onError) {
        String id = db.collection("Medicine").document().getId();
        med.setId(id);

        db.collection("Medicine")
                .document(id)
                .set(med)
                .addOnSuccessListener(aVoid -> {
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }
}
