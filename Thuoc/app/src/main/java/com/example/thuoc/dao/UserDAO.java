package com.example.thuoc.dao;

import android.util.Log;

import androidx.annotation.NonNull;

import com.example.thuoc.model.User;
import com.google.firebase.firestore.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class UserDAO {
    private FirebaseFirestore db;

    public UserDAO() {
        db = FirebaseFirestore.getInstance();
    }

    public void addUser(@NonNull User user) {
        db.collection("Users")
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    int newId = querySnapshot.size() + 1;
                    user.setId(String.valueOf(newId)); // ép id = số thứ tự

                    db.collection("Users")
                            .document(String.valueOf(newId)) // documentId = "1","2","3"
                            .set(user)                       // lưu field id = "1"
                            .addOnSuccessListener(aVoid ->
                                    Log.d("UserDao", "Thêm user thành công, id = " + newId))
                            .addOnFailureListener(e ->
                                    Log.e("UserDao", "Lỗi thêm user", e));
                })
                .addOnFailureListener(e ->
                        Log.e("UserDao", "Lỗi khi lấy số lượng Users", e));
    }

}
