package com.example.thuoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.model.Medicine;

import java.util.List;

public class SelectMedicineAdapter extends RecyclerView.Adapter<SelectMedicineAdapter.ViewHolder> {

    public interface OnMedicineClickListener {
        void onMedicineClick(Medicine medicine);
    }

    private List<Medicine> medicineList;
    private OnMedicineClickListener listener;

    public SelectMedicineAdapter(List<Medicine> medicineList, OnMedicineClickListener listener) {
        this.medicineList = medicineList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_select, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine m = medicineList.get(position);
        holder.tvName.setText(m.getName());
        holder.tvQuantity.setText("Số lượng: " + m.getQuantity() + " viên");
        holder.tvExpiry.setText("HSD: " + m.getDescription());

        // Bắt sự kiện click để chọn thuốc
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onMedicineClick(m);
            }
        });
    }

    @Override
    public int getItemCount() {
        return medicineList != null ? medicineList.size() : 0;
    }

    public void updateData(List<Medicine> newList) {
        this.medicineList = newList;
        notifyDataSetChanged();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvExpiry;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvQuantity = itemView.findViewById(R.id.tvMedicineQuantity);
            tvExpiry = itemView.findViewById(R.id.tvMedicineExpiry);
        }
    }
}
