package com.example.thuoc.model;

public class UserMedicine {
    private String userId;        // 🔹 Liên kết với người dùng trong Firestore
    private String userName;
    private String phone;
    private boolean textNotify;   // Thông báo văn bản
    private boolean voiceNotify;  // Thông báo giọng nói

    public UserMedicine() {}

    public UserMedicine(String userName, String phone) {
        this.userName = userName;
        this.phone = phone;
        this.textNotify = false;
        this.voiceNotify = false;
    }

    public UserMedicine(String userName, String phone, boolean textNotify, boolean voiceNotify) {
        this.userName = userName;
        this.phone = phone;
        this.textNotify = textNotify;
        this.voiceNotify = voiceNotify;
    }

    // ✅ Getter Setter
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public boolean isTextNotify() { return textNotify; }
    public void setTextNotify(boolean textNotify) { this.textNotify = textNotify; }

    public boolean isVoiceNotify() { return voiceNotify; }
    public void setVoiceNotify(boolean voiceNotify) { this.voiceNotify = voiceNotify; }
}
