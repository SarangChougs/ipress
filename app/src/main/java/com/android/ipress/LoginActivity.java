package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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
    TextView mSignUpTV, mForgotPasswordBtn;
    Button mLoginBtn;
    FirebaseAuth mAuth;
    FirebaseUser mUser;
    DatabaseReference mDatabaseRef;
    String mUID, mPassword;
    Dialog dialog;
    TextView mUsernameValidationTV;

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
                startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                finish();
            }
        }
        //dynamic validation
        mUsernameET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (!mUsernameET.getText().toString().trim().equals("") && !mPasswordET.getText().toString().trim().equals("")) {
                    mLoginBtn.setAlpha(1);
                    mLoginBtn.setClickable(true);
                } else {
                    mLoginBtn.setAlpha((float) 0.35);
                    mLoginBtn.setClickable(false);
                }
                if (s.toString().contains(".") ||
                        s.toString().contains(".") ||
                        s.toString().contains("#") ||
                        s.toString().contains("$") ||
                        s.toString().contains("[") ||
                        s.toString().contains("]")) {
                    mLoginBtn.setAlpha((float) 0.35);
                    mLoginBtn.setClickable(false);
                    StringBuilder builder = new StringBuilder();
                    builder.append("must not contain ");
                    if(s.toString().contains("."))
                        builder.append("'.' ");
                    if(s.toString().contains("#"))
                        builder.append("'#' ");
                    if(s.toString().contains("$"))
                        builder.append("'$' ");
                    if(s.toString().contains("["))
                        builder.append("'[' ");
                    if(s.toString().contains("]"))
                        builder.append("']' ");
                    mUsernameValidationTV.setText(builder.toString());
                }else{
                    mUsernameValidationTV.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        //dynamic validation
        mPasswordET.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String Username = mUsernameET.getText().toString().trim();
                if (!mUsernameET.getText().toString().trim().equals("") && !mPasswordET.getText().toString().trim().equals("")) {
                    mLoginBtn.setAlpha(1);
                    mLoginBtn.setClickable(true);
                } else {
                    mLoginBtn.setAlpha((float) 0.35);
                    mLoginBtn.setClickable(false);
                }
                if (Username.contains(".") ||
                        Username.contains("#") ||
                        Username.contains("$") ||
                        Username.contains("[") ||
                        Username.contains("]")) {
                    mLoginBtn.setAlpha((float) 0.35);
                    mLoginBtn.setClickable(false);
                    StringBuilder builder = new StringBuilder();
                    builder.append("must not contain ");
                    if(Username.contains("."))
                        builder.append("'.' ");
                    if(Username.contains("#"))
                        builder.append("'#' ");
                    if(Username.contains("$"))
                        builder.append("'$' ");
                    if(Username.contains("["))
                        builder.append("'[' ");
                    if(Username.contains("]"))
                        builder.append("']' ");
                    mUsernameValidationTV.setText(builder.toString());
                }else{
                    mUsernameValidationTV.setText("");
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
        mLoginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!mUsernameET.getText().toString().trim().equals("") && !mPasswordET.getText().toString().trim().equals(""))
                    loginUser();
                else
                    Toast.makeText(LoginActivity.this, "Some fields are empty", Toast.LENGTH_SHORT).show();
            }
        });

        mForgotPasswordBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setupForgotPasswordDialog();
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
        mUsernameValidationTV = findViewById(R.id.username_validation);
        mPasswordET = findViewById(R.id.PasswordET);
        mSignUpTV = findViewById(R.id.sign_up_btn);
        mLoginBtn = findViewById(R.id.LoginBtn);
        mForgotPasswordBtn = findViewById(R.id.ForgotPasswordBtn);
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
                                            startActivity(new Intent(LoginActivity.this, HomeActivity.class));
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

    public void setupForgotPasswordDialog() {
        dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.forgot_password_dialog);
        final EditText editText = dialog.findViewById(R.id.EmailField);
        final Button reset = dialog.findViewById(R.id.ResetBtn);
        editText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() == 0) {
                    reset.setClickable(false);
                    reset.setAlpha((float) 0.15);
                } else {
                    reset.setAlpha(1);
                    reset.setClickable(true);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final String EnteredEmail = editText.getText().toString().trim();
                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
                reference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        int flag = 0;
                        for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                            String email = dataSnapshot.child("email").getValue().toString();
                            if (email.equals(EnteredEmail)) {
                                flag = 1;
                                mAuth.sendPasswordResetEmail(EnteredEmail).addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            Toast.makeText(LoginActivity.this, "Reset link sent to email", Toast.LENGTH_SHORT).show();
                                            dialog.dismiss();
                                        } else {
                                            Toast.makeText(LoginActivity.this, "Error sending reset link", Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                            }
                        }
                        if (flag == 0) {
                            Toast.makeText(LoginActivity.this, "Account doesn't exist with this email", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            }
        });
        dialog.show();
    }
}