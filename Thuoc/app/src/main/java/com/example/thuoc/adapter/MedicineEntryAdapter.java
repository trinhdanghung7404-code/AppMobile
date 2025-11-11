package com.example.thuoc.adapter;

import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.model.MedicineEntry;

import java.util.List;
import java.util.Map;

public class MedicineEntryAdapter extends RecyclerView.Adapter<MedicineEntryAdapter.MedicineEntryViewHolder> {

    private List<MedicineEntry> medicineList;
    private OnItemClickListener listener;

    // 🔹 Interface cho sự kiện click item
    public interface OnItemClickListener {
        void onItemClick(MedicineEntry entry, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MedicineEntryAdapter(List<MedicineEntry> medicineList) {
        this.medicineList = medicineList;
    }

    public void updateData(List<MedicineEntry> newList) {
        this.medicineList = newList;
        notifyDataSetChanged();
    }

    public List<MedicineEntry> getCurrentList() {
        return medicineList;
    }

    @NonNull
    @Override
    public MedicineEntryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_user_medicine, parent, false);
        return new MedicineEntryViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineEntryViewHolder holder, int position) {
        MedicineEntry med = medicineList.get(position);
        if (med == null) return;

        holder.tvName.setText(med.getName());

        if (med.getTimes() != null && !med.getTimes().isEmpty()) {
            SpannableStringBuilder timeDisplay = new SpannableStringBuilder();

            for (Map<String, String> entry : med.getTimes()) {
                String time = entry.get("time");
                String dose = entry.get("dosage");

                if (time != null && dose != null) {
                    // "Giờ: "
                    timeDisplay.append("Giờ: ");

                    // 👉 Làm nổi phần thời gian
                    int startTime = timeDisplay.length();
                    timeDisplay.append(time);
                    int endTime = timeDisplay.length();
                    timeDisplay.setSpan(
                            new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#1976D2")), // xanh lam
                            startTime, endTime,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    timeDisplay.setSpan(
                            new android.text.style.RelativeSizeSpan(1.5f), // to hơn 15%
                            startTime, endTime,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    // " - Uống với liều lượng: "
                    timeDisplay.append(" - Uống với liều lượng: ");

                    // 👉 Làm nổi phần liều lượng
                    int startDose = timeDisplay.length();
                    timeDisplay.append(dose);
                    int endDose = timeDisplay.length();
                    timeDisplay.setSpan(
                            new android.text.style.ForegroundColorSpan(android.graphics.Color.parseColor("#D32F2F")), // đỏ sậm
                            startDose, endDose,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );
                    timeDisplay.setSpan(
                            new android.text.style.RelativeSizeSpan(1.15f), // to hơn 15%
                            startDose, endDose,
                            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    );

                    timeDisplay.append("\n");
                }
            }

            holder.tvTime.setText(timeDisplay);
        } else {
            holder.tvTime.setText("Chưa có giờ uống");
        }

        // 🔹 Xử lý click item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(med, position);
        });
    }

    @Override
    public int getItemCount() {
        return medicineList != null ? medicineList.size() : 0;
    }

    static class MedicineEntryViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvTime;
        public MedicineEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvTime = itemView.findViewById(R.id.tvMedicineTime);
        }
    }
}
