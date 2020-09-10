package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        LinearLayout ProfileOption = findViewById(R.id.ProfileOption);
        ProfileOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), ProfileActivity.class));
                overridePendingTransition(0, 0);
                finish();
            }
        });

        LinearLayout LogoutOption = findViewById(R.id.LogoutBtn);
        LogoutOption.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                GlobalClass.CurrentUserEmail = "";
                ActivityStack.stack.clear();
                finishAffinity();
            }
        });
        setupBottomNavBar();
    }

    //method to set up bottom nav bar
    public void setupBottomNavBar() {
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.settings);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        ActivityStack.push("Settings");
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.favourite:
                        ActivityStack.push("Settings");
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
        switch (ActivityStack.pop()) {
            case "Home":
                startActivity(new Intent(getApplicationContext(), AppliancesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                break;
            case "Favourites":
                startActivity(new Intent(getApplicationContext(), FavouritesActivity.class));
                overridePendingTransition(0, 0);
                finish();
                break;
        }
    }

    public void backClick(View view) {
        onBackPressed();
    }
}