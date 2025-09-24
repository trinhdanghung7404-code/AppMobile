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

public class MedicineEntryAdapter extends RecyclerView.Adapter<MedicineEntryAdapter.MedicineEntryViewHolder> {

    private List<MedicineEntry> medicineList;
    private OnItemClickListener listener;

    // Interface cho click item
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
        holder.tvDosage.setText("Liều lượng: " + med.getDosage());

        // Nếu bạn có list times → ghép chuỗi lại
        if (med.getTimes() != null && !med.getTimes().isEmpty()) {
            holder.tvTime.setText("Thời gian: " + String.join(", ", med.getTimes()));
        } else {
            holder.tvTime.setText("Thời gian: chưa có");
        }

        // xử lý click
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
