package com.android.ipress;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class DashboardActivity extends AppCompatActivity {

    String mLoggedInUsername;
    String mButtonText;
    int State;
    Button Appliance1;
    String mSetterValue;
    boolean mButtonPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);

        LinearLayout Logout = findViewById(R.id.LogoutBtn);
        Logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        });

        Appliance1 = findViewById(R.id.Appliance1);
        Appliance1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Appliance1.setAlpha((float) 0.45);
                mButtonText = Appliance1.getText().toString().toUpperCase();
                if(mButtonText.equals("ON")){
                    State = 1;
                    mSetterValue = "OFF";
                }else{
                    State = 0;
                    mSetterValue = "ON";
                }
                getCurrentUserName();
            }
        });
    }

    public void getCurrentUserName(){
        mButtonPressed = true;
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mLoggedInUsername = dataSnapshot.child("username").getValue().toString();
                        if(mButtonPressed == true)
                            ModifyApplianceState();
                        break;
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void ModifyApplianceState(){
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users/" + mLoggedInUsername + "/Appliances");
        ApplianceInfo applianceInfo = new ApplianceInfo(State);
        TextView applianceLbl = findViewById(R.id.Appliance1Lbl);
        reference.child(applianceLbl.getText().toString()).setValue(applianceInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Appliance1.setAlpha(1);
                    Appliance1.setText(mSetterValue);
                    mButtonPressed = false;
                }else{
                    Toast.makeText(DashboardActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                    Appliance1.setAlpha(1);
                }
            }
        });

    }
}