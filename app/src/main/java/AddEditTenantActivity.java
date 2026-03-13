package com.Team6Nhom10.roommanagement;

import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.WindowCompat;

import com.google.android.material.button.MaterialButton;
import com.Team6Nhom10.roommanagement.database.DataManager;
import com.Team6Nhom10.roommanagement.model.TenantEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AddEditTenantActivity extends AppCompatActivity {

    private int tenantId = -1;
    private boolean isEditMode = false;

    private EditText etName, etPhone, etIdCard, etNotes;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_edit_tenant);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null)
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        tenantId = getIntent().getIntExtra("tenant_id", -1);
        isEditMode = (tenantId != -1);

        if (getSupportActionBar() != null)
            getSupportActionBar().setTitle(
                    isEditMode ? getString(R.string.edit_tenant_title) : getString(R.string.add_tenant_title));

        etName = findViewById(R.id.etTenantName);
        etPhone = findViewById(R.id.etTenantPhone);
        etIdCard = findViewById(R.id.etIdCard);
        etNotes = findViewById(R.id.etNotes);

        if (isEditMode)
            loadExistingTenant();

        MaterialButton btnSave = findViewById(R.id.btnSaveTenant);
        btnSave.setOnClickListener(v -> saveTenant());

        MaterialButton btnCancel = findViewById(R.id.btnCancelTenant);
        btnCancel.setOnClickListener(v -> finish());
    }

    private void loadExistingTenant() {
        executor.execute(() -> {
            TenantEntity t = DataManager.getInstance(this).getTenantById(tenantId);
            if (t == null) {
                runOnUiThread(this::finish);
                return;
            }
            runOnUiThread(() -> {
                etName.setText(t.name);
                etPhone.setText(t.phone);
                etIdCard.setText(t.idCard != null ? t.idCard : "");
                etNotes.setText(t.notes != null ? t.notes : "");
            });
        });
    }

    private void saveTenant() {
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();

        if (TextUtils.isEmpty(name) || TextUtils.isEmpty(phone)) {
            Toast.makeText(this, getString(R.string.error_required), Toast.LENGTH_SHORT).show();
            return;
        }

        TenantEntity tenant = new TenantEntity();
        tenant.id = tenantId;
        tenant.name = name;
        tenant.phone = phone;
        tenant.idCard = etIdCard.getText().toString().trim();
        tenant.notes = etNotes.getText().toString().trim();

        executor.execute(() -> {
            DataManager dm = DataManager.getInstance(this);
            String error;
            if (isEditMode) {
                error = dm.updateTenant(tenant);
            } else {
                error = dm.addTenant(tenant);
            }

            runOnUiThread(() -> {
                if (error != null) {
                    Toast.makeText(this, error, Toast.LENGTH_LONG).show();
                } else {
                    String msg = isEditMode ? "Đã cập nhật " + name : "Đã thêm " + name;
                    Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
                    finish();
                }
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
