package com.example.thuoc.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.model.Medicine;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

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
    private boolean isExpired(String expiryDate) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            sdf.setLenient(false);

            Date expiry = sdf.parse(expiryDate);
            Date today = new Date();

            return expiry != null && expiry.before(today);
        } catch (Exception e) {
            return false;
        }
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_medicine_select, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        Medicine med = medicineList.get(pos);

        h.tvName.setText(med.getName());
        h.tvExpiry.setText("HSD: " + med.getExpiryDate());
        h.tvQuantity.setText("Số lượng: " + med.getQuantity() + med.getUnit());

        boolean expired = isExpired(med.getExpiryDate());

        if (expired) {
            h.itemView.setAlpha(0.4f);
            h.itemView.setBackgroundColor(Color.parseColor("#55FF0000")); // đỏ mờ

            h.itemView.setOnClickListener(v ->
                    Toast.makeText(
                            v.getContext(),
                            "Thuốc đã hết hạn",
                            Toast.LENGTH_SHORT
                    ).show()
            );
        } else {
            h.itemView.setAlpha(1f);
            h.itemView.setBackgroundColor(Color.TRANSPARENT);

            h.itemView.setOnClickListener(v -> listener.onMedicineClick(med));
        }
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
