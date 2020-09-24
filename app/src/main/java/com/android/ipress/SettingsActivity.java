package com.android.ipress;

import android.app.Dialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.android.ipress.IconUploader.Uploader;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

public class SettingsActivity extends AppCompatActivity {

    int counter = 0;
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
        openUploaderOption();
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
                    case R.id.automation:
                        ActivityStack.push("Settings");
                        startActivity(new Intent(getApplicationContext(), AutomationActivity.class));
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
            case "Automation":
                startActivity(new Intent(getApplicationContext(), AutomationActivity.class));
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

    private void openUploaderOption(){
        final Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.uploader_gate_way);
        if (dialog.getWindow() != null)
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        EditText editText = dialog.findViewById(R.id.key);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s.toString().equals("flyyoufools")){
                    startActivity(new Intent(getApplicationContext(), Uploader.class));
                    dialog.hide();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        TextView textView = findViewById(R.id.hiddenBtn);
        textView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(counter != 5){
                    counter++;
                }else{
                    counter = 0;
                    dialog.show();
                }
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        counter = 0;
    }
}