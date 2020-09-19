package com.android.ipress;

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
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AppliancesActivity extends AppCompatActivity {

    private static final String TAG = "Appliances";
    String mLoggedInUsername;
    List<ApplianceInfo> mAppliances;
    FloatingActionButton floatingActionButton;
    Dialog mDialog;
    String mApplianceName;
    GridView mGridView;
    ApplianceAdapter mAdapter;
    RoomInfo mRoomInfo;
    String mRoomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appliances);

        mDialog = new Dialog(this);
        mAppliances = new ArrayList<>();
        mRoomInfo = HomeActivity.SelectedRoomInfo;
        if(mRoomInfo != null)
            mRoomName = mRoomInfo.getRoomName();
        TextView RoomLbl = findViewById(R.id.room_lbl);
        RoomLbl.setText(mRoomName);

        //floating action button to add new appliance
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.setContentView(R.layout.appliance_name_getter);
                mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                final EditText NameET = mDialog.findViewById(R.id.NameET);
                Button Add = mDialog.findViewById(R.id.AddBtn);
                Add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mApplianceName = NameET.getText().toString().trim().toLowerCase();
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
                                        AddAppliances();
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
        setupGridView();
        setupBottomNavBar();
    }

    //add new appliance method definition
    public void AddAppliances() {
        final String path = "Registered Users/" + mLoggedInUsername + "/Rooms/"+ mRoomName + "/Appliances/" + mApplianceName;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("state").getValue() == null) {
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(path);
                    String parent = mRoomName;
                    ApplianceInfo applianceInfo = new ApplianceInfo(0,0, mApplianceName, parent);
                    reference1.setValue(applianceInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()) {
                                Toast.makeText(AppliancesActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                            }else{
                                String Path = "Registered Users/" + mLoggedInUsername + "/Rooms/"+ mRoomName + "/deviceCount";
                                final DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference(Path);
                                reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        if(snapshot.getValue() != null) {
                                            int Count = Integer.parseInt(snapshot.getValue().toString());
                                            Count++;
                                            reference2.setValue(Count);
                                        }
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

                                    }
                                });
                            }
                        }
                    });
                } else {
                    Toast.makeText(AppliancesActivity.this, "Appliance name already exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
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
                        String path = "Registered Users/" + mLoggedInUsername + "/Rooms/"+ mRoomName + "/Appliances";
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(path);
                        reference1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Log.d(TAG, "onDataChange: " + snapshot);
                                mAppliances.clear();
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    ApplianceInfo applianceInfo = new ApplianceInfo();
                                    applianceInfo.setName(postSnapshot.child("name").getValue().toString());
                                    applianceInfo.setState(postSnapshot.child("state").getValue().toString());
                                    applianceInfo.setFavourite(Integer.parseInt(postSnapshot.child("favourite").getValue().toString()));
                                    mAppliances.add(applianceInfo);
                                }
                                mAdapter = new ApplianceAdapter(AppliancesActivity.this, mAppliances);
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
                grid = layoutInflater.inflate(R.layout.grid_item, null);
                ImageView DeleteBtn, ApplianceIcon, FavouriteBtn;
                Button ChangeBtn;
                TextView ApplianceName;
                ApplianceIcon = grid.findViewById(R.id.ApplianceIcon);
                DeleteBtn = grid.findViewById(R.id.DeleteBtn);
                ApplianceName = grid.findViewById(R.id.ApplianceName);
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

                ChangeBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        changeApplianceStatus(position);
                    }
                });

                DeleteBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mDialog.setContentView(R.layout.delete_confirmation_popup);
                        Button Cancel = mDialog.findViewById(R.id.cancel_button),Delete = mDialog.findViewById(R.id.delete_button);
                        Cancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                mDialog.dismiss();
                            }
                        });
                        Delete.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                removeAppliance(position);
                            }
                        });
                        mDialog.show();
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
        final String Name = applianceInfo.getName();
        int State = applianceInfo.getState();
        if (State == 0)
            State = 1;
        else
            State = 0;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        final int finalState = State;
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                        String path = "Registered Users/" + mLoggedInUsername + "/Rooms/"+ mRoomName + "/Appliances/"+ Name + "/state";
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(path);
                        reference1.setValue(finalState).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(AppliancesActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
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

    //method to remove appliance on remove button click
    public void removeAppliance(int position) {
        final ApplianceInfo applianceInfo = mAppliances.get(position);
        applianceInfo.setParent(mRoomName);
        final String Name = applianceInfo.getName();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                        String path = "Registered Users/" + mLoggedInUsername + "/Rooms/"+ mRoomName + "/Appliances";
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(path);
                        reference1.child(Name).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(AppliancesActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }else{
                                    String Path = "Registered Users/" + mLoggedInUsername + "/Rooms/"+ mRoomName + "/deviceCount";
                                    final DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference(Path);
                                    reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                                        @Override
                                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                                            if(snapshot.getValue() != null) {
                                                int Count = Integer.parseInt(snapshot.getValue().toString());
                                                Count--;
                                                reference2.setValue(Count);
                                            }
                                        }

                                        @Override
                                        public void onCancelled(@NonNull DatabaseError error) {

                                        }
                                    });
                                }
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

    public void changeFavouriteStatus(int position) {
        final ApplianceInfo info = mAppliances.get(position);
        final String Name = info.getName();
        int Favourite = info.getFavourite();
        if (Favourite == 1)
            Favourite = 0;
        else
            Favourite = 1;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        final int finalFavourite = Favourite;
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                        String path = "Registered Users/" + mLoggedInUsername + "/Rooms/"+ mRoomName + "/Appliances/"+ Name + "/favourite";
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(path);
                        reference1.setValue(finalFavourite).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (!task.isSuccessful()) {
                                    Toast.makeText(AppliancesActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                }
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

    //method to set up bottom nav bar
    public void setupBottomNavBar() {
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.home);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.settings:
                        ActivityStack.push("Home");
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
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

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}