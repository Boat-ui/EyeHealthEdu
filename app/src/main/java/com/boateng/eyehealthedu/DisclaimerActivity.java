package com.boateng.eyehealthedu;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;

public class DisclaimerActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_disclaimer);

        TextView disclaimerText = findViewById(R.id.disclaimer_text);
        Button acceptButton = findViewById(R.id.accept_button);
        Button rejectButton = findViewById(R.id.reject_button);

        // Set disclaimer text
        disclaimerText.setText(String.format("%sThis app is for EDUCATIONAL PURPOSES ONLY.\n\nIt demonstrates how pattern recognition might work.\n\nIt DOES NOT provide medical diagnosis.\n\nAlways consult qualified healthcare professionals.", getString(R.string.important_educational_tool_only)));

        // Accept button
        acceptButton.setOnClickListener(v -> {
            Intent intent = new Intent(DisclaimerActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        });

        // Reject button
        rejectButton.setOnClickListener(v -> finish());
    }
}