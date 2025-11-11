package com.example.thuoc.dao;

import android.util.Log;

import com.example.thuoc.model.MedicineEntry;
import com.example.thuoc.model.UserMedicine;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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

    public void addUserMedicine(String userId, UserMedicine usermedicine, AddUserCallback callback) {
        if (userId == null || userId.isEmpty()) {
            if (callback != null) callback.onFailure(new Exception("UserId bị trống"));
            return;
        }

        usermedicine.setUserId(userId);

        // Nếu chưa có avatarType thì mặc định là "boy"
        if (usermedicine.getAvatarType() == null || usermedicine.getAvatarType().isEmpty()) {
            usermedicine.setAvatarType("boy");
        }

        // Kiểm tra số điện thoại đã tồn tại chưa
        db.collection("UserMedicine")
                .whereEqualTo("userId", userId)
                .whereEqualTo("phone", usermedicine.getPhone())
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        if (callback != null)
                            callback.onFailure(new Exception("Số điện thoại đã tồn tại"));
                    } else {
                        // Lấy tất cả document của userId để tính stt mới
                        db.collection("UserMedicine")
                                .whereEqualTo("userId", userId)
                                .get()
                                .addOnSuccessListener(allDocs -> {
                                    int maxId = 0;

                                    for (var doc : allDocs) {
                                        try {
                                            int currentId = Integer.parseInt(doc.getId());
                                            if (currentId > maxId) maxId = currentId;
                                        } catch (NumberFormatException ignored) {
                                            // bỏ qua document có id không phải số
                                        }
                                    }

                                    int nextId = maxId + 1;
                                    String newDocId = String.valueOf(nextId);

                                    db.collection("UserMedicine")
                                            .document(newDocId)
                                            .set(usermedicine)
                                            .addOnSuccessListener(aVoid -> {
                                                if (callback != null) callback.onSuccess();
                                            })
                                            .addOnFailureListener(e -> {
                                                if (callback != null) callback.onFailure(e);
                                            });
                                })
                                .addOnFailureListener(e -> {
                                    if (callback != null) callback.onFailure(e);
                                });
                    }
                })
                .addOnFailureListener(e -> {
                    if (callback != null) callback.onFailure(e);
                });
    }
    // 🔹 Lấy thông tin UserMedicine theo userId
    public void getUserInfo(String userId, Consumer<UserMedicine> onSuccess, Consumer<Exception> onError) {
        if (userId == null || userId.isEmpty()) {
            if (onError != null) onError.accept(new Exception("UserId trống"));
            return;
        }

        db.collection("UserMedicine").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        UserMedicine user = doc.toObject(UserMedicine.class);
                        if (onSuccess != null) onSuccess.accept(user);
                    } else {
                        if (onError != null) onError.accept(new Exception("Không tìm thấy người dùng"));
                    }
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    // 🔹 Cập nhật thông tin người dùng
    public void updateUserInfo(String userId, String name, String phone,
                               boolean textNotify, boolean voiceNotify,
                               Runnable onSuccess, Consumer<Exception> onError) {
        if (userId == null || userId.isEmpty()) {
            if (onError != null) onError.accept(new Exception("UserId trống"));
            return;
        }

        db.collection("UserMedicine")
                .document(userId)
                .update(
                        "name", name,
                        "phone", phone,
                        "textNotify", textNotify,
                        "voiceNotify", voiceNotify
                )
                .addOnSuccessListener(aVoid -> {
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    // 🔹 Xóa toàn bộ thông tin UserMedicine của userId
    public void deleteUser(String userId, Runnable onSuccess, Consumer<Exception> onError) {
        if (userId == null || userId.isEmpty()) {
            if (onError != null) onError.accept(new Exception("UserId trống"));
            return;
        }

        db.collection("UserMedicine").document(userId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    db.collection("UserMedicine").document(userId)
                            .collection("Medicines")
                            .get()
                            .addOnSuccessListener(snapshot -> {
                                WriteBatch batch = db.batch();
                                for (DocumentSnapshot doc : snapshot) {
                                    batch.delete(doc.getReference());
                                }
                                batch.commit()
                                        .addOnSuccessListener(b -> {
                                            if (onSuccess != null) onSuccess.run();
                                        })
                                        .addOnFailureListener(e -> {
                                            if (onError != null) onError.accept(e);
                                        });
                            })
                            .addOnFailureListener(e -> {
                                if (onError != null) onError.accept(e);
                            });
                })
                .addOnFailureListener(e -> {
                    if (onError != null) onError.accept(e);
                });
    }

    public void deleteMedicine(String userId, String medicineId, Runnable onSuccess, OnFailureListener onFailure) {
        db.collection("UserMedicine")
                .document(userId)
                .collection("Medicines")
                .document(medicineId)
                .delete()
                .addOnSuccessListener(aVoid -> {
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    if (onFailure != null) onFailure.onFailure(e);
                });
    }
    public void getMedicinesByUserId(String userId, Consumer<List<MedicineEntry>> onSuccess, Consumer<Exception> onFailure) {
        db.collection("UserMedicine")
                .document(userId)
                .collection("Medicines")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<MedicineEntry> medicines = new ArrayList<>();
                    for (QueryDocumentSnapshot doc : querySnapshot) {
                        MedicineEntry med = doc.toObject(MedicineEntry.class);
                        if (med != null) {
                            med.setDocId(doc.getId());
                            medicines.add(med);
                        }
                    }
                    onSuccess.accept(medicines);
                })
                .addOnFailureListener(onFailure::accept);
    }
    public void getNotificationSettings(String userId, BiConsumer<Boolean, Boolean> onSuccess, Consumer<Exception> onError) {
        db.collection("UserMedicine").document(userId).get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        boolean textNotify = getBooleanSafe(doc, "textNotify");
                        boolean voiceNotify = getBooleanSafe(doc, "voiceNotify");
                        onSuccess.accept(textNotify, voiceNotify);
                    } else {
                        Log.w("UserDAO", "⚠️ Không tìm thấy document cho userId: " + userId);
                        onSuccess.accept(false, false);
                    }
                })
                .addOnFailureListener(onError::accept);
    }

    private boolean getBooleanSafe(DocumentSnapshot doc, String field) {
        Boolean val = doc.getBoolean(field);
        return val != null && val;
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
