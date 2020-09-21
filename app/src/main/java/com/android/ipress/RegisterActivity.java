package com.android.ipress;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.model.Document;

import java.util.HashMap;
import java.util.Map;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";
    EditText mFullNameET,mEmailET, mUsernameET, mPasswordET, mConfirmPasswordET;
    TextView mLoginTV;
    Button mRegisterBtn;
    FirebaseAuth mAuth;
    FirebaseDatabase mFirebaseDatabase;
    DatabaseReference mDatabaseReference;
    boolean mBtnPressed;
    FirebaseFirestore  mStoreRef;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);
        mAuth = FirebaseAuth.getInstance();
        mFirebaseDatabase = FirebaseDatabase.getInstance();
        mDatabaseReference = mFirebaseDatabase.getReference("RegisteredUsers");
        mStoreRef = FirebaseFirestore.getInstance();

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
                        && mPasswordET.getText().toString().trim().length() >= 6) || true)
                    checkUser();
                else {
                    if(mPasswordET.getText().toString().trim().length() < 6)
                        Toast.makeText(RegisterActivity.this, "Password too weak", Toast.LENGTH_SHORT).show();
                    if(!mPasswordET.getText().toString().trim().equals(mConfirmPasswordET.getText().toString().trim()))
                        Toast.makeText(RegisterActivity.this, "Password did not match", Toast.LENGTH_SHORT).show();
                    if(mEmailET.getText().toString().trim().equals(""))
                        Toast.makeText(RegisterActivity.this, "Email Required", Toast.LENGTH_SHORT).show();
                    if(mFullNameET.getText().toString().trim().equals(""))
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
    public void checkUser(){
        final String EnteredUsername = mUsernameET.getText().toString().trim().toLowerCase();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("username",EnteredUsername)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if(task.isSuccessful()){
                            QuerySnapshot querySnapshot = task.getResult();
                            if(querySnapshot != null && querySnapshot.isEmpty())
                                registerUser();
                            else{
                                Toast.makeText(RegisterActivity.this, "Username already taken", Toast.LENGTH_SHORT).show();
                            }
                        }else{
                            Log.d(TAG, "check user task unsuccessful : " + task.getException().getMessage());
                        }
                    }
                });
    }

    //register new user in the database
    public void registerUser() {
        final String fullName = "Peregrin Took",email = "peregrintook@gmail.com",username = "pippin",password = "12345678";
//        fullName = mFullNameET.getText().toString().trim();
//        email = mEmailET.getText().toString().trim();
//        username = mUsernameET.getText().toString().trim().toLowerCase();
//        password = mPasswordET.getText().toString().trim();

        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            db.collection("ids").document("userids").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                        @Override
                                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                            if(task.isSuccessful()){
                                                FirebaseFirestore ref = FirebaseFirestore.getInstance();
                                                DocumentSnapshot document = task.getResult();
                                                if(document != null && document.get("count") != null){
                                                    Object o =  document.get("count");
                                                    long userid = (long) o;
                                                    userid++;
                                                    Map<String,Object> map = new HashMap<>();
                                                    map.put("count",userid);
                                                    ref.collection("ids").document("userids").set(map);
                                                    String id = String.valueOf(userid);
                                                    String picUrl = "https://yt3.ggpht.com/a/AATXAJzvYsfy_gOdq3zN66TUhcx5XjxT36erB6BoNG5xoQ=s900-c-k-c0xffffffff-no-rj-mo";
                                                    UserDetails user = new UserDetails(id,fullName,email,username,picUrl);
                                                    ref.collection("users").document(id).set(user);
                                                    Toast.makeText(RegisterActivity.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                                                }else{
                                                    Log.d(TAG, "returned null document result ");
                                                }
                                            }else{
                                                Log.d(TAG, "register user task unsuccessful : " + task.getException().getMessage());
                                            }
                                        }
                                    });
                        } else {
                            Toast.makeText(RegisterActivity.this, task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    }
                });
    }
}