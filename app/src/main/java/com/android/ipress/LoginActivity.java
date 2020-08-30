package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class LoginActivity extends AppCompatActivity {

    EditText mUsernameET, mPasswordET;
    TextView mSignUpTV;
    Button mLoginBtn;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mDatabaseRef;
    String mUID, mPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupActivityVariables();

        checkRememberMe();

        //to check if any user is already logged in
        if (mUser != null) {
            if (mUser.isEmailVerified()) {
                GlobalClass.CurrentUserEmail = mUser.getEmail();
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                finish();
            }
        }
        //login method
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mUsernameET.getText().toString().trim().equals("") && !mPasswordET.getText().toString().trim().equals(""))
                    loginUser();
                else
                    Toast.makeText(LoginActivity.this, "Some fields are empty", Toast.LENGTH_SHORT).show();
            }
        });
        //register new user
        mSignUpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });
    }

    public void setupActivityVariables() {
        mUsernameET = findViewById(R.id.UsernameET);
        mPasswordET = findViewById(R.id.PasswordET);
        mSignUpTV = findViewById(R.id.sign_up_btn);
        mLoginBtn = findViewById(R.id.LoginBtn);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    public void checkRememberMe() {
        SharedPreferences rememberMePreferences = getSharedPreferences("RememberMe", Context.MODE_PRIVATE);
        String uid = rememberMePreferences.getString("username", null);
        String password = rememberMePreferences.getString("Password", null);

        if (uid != null && password != null) {
            mUsernameET.setText(uid);
            mPasswordET.setText(password);
        }
    }

    public void loginUser() {
        String username;
        final String password;
        username = mUsernameET.getText().toString().trim();
        password = mPasswordET.getText().toString().trim();
        mDatabaseRef = FirebaseDatabase.getInstance().getReference("Registered Users").child(username);
        mDatabaseRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child("email").getValue() != null) {
                    String email = snapshot.child("email").getValue().toString();
                    mAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        mAuth = FirebaseAuth.getInstance();
                                        mUser = mAuth.getCurrentUser();
                                        if (mUser.isEmailVerified()) {
                                            GlobalClass.CurrentUserEmail = mUser.getEmail();
                                            EditRememberMePreference();
                                            startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                            finish();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Please verify your account", Toast.LENGTH_SHORT).show();
                                        }
                                    } else {
                                        Toast.makeText(LoginActivity.this, "Password did not match", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                } else {
                    Toast.makeText(LoginActivity.this, "Username does not exist", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void EditRememberMePreference() {
        CheckBox checkBox = findViewById(R.id.RememberMe);
        SharedPreferences sharedpreferences;
        SharedPreferences.Editor editor;
        if (checkBox.isChecked()) {
            mUID = mUsernameET.getText().toString().trim();
            mPassword = mPasswordET.getText().toString().trim();
        } else {
            mUID = "";
            mPassword = "";
        }
        sharedpreferences = getSharedPreferences("RememberMe", Context.MODE_PRIVATE); // saving credentials for next time login
        editor = sharedpreferences.edit();
        editor.putString("username", mUID);
        editor.putString("Password", mPassword);
        editor.commit();
    }
}