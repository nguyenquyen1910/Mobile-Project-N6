package com.Team6Nhom10.roommanagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.Team6Nhom10.roommanagement.R;
import com.Team6Nhom10.roommanagement.model.TenantEntity;

import java.util.ArrayList;
import java.util.List;

public class TenantAdapter extends RecyclerView.Adapter<TenantAdapter.TenantViewHolder> {

    public interface OnTenantClickListener {
        void onTenantClick(TenantEntity tenant);
    }

    private List<TenantEntity> tenants = new ArrayList<>();
    private List<Integer> rentingIds = new ArrayList<>(); // IDs đang thuê phòng
    private final Context context;
    private OnTenantClickListener listener;

    public TenantAdapter(Context context) {
        this.context = context;
    }

    public void setOnTenantClickListener(OnTenantClickListener listener) {
        this.listener = listener;
    }

    public void setData(List<TenantEntity> tenants, List<Integer> rentingIds) {
        this.tenants = tenants != null ? tenants : new ArrayList<>();
        this.rentingIds = rentingIds != null ? rentingIds : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TenantViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_tenant, parent, false);
        return new TenantViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TenantViewHolder holder, int position) {
        TenantEntity t = tenants.get(position);
        holder.bind(t, rentingIds.contains(t.id));
    }

    @Override
    public int getItemCount() {
        return tenants.size();
    }

    class TenantViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvAvatar, tvName, tvPhone, tvIdCard, tvRentingBadge;

        TenantViewHolder(@NonNull View itemView) {
            super(itemView);
            tvAvatar = itemView.findViewById(R.id.tvAvatar);
            tvName = itemView.findViewById(R.id.tvTenantItemName);
            tvPhone = itemView.findViewById(R.id.tvTenantItemPhone);
            tvIdCard = itemView.findViewById(R.id.tvTenantItemIdCard);
            tvRentingBadge = itemView.findViewById(R.id.tvRentingBadge);

            itemView.setOnClickListener(v -> {
                if (listener != null)
                    listener.onTenantClick(tenants.get(getAdapterPosition()));
            });
        }

        void bind(TenantEntity t, boolean isRenting) {
            tvName.setText(t.name);
            tvPhone.setText(t.phone);
            // Avatar: lấy chữ đầu tiên của tên
            tvAvatar.setText(t.name != null && !t.name.isEmpty()
                    ? String.valueOf(t.name.charAt(0)).toUpperCase()
                    : "?");

            if (t.idCard != null && !t.idCard.isEmpty()) {
                tvIdCard.setText("CCCD: " + t.idCard);
                tvIdCard.setVisibility(View.VISIBLE);
            } else {
                tvIdCard.setVisibility(View.GONE);
            }

            tvRentingBadge.setVisibility(isRenting ? View.VISIBLE : View.GONE);
        }
    }
}
