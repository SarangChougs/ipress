package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
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
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HomeActivity extends AppCompatActivity {

    GridView mGridView;
    String mLoggedInUsername;
    List<RoomInfo> mRooms = new ArrayList<>();
    RoomAdapter mAdapter;
    FloatingActionButton floatingActionButton;
    Dialog mDialog;
    String mRoomName, mIconUrl;
    public static RoomInfo SelectedRoomInfo;
    public String TAG = "HomeActivity";
    TextView TotalDeviceCountTV;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        GlobalClass.iconUrl = "";
        GlobalClass.roomName = "";
        TotalDeviceCountTV = findViewById(R.id.TotalDeviceCount);
        ActivityStack.setEmpty();
        setupBottomNavBar();
        setupGridView();
        setupAddRoomDialog();
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
                        startActivity(new Intent(getApplicationContext(), FavouritesActivity.class));
                        overridePendingTransition(0, 0);
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
                Intent intent = new Intent(getApplicationContext(), AppliancesActivity.class);
                Pair pair = new Pair<View, String>(mGridView, "room");
                ActivityOptions options = ActivityOptions.makeSceneTransitionAnimation(HomeActivity.this, pair);
                startActivity(intent, options.toBundle());
                overridePendingTransition(0, 0);
                finish();
            }
        });
    }

    //method to set up add event dialog
    public void setupAddRoomDialog() {
        mDialog = new Dialog(this);
        mDialog.setContentView(R.layout.name_getter);
        if (mDialog.getWindow() != null)
            mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        final EditText NameET = mDialog.findViewById(R.id.NameET);
        final Button Add = mDialog.findViewById(R.id.AddBtn);
        TextView lbl = mDialog.findViewById(R.id.lbl);
        lbl.setText("Enter Room Name");
        ImageView Icon = mDialog.findViewById(R.id.DialogIcon);
        Button ChooseIcon = mDialog.findViewById(R.id.ChooseIconBtn);
        NameET.setText(GlobalClass.roomName);
        if (!GlobalClass.iconUrl.isEmpty()) {
            Picasso.with(HomeActivity.this)
                    .load(GlobalClass.iconUrl)
                    .placeholder(R.drawable.image_icon)
                    .into(Icon);
            if (NameET.getText().toString().trim().length() != 0) {
                Add.setAlpha(1);
                Add.setClickable(true);
            }
        }
        ChooseIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GlobalClass.roomName = NameET.getText().toString().trim();
                mDialog.dismiss();
                startActivityForResult(new Intent(getApplicationContext(), IconSelector.class), 1);
            }
        });

        NameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() != 0 && !GlobalClass.iconUrl.equals("")) {
                    Add.setAlpha(1);
                    Add.setClickable(true);
                } else {
                    Add.setAlpha(0.2f);
                    Add.setClickable(false);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        Add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRoomName = NameET.getText().toString().trim().toLowerCase();
                mIconUrl = GlobalClass.iconUrl;
                //to get the username for currently logged in user's username
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String email = dataSnapshot.child("email").getValue().toString();
                            if (email.equals(GlobalClass.CurrentUserEmail)) {
                                mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                                //add new room's method call
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
    }

    //add new appliance method definition
    public void AddRoom() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Rooms/" + mRoomName);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("roomName").getValue() == null) {
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Rooms");
                    RoomInfo roomInfo = new RoomInfo("" + mRoomName, 0, "" + mIconUrl);
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
    public void updateGridView() {
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
                                mRooms.clear();
                                int TotalDeviceCount = 0;
                                for (DataSnapshot RoomSnapshot : AllRoomSnapshots.getChildren()) {
                                    RoomInfo roomInfo = new RoomInfo();
                                    String RoomName = RoomSnapshot.child("roomName").getValue().toString();
                                    int DeviceCount = Integer.parseInt(RoomSnapshot.child("deviceCount").getValue().toString());
                                    roomInfo.setRoomName(RoomName);
                                    roomInfo.setDeviceCount(DeviceCount);
                                    roomInfo.setIconUrl(RoomSnapshot.child("iconUrl").getValue().toString());
                                    mRooms.add(roomInfo);
                                    TotalDeviceCount += DeviceCount;
                                }
                                String text;
                                if (TotalDeviceCount == 1)
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
                ImageView ApplianceIcon = grid.findViewById(R.id.ApplianceIcon);
                TextView RoomName = grid.findViewById(R.id.RoomName);
                TextView DeviceCount = grid.findViewById(R.id.DeviceCount);
                ImageView DeleteRoom = grid.findViewById(R.id.DeleteBtn);

                Picasso.with(HomeActivity.this)
                        .load(info.getIconUrl())
                        .placeholder(R.drawable.image_icon)
                        .into(ApplianceIcon);

                DeleteRoom.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        removeRoom(position);
                    }
                });
                RoomName.setText(info.getRoomName());
                String text = String.valueOf(info.getDeviceCount());
                if (text.equals("1"))
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

    //method to remove room on remove button click
    public void removeRoom(int position) {
        RoomInfo roomInfo = mRooms.get(position);
        final String Name = roomInfo.getRoomName();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                        String path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + Name + "/Appliances";
                        FirebaseDatabase.getInstance().getReference(path).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                final List<String> appIds = new ArrayList<>();
                                String path;
                                if (snapshot.hasChildren()) {
                                    for (DataSnapshot iterator : snapshot.getChildren()) {
                                        appIds.add(iterator.child("applianceId").getValue().toString());
                                    }
                                    path = "Registered Users/" + mLoggedInUsername + "/Events";
                                    FirebaseDatabase.getInstance().getReference(path).addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            for (DataSnapshot EventSnapshot : snapshot.getChildren()) {
                                                String Path;
                                                String Ids = EventSnapshot.child("applianceIds").getValue().toString();
                                                String eventName = EventSnapshot.child("eventName").getValue().toString();
                                                if (!Ids.equals("")) {
                                                    List<String> EventAppIds = new ArrayList<>(Arrays.asList(Ids.split(" ")));
                                                    for (int i = 0; i < appIds.size(); i++) {
                                                        EventAppIds.remove(appIds.get(i));
                                                    }
                                                    int i = 0;
                                                    Ids = "";
                                                    while (i < EventAppIds.size()) {
                                                        if (Ids.length() == 0)
                                                            Ids = EventAppIds.get(i) + " ";
                                                        else
                                                            Ids = Ids + EventAppIds.get(i) + " ";
                                                        i++;
                                                    }
                                                    Path = "Registered Users/" + mLoggedInUsername + "/Events/" + eventName + "/applianceIds";
                                                    FirebaseDatabase.getInstance().getReference(Path).setValue(Ids);
                                                    String[] count = Ids.split(" ");
                                                    int deviceCount = count.length;
                                                    if (count[0].equals("")) {
                                                        deviceCount -= 1;
                                                    }
                                                    FirebaseDatabase.getInstance()
                                                            .getReference("Registered Users/" + mLoggedInUsername + "/Events/" + eventName + "/deviceCount")
                                                            .setValue(deviceCount);
                                                }
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
                                path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + Name;
                                FirebaseDatabase.getInstance().getReference(path).removeValue();
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

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setupAddRoomDialog();
        mDialog.show();
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