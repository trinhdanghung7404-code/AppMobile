package com.example.thuoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.model.Medicine;
import com.example.thuoc.model.UserMedicine;

import java.util.List;

public class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {

    private List<UserMedicine> members;
    private OnItemClickListener listener;
    private OnEditClickListener editListener;

    // ===== Interface callback =====
    public interface OnItemClickListener {
        void onItemClick(UserMedicine userMed, int position);
    }

    public interface OnEditClickListener {
        void onEditClick(UserMedicine userMed, int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnEditClickListener(OnEditClickListener listener) {
        this.editListener = listener;
    }

    public MemberAdapter(List<UserMedicine> members) {
        this.members = members;
    }

    @NonNull
    @Override
    public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_member, parent, false);
        return new MemberViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
        UserMedicine u = members.get(position);
        if (u == null) return;

        // Hiển thị tên + số điện thoại
        holder.tvName.setText(u.getUserName() != null ? u.getUserName() : "Chưa có tên");
        holder.tvPhone.setText(u.getPhone() != null ? u.getPhone() : "Chưa có số");

        // Gán avatar theo loại
        int avatarRes = getAvatarRes(u.getAvatarType());
        holder.imgAvatar.setImageResource(avatarRes);

        // Sự kiện click vào item
        holder.itemView.setOnClickListener(v -> {
            if (listener != null) listener.onItemClick(u, position);
        });

    }

    @Override
    public int getItemCount() {
        return members != null ? members.size() : 0;
    }

    // Hàm cập nhật danh sách
    public void updateData(List<UserMedicine> newMembers) {
        this.members = newMembers;
        notifyDataSetChanged();
    }
    // ===== ViewHolder =====
    static class MemberViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAvatar;
        TextView tvName, tvPhone;

        MemberViewHolder(View itemView) {
            super(itemView);
            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            tvName = itemView.findViewById(R.id.tvName);
            tvPhone = itemView.findViewById(R.id.tvPhone);
        }
    }

    // ===== Lấy ảnh avatar theo loại =====
    private int getAvatarRes(String avatarType) {
        if (avatarType == null) return R.drawable.ic_boy;

        switch (avatarType.toLowerCase()) {
            case "boy":
                return R.drawable.ic_boy;
            case "girl":
                return R.drawable.ic_girl;
            case "men":
                return R.drawable.ic_man;
            case "women":
                return R.drawable.ic_woman;
            case "grandpa":
                return R.drawable.ic_grandpa;
            case "grandma":
                return R.drawable.ic_grandma;
            default:
                return R.drawable.ic_boy; // mặc định
        }
    }
}
