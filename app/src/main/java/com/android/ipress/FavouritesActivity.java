package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class FavouritesActivity extends AppCompatActivity {

    private static final String TAG = "Favourites";
    String mLoggedInUsername;
    List<ApplianceInfo> mAppliances;
    GridView mGridView;
    ApplianceAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favourites);
        setupGridView();
        mAppliances = new ArrayList<>();
        setupBottomNavBar();
    }

    //initialize grid view
    public void setupGridView() {
        mGridView = findViewById(R.id.SampleGridView);
        updateGridView();
    }

    //update UI with new data set
    public void updateGridView() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                        String Path = "Registered Users/" + mLoggedInUsername + "/Rooms";
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Path);
                        reference1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot AllRoomsSnapshot) {
                                Log.d(TAG, "onDataChange: " + AllRoomsSnapshot);
                                mAppliances.clear();
                                for (DataSnapshot roomSnapshot : AllRoomsSnapshot.getChildren()) {
                                    DataSnapshot applianceSnapShot = roomSnapshot.child("Appliances");
                                    for(DataSnapshot iterator : applianceSnapShot.getChildren()){
                                        ApplianceInfo applianceInfo = new ApplianceInfo();
                                        applianceInfo.setName(iterator.child("name").getValue().toString());
                                        applianceInfo.setState(iterator.child("state").getValue().toString());
                                        applianceInfo.setParent(iterator.child("parent").getValue().toString());
                                        applianceInfo.setFavourite(Integer.parseInt(iterator.child("favourite").getValue().toString()));
                                        if(applianceInfo.getFavourite() == 1)
                                            mAppliances.add(applianceInfo);
                                    }
                                }
                                mAdapter = new ApplianceAdapter(FavouritesActivity.this, mAppliances);
                                mGridView.setAdapter(mAdapter);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public class ApplianceAdapter extends BaseAdapter {

        Context mContext;
        List<ApplianceInfo> list;

        public ApplianceAdapter(Context mContext, List<ApplianceInfo> list) {
            this.mContext = mContext;
            this.list = list;
        }

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int position) {
            return list.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(final int position, View convertView, ViewGroup parent) {
            View grid;
            LayoutInflater layoutInflater = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            ApplianceInfo info = list.get(position);
            if (convertView == null) {
                grid = layoutInflater.inflate(R.layout.favourite_grid_item, null);
                ImageView ApplianceIcon, FavouriteBtn;
                Button ChangeBtn;
                TextView ApplianceName,ParentName;
                ApplianceIcon = grid.findViewById(R.id.ApplianceIcon);
                ApplianceName = grid.findViewById(R.id.ApplianceName);
                ParentName = grid.findViewById(R.id.ParentName);
                ChangeBtn = grid.findViewById(R.id.ChangeBtn);
                FavouriteBtn = grid.findViewById(R.id.FavouriteBtn);

                if (info.getFavourite() == 1) {
                    FavouriteBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.favourite_selected));
                } else {
                    FavouriteBtn.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.favourite_not_selected));
                }
                if (info.getState() == 1) {
                    ApplianceIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.flash_on_vector));
                    ChangeBtn.setText("OFF");
                    ChangeBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.grid_off_btn_drawable));
                    ChangeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.rich_black));
                } else {
                    ApplianceIcon.setImageDrawable(ContextCompat.getDrawable(getApplicationContext(), R.drawable.flash_off_vector));
                    ChangeBtn.setText("ON");
                    ChangeBtn.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.grid_on_btn_drawable));
                    ChangeBtn.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.app_primary_color));
                }
                ApplianceName.setText(info.getName());
                ParentName.setText(info.getParent());

                ChangeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeApplianceStatus(position);
                    }
                });

                FavouriteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeFavouriteStatus(position);
                    }
                });
            } else {
                grid = convertView;
            }
            return grid;
        }
    }

    //method to change appliance state to ON/OFF on change button click
    public void changeApplianceStatus(int position) {
        final ApplianceInfo applianceInfo = mAppliances.get(position);
        final String Parent = applianceInfo.getParent();
        final String Name = applianceInfo.getName();
        int State = applianceInfo.getState();
        if (State == 0)
            State = 1;
        else
            State = 0;
        final int FinalState = State;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                        String Path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + Parent + "/Appliances/" + Name + "/state";
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Path);
                        reference1.setValue(FinalState);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //method to remove appliance from favourites
    public void changeFavouriteStatus(int position) {
        final ApplianceInfo info = mAppliances.get(position);
        final String Parent = info.getParent();
        final String Name = info.getName();
        int Favourite = info.getFavourite();
        if(Favourite == 1)
            Favourite = 0;
        else
            Favourite = 1;
        final int FinalFavourite = Favourite;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                        String Path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + Parent + "/Appliances/" + Name + "/favourite";
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(Path);
                        reference1.setValue(FinalFavourite);
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //method to set up bottom nav bar
    public void setupBottomNavBar() {
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.favourite);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.settings:
                        ActivityStack.push("Favourites");
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.automation:
                        ActivityStack.push("Favourites");
                        startActivity(new Intent(getApplicationContext(), AutomationActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.home:
                        ActivityStack.push("Favourites");
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
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
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                break;
            case "Settings":
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(0, 0);
                break;
            case "Automation":
                startActivity(new Intent(getApplicationContext(), AutomationActivity.class));
                overridePendingTransition(0, 0);
                break;
        }
    }
}