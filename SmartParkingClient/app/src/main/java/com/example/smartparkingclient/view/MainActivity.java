package com.example.smartparkingclient.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import com.example.smartparkingclient.R;
import com.example.smartparkingclient.api.models.User;
import com.example.smartparkingclient.api.utils.UserDataService;
import com.example.smartparkingclient.view.adapters.PagesAdapter;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;


import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity implements TabLayoutMediator.TabConfigurationStrategy {
    ViewPager2 viewPager;
    TabLayout tabLayout;
    ArrayList<String> names = new ArrayList<>(Arrays.asList("Profile", "Parking", "Booking"));

    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        var userDataService = UserDataService.getInstance(MainActivity.this);
        user = userDataService.getUser();
        if (user == null) {
            Intent intent = new Intent(MainActivity.this, LoginActivity.class);
            startActivity(intent);
        }

        viewPager = findViewById(R.id.pager);
        tabLayout = findViewById(R.id.tab_layout);
        setViewPageAdapter();

        FloatingActionButton fab = findViewById(R.id.new_booking);
        fab.setOnClickListener(v -> {
            var intent = new Intent(MainActivity.this, CreateEditActivity.class);
            startActivity(intent);
        });
    }

    void setViewPageAdapter() {
        PagesAdapter adapter = new PagesAdapter(this);
        ArrayList<Fragment> fragments = new ArrayList<>();

        fragments.add(new UserFragment(user));
        fragments.add(new ParkingsFragment());
        fragments.add(new BookingsFragment());
        adapter.setFragments(fragments);
        viewPager.setAdapter(adapter);
        new TabLayoutMediator(tabLayout, viewPager, this).attach();
    }

    @Override
    public void onConfigureTab(@NonNull TabLayout.Tab tab, int position) {
        tab.setText(names.get(position));
    }
}