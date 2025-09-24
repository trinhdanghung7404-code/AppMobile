package com.example.thuoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.model.UserMedicine;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<UserMedicine> members;
    private OnItemClickListener listener;

    // Interface callback
    public interface OnItemClickListener {
        void onItemClick(UserMedicine userMed, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public MemberAdapter(List<UserMedicine> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(android.R.layout.simple_list_item_2, parent, false);
        return new MemberViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        UserMedicine u = members.get(position);
        if (u == null) return;

        holder.tvLine1.setText(u.getUserName() != null ? u.getUserName() : "Chưa có tên");
        holder.tvLine2.setText(u.getPhone() != null ? u.getPhone() : "Chưa có số");

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(u, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return members != null ? members.size() : 0;
    }

    // Hàm cập nhật dữ liệu
    public void updateData(List<UserMedicine> newMembers) {
        this.members = newMembers;
        notifyDataSetChanged();
    }

    static class MemberViewHolder extends RecyclerView.ViewHolder {
        TextView tvLine1, tvLine2;

        MemberViewHolder(View itemView) {
            super(itemView);
            tvLine1 = itemView.findViewById(android.R.id.text1);
            tvLine2 = itemView.findViewById(android.R.id.text2);
        }
    }
}
