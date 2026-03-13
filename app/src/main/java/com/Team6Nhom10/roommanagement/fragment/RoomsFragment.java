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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.chip.ChipGroup;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.Team6Nhom10.roommanagement.AddEditRoomActivity;
import com.Team6Nhom10.roommanagement.R;
import com.Team6Nhom10.roommanagement.RoomDetailActivity;
import com.Team6Nhom10.roommanagement.adapter.RoomAdapter;
import com.Team6Nhom10.roommanagement.datastorage.DataManager;
import com.Team6Nhom10.roommanagement.model.RoomEntity;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RoomsFragment extends Fragment {

    // Filter constants: -1=all, 0=vacant, 1=occupied
    private int currentFilter = -1;
    private String currentQuery = "";

    private RoomAdapter adapter;
    private TextView tvEmptyState;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_rooms, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerRooms);
        recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        adapter = new RoomAdapter(requireContext());
        recyclerView.setAdapter(adapter);
        tvEmptyState = view.findViewById(R.id.tvEmptyState);

        adapter.setOnRoomClickListener(room -> {
            Intent intent = new Intent(requireContext(), RoomDetailActivity.class);
            intent.putExtra("room_id", room.id);
            startActivity(intent);
        });

        // Search
        EditText etSearch = view.findViewById(R.id.etSearch);
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
                loadRooms();
            }
        });

        // Filter chips
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupFilter);
        chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
            if (checkedIds.isEmpty())
                return;
            int checkedId = checkedIds.get(0);
            if (checkedId == R.id.chipAll)
                currentFilter = -1;
            else if (checkedId == R.id.chipVacant)
                currentFilter = 0;
            else if (checkedId == R.id.chipOccupied)
                currentFilter = 1;
            loadRooms();
        });

        // FAB add room
        FloatingActionButton fabAdd = view.findViewById(R.id.fabAddRoom);
        fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), AddEditRoomActivity.class);
            startActivity(intent);
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isHidden()) loadRooms();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadRooms();
    }

    private void loadRooms() {
        Context ctx = getContext();
        if (ctx == null) return;

        executor.execute(() -> {
            DataManager dm = DataManager.getInstance(ctx);
            List<RoomEntity> rooms = dm.searchRooms(currentQuery, currentFilter);

            if (getView() == null) return;
            final List<RoomEntity> finalRooms = rooms;
            requireActivity().runOnUiThread(() -> {
                if (getView() == null) return;
                adapter.setRooms(finalRooms);
                if (finalRooms.isEmpty()) {
                    tvEmptyState.setVisibility(View.VISIBLE);
                    tvEmptyState.setText(currentQuery.isEmpty() ? getString(R.string.no_rooms)
                            : getString(R.string.no_rooms_filter));
                } else {
                    tvEmptyState.setVisibility(View.GONE);
                }
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
