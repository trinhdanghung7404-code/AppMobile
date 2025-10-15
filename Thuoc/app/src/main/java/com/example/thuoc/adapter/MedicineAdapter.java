package com.example.thuoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.model.Medicine;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class MedicineAdapter extends RecyclerView.Adapter<MedicineAdapter.MedicineViewHolder> {

    private List<Medicine> medicineList;
    private Set<String> selectedIds = new HashSet<>();

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

        holder.cbSelect.setOnCheckedChangeListener(null);
        holder.cbSelect.setChecked(selectedIds.contains(med.getId()));

        holder.cbSelect.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                selectedIds.add(med.getId());
            } else {
                selectedIds.remove(med.getId());
            }
        });

        // ✅ Click toàn bộ item cũng toggle checkbox
        holder.itemView.setOnClickListener(v -> {
            boolean checked = !holder.cbSelect.isChecked();
            holder.cbSelect.setChecked(checked);
        });
    }

    @Override
    public int getItemCount() {
        return medicineList.size();
    }

    static class MedicineViewHolder extends RecyclerView.ViewHolder {
        TextView tvName, tvQuantity, tvExpiry;
        CheckBox cbSelect;

        public MedicineViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName = itemView.findViewById(R.id.tvMedicineName);
            tvQuantity = itemView.findViewById(R.id.tvMedicineQuantity);
            tvExpiry = itemView.findViewById(R.id.tvMedicineExpiry);
            cbSelect = itemView.findViewById(R.id.cbSelectMedicine);
        }
    }

    public void updateData(List<Medicine> newList) {
        this.medicineList = newList;
        notifyDataSetChanged();
    }

    public Set<String> getSelectedIds() {
        return selectedIds;
    }
}
