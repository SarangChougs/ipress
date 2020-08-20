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
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    EditText mEmailET, mPasswordET;
    TextView mSignUpTV;
    Button mLoginBtn;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        setupActivityVariables();

        if (mUser != null) {
            if(mUser.isEmailVerified()){
                GlobalClass.CurrentUserEmail = mUser.getEmail();
                startActivity(new Intent(getApplicationContext(), DashboardActivity.class));
                finish();
            }
        }
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mEmailET.getText().toString().trim().equals("") && !mPasswordET.getText().toString().trim().equals(""))
                    loginUser();
                else
                    Toast.makeText(LoginActivity.this, "Some fields are empty", Toast.LENGTH_SHORT).show();
            }
        });
        mSignUpTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getApplicationContext(), RegisterActivity.class));
            }
        });
    }

    public void setupActivityVariables() {
        mEmailET = findViewById(R.id.EmailET);
        mPasswordET = findViewById(R.id.PasswordET);
        mSignUpTV = findViewById(R.id.sign_up_btn);
        mLoginBtn = findViewById(R.id.LoginBtn);
        mAuth = FirebaseAuth.getInstance();
        mUser = mAuth.getCurrentUser();
    }

    public void loginUser() {
        String email, password;
        email = mEmailET.getText().toString().trim();
        password = mPasswordET.getText().toString().trim();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            mAuth = FirebaseAuth.getInstance();
                            mUser = mAuth.getCurrentUser();
                            if(mUser.isEmailVerified()){
                                GlobalClass.CurrentUserEmail = mUser.getEmail();
                                startActivity(new Intent(LoginActivity.this, DashboardActivity.class));
                                finish();
                            }else{
                                Toast.makeText(LoginActivity.this, "Please verify your account", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}