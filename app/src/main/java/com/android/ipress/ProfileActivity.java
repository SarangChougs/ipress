package com.android.ipress;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ProfileActivity extends AppCompatActivity {

    String mUsername, mEmail;
    String TAG = "ProfileActivity";
    TextView NameTV, EmailTV, UsernameTV;
    Button mEditPic, mChooseAgain;
    Uri mImageUri;
    ImageView ProfilePic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ProfilePic = findViewById(R.id.ProfilePic);
        setupBottomNavBar();
        displayUserInfo();
        editPicBtn();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                mImageUri = result.getUri();
                Picasso.with(getApplicationContext())
                        .load(mImageUri)
                        .placeholder(R.drawable.user_vector)
                        .into(ProfilePic);
                mEditPic.setText("Update");
                mEditPic.setClickable(true);
                mChooseAgain.setVisibility(View.VISIBLE);
            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Toast.makeText(this, "Failed to select image Error : " + error , Toast.LENGTH_SHORT).show();
                mEditPic.setClickable(false);
            }
        } else{
            Toast.makeText(this, "Failed to select image", Toast.LENGTH_SHORT).show();
            mEditPic.setClickable(false);
        }
    }

    //method to display user info
    public void displayUserInfo() {
        NameTV = findViewById(R.id.name);
        EmailTV = findViewById(R.id.email);
        UsernameTV = findViewById(R.id.username);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    String email = dataSnapshot.child("email").getValue().toString();
                    if (email.equals(GlobalClass.CurrentUserEmail)) {
                        mUsername = dataSnapshot.child("username").getValue().toString();
                        DatabaseReference reference1 = FirebaseDatabase.getInstance().getReference("Registered Users/" + mUsername);
                        reference1.addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Log.d(TAG, "onDataChange: " + snapshot);
                                String name = snapshot.child("fullName").getValue().toString();
                                String FileUrl = snapshot.child("picUrl").getValue().toString();
                                NameTV.setText(name);
                                EmailTV.setText(GlobalClass.CurrentUserEmail);
                                UsernameTV.setText(mUsername);
                                Picasso.with(getApplicationContext())
                                        .load(FileUrl)
                                        .placeholder(R.drawable.user_vector)
                                        .into(ProfilePic);
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

    //method to set click listener for picture button
    public void editPicBtn() {
        mEditPic = findViewById(R.id.EditPicBtn);
        mChooseAgain = findViewById(R.id.ChooseAgainBtn);
        mEditPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mEditPic.getText().toString().equals("Choose picture")) {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(ProfileActivity.this);
                } else if (mEditPic.getText().toString().equals("Update")) {
                    mEditPic.setText("Updating...");
                    mEditPic.setClickable(false);
                    updateProfilePic();
                }
            }
        });
        mChooseAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(ProfileActivity.this);
            }
        });
    }

    //method to update selected picture
    public void updateProfilePic() {
        mChooseAgain.setVisibility(View.GONE);
        mUsername = UsernameTV.getText().toString();
        String FileName = mUsername + "_profile_pic." + getFileExtension(mImageUri);
        StorageReference storageReference = FirebaseStorage.getInstance().getReference("profile_pictures/" + mUsername + "/" + FileName);
        storageReference.putFile(mImageUri)
                .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                        taskSnapshot.getMetadata().getReference().getDownloadUrl()
                                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                                    @Override
                                    public void onSuccess(Uri uri) {
                                        String FileUrl = uri.toString();
                                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Registered Users/" + mUsername + "/picUrl");
                                        reference.setValue(FileUrl)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            mEditPic.setText("Choose picture");
                                                            mEditPic.setClickable(true);
                                                            mChooseAgain.setVisibility(View.GONE);
                                                        } else {
                                                            Toast.makeText(ProfileActivity.this, "Failed to update profile picture", Toast.LENGTH_SHORT).show();
                                                            mEditPic.setText("Update");
                                                            mEditPic.setClickable(true);
                                                        }
                                                    }
                                                });
                                    }
                                });
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(ProfileActivity.this, "Invalid File selected", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    //method to get file extension
    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    //method to set up bottom nav bar
    public void setupBottomNavBar() {
        BottomNavigationView bottomNavigationView;
        bottomNavigationView = findViewById(R.id.nav_bar);
        bottomNavigationView.setSelectedItemId(R.id.settings);
        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.home:
                        ActivityStack.push("Settings");
                        startActivity(new Intent(getApplicationContext(), HomeActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.automation:
                        ActivityStack.push("Settings");
                        startActivity(new Intent(getApplicationContext(), AutomationActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.favourite:
                        ActivityStack.push("Settings");
                        startActivity(new Intent(getApplicationContext(), FavouritesActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                    case R.id.settings:
                        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
                        overridePendingTransition(0, 0);
                        return true;
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        //goto parent activity
        startActivity(new Intent(getApplicationContext(), SettingsActivity.class));
        overridePendingTransition(0, 0);
        finish();
    }

    public void backClick(View view) {
        onBackPressed();
    }
}