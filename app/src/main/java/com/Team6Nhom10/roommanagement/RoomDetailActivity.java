package com.Team6Nhom10.roommanagement;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.WindowCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.Team6Nhom10.roommanagement.database.DataManager;
import com.Team6Nhom10.roommanagement.model.RoomEntity;

import java.io.File;
import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomDetailActivity extends AppCompatActivity {

    private int roomId;
    private RoomEntity currentRoom;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private ImageView imgRoom;
    private TextView tvRoomCode, tvRoomName, tvStatus;
    private TextView tvPrice, tvArea, tvElectricity, tvWater, tvDescription;
    private LinearLayout layoutTenantSection;
    private TextView tvTenantName, tvTenantPhone, tvStartDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_room_detail);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(getString(R.string.room_detail));
        }

        roomId = getIntent().getIntExtra("room_id", -1);

        imgRoom = findViewById(R.id.imgRoomDetail);
        tvRoomCode = findViewById(R.id.tvDetailRoomCode);
        tvRoomName = findViewById(R.id.tvDetailRoomName);
        tvStatus = findViewById(R.id.tvDetailStatus);
        tvPrice = findViewById(R.id.tvDetailPrice);
        tvArea = findViewById(R.id.tvDetailArea);
        tvElectricity = findViewById(R.id.tvDetailElectricity);
        tvWater = findViewById(R.id.tvDetailWater);
        tvDescription = findViewById(R.id.tvDetailDescription);
        layoutTenantSection = findViewById(R.id.layoutTenantSection);
        tvTenantName = findViewById(R.id.tvDetailTenantName);
        tvTenantPhone = findViewById(R.id.tvDetailTenantPhone);
        tvStartDate = findViewById(R.id.tvDetailStartDate);

        MaterialButton btnEdit = findViewById(R.id.btnEditRoom);
        MaterialButton btnDelete = findViewById(R.id.btnDeleteRoom);

        btnEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddEditRoomActivity.class);
            intent.putExtra("room_id", roomId);
            startActivity(intent);
        });

        btnDelete.setOnClickListener(v -> confirmDelete());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadRoomDetail();
    }

    private void loadRoomDetail() {
        executor.execute(() -> {
            currentRoom = DataManager.getInstance(this).getRoomById(roomId);

            if (currentRoom == null) {
                runOnUiThread(this::finish);
                return;
            }

            runOnUiThread(this::bindView);
        });
    }

    private void bindView() {
        NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));

        tvRoomCode.setText(currentRoom.roomCode);
        tvRoomName.setText(currentRoom.roomName);
        tvPrice.setText(nf.format(currentRoom.price) + " VNĐ/tháng");
        tvArea.setText(currentRoom.area + " m²");
        tvElectricity.setText(nf.format(currentRoom.electricityPrice) + " VNĐ/kWh");
        tvWater.setText(nf.format(currentRoom.waterPrice) + " VNĐ/m³");

        String desc = (currentRoom.description != null && !currentRoom.description.isEmpty())
                ? currentRoom.description
                : "Không có mô tả";
        tvDescription.setText(desc);

        if (currentRoom.isOccupied()) {
            tvStatus.setText(getString(R.string.status_occupied));
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.colorOccupied));
            tvStatus.setBackgroundResource(R.drawable.bg_status_occupied_large);
            layoutTenantSection.setVisibility(View.VISIBLE);
            tvTenantName.setText(currentRoom.tenantName);
            tvTenantPhone.setText(currentRoom.tenantPhone);
            String dateText = (currentRoom.startDate != null && !currentRoom.startDate.isEmpty())
                    ? currentRoom.startDate
                    : "Chưa ghi nhận";
            tvStartDate.setText(dateText);
        } else {
            tvStatus.setText(getString(R.string.status_vacant));
            tvStatus.setTextColor(ContextCompat.getColor(this, R.color.colorVacant));
            tvStatus.setBackgroundResource(R.drawable.bg_status_vacant_large);
            layoutTenantSection.setVisibility(View.GONE);
        }

        if (currentRoom.photoPath != null && !currentRoom.photoPath.isEmpty()) {
            Glide.with(this)
                    .load(new File(currentRoom.photoPath))
                    .placeholder(R.drawable.ic_room_placeholder)
                    .centerCrop()
                    .into(imgRoom);
        } else {
            imgRoom.setImageResource(R.drawable.ic_room_placeholder);
        }
    }

    private void confirmDelete() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.delete_confirm_title))
                .setMessage(getString(R.string.delete_confirm_message))
                .setPositiveButton(getString(R.string.delete_confirm_yes), (dialog, which) -> deleteRoom())
                .setNegativeButton(getString(R.string.delete_confirm_no), null)
                .show();
    }

    private void deleteRoom() {
        executor.execute(() -> {
            DataManager.getInstance(this).deleteRoom(currentRoom);
            runOnUiThread(() -> {
                Toast.makeText(this, "Đã xóa phòng " + currentRoom.roomCode, Toast.LENGTH_SHORT).show();
                finish();
            });
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
