package com.example.thuoc.dao;

import android.util.Log;

import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.Timestamp;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MedicationLogDAO {

    private static final String TAG = "MedicationLogDAO";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();

    public interface HistoryCallback {
        void onSuccess(List<Map<String, Object>> logs);
        void onError(Exception e);
    }

    public void logEvent(
            String usermedId,
            String managerId,
            String medicineName,
            String dosage,
            String status,
            String method
    ) {

        DocumentReference ref =
                db.collection("medication_logs").document(usermedId);

        Map<String, Object> newLog = new HashMap<>();
        newLog.put("status", status);
        newLog.put("method", method);
        newLog.put("dosage", dosage);
        newLog.put("medicineName", medicineName);
        newLog.put("createdAt", Timestamp.now());

        db.runTransaction(transaction -> {
            DocumentSnapshot snap = transaction.get(ref);

            List<Map<String, Object>> logs;

            if (snap.exists() && snap.contains("logs")) {
                logs = (List<Map<String, Object>>) snap.get("logs");
            } else {
                logs = new ArrayList<>();
            }

            logs.add(0, newLog); // thêm log mới lên đầu

            // 👉 Giữ tối đa 10 log
            if (logs.size() > 10) {
                logs = logs.subList(0, 10);
            }

            Map<String, Object> data = new HashMap<>();
            data.put("usermedId", usermedId);
            data.put("managerId", managerId);
            data.put("logs", logs);
            data.put("updatedAt", FieldValue.serverTimestamp());

            transaction.set(ref, data, SetOptions.merge());
            return null;
        }).addOnSuccessListener(v ->
                Log.d(TAG, "✅ Log grouped by usermedId OK")
        ).addOnFailureListener(e ->
                Log.e(TAG, "❌ Log transaction failed", e)
        );
    }

    public void getHistoryByUserMedId(String usermedId, HistoryCallback callback) {
        db.collection("medication_logs")
                .whereEqualTo("usermedId", usermedId)
                .get()
                .addOnSuccessListener(qs -> {
                    List<Map<String, Object>> result = new ArrayList<>();

                    for (DocumentSnapshot d : qs) {
                        List<Map<String, Object>> arr =
                                (List<Map<String, Object>>) d.get("logs");

                        if (arr != null) {
                            result.addAll(arr);
                        }
                    }

                    callback.onSuccess(result);
                })
                .addOnFailureListener(callback::onError);
    }
}
