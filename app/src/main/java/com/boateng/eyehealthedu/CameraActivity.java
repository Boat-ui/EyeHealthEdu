package com.boateng.eyehealthedu;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class CameraActivity extends AppCompatActivity {

    private ImageView imageView;
    private static final int CAMERA_PERMISSION_CODE = 100;
    private static final int CAMERA_REQUEST_CODE = 101;
    private static final int GALLERY_REQUEST_CODE = 102;
    private File selectedImage = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_camera);

        imageView = findViewById(R.id.image_view);

        Button btnTakePhoto = findViewById(R.id.btn_take_photo);
        Button btnChoosePhoto = findViewById(R.id.btn_choose_photo);
        Button btnAnalyze = findViewById(R.id.btn_analyze);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                checkCameraPermission();
            }
        });

        btnChoosePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();
            }
        });

        btnAnalyze.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (selectedImage != null && selectedImage.exists()) {
                    Bitmap bitmap = BitmapFactory.decodeFile(selectedImage.getAbsolutePath());

                    if (bitmap != null) {
                        ProgressDialog progress = new ProgressDialog(CameraActivity.this);
                        progress.setMessage("Running educational AI analysis...");
                        progress.setCancelable(false);
                        progress.show();

                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                                EyePatternAnalyzer analyzer = new EyePatternAnalyzer();
                                EyePatternAnalyzer.PatternCategory result = analyzer.analyzeEyePattern(bitmap);
                                Bitmap heatmap = analyzer.createHeatmap(bitmap);
                                String explanation = analyzer.getEducationalExplanation(result);

                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        progress.dismiss();
                                        showAnalysisResults(result, explanation, heatmap);
                                    }
                                });
                            }
                        }).start();
                    } else {
                        Toast.makeText(CameraActivity.this,
                                "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(CameraActivity.this,
                            "Please select an image first", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                == PackageManager.PERMISSION_GRANTED) {
            openCamera();
        } else {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_CODE);
        }
    }

    private void openCamera() {
        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (cameraIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE);
        } else {
            Toast.makeText(this, "No camera app found", Toast.LENGTH_SHORT).show();
        }
    }

    private void openGallery() {
        Intent galleryIntent = new Intent(Intent.ACTION_PICK);
        galleryIntent.setType("image/*");
        startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {
            if (requestCode == CAMERA_REQUEST_CODE && data != null) {
                Bundle extras = data.getExtras();
                if (extras != null) {
                    Bitmap imageBitmap = (Bitmap) extras.get("data");
                    imageView.setImageBitmap(imageBitmap);
                    saveImage(imageBitmap);
                    Toast.makeText(this, "Photo saved", Toast.LENGTH_SHORT).show();
                }
            } else if (requestCode == GALLERY_REQUEST_CODE && data != null) {
                Uri imageUri = data.getData();
                if (imageUri != null) {
                    try {
                        Bitmap bitmap = BitmapFactory.decodeStream(
                                getContentResolver().openInputStream(imageUri)
                        );
                        imageView.setImageBitmap(bitmap);
                        saveImage(bitmap);
                        Toast.makeText(this, "Image loaded", Toast.LENGTH_SHORT).show();
                    } catch (IOException e) {
                        e.printStackTrace();
                        Toast.makeText(this, "Failed to load image", Toast.LENGTH_SHORT).show();
                    }
                }
            }
        }
    }

    private void saveImage(Bitmap bitmap) {
        try {
            String filename = "eye_photo_" + System.currentTimeMillis() + ".jpg";
            selectedImage = new File(getFilesDir(), filename);
            FileOutputStream fos = new FileOutputStream(selectedImage);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
            fos.close();

            // Debug: Show saved path
            Toast.makeText(this, "Saved to: " + selectedImage.getAbsolutePath(),
                    Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Failed to save image", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAnalysisResults(EyePatternAnalyzer.PatternCategory category,
                                     String explanation, Bitmap heatmap) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("🧠 EDUCATIONAL AI ANALYSIS");

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_ai_analysis, null);
        builder.setView(dialogView);

        TextView tvCategory = dialogView.findViewById(R.id.tv_category);
        TextView tvExplanation = dialogView.findViewById(R.id.tv_explanation);
        ImageView ivHeatmap = dialogView.findViewById(R.id.iv_heatmap);
        TextView tvDisclaimer = dialogView.findViewById(R.id.tv_disclaimer);

        tvCategory.setText("Pattern Detected: " + category.getDescription());
        tvExplanation.setText(explanation);
        ivHeatmap.setImageBitmap(heatmap);
        tvDisclaimer.setText("⚠️ EDUCATIONAL DEMO ONLY\n" +
                "This AI simulation demonstrates pattern recognition.\n" +
                "It does NOT provide medical diagnosis.\n" +
                "Always consult healthcare professionals.");

        builder.setPositiveButton("OK", null);
        builder.setNegativeButton("Learn More", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("https://www.aao.org/eye-health"));
                startActivity(intent);
            }
        });

        builder.show();
    }
}