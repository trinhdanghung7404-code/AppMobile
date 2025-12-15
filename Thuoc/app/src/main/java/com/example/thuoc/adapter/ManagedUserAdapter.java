package com.example.thuoc.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.thuoc.R;
import com.example.thuoc.model.UserMedicine;

import java.util.List;

public class ManagedUserAdapter
        extends RecyclerView.Adapter<ManagedUserAdapter.ViewHolder> {

    public interface OnItemClick {
        void onClick(String usermedId, UserMedicine user);
    }

    private final List<UserMedicine> list;
    private final OnItemClick listener;

    public ManagedUserAdapter(List<UserMedicine> list, OnItemClick listener) {
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(
            @NonNull ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_managed_user, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int pos) {
        UserMedicine user = list.get(pos);

        h.tvName.setText(
                "Lịch sử uống thuốc của: " + user.getUserName()
        );

        h.itemView.setOnClickListener(v ->
                listener.onClick(user.getUsermedId(), user)
        );
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvName;

        ViewHolder(View v) {
            super(v);
            tvName = v.findViewById(R.id.tvUserName);
        }
    }
}
