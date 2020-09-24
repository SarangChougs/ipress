package com.android.ipress;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

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

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AppliancesActivity extends AppCompatActivity {
    String mLoggedInUsername;
    List<ApplianceInfo> mAppliances;
    FloatingActionButton floatingActionButton;
    Dialog mDialog;
    String mApplianceName, mIconUrl;
    GridView mGridView;
    ApplianceAdapter mAdapter;
    RoomInfo mRoomInfo;
    String mRoomName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_appliances);
        GlobalClass.iconUrl = "";
        GlobalClass.applianceName = "";
        mAppliances = new ArrayList<>();
        mRoomInfo = HomeActivity.SelectedRoomInfo;
        if (mRoomInfo != null)
            mRoomName = mRoomInfo.getRoomName();
        TextView RoomLbl = findViewById(R.id.room_lbl);
        RoomLbl.setText(mRoomName);

        setupAddApplianceDialog();
        //floating action button to add new appliance
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.show();
            }
        });
        setupGridView();
        setupBottomNavBar();
    }

    //method to set up add event dialog
    public void setupAddApplianceDialog() {
        mDialog = new Dialog(this);
        mDialog.setContentView(R.layout.name_getter);
        if (mDialog.getWindow() != null)
            mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        final EditText NameET = mDialog.findViewById(R.id.NameET);
        final Button Add = mDialog.findViewById(R.id.AddBtn);
        TextView lbl = mDialog.findViewById(R.id.lbl);
        lbl.setText("Enter Appliance Name");
        ImageView Icon = mDialog.findViewById(R.id.DialogIcon);
        Button ChooseIcon = mDialog.findViewById(R.id.ChooseIconBtn);
        NameET.setText(GlobalClass.applianceName);
        if (!GlobalClass.iconUrl.isEmpty()) {
            Picasso.with(AppliancesActivity.this)
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
                GlobalClass.applianceName = NameET.getText().toString().trim();
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
                mApplianceName = NameET.getText().toString().trim().toLowerCase();
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
    }

    //add new appliance method definition
    public void AddAppliances() {
        final String path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + mRoomName + "/Appliances/" + mApplianceName;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference(path);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("state").getValue() == null) {
                    FirebaseDatabase.getInstance().getReference("counts/applianceId").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            if (snapshot.getValue() != null) {
                                String idStr = snapshot.getValue().toString();
                                int id = Integer.parseInt(idStr);
                                id++;
                                idStr = id + "";
                                String parent = mRoomName;
                                ApplianceInfo applianceInfo = new ApplianceInfo("" + idStr,
                                        0,
                                        0,
                                        "" + mApplianceName,
                                        "" + parent,
                                        "" + mIconUrl);
                                //increment appliance id count
                                FirebaseDatabase.getInstance().getReference("counts/applianceId").setValue(idStr);
                                //add appliance to database
                                FirebaseDatabase.getInstance().getReference(path).setValue(applianceInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(AppliancesActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            String Path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + mRoomName + "/deviceCount";
                                            final DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference(Path);
                                            reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.getValue() != null) {
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
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

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
                        String path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + mRoomName + "/Appliances";
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(path);
                        reference1.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                mAppliances.clear();
                                for (DataSnapshot postSnapshot : snapshot.getChildren()) {
                                    ApplianceInfo applianceInfo = new ApplianceInfo();
                                    applianceInfo.setApplianceId(postSnapshot.child("applianceId").getValue().toString());
                                    applianceInfo.setName(postSnapshot.child("name").getValue().toString());
                                    applianceInfo.setState(postSnapshot.child("state").getValue().toString());
                                    applianceInfo.setFavourite(Integer.parseInt(postSnapshot.child("favourite").getValue().toString()));
                                    applianceInfo.setIconUrl(postSnapshot.child("iconUrl").getValue().toString());
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

                Picasso.with(AppliancesActivity.this)
                        .load(info.getIconUrl())
                        .placeholder(R.drawable.image_icon)
                        .into(ApplianceIcon);

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
                        Button Cancel = mDialog.findViewById(R.id.cancel_button), Delete = mDialog.findViewById(R.id.delete_button);
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
                                mDialog.dismiss();
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
                        String path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + mRoomName + "/Appliances/" + Name + "/state";
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
        final String applianceId = applianceInfo.getApplianceId();
        final String Name = applianceInfo.getName();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                        String path = "Registered Users/" + mLoggedInUsername + "/Events";
                        FirebaseDatabase.getInstance().getReference(path).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot EventSnapshot : snapshot.getChildren()) {
                                    String Path;
                                    String Ids = EventSnapshot.child("applianceIds").getValue().toString();
                                    String eventName = EventSnapshot.child("eventName").getValue().toString();
                                    List<String> EventAppIds = new ArrayList<>(Arrays.asList(Ids.split(" ")));
                                    if (!Ids.equals("")) {
                                        EventAppIds.remove(applianceId);
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
                                String path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + mRoomName + "/Appliances";
                                DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference(path);
                                reference1.child(Name).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (!task.isSuccessful()) {
                                            Toast.makeText(AppliancesActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                        } else {
                                            String Path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + mRoomName + "/deviceCount";
                                            final DatabaseReference reference2 = FirebaseDatabase.getInstance().getReference(Path);
                                            reference2.addListenerForSingleValueEvent(new ValueEventListener() {
                                                @Override
                                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                                    if (snapshot.getValue() != null) {
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
                        String path = "Registered Users/" + mLoggedInUsername + "/Rooms/" + mRoomName + "/Appliances/" + Name + "/favourite";
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
                    case R.id.automation:
                        ActivityStack.push("Home");
                        startActivity(new Intent(getApplicationContext(), AutomationActivity.class));
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
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        setupAddApplianceDialog();
        mDialog.show();
    }

    @Override
    public void onBackPressed() {
        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }
}