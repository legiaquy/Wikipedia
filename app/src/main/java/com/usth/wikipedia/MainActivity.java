package com.usth.wikipedia;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;


import com.usth.wikipedia.fragment.explore.ExploreFragment;
import com.usth.wikipedia.fragment.HistoryFragment;
import com.usth.wikipedia.fragment.SavedFragment;
import com.usth.wikipedia.fragment.search.SearchFragment;
import com.usth.wikipedia.fragment.SettingsFragment;


public class MainActivity extends AppCompatActivity {
    // 5 main fragments
    private final Fragment exploreFragment = new ExploreFragment();
    private final Fragment savedFragment = new SavedFragment();
    private final Fragment historyFragment = new HistoryFragment();
    private final Fragment searchFragment = new SearchFragment();
    private final Fragment settingsFragment = new SettingsFragment();

    // Manager to switch between fragments
    private final FragmentManager fm = getSupportFragmentManager();
    private Fragment active = exploreFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView navigationView = findViewById(R.id.bottom_navigation);
        navigationView.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);

        fm.beginTransaction().add(R.id.fragment_container, searchFragment, "5").hide(searchFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, settingsFragment, "4").hide(settingsFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, savedFragment, "3").hide(savedFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, historyFragment, "2").hide(historyFragment).commit();
        fm.beginTransaction().add(R.id.fragment_container, exploreFragment, "1").commit();


    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {

        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.nav_explore:
                    fm.beginTransaction().hide(active).show(exploreFragment).commit();
                    active = exploreFragment;
                    return true;

                case R.id.nav_history:
                    fm.beginTransaction().hide(active).show(historyFragment).commit();
                    active = historyFragment;
                    return true;

                case R.id.nav_saved:
                    fm.beginTransaction().hide(active).show(savedFragment).commit();
                    active = savedFragment;
                    return true;
                case R.id.nav_search:
                    fm.beginTransaction().hide(active).show(searchFragment).commit();
                    active = searchFragment;
                    return true;
                case R.id.nav_settings:
                    fm.beginTransaction().hide(active).show(settingsFragment).commit();
                    active = settingsFragment;
                    return true;
            }
            return false;
        }
    };


}
