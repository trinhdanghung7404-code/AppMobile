package com.example.thuoc.view;

import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.adapter.MedicationLogAdapter;
import com.example.thuoc.dao.MedicationLogDAO;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ManagerMedicationHistoryActivity extends AppCompatActivity {

    private TextView tvGreeting;
    private RecyclerView rvHistory;

    private final List<Map<String, Object>> logs = new ArrayList<>();
    private MedicationLogAdapter adapter;
    private MedicationLogDAO logDao;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_medication_history);

        tvGreeting = findViewById(R.id.tvGreeting);
        rvHistory = findViewById(R.id.rvHistory);

        rvHistory.setLayoutManager(new LinearLayoutManager(this));
        adapter = new MedicationLogAdapter(logs);
        rvHistory.setAdapter(adapter);

        logDao = new MedicationLogDAO();

        String usermedId = getIntent().getStringExtra("usermedId");
        String userName = getIntent().getStringExtra("userName");

        tvGreeting.setText("Xin chào: " + userName);

        loadHistory(usermedId);
    }

    private void loadHistory(String usermedId) {
        logDao.getHistoryByUserMedId(usermedId, new MedicationLogDAO.HistoryCallback() {
            @Override
            public void onSuccess(List<Map<String, Object>> data) {
                logs.clear();
                logs.addAll(data);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onError(Exception e) {
                // TODO: show error UI / toast / log
                e.printStackTrace();
            }
        });
    }
}
