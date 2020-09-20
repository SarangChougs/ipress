package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    GridView mGridView;
    String mLoggedInUsername;
    List<RoomInfo> mRooms = new ArrayList<>();
    RoomAdapter mAdapter;
    FloatingActionButton floatingActionButton;
    Dialog mDialog;
    String mRoomName;
    public static RoomInfo SelectedRoomInfo;
    public String TAG = "HomeActivity";
    TextView TotalDeviceCountTV;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        TotalDeviceCountTV = findViewById(R.id.TotalDeviceCount);
        ActivityStack.setEmpty();
        setupBottomNavBar();
        setupGridView();
        mDialog = new Dialog(this);
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.setContentView(R.layout.room_name_getter);
                mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                final EditText NameET = mDialog.findViewById(R.id.NameET);
                Button Add = mDialog.findViewById(R.id.AddBtn);
                Add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mRoomName = NameET.getText().toString().trim().toLowerCase();
                        Log.d(TAG, "into add function");
                        //to get the username for currently logged in user's username
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String email = dataSnapshot.child("email").getValue().toString();
                                    Log.d(TAG, " add button username search loop");
                                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                                        Log.d(TAG, "" + email);
                                        //add new appliance method call
                                        AddRoom();
                                        break;
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                        mDialog.dismiss();
                    }
                });
                mDialog.show();
            }
        });
    }

    //method to set up bottom nav bar
    public void setupBottomNavBar() {
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.settings:
                        ActivityStack.push("Home");
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.automation:
                        ActivityStack.push("Home");
                        startActivity(new Intent(getApplicationContext(), AutomationActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.favourite:
                        ActivityStack.push("Home");
                        startActivity(new Intent(getApplicationContext(),FavouritesActivity.class));
                        overridePendingTransition(0,0);
                        return true;
                }
                return false;
            }
        });
    }

    //initialize grid view
    public void setupGridView() {
        mGridView = findViewById(R.id.SampleGridView);
        updateGridView();
        mGridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SelectedRoomInfo = mRooms.get(position);
                startActivity(new Intent(getApplicationContext(),AppliancesActivity.class));
                overridePendingTransition(0,0);
                finish();
            }
        });
    }

    //add new appliance method definition
    public void AddRoom() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Rooms/" + mRoomName);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("roomName").getValue() == null) {
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Rooms");
                    RoomInfo roomInfo = new RoomInfo(mRoomName,0);
                    reference1.child(mRoomName).setValue(roomInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(HomeActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } else {
                    Toast.makeText(HomeActivity.this, "Room already exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //update UI with new data set
    public void updateGridView(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Rooms");
                        reference1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot AllRoomSnapshots) {
                                Log.d(TAG, "onDataChange: " + AllRoomSnapshots);
                                mRooms.clear();
                                int TotalDeviceCount = 0;
                                for (DataSnapshot RoomSnapshot : AllRoomSnapshots.getChildren()) {
                                    RoomInfo roomInfo = new RoomInfo();
                                    String RoomName = RoomSnapshot.child("roomName").getValue().toString();
                                    int DeviceCount = Integer.parseInt(RoomSnapshot.child("deviceCount").getValue().toString());
                                    roomInfo.setRoomName(RoomName);
                                    roomInfo.setDeviceCount(DeviceCount);
                                    mRooms.add(roomInfo);
                                    TotalDeviceCount += DeviceCount;
                                }
                                String text;
                                if(TotalDeviceCount == 1)
                                    text = TotalDeviceCount + " Device";
                                else
                                    text = TotalDeviceCount + " Devices";
                                TotalDeviceCountTV.setText(text);
                                mAdapter = new RoomAdapter(HomeActivity.this, mRooms);
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

    public class RoomAdapter extends BaseAdapter {

        Context mContext;
        List<RoomInfo> list;

        public RoomAdapter(Context mContext, List<RoomInfo> list) {
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
            RoomInfo info = list.get(position);
            if (convertView == null) {
                grid = layoutInflater.inflate(R.layout.room_grid_item, null);
                TextView RoomName = grid.findViewById(R.id.RoomName);
                TextView DeviceCount = grid.findViewById(R.id.DeviceCount);

                RoomName.setText(info.getRoomName());
                String text = String.valueOf(info.getDeviceCount());
                if(text.equals("1"))
                    text += " Device";
                else
                    text += " Devices";

                DeviceCount.setText(text);
            } else {
                grid = convertView;
            }
            return grid;
        }
    }

    @Override
    public void onBackPressed() {
        GlobalClass.BackCounter += 1;
        if (GlobalClass.BackCounter != 2)
            Toast.makeText(getApplicationContext(), "Press again to exit", Toast.LENGTH_SHORT).show();
        if (GlobalClass.BackCounter == 2) {
            HomeActivity.this.finish();
            finishAffinity();
            System.exit(0);
        }
        Handler handler = new Handler();
        //reset counter
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                GlobalClass.BackCounter = 0;
            }
        }, 3000);
    }
}