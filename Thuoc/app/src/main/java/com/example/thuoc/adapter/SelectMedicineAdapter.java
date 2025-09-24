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
                .inflate(R.layout.item_medicine, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Medicine med = medicineList.get(position);
        holder.tvName.setText(med.getName());   // hiển thị tên thuốc

        // Nếu muốn hiển thị thêm mô tả thì mở comment
        // holder.tvDescription.setText(med.getDescription());

        holder.itemView.setOnClickListener(v -> listener.onMedicineClick(med));
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
        TextView tvName; // tvDescription;

        ViewHolder(View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            // tvDescription = itemView.findViewById(R.id.tvMedicineDescription);
        }
    }
}
