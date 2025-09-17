package com.example.thuoc.dao;

import com.example.thuoc.model.User;
import com.google.firebase.firestore.FirebaseFirestore;

public class UserDao {
    private final FirebaseFirestore db;

    public UserDao() {
        db = FirebaseFirestore.getInstance();
    }

    public void addUser(User user) {
        String id = db.collection("Users").document().getId();
        user.setId(id);
        db.collection("Users").document(id).set(user);
    }
}
