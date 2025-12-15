package com.example.thuoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class MedicationLogAdapter
        extends RecyclerView.Adapter<MedicationLogAdapter.VH> {

    private final List<Map<String, Object>> list;
    private final SimpleDateFormat dfDate =
            new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
    private final SimpleDateFormat dfTime =
            new SimpleDateFormat("HH:mm", Locale.getDefault());

    public MedicationLogAdapter(List<Map<String, Object>> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup p, int v) {
        return new VH(LayoutInflater.from(p.getContext())
                .inflate(R.layout.item_medication_log, p, false));
    }

    @Override
    public void onBindViewHolder(@NonNull VH h, int i) {
        Map<String, Object> m = list.get(i);

        Timestamp ts = (Timestamp) m.get("createdAt");

        h.tvDate.setText("Ngày: " + dfDate.format(ts.toDate()));
        h.tvTime.setText("Giờ: " + dfTime.format(ts.toDate()));
        h.tvInfo.setText(
                "Uống " + m.get("medicineName") +
                        " (" + m.get("dosage") + ") : " +
                        ("TAKEN".equals(m.get("status")) ? "Đã uống" : "Quên uống")
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class VH extends RecyclerView.ViewHolder {
        TextView tvDate, tvTime, tvInfo;
        VH(View v) {
            super(v);
            tvDate = v.findViewById(R.id.tvDate);
            tvTime = v.findViewById(R.id.tvTime);
            tvInfo = v.findViewById(R.id.tvInfo);
        }
    }
}
