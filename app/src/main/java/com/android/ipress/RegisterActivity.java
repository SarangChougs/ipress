package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class RegisterActivity extends AppCompatActivity {

    EditText mFullNameET, mEmailET, mUsernameET, mPasswordET, mConfirmPasswordET;
    TextView mLoginTV;
    Button mRegisterBtn;
    FirebaseAuth mAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    boolean mBtnPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("RegisteredUsers");

        mFullNameET = findViewById(R.id.FullNameET);
        mEmailET = findViewById(R.id.EmailET);
        mUsernameET = findViewById(R.id.UsernameET);
        mPasswordET = findViewById(R.id.PasswordET);
        mConfirmPasswordET = findViewById(R.id.ConfirmPasswordET);
        mLoginTV = findViewById(R.id.login_btn);
        mRegisterBtn = findViewById(R.id.RegisterBtn);
        mRegisterBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mBtnPressed = true;
                //validation
                if ((mPasswordET.getText().toString().trim().equals(mConfirmPasswordET.getText().toString().trim())
                        && !mEmailET.getText().toString().trim().equals("")
                        && !mFullNameET.getText().toString().trim().equals("")
                        && mPasswordET.getText().toString().trim().length() >= 6))
                    checkUser();
                else {
                    if (mPasswordET.getText().toString().trim().length() < 6)
                        Toast.makeText(RegisterActivity.this, "Password too weak", Toast.LENGTH_SHORT).show();
                    if (!mPasswordET.getText().toString().trim().equals(mConfirmPasswordET.getText().toString().trim()))
                        Toast.makeText(RegisterActivity.this, "Password did not match", Toast.LENGTH_SHORT).show();
                    if (mEmailET.getText().toString().trim().equals(""))
                        Toast.makeText(RegisterActivity.this, "Email Required", Toast.LENGTH_SHORT).show();
                    if (mFullNameET.getText().toString().trim().equals(""))
                        Toast.makeText(RegisterActivity.this, "Full Name Required", Toast.LENGTH_SHORT).show();
                }
            }
        });
        mLoginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
        });
    }

    //to check username availability
    public void checkUser() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Registered Users").child(mUsernameET.getText().toString().toLowerCase());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("email").getValue() == null) {
                    //if username is available, register
                    registerUser();
                } else
                    //if username isn't available then show toast message.
                    Toast.makeText(RegisterActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    //method to register new user in the database
    public void registerUser() {
        final String fullName, email, username, password;
        fullName = mFullNameET.getText().toString().trim();
        email = mEmailET.getText().toString().trim();
        username = mUsernameET.getText().toString().trim().toLowerCase();
        password = mPasswordET.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            System.out.println("account creation successful");
                            //getting id for new user
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("counts/userId");
                            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.getValue() != null) {
                                        System.out.println("reading userid count successful");
                                        int id = Integer.parseInt(snapshot.getValue().toString());
                                        //incrementing for new user
                                        id++;
                                        final String uid = String.valueOf(id);
                                        //creating user info object and setting up new user
                                        UserDetails userInfo = new UserDetails(uid, fullName, email, username, "https://yt3.ggpht.com/a/AATXAJzvYsfy_gOdq3zN66TUhcx5XjxT36erB6BoNG5xoQ=s900-c-k-c0xffffffff-no-rj-mo");
                                        mDatabaseReference = mFirebaseDatabase.getReference("Registered Users");
                                        mDatabaseReference.child(username).setValue(userInfo).addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if (task.isSuccessful()) {
                                                    System.out.println("adding user info to database successful");
                                                    if (mAuth.getCurrentUser() != null)
                                                        mAuth.getCurrentUser().sendEmailVerification()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            mAuth.signOut();
                                                                            mBtnPressed = false;
                                                                            Toast.makeText(RegisterActivity.this, "Registration successful, please verify your account", Toast.LENGTH_SHORT).show();
                                                                            FirebaseDatabase.getInstance().getReference("counts/userId").setValue(uid);
                                                                            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                                                                            finish();
                                                                        }
                                                                    }
                                                                });
                                                } else {
                                                    Toast.makeText(RegisterActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                                                }
                                            }
                                        });
                                    }else{
                                        System.out.println("count value null");
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Toast.makeText(RegisterActivity.this, "Error fetching id data", Toast.LENGTH_SHORT).show();
                                }
                            });
                        } else {
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}