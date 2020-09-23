package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AutomationActivity extends AppCompatActivity {

    ViewPager viewPager;
    String mLoggedInUsername;
    List<EventInfo> mEvents = new ArrayList<>();
    FloatingActionButton floatingActionButton;
    Dialog mDialog;
    String mEventName;
    SliderAdapter sliderAdapter;
    public static EventInfo SelectedEventInfo;
    public String TAG = "HomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_automation);
        setupBottomNavBar();
        setupViewPager();
        setupAddEvent();
    }

    //initialize grid view
    public void setupViewPager() {
        viewPager = findViewById(R.id.ViewPager);
        updatePageSlider();
    }

    public void updatePageSlider() {
        FirebaseDatabase.getInstance().getReference("Registered Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                System.out.println("username search");
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child("email").getValue().toString().equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = userSnapshot.child("username").getValue().toString();
                        System.out.println("user found : " + mLoggedInUsername);
                        FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Events").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                mEvents.clear();
                                for (DataSnapshot eventSnapshot : snapshot.getChildren()) {
                                    EventInfo eventInfo = new EventInfo();
                                    eventInfo.setDeviceCount(eventSnapshot.child("deviceCount").getValue().toString());
                                    eventInfo.setEventName(eventSnapshot.child("eventName").getValue().toString());
                                    eventInfo.setActivated(eventSnapshot.child("activated").getValue().toString());
                                    eventInfo.setApplianceIds(eventSnapshot.child("applianceIds").getValue().toString());
                                    mEvents.add(eventInfo);
                                }
                                sliderAdapter = new SliderAdapter(getApplicationContext(), mEvents);
                                viewPager.setAdapter(sliderAdapter);
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

    //PagerAdapter class
    public class SliderAdapter extends PagerAdapter {

        Context mContext;
        List<EventInfo> mList;
        LayoutInflater layoutInflater;

        public SliderAdapter(Context context, List<EventInfo> list) {
            this.mContext = context;
            this.mList = list;
        }

        @Override
        public int getCount() {
            return mList.size();
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == (RelativeLayout) object;
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, final int position) {
            layoutInflater = (LayoutInflater) mContext.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
            View view = layoutInflater.inflate(R.layout.slide_layout, container, false);

            ImageView EventIcon = view.findViewById(R.id.event_icon);
            TextView EventName = view.findViewById(R.id.event_name);
            TextView DeviceCount = view.findViewById(R.id.device_count);
            Button Activate = view.findViewById(R.id.activate_btn);

            EventInfo eventInfo = mList.get(position);

            EventIcon.setImageResource(R.drawable.event);
            EventName.setText(eventInfo.getEventName());
            if(eventInfo.getDeviceCount().equals("1")){
                DeviceCount.setText(eventInfo.getDeviceCount() + " Device");
            }else{
                DeviceCount.setText(eventInfo.getDeviceCount() + " Devices");
            }

            EventIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SelectedEventInfo = mEvents.get(position);
                    startActivity(new Intent(getApplicationContext(), SelectedEventActivity.class));
                    overridePendingTransition(0, 0);
                }
            });

            if (eventInfo.getActivated().equals("1")) {
                Activate.setText("Deactivate");
                Activate.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.deactivate_drawable));
                Activate.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.rich_black));
            } else {
                Activate.setText("Activate");
                Activate.setBackground(ContextCompat.getDrawable(getApplicationContext(), R.drawable.activate_drawable));
                Activate.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.app_primary_color));
            }

            Activate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    changeEventStatus(position);
                }
            });

            container.addView(view);

            return view;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((RelativeLayout) object);
        }
    }

    private void changeEventStatus(int position) {
        EventInfo eventInfo = mEvents.get(position);
        final String eventName = eventInfo.getEventName();
        String Ids = eventInfo.getApplianceIds();
        final List<String> applianceIds = new ArrayList<>(Arrays.asList(Ids.split(" ")));
        String initialState = eventInfo.getActivated();
        if (initialState.equals("1"))
            initialState = "0";
        else
            initialState = "1";
        final String finalState = initialState;
        FirebaseDatabase.getInstance().getReference("Registered Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child("email").getValue().toString().equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = userSnapshot.child("username").getValue().toString();
                        FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Rooms").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot AllRoomsSnapshot) {
                                for (DataSnapshot roomSnapshot : AllRoomsSnapshot.getChildren()) {
                                    String roomName = roomSnapshot.child("roomName").getValue().toString();
                                    DataSnapshot applianceSnapShot = roomSnapshot.child("Appliances");
                                    for(DataSnapshot iterator : applianceSnapShot.getChildren()){
                                        String applianceName = iterator.child("name").getValue().toString();
                                        String id = iterator.child("applianceId").getValue().toString();
                                        if(applianceIds.contains(id)){
                                            FirebaseDatabase.getInstance()
                                                    .getReference("Registered Users/" + mLoggedInUsername + "/Rooms/" + roomName + "/Appliances/" + applianceName + "/state")
                                                    .setValue(finalState);
                                        }
                                    }
                                }
                                FirebaseDatabase.getInstance()
                                        .getReference("Registered Users/" + mLoggedInUsername + "/Events/" + eventName + "/activated")
                                        .setValue(finalState);
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

    //method to set up variables for add event fab, references, etc
    public void setupAddEvent() {
        mDialog = new Dialog(this);
        floatingActionButton = findViewById(R.id.fab);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDialog.setContentView(R.layout.event_name_getter);
                if(mDialog.getWindow() != null)
                    mDialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                final EditText NameET = mDialog.findViewById(R.id.NameET);
                Button Add = mDialog.findViewById(R.id.AddBtn);
                Add.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        mEventName = NameET.getText().toString().trim().toLowerCase();
                        //to get the username for currently logged in user's username
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
                        reference.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                    String email = dataSnapshot.child("email").getValue().toString();
                                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                                        //add new appliance; method call
                                        AddEvent();
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

    //method to add event
    private void AddEvent() {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Events/" + mEventName);
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("eventName").getValue() == null) {
                    DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Events");
                    EventInfo eventInfo = new EventInfo(mEventName, String.valueOf(0), "0", "");
                    reference1.child(mEventName).setValue(eventInfo);
                } else {
                    Toast.makeText(AutomationActivity.this, "Event already exists", Toast.LENGTH_SHORT).show();
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
        switch (ActivityStack.pop()) {
            case "Home":
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                overridePendingTransition(0, 0);
                break;
            case "Settings":
                startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                overridePendingTransition(0, 0);
                break;
            case "Favourites":
                startActivity(new Intent(getApplicationContext(), FavouritesActivity.class));
                overridePendingTransition(0, 0);
                break;
        }
    }
}