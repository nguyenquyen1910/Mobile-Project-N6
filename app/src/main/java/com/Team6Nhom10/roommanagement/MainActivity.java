package com.Team6Nhom10.roommanagement;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.WindowCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.Team6Nhom10.roommanagement.fragment.RoomsFragment;
import com.Team6Nhom10.roommanagement.fragment.StatisticsFragment;
import com.Team6Nhom10.roommanagement.fragment.TenantsFragment;

public class MainActivity extends AppCompatActivity {

    private static final String TAG_STATS   = "tab_statistics";
    private static final String TAG_ROOMS   = "tab_rooms";
    private static final String TAG_TENANTS = "tab_tenants";

    private StatisticsFragment statisticsFragment;
    private RoomsFragment roomsFragment;
    private TenantsFragment tenantsFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        WindowCompat.setDecorFitsSystemWindows(getWindow(), true);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        FragmentManager fm = getSupportFragmentManager();

        if (savedInstanceState == null) {
            // First launch: create and add all three fragments.
            statisticsFragment = new StatisticsFragment();
            roomsFragment      = new RoomsFragment();
            tenantsFragment    = new TenantsFragment();

            fm.beginTransaction()
                    .add(R.id.fragmentContainer, statisticsFragment, TAG_STATS)
                    .add(R.id.fragmentContainer, roomsFragment,      TAG_ROOMS).hide(roomsFragment)
                    .add(R.id.fragmentContainer, tenantsFragment,    TAG_TENANTS).hide(tenantsFragment)
                    .commit();
        } else {
            // Activity recreated (rotation, low-memory restore, etc.).
            // FragmentManager already holds the fragments — just retrieve them.
            statisticsFragment = (StatisticsFragment) fm.findFragmentByTag(TAG_STATS);
            roomsFragment      = (RoomsFragment)      fm.findFragmentByTag(TAG_ROOMS);
            tenantsFragment    = (TenantsFragment)    fm.findFragmentByTag(TAG_TENANTS);
        }

        BottomNavigationView bottomNav = findViewById(R.id.bottomNavigationView);
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            FragmentTransaction ft = fm.beginTransaction();
            if (id == R.id.nav_statistics) {
                ft.show(statisticsFragment).hide(roomsFragment).hide(tenantsFragment);
            } else if (id == R.id.nav_rooms) {
                ft.hide(statisticsFragment).show(roomsFragment).hide(tenantsFragment);
            } else if (id == R.id.nav_tenants) {
                ft.hide(statisticsFragment).hide(roomsFragment).show(tenantsFragment);
            } else {
                return false;
            }
            ft.commit();
            return true;
        });
    }
}

