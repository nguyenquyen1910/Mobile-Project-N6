package com.Team6Nhom10.roommanagement;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.PickVisualMediaRequest;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;

import com.bumptech.glide.Glide;
import com.google.android.material.button.MaterialButton;
import com.Team6Nhom10.roommanagement.database.DataManager;
import com.Team6Nhom10.roommanagement.model.RoomEntity;
import com.Team6Nhom10.roommanagement.model.TenantEntity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditRoomActivity extends AppCompatActivity {

    private int roomId = -1; // -1 = add mode
    private boolean isEditMode = false;
    private String photoPath = null;
    private int selectedTenantId = -1;

    private EditText etRoomCode, etRoomName, etPrice, etArea;
    private EditText etDescription, etElectricity, etWater, etStartDate;
    private RadioGroup rgStatus;
    private RadioButton rbVacant, rbOccupied;
    private LinearLayout layoutTenantInfo;
    private ImageView imgPickedPhoto;
    private TextView tvSelectedTenant;

    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    private final ActivityResultLauncher<PickVisualMediaRequest> pickMedia = registerForActivityResult(
            new ActivityResultContracts.PickVisualMedia(), uri -> {
                if (uri != null) {
                    copyImageToInternal(uri);
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_room);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        roomId = getIntent().getIntExtra("room_id", -1);
        isEditMode = (roomId != -1);

        if (getSupportActionBar() != null) {
            getSupportActionBar()
                    .setTitle(isEditMode ? getString(R.string.edit_room_title) : getString(R.string.add_room_title));
        }

        bindViews();

        if (isEditMode) {
            loadExistingRoom();
        }

        // Status change: show/hide tenant section
        rgStatus.setOnCheckedChangeListener((group, checkedId) -> {
            layoutTenantInfo.setVisibility(
                    checkedId == R.id.rbOccupied ? View.VISIBLE : View.GONE);
        });

        // Pick photo
        imgPickedPhoto.setOnClickListener(v -> pickPhoto());
        findViewById(R.id.btnPickPhoto).setOnClickListener(v -> pickPhoto());

        // Tenant picker
        findViewById(R.id.btnPickTenant).setOnClickListener(v -> showTenantPicker());

        // Save button
        MaterialButton btnSave = findViewById(R.id.btnSave);
        btnSave.setOnClickListener(v -> saveRoom());

        // Cancel
        MaterialButton btnCancel = findViewById(R.id.btnCancel);
        btnCancel.setOnClickListener(v -> finish());
    }

    private void bindViews() {
        etRoomCode    = findViewById(R.id.etRoomCode);
        etRoomName    = findViewById(R.id.etRoomName);
        etPrice       = findViewById(R.id.etPrice);
        etArea        = findViewById(R.id.etArea);
        etDescription = findViewById(R.id.etDescription);
        etElectricity = findViewById(R.id.etElectricity);
        etWater       = findViewById(R.id.etWater);
        etStartDate   = findViewById(R.id.etStartDate);
        rgStatus      = findViewById(R.id.rgStatus);
        rbVacant      = findViewById(R.id.rbVacant);
        rbOccupied    = findViewById(R.id.rbOccupied);
        layoutTenantInfo  = findViewById(R.id.layoutTenantInfo);
        imgPickedPhoto    = findViewById(R.id.imgPickedPhoto);
        tvSelectedTenant  = findViewById(R.id.tvSelectedTenant);
    }

    private void loadExistingRoom() {
        executor.execute(() -> {
            RoomEntity room = DataManager.getInstance(this).getRoomById(roomId);
            if (room == null) {
                runOnUiThread(this::finish);
                return;
            }
            runOnUiThread(() -> populateFields(room));
        });
    }

    private void populateFields(RoomEntity room) {
        etRoomCode.setText(room.roomCode);
        etRoomCode.setEnabled(false); // Mã phòng không được sửa
        etRoomCode.setAlpha(0.6f);

        etRoomName.setText(room.roomName);
        etPrice.setText(String.valueOf((long) room.price));
        etArea.setText(String.valueOf(room.area));
        etDescription.setText(room.description);
        etElectricity.setText(String.valueOf((long) room.electricityPrice));
        etWater.setText(String.valueOf((long) room.waterPrice));

        if (room.isOccupied()) {
            rbOccupied.setChecked(true);
            layoutTenantInfo.setVisibility(View.VISIBLE);
            etStartDate.setText(room.startDate);
            selectedTenantId = room.tenantId;
            if (room.tenantName != null && !room.tenantName.isEmpty()) {
                tvSelectedTenant.setText(room.tenantName + "  |  " + room.tenantPhone);
            }
        } else {
            rbVacant.setChecked(true);
            layoutTenantInfo.setVisibility(View.GONE);
        }

        photoPath = room.photoPath;
        if (photoPath != null && !photoPath.isEmpty()) {
            Glide.with(this)
                    .load(new File(photoPath))
                    .centerCrop()
                    .into(imgPickedPhoto);
        }
    }

    private void showTenantPicker() {
        executor.execute(() -> {
            List<TenantEntity> list = DataManager.getInstance(this).getAllTenants();
            runOnUiThread(() -> {
                if (list.isEmpty()) {
                    Toast.makeText(this, "Chưa có người thuê nào. Hãy thêm người thuê trước.", Toast.LENGTH_SHORT).show();
                    return;
                }
                String[] labels = new String[list.size()];
                for (int i = 0; i < list.size(); i++) {
                    labels[i] = list.get(i).getDisplayLabel();
                }
                new AlertDialog.Builder(this)
                        .setTitle(getString(R.string.pick_tenant))
                        .setItems(labels, (dialog, which) -> {
                            TenantEntity picked = list.get(which);
                            selectedTenantId = picked.id;
                            tvSelectedTenant.setText(picked.getDisplayLabel());
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
                        .show();
            });
        });
    }

    private void pickPhoto() {
        pickMedia.launch(new PickVisualMediaRequest.Builder()
                .setMediaType(ActivityResultContracts.PickVisualMedia.ImageOnly.INSTANCE)
                .build());
    }

    private void copyImageToInternal(Uri uri) {
        executor.execute(() -> {
            try {
                String fileName = "room_" + System.currentTimeMillis() + ".jpg";
                File destFile = new File(getFilesDir(), fileName);
                try (InputStream in = getContentResolver().openInputStream(uri);
                        FileOutputStream out = new FileOutputStream(destFile)) {
                    if (in != null) {
                        byte[] buffer = new byte[4096];
                        int len;
                        while ((len = in.read(buffer)) != -1) {
                            out.write(buffer, 0, len);
                        }
                    }
                }
                photoPath = destFile.getAbsolutePath();
                runOnUiThread(() -> Glide.with(this)
                        .load(destFile)
                        .centerCrop()
                        .into(imgPickedPhoto));
            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(this, "Không thể tải ảnh", Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void saveRoom() {
        String code    = etRoomCode.getText().toString().trim();
        String name    = etRoomName.getText().toString().trim();
        String priceStr = etPrice.getText().toString().trim();
        String areaStr  = etArea.getText().toString().trim();

        if (TextUtils.isEmpty(code) || TextUtils.isEmpty(name) ||
                TextUtils.isEmpty(priceStr) || TextUtils.isEmpty(areaStr)) {
            Toast.makeText(this, getString(R.string.error_required), Toast.LENGTH_SHORT).show();
            return;
        }

        double price, area, electricity, water;
        try {
            price = Double.parseDouble(priceStr);
            area  = Double.parseDouble(areaStr);
            String elecStr  = etElectricity.getText().toString().trim();
            String waterStr = etWater.getText().toString().trim();
            electricity = TextUtils.isEmpty(elecStr)  ? 0 : Double.parseDouble(elecStr);
            water       = TextUtils.isEmpty(waterStr) ? 0 : Double.parseDouble(waterStr);
        } catch (NumberFormatException e) {
            Toast.makeText(this, getString(R.string.error_invalid_number), Toast.LENGTH_SHORT).show();
            return;
        }

        int status = rbOccupied.isChecked() ? 1 : 0;

        if (status == 1 && selectedTenantId == -1) {
            Toast.makeText(this, "Vui lòng chọn người thuê", Toast.LENGTH_SHORT).show();
            return;
        }

        executor.execute(() -> {
            DataManager dm = DataManager.getInstance(this);

            // Check duplicate room code (only when adding)
            if (!isEditMode) {
                RoomEntity existing = dm.getRoomByCode(code);
                if (existing != null) {
                    runOnUiThread(() -> Toast
                            .makeText(this, getString(R.string.error_room_code_exists), Toast.LENGTH_SHORT).show());
                    return;
                }
            }

            RoomEntity room = isEditMode ? dm.getRoomById(roomId) : new RoomEntity();
            if (room == null) {
                runOnUiThread(this::finish);
                return;
            }

            room.roomCode        = code;
            room.roomName        = name;
            room.price           = price;
            room.area            = area;
            room.description     = etDescription.getText().toString().trim();
            room.electricityPrice = electricity;
            room.waterPrice      = water;
            room.status          = status;
            room.photoPath       = photoPath;

            if (status == 1) {
                TenantEntity tenant = dm.getTenantById(selectedTenantId);
                if (tenant == null) {
                    runOnUiThread(() -> Toast.makeText(this, "Người thuê không tìm thấy", Toast.LENGTH_SHORT).show());
                    return;
                }
                room.tenantId    = selectedTenantId;
                room.tenantName  = tenant.name;
                room.tenantPhone = tenant.phone;
                room.startDate   = etStartDate.getText().toString().trim();
            } else {
                room.tenantId    = -1;
                room.tenantName  = null;
                room.tenantPhone = null;
                room.startDate   = null;
            }

            if (isEditMode) {
                dm.updateRoom(room);
            } else {
                dm.addRoom(room);
            }

            runOnUiThread(() -> {
                String msg = isEditMode ? "Đã cập nhật phòng " + room.roomCode
                        : "Đã thêm phòng " + room.roomCode;
                Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
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
