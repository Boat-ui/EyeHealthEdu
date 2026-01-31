package com.boateng.eyehealthedu;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        TextView miniDisclaimer = findViewById(R.id.mini_disclaimer);
        miniDisclaimer.setText("Educational Tool Only • Not Medical Advice");

        Button btnCamera = findViewById(R.id.btn_camera);
        Button btnLearn = findViewById(R.id.btn_learn);
        Button btnDoctors = findViewById(R.id.btn_doctors);

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, CameraActivity.class);
                startActivity(intent);
            }
        });

        btnLearn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open educational website about eye health
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.nei.nih.gov/learn-about-eye-health"));
                startActivity(intent);
            }
        });

        btnDoctors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Open Google Maps to find eye doctors
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.google.com/maps/search/ophthalmologist+near+me"));
                startActivity(intent);
            }
        });
    }
}