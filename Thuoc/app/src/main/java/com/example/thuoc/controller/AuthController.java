package com.example.thuoc.controller;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.thuoc.dao.UserDAO;
import com.example.thuoc.model.User;
import com.example.thuoc.view.ManagerDashboardActivity;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();

    // ============================================================
    // 🔐 HASH PASSWORD (SHA-256)
    // ============================================================
    private String hashPassword(String pass) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(pass.getBytes());
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }
            return hexString.toString();

        } catch (NoSuchAlgorithmException e) {
            return pass; // fallback
        }
    }

    // ============================================================
    // 🔁 TẠO INTENT VÀO DASHBOARD
    // ============================================================
    private Intent getDashboardIntent(Context context, User user) {
        Intent intent = new Intent(context, ManagerDashboardActivity.class);

        intent.putExtra("userId", user.getId());
        intent.putExtra("fullName", user.getName());
        intent.putExtra("phone", user.getPhone());
        intent.putExtra("email", user.getEmail());
        intent.putExtra("role", user.getRole());

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    public void registerManager(Context context,
                                String fullName,
                                String phone,
                                String email,
                                String pass,
                                String confirm) {

        if (fullName.isEmpty() || phone.isEmpty() || email.isEmpty()
                || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!pass.equals(confirm)) {
            Toast.makeText(context, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        // Kiểm tra phone trùng
        db.collection("Users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(q1 -> {
                    if (!q1.isEmpty()) {
                        Toast.makeText(context, "Số điện thoại đã được dùng!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Kiểm tra email trùng
                    db.collection("Users")
                            .whereEqualTo("email", email)
                            .get()
                            .addOnSuccessListener(q2 -> {
                                if (!q2.isEmpty()) {
                                    Toast.makeText(context, "Email đã được sử dụng!", Toast.LENGTH_SHORT).show();
                                    return;
                                }

                                // 🔐 Tạo Firebase Auth
                                auth.createUserWithEmailAndPassword(email, pass)
                                        .addOnSuccessListener(authResult -> {

                                            String uid = authResult.getUser().getUid(); // 🔥 ID CHUẨN
                                            String hash = hashPassword(pass);

                                            User user = new User(
                                                    uid,        // ✅ id = Firebase UID
                                                    fullName,   // ✅ KHÔNG rỗng
                                                    phone,
                                                    email,
                                                    hash,
                                                    "manager"
                                            );

                                            new UserDAO().addUser(user,
                                                    () -> Toast.makeText(context,
                                                            "Đăng ký thành công!",
                                                            Toast.LENGTH_SHORT).show(),
                                                    e -> Toast.makeText(context,
                                                            "Lỗi lưu dữ liệu",
                                                            Toast.LENGTH_SHORT).show()
                                            );
                                        })
                                        .addOnFailureListener(e ->
                                                Toast.makeText(context,
                                                        "Lỗi tạo tài khoản: " + e.getMessage(),
                                                        Toast.LENGTH_LONG).show()
                                        );
                            });
                });
    }

    // ============================================================
    // 🟡 ĐĂNG NHẬP BẰNG SỐ ĐIỆN THOẠI
    // ============================================================
    public void loginUser(Context context, String phone, String pass) {

        db.collection("Users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        Toast.makeText(context, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User user = query.getDocuments().get(0).toObject(User.class);
                    if (user == null) {
                        Toast.makeText(context, "Lỗi dữ liệu tài khoản", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    String email = user.getEmail();
                    if (email == null || email.isEmpty()) {
                        Toast.makeText(context, "Tài khoản không có email!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
                            .edit()
                            .putString("user_id", user.getId())   // UID Firestore / Firebase
                            .putString("role", user.getRole())
                            .apply();

                    // Đăng nhập Firebase Auth
                    auth.signInWithEmailAndPassword(email, pass)
                            .addOnSuccessListener(s -> {
                                Toast.makeText(context, "Đăng nhập thành công!", Toast.LENGTH_SHORT).show();
                                context.startActivity(getDashboardIntent(context, user));
                            })
                            .addOnFailureListener(e ->
                                    Toast.makeText(context, "Sai mật khẩu", Toast.LENGTH_SHORT).show()
                            );
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Lỗi kết nối: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    // ============================================================
    // 🔵 QUÊN MẬT KHẨU THEO SỐ ĐIỆN THOẠI
    // ============================================================
    public void resetPassword(Context context, String phone) {

        db.collection("Users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(query -> {

                    if (query.isEmpty()) {
                        Toast.makeText(context, "Không tìm thấy tài khoản", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    User user = query.getDocuments().get(0).toObject(User.class);
                    if (user == null || user.getEmail() == null) {
                        Toast.makeText(context, "Tài khoản không có email!", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Firebase gửi mail reset
                    auth.sendPasswordResetEmail(user.getEmail())
                            .addOnSuccessListener(a ->
                                    Toast.makeText(context,
                                            "Đã gửi email khôi phục đến " + user.getEmail(),
                                            Toast.LENGTH_LONG).show()
                            )
                            .addOnFailureListener(e ->
                                    Toast.makeText(context,
                                            "Lỗi gửi mail: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show()
                            );
                });
    }
}
