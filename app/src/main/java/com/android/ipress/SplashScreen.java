package com.android.ipress;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.Image;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class SplashScreen extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        GlobalClass.setMapping();

        View top = findViewById(R.id.top_line), bottom = findViewById(R.id.bottom_line);
        ImageView imageView = findViewById(R.id.logo);
        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.splash_rotator);
        Animation topA = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.top_line_anim);
        Animation bottomA = AnimationUtils.loadAnimation(getApplicationContext(),R.anim.bottom_line_anim);
        top.startAnimation(topA);
        bottom.startAnimation(bottomA);
        imageView.startAnimation(animation);

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                startActivity(new Intent(getApplicationContext(),LoginActivity.class));
                finish();
            }
        },2000);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);

    }
}