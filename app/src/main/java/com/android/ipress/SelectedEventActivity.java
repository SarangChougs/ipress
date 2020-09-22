package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SelectedEventActivity extends AppCompatActivity {

    TextView mSelectedEventNameTV;
    RecyclerView mToAddRecyclerView, mAddedRecyclerView;
    ToAddApplianceAdapter mToAddAdapter;
    AddedApplianceAdapter mAddedAdapter;
    List<ApplianceInfo> mAddedAppliances = new ArrayList<>();
    List<ApplianceInfo> mToAddAppliances = new ArrayList<>();
    String mSelectedEventName;
    String mLoggedInUsername;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_selected_event);
        EventInfo SelectedEventInfo = AutomationActivity.SelectedEventInfo;
        mSelectedEventNameTV = findViewById(R.id.selected_event_lbl);
        mSelectedEventName = SelectedEventInfo.getEventName();
        mSelectedEventNameTV.setText(mSelectedEventName);
        updateRecyclerView();
        setupBottomNavBar();
    }

    private void updateRecyclerView() {
        mToAddRecyclerView = findViewById(R.id.to_add_appliance_recycler_view);
        mToAddRecyclerView.setHasFixedSize(true);
        mToAddRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mToAddAdapter = new ToAddApplianceAdapter(this, mToAddAppliances);
        mToAddRecyclerView.setAdapter(mToAddAdapter);

        mAddedRecyclerView = findViewById(R.id.added_appliance_recycler_view);
        mAddedRecyclerView.setHasFixedSize(true);
        mAddedRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mAddedAdapter = new AddedApplianceAdapter(this, mAddedAppliances);
        mAddedRecyclerView.setAdapter(mAddedAdapter);


        FirebaseDatabase.getInstance().getReference("Registered Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child("email").getValue() != null && userSnapshot.child("email").getValue().toString().equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = userSnapshot.child("username").getValue().toString();
                        System.out.println("selected event activity username found :" + mLoggedInUsername);
                        FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Events/" + mSelectedEventName + "/applianceIds").addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                final List<String> eventAppIds = new ArrayList<>();
                                if (snapshot.getValue() != null) {
                                    String Ids = snapshot.getValue().toString();
                                    String[] temp = Ids.split(" ");
                                    eventAppIds.addAll(Arrays.asList(temp));
                                }
                                System.out.println("Size of eventAppIds : " + eventAppIds.size());
                                FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Rooms").addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        mAddedAppliances.clear();
                                        mToAddAppliances.clear();
                                        for (DataSnapshot roomSnapshot : snapshot.getChildren()) {
                                            DataSnapshot applianceSnapShot = roomSnapshot.child("Appliances");
                                            for (DataSnapshot iterator : applianceSnapShot.getChildren()) {
                                                String Id = iterator.child("applianceId").getValue().toString();
                                                ApplianceInfo applianceInfo = new ApplianceInfo();
                                                applianceInfo.setName(iterator.child("name").getValue().toString());
                                                applianceInfo.setState(iterator.child("state").getValue().toString());
                                                applianceInfo.setParent(iterator.child("parent").getValue().toString());
                                                applianceInfo.setApplianceId(iterator.child("applianceId").getValue().toString());
                                                applianceInfo.setFavourite(Integer.parseInt(iterator.child("favourite").getValue().toString()));
                                                if (eventAppIds.contains(Id))
                                                    mAddedAppliances.add(applianceInfo);
                                                else
                                                    mToAddAppliances.add(applianceInfo);
                                            }
                                        }
                                        mAddedAdapter.notifyDataSetChanged();
                                        mToAddAdapter.notifyDataSetChanged();
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {

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

    public class ToAddApplianceAdapter extends RecyclerView.Adapter<ToAddApplianceAdapter.ApplianceViewHolder> {

        private Context mContext;
        private List<ApplianceInfo> mAppliances;

        public ToAddApplianceAdapter(Context context, List<ApplianceInfo> List) {
            this.mContext = context;
            this.mAppliances = List;
        }

        @NonNull
        @Override
        public ApplianceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.to_add_appliance_row_item, parent, false);
            return new ApplianceViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ApplianceViewHolder holder, final int position) {
            ApplianceInfo applianceInfo = mAppliances.get(position);
            String Name = applianceInfo.getName();
            String Parent = applianceInfo.getParent();
            holder.NameTV.setText(Name);
            holder.ParentTV.setText(Parent);
            holder.AddBtnLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    addApplianceToEvent(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAppliances.size();
        }

        public class ApplianceViewHolder extends RecyclerView.ViewHolder {

            public TextView NameTV, ParentTV;
            public LinearLayout AddBtnLayout;

            public ApplianceViewHolder(View itemView) {
                super(itemView);
                NameTV = itemView.findViewById(R.id.text_view_name);
                ParentTV = itemView.findViewById(R.id.text_view_parent);
                AddBtnLayout = itemView.findViewById(R.id.add_btn);
            }
        }
    }

    private void addApplianceToEvent(int position) {
        ApplianceInfo applianceInfo = mToAddAppliances.get(position);
        final String Id = applianceInfo.getApplianceId();
        FirebaseDatabase.getInstance().getReference("Registered Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child("email").getValue().toString().equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = userSnapshot.child("username").getValue().toString();
                        final String Path = "Registered Users/" + mLoggedInUsername + "/Events/" + mSelectedEventName + "/applianceIds";
                        FirebaseDatabase.getInstance().getReference(Path).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    String ids = snapshot.getValue().toString();
                                    if (ids.length() == 0) {
                                        ids = Id + " ";
                                    } else {
                                        ids = ids + Id + " ";
                                    }
                                    String[] count = ids.split(" ");
                                    FirebaseDatabase.getInstance().getReference(Path).setValue(ids);
                                    FirebaseDatabase.getInstance()
                                            .getReference("Registered Users/" + mLoggedInUsername + "/Events/" + mSelectedEventName + "/deviceCount")
                                            .setValue(count.length);
                                }
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

    public class AddedApplianceAdapter extends RecyclerView.Adapter<AddedApplianceAdapter.ApplianceViewHolder> {

        private Context mContext;
        private List<ApplianceInfo> mAppliances;

        public AddedApplianceAdapter(Context context, List<ApplianceInfo> List) {
            this.mContext = context;
            this.mAppliances = List;
        }

        @NonNull
        @Override
        public ApplianceViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(mContext).inflate(R.layout.added_appliance_row_item, parent, false);
            return new ApplianceViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ApplianceViewHolder holder, final int position) {
            ApplianceInfo applianceInfo = mAppliances.get(position);
            String Name = applianceInfo.getName();
            String Parent = applianceInfo.getParent();
            holder.NameTV.setText(Name);
            holder.ParentTV.setText(Parent);
            holder.RemoveBtnLayout.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    removeApplianceFromEvent(position);
                }
            });
        }

        @Override
        public int getItemCount() {
            return mAppliances.size();
        }

        public class ApplianceViewHolder extends RecyclerView.ViewHolder {

            public TextView NameTV, ParentTV;
            public LinearLayout RemoveBtnLayout;

            public ApplianceViewHolder(View itemView) {
                super(itemView);
                NameTV = itemView.findViewById(R.id.text_view_name);
                ParentTV = itemView.findViewById(R.id.text_view_parent);
                RemoveBtnLayout = itemView.findViewById(R.id.delete_btn);
            }
        }
    }

    private void removeApplianceFromEvent(int position) {
        ApplianceInfo applianceInfo = mAddedAppliances.get(position);
        final String id = applianceInfo.getApplianceId();
        FirebaseDatabase.getInstance().getReference("Registered Users").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot userSnapshot : snapshot.getChildren()) {
                    if (userSnapshot.child("email").getValue().toString().equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = userSnapshot.child("username").getValue().toString();
                        final String Path = "Registered Users/" + mLoggedInUsername + "/Events/" + mSelectedEventName + "/applianceIds";
                        FirebaseDatabase.getInstance().getReference(Path).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                if (snapshot.getValue() != null) {
                                    String Ids = snapshot.getValue().toString();
                                    String[] arr = Ids.split(" ");
                                    List<String> eventAppIds = new ArrayList<>(Arrays.asList(arr));
                                    eventAppIds.remove(id);
                                    Ids = "";
                                    int i = 0;
                                    while (i < eventAppIds.size()) {
                                        if (Ids.length() == 0)
                                            Ids = eventAppIds.get(i) + " ";
                                        else
                                            Ids = Ids + eventAppIds.get(i) + " ";
                                        i++;
                                    }
                                    FirebaseDatabase.getInstance().getReference(Path).setValue(Ids);
                                    String[] count = Ids.split(" ");
                                    FirebaseDatabase.getInstance()
                                            .getReference("Registered Users/" + mLoggedInUsername + "/Events/" + mSelectedEventName + "/deviceCount")
                                            .setValue(count.length);

                                } else {
                                    System.out.println("null applianceIds, Activity : SelectedEventActivity Method : removeApplianceFromEvent");
                                }
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