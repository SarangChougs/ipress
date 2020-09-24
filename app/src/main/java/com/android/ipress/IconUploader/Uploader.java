package com.android.ipress.IconUploader;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.TextureView;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.android.ipress.ProfileActivity;
import com.android.ipress.R;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import org.w3c.dom.Text;

import java.io.File;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;

public class Uploader extends AppCompatActivity {

    Button button;
    ImageView Image;
    Uri mImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_uploader);

        button = findViewById(R.id.Choose);
        Image = findViewById(R.id.selected_image);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (button.getText().toString().toLowerCase().equals("choose icon")) {
                    CropImage.activity()
                            .setGuidelines(CropImageView.Guidelines.ON)
                            .start(Uploader.this);
                } else if (button.getText().toString().toLowerCase().equals("upload")) {
                    sendFile();
                }
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE && resultCode == RESULT_OK) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            mImageUri = result.getUri();
            Picasso.with(getApplicationContext())
                    .load(mImageUri)
                    .placeholder(R.drawable.user_vector)
                    .into(Image);
            button.setText("Upload");
            displayDimen();
        }
    }

    private void sendFile() {
        final Dialog dialog = new Dialog(Uploader.this);
        dialog.setContentView(R.layout.progress_bar_dialog);
        dialog.show();
        final ProgressBar progressBar = dialog.findViewById(R.id.progress_bar);
        final TextView textView = dialog.findViewById(R.id.progress_text);
        if (mImageUri == null) {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        } else {
            final String FileName = "uploaded_icon_" + System.currentTimeMillis() + "." + getFileExtension(mImageUri);
            FirebaseStorage.getInstance()
                    .getReference("icons")
                    .child(FileName)
                    .putFile(mImageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            taskSnapshot.getMetadata().getReference().getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    HashMap<String, String> map = new HashMap<>();
                                    map.put("iconUrl", uri.toString());
                                    FirebaseDatabase.getInstance()
                                            .getReference("icons")
                                            .child("" + System.currentTimeMillis())
                                            .setValue(map).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            button.setText("Choose Icon");
                                        }
                                    });
                                }
                            });
                            textView.setText("Done.");
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    dialog.dismiss();
                                }
                            }, 2000);
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            progressBar.setProgress((int) progress);
                            DecimalFormat decimalFormat = new DecimalFormat("#.##");
                            decimalFormat.setRoundingMode(RoundingMode.UP);
                            textView.setText(decimalFormat.format(progress) + " %");
                        }
                    });
        }
    }

    private String getFileExtension(Uri uri) {
        ContentResolver cR = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cR.getType(uri));
    }

    private void displayDimen() {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(new File(mImageUri.getPath()).getAbsolutePath(), options);
        int imageHeight = options.outHeight;
        int imageWidth = options.outWidth;

        DisplayMetrics metrics = getResources().getDisplayMetrics();
        int mf = ((int) metrics.density * 160);

        int hdp = (imageHeight * 160) / mf;
        int wdp = (imageWidth * 160) / mf;

        String Text = "" + hdp + " x " + wdp  + " cropped image (Units - dp)";

        TextView textView = findViewById(R.id.dimen);
        textView.setText(Text);
    }
}