package com.Team6Nhom10.roommanagement.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.Team6Nhom10.roommanagement.R;
import com.Team6Nhom10.roommanagement.model.RoomEntity;

import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class RoomAdapter extends RecyclerView.Adapter<RoomAdapter.RoomViewHolder> {

    public interface OnRoomClickListener {
        void onRoomClick(RoomEntity room);
    }

    private List<RoomEntity> rooms = new ArrayList<>();
    private final Context context;
    private OnRoomClickListener listener;

    public RoomAdapter(Context context) {
        this.context = context;
    }

    public void setOnRoomClickListener(OnRoomClickListener listener) {
        this.listener = listener;
    }

    public void setRooms(List<RoomEntity> rooms) {
        this.rooms = rooms != null ? rooms : new ArrayList<>();
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public RoomViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_room, parent, false);
        return new RoomViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RoomViewHolder holder, int position) {
        RoomEntity room = rooms.get(position);
        holder.bind(room);
    }

    @Override
    public int getItemCount() {
        return rooms.size();
    }

    class RoomViewHolder extends RecyclerView.ViewHolder {
        private final ImageView imgRoom;
        private final TextView tvRoomCode;
        private final TextView tvRoomName;
        private final TextView tvPrice;
        private final TextView tvArea;
        private final TextView tvStatus;
        private final TextView tvTenantName;
        private final CardView cardView;

        RoomViewHolder(@NonNull View itemView) {
            super(itemView);
            imgRoom = itemView.findViewById(R.id.imgRoom);
            tvRoomCode = itemView.findViewById(R.id.tvRoomCode);
            tvRoomName = itemView.findViewById(R.id.tvRoomName);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvArea = itemView.findViewById(R.id.tvArea);
            tvStatus = itemView.findViewById(R.id.tvStatus);
            tvTenantName = itemView.findViewById(R.id.tvTenantName);
            cardView = itemView.findViewById(R.id.cardRoom);

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onRoomClick(rooms.get(getAdapterPosition()));
                }
            });
        }

        void bind(RoomEntity room) {
            tvRoomCode.setText(room.roomCode);
            tvRoomName.setText(room.roomName);

            NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
            tvPrice.setText(nf.format(room.price) + " VNĐ/tháng");
            tvArea.setText(room.area + " m²");

            if (room.isOccupied()) {
                tvStatus.setText(context.getString(R.string.status_occupied));
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorOccupied));
                tvStatus.setBackgroundResource(R.drawable.bg_status_occupied);
                tvTenantName.setVisibility(View.VISIBLE);
                tvTenantName.setText(room.tenantName != null ? room.tenantName : "");
            } else {
                tvStatus.setText(context.getString(R.string.status_vacant));
                tvStatus.setTextColor(ContextCompat.getColor(context, R.color.colorVacant));
                tvStatus.setBackgroundResource(R.drawable.bg_status_vacant);
                tvTenantName.setVisibility(View.GONE);
            }

            if (room.photoPath != null && !room.photoPath.isEmpty()) {
                Glide.with(context)
                        .load(new File(room.photoPath))
                        .placeholder(R.drawable.ic_room_placeholder)
                        .centerCrop()
                        .into(imgRoom);
            } else {
                imgRoom.setImageResource(R.drawable.ic_room_placeholder);
            }
        }
    }
}
