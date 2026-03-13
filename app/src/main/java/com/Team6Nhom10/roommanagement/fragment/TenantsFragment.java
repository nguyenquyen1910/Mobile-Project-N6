package com.Team6Nhom10.roommanagement.fragment;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.Team6Nhom10.roommanagement.AddEditTenantActivity;
import com.Team6Nhom10.roommanagement.R;
import com.Team6Nhom10.roommanagement.adapter.TenantAdapter;
import com.Team6Nhom10.roommanagement.database.DataManager;
import com.Team6Nhom10.roommanagement.model.TenantEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TenantsFragment extends Fragment {

    private TenantAdapter adapter;
    private TextView tvEmpty;
    private String currentQuery = "";
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tenants, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView rv = view.findViewById(R.id.recyclerTenants);
        rv.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new TenantAdapter(requireContext());
        rv.setAdapter(adapter);
        tvEmpty = view.findViewById(R.id.tvEmptyTenants);

        adapter.setOnTenantClickListener(tenant -> showTenantOptions(tenant));

        EditText etSearch = view.findViewById(R.id.etSearchTenant);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                currentQuery = s.toString().trim();
                loadTenants();
            }
        });

        FloatingActionButton fab = view.findViewById(R.id.fabAddTenant);
        fab.setOnClickListener(v -> startActivity(
                new Intent(requireContext(), AddEditTenantActivity.class)));
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) loadTenants();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadTenants();
    }

    private void loadTenants() {
        Context ctx = getContext();
        if (ctx == null) return;

        executor.execute(() -> {
            DataManager dm = DataManager.getInstance(ctx);
            List<TenantEntity> list = dm.searchTenants(currentQuery);

            List<Integer> rentingIds = new ArrayList<>();
            for (TenantEntity t : list) {
                if (dm.isTenantRenting(t.id))
                    rentingIds.add(t.id);
            }

            if (getView() == null) return;
            requireActivity().runOnUiThread(() -> {
                if (getView() == null) return;
                adapter.setData(list, rentingIds);
                tvEmpty.setVisibility(list.isEmpty() ? View.VISIBLE : View.GONE);
                tvEmpty.setText(currentQuery.isEmpty() ? R.string.no_tenants : R.string.no_tenants_filter);
            });
        });
    }

    private void showTenantOptions(TenantEntity tenant) {
        String[] options = { getString(R.string.edit_tenant), getString(R.string.delete_tenant) };
        new AlertDialog.Builder(requireContext())
                .setTitle(tenant.name)
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        // Edit
                        Intent intent = new Intent(requireContext(), AddEditTenantActivity.class);
                        intent.putExtra("tenant_id", tenant.id);
                        startActivity(intent);
                    } else {
                        // Delete
                        confirmDeleteTenant(tenant);
                    }
                })
                .show();
    }

    private void confirmDeleteTenant(TenantEntity tenant) {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.tenant_delete_confirm_title))
                .setMessage(getString(R.string.tenant_delete_confirm_msg))
                .setPositiveButton(getString(R.string.delete_confirm_yes), (dialog, which) -> {
                    Context ctx = getContext();
                    if (ctx == null) return;
                    executor.execute(() -> {
                        boolean deleted = DataManager.getInstance(ctx).deleteTenant(tenant);
                        if (getView() == null) return;
                        requireActivity().runOnUiThread(() -> {
                            if (getView() == null) return;
                            if (deleted) {
                                Toast.makeText(ctx, "Đã xóa " + tenant.name, Toast.LENGTH_SHORT).show();
                                loadTenants();
                            } else {
                                Toast.makeText(ctx,
                                        getString(R.string.tenant_renting_cannot_delete), Toast.LENGTH_LONG).show();
                            }
                        });
                    });
                })
                .setNegativeButton(getString(R.string.delete_confirm_no), null)
                .show();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
