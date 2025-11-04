package com.example.thuoc.controller;

import android.content.Context;
import android.content.Intent;
import android.widget.Toast;

import com.example.thuoc.dao.UserDAO;
import com.example.thuoc.model.User;
import com.example.thuoc.view.ManagerDashboardActivity;
import com.google.firebase.firestore.FirebaseFirestore;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class AuthController {

    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    // 🔹 Hash mật khẩu SHA-256
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

    // 🔹 Hàm tạo Intent theo role
    private Intent getDashboardIntent(Context context, User user) {
        Intent intent;

        intent = new Intent(context, ManagerDashboardActivity.class);

        intent.putExtra("userId", user.getId());
        intent.putExtra("fullName", user.getName());
        intent.putExtra("phone", user.getPhone());

        // Xóa back stack
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    // 🔹 Đăng ký
    // Đăng ký
    public void registerUser(Context context, String fullName, String phone, String pass, String confirm) {
        if (fullName.isEmpty() || phone.isEmpty() || pass.isEmpty() || confirm.isEmpty()) {
            Toast.makeText(context, "Vui lòng nhập đủ thông tin", Toast.LENGTH_SHORT).show();
            return;
        }
        if (!pass.equals(confirm)) {
            Toast.makeText(context, "Mật khẩu không khớp", Toast.LENGTH_SHORT).show();
            return;
        }

        db.collection("Users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    if (!querySnapshot.isEmpty()) {
                        Toast.makeText(context, "Số điện thoại đã được đăng ký", Toast.LENGTH_SHORT).show();
                    } else {
                        String hashedPass = hashPassword(pass);

                        // 👉 Tạo user mới, chưa có id
                        User user = new User(fullName, phone, hashedPass);

                        // 👉 Dùng UserDao để thêm user (id = số thứ tự)
                        new UserDAO().addUser(user);

                        Toast.makeText(context, "Đăng ký thành công", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }

    // 🔹 Đăng nhập
    public void loginUser(Context context, String phone, String pass) {
        String hashedPass = hashPassword(pass);

        db.collection("Users")
                .whereEqualTo("phone", phone)
                .get()
                .addOnSuccessListener(snapshots -> {
                    if (!snapshots.isEmpty()) {
                        User user = snapshots.getDocuments().get(0).toObject(User.class);

                        if (user != null && user.getPassword() != null && user.getPassword().equals(hashedPass)) {
                            context.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
                                    .edit()
                                    .putString("user_id", user.getId())
                                    .apply();

                            Toast.makeText(context, "Đăng nhập thành công", Toast.LENGTH_SHORT).show();

                            context.startActivity(getDashboardIntent(context, user));
                        } else {
                            Toast.makeText(context, "Sai mật khẩu", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(context, "Tài khoản không tồn tại", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e ->
                        Toast.makeText(context, "Lỗi: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
    }
}
