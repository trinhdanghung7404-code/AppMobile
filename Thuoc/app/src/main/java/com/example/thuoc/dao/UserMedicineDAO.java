package com.example.thuoc.dao;

import android.util.Log;

import com.example.thuoc.model.UserMedicine;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;

public class UserMedicineDAO {
    private FirebaseFirestore db;

    public UserMedicineDAO() {
        db = FirebaseFirestore.getInstance();
    }

    // 🔹 Lắng nghe danh sách UserMedicine theo userId
    public ListenerRegistration listenAll(String userId, UserMedicineListener listener) {
        if (userId == null || userId.isEmpty()) {
            listener.onError(new Exception("UserId trống — chưa truyền từ Intent sang"));
            return null;
        }

        Log.d("UserMedicineDAO", "👂 ListenAll userId = " + userId);

        return db.collection("UserMedicine")
                .whereEqualTo("userId", userId)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        listener.onError(e);
                        return;
                    }

                    List<UserMedicine> list = new ArrayList<>();
                    List<String> ids = new ArrayList<>();

                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots.getDocuments()) {
                            UserMedicine u = doc.toObject(UserMedicine.class);
                            if (u != null) {
                                list.add(u);
                                ids.add(doc.getId());
                            }
                        }
                    }

                    Log.d("UserMedicineDAO", "✅ ListenAll found " + list.size() + " items");
                    listener.onDataChange(list, ids);
                });
    }

    // 🔹 Thêm mới UserMedicine cho userId cụ thể
    public void addUserMedicine(String userId, UserMedicine medicine, AddUserCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onFailure(new Exception("UserId bị trống"));
            return;
        }

        medicine.setUserId(userId);

        String docId = String.valueOf(System.currentTimeMillis());
        db.collection("UserMedicine").document(docId)
                .set(medicine)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserMedicineDAO", "✅ Added user medicine for userId = " + userId);
                    if (callback != null) callback.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserMedicineDAO", "❌ Add failed: " + e.getMessage());
                    if (callback != null) callback.onFailure(e);
                });
    }

    // Callback interface
    public interface UserMedicineListener {
        void onDataChange(List<UserMedicine> users, List<String> ids);
        void onError(Exception e);
    }

    public interface AddUserCallback {
        void onSuccess();
        void onFailure(Exception e);
    }
}
