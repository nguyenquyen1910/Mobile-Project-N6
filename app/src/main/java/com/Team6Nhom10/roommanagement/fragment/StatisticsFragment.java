package com.Team6Nhom10.roommanagement.fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.Team6Nhom10.roommanagement.R;
import com.Team6Nhom10.roommanagement.datastorage.DataManager;

import java.text.NumberFormat;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class StatisticsFragment extends Fragment {

    private TextView tvTotalRooms, tvVacantRooms, tvOccupiedRooms;
    private TextView tvOccupancyRate, tvMonthlyRevenue;
    private ProgressBar progressOccupancy;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvTotalRooms = view.findViewById(R.id.tvTotalRooms);
        tvVacantRooms = view.findViewById(R.id.tvVacantRooms);
        tvOccupiedRooms = view.findViewById(R.id.tvOccupiedRooms);
        tvOccupancyRate = view.findViewById(R.id.tvOccupancyRate);
        tvMonthlyRevenue = view.findViewById(R.id.tvMonthlyRevenue);
        progressOccupancy = view.findViewById(R.id.progressOccupancy);
    }

    @Override
    public void onResume() {
        super.onResume();
        // With show/hide, all fragments get onResume; skip load if this tab is hidden.
        if (!isHidden()) loadStatistics();
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (!hidden) loadStatistics();
    }

    private void loadStatistics() {
        // Capture context before submitting — avoids crash when fragment detaches
        // (requireContext() inside the lambda would throw IllegalStateException)
        Context ctx = getContext();
        if (ctx == null) return;

        executor.execute(() -> {
            DataManager dm = DataManager.getInstance(ctx);
            int total    = dm.getTotalCount();
            int vacant   = dm.getVacantCount();
            int occupied = dm.getOccupiedCount();
            double monthly = dm.getMonthlyRevenue();
            float rate = total > 0 ? (occupied * 100f / total) : 0f;

            if (getView() == null) return;
            requireActivity().runOnUiThread(() -> {
                if (getView() == null) return;
                tvTotalRooms.setText(String.valueOf(total));
                tvVacantRooms.setText(String.valueOf(vacant));
                tvOccupiedRooms.setText(String.valueOf(occupied));
                tvOccupancyRate.setText(String.format(Locale.getDefault(), "%.1f%%", rate));
                progressOccupancy.setProgress((int) rate);

                NumberFormat nf = NumberFormat.getNumberInstance(new Locale("vi", "VN"));
                tvMonthlyRevenue.setText(nf.format(monthly) + " VNĐ");
            });
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        executor.shutdown();
    }
}
