package com.example.thuoc.adapter;

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
        holder.tvDosage.setText("Liều lượng mặc định: " + med.getDosage());

        // 🔹 Hiển thị danh sách giờ uống + liều lượng
        if (med.getTimes() != null && !med.getTimes().isEmpty()) {
            StringBuilder timeDisplay = new StringBuilder();
            for (Map<String, String> entry : med.getTimes()) {
                String time = entry.get("time");
                String dose = entry.get("dosage");
                if (time != null && dose != null) {
                    timeDisplay.append(time).append(" - ").append(dose).append("\n");
                }
            }
            holder.tvTime.setText(timeDisplay.toString().trim());
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
        TextView tvName, tvDosage, tvTime;

        public MedicineEntryViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvDosage = itemView.findViewById(R.id.tvMedicineDosage);
            tvTime = itemView.findViewById(R.id.tvMedicineTime);
        }
    }
}
