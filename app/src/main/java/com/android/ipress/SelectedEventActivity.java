package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class SelectedEventActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_event);
        setupBottomNavBar();
        Toast.makeText(this, "" + AutomationActivity.SelectedEventInfo.getEventName(), Toast.LENGTH_SHORT).show();
    }

    //method to set up bottom nav bar
    public void setupBottomNavBar() {
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.automation);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        ActivityStack.push("Automation");
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.automation:
                        startActivity(new Intent(getApplicationContext(), AutomationActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.settings:
                        ActivityStack.push("Automation");
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.favourite:
                        ActivityStack.push("Automation");
                        startActivity(new Intent(getApplicationContext(), FavouritesActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), AutomationActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}