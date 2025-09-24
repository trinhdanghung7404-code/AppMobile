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

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private List<Medicine> medicineList;

    public MedicineAdapter(List<Medicine> medicineList) {
        this.medicineList = medicineList;
    }

    @NonNull
    @Override
    public MedicineViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine, parent, false);
        return new MedicineViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MedicineViewHolder holder, int position) {
        Medicine med = medicineList.get(position);
        holder.tvName.setText(med.getName());
        holder.tvQuantity.setText("Số lượng: " + med.getQuantity() + " " + med.getUnit());
        holder.tvExpiry.setText("HSD: " + med.getDescription());
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvExpiry;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvQuantity = itemView.findViewById(R.id.tvMedicineQuantity);
            tvExpiry = itemView.findViewById(R.id.tvMedicineExpiry);
        }
    }

    public void updateData(List<Medicine> newList) {
        this.medicineList = newList;
        notifyDataSetChanged();
    }
}
