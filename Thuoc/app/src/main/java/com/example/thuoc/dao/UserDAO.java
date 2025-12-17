package com.example.thuoc.dao;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.thuoc.model.User;
import com.example.thuoc.model.UserMedicine;
import com.google.firebase.firestore.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UserDAO {
    private FirebaseFirestore db;

    public UserDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void addUser(@NonNull User user,
                        Runnable onSuccess,
                        Consumer<Exception> onError) {

        db.collection("Users")
                .document(user.getId()) // 🔥 DÙNG UID
                .set(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d("UserDAO", "✅ Thêm user thành công, id = " + user.getId());
                    if (onSuccess != null) onSuccess.run();
                })
                .addOnFailureListener(e -> {
                    Log.e("UserDAO", "❌ Lỗi thêm user", e);
                    if (onError != null) onError.accept(e);
                });
    }


    public void loadManagerInfo(String managerId,
                                Consumer<User> onSuccess,
                                Consumer<Exception> onError) {

        db.collection("Users")
                .document(managerId)
                .get()
                .addOnSuccessListener(d -> {
                    if (!d.exists()) {
                        onSuccess.accept(null);
                        return;
                    }
                    onSuccess.accept(d.toObject(User.class));
                })
                .addOnFailureListener(e -> {
                    Log.e("ManagerAccountDAO", "Load manager info failed", e);
                    if (onError != null) onError.accept(e);
                });
    }

    public void loadManagedUsers(String managerId,
                                 Consumer<List<UserMedicine>> onSuccess,
                                 Consumer<Exception> onError) {

        db.collection("UserMedicine")
                .whereEqualTo("userId", managerId)
                .get()
                .addOnSuccessListener(qs -> {
                    List<UserMedicine> result = new ArrayList<>();

                    for (DocumentSnapshot d : qs) {
                        UserMedicine um = d.toObject(UserMedicine.class);
                        if (um != null) {
                            um.setUsermedId(d.getId()); // 🔥 DAO chịu trách nhiệm mapping
                            result.add(um);
                        }
                    }
                    onSuccess.accept(result);
                })
                .addOnFailureListener(e -> {
                    Log.e("ManagerAccountDAO", "Load managed users failed", e);
                    if (onError != null) onError.accept(e);
                });
    }

}
