package com.example.focusguardian.view;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.focusguardian.R;
import com.example.focusguardian.service.AppMonitorService;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.label.ImageLabel;
import com.google.mlkit.vision.label.ImageLabeler;
import com.google.mlkit.vision.label.ImageLabeling;
import com.google.mlkit.vision.label.defaults.ImageLabelerOptions;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ProofUploadActivity extends AppCompatActivity {

    private static final int REQ_IMAGE_CAPTURE = 1001;
    private static final int REQ_CAMERA_PERMISSION = 1002;

    private ImageView imgPreview;
    private Button btnTake, btnSubmit, btnSkip;
    private TextView tvInstructions, tvAnalysisResult;
    private ProgressBar progressBar;

    private Bitmap lastBitmap;
    private ImageLabeler labeler;
    private SharedPreferences prefs;

    // Focus keywords
    private static final List<String> CODING_KEYWORDS = Arrays.asList(
            "computer", "laptop", "keyboard", "monitor", "screen", "desk",
            "electronics", "technology", "code", "programming"
    );
    private static final List<String> STUDYING_KEYWORDS = Arrays.asList(
            "book", "notebook", "paper", "desk", "writing", "pen",
            "study", "library", "table", "text"
    );
    private static final List<String> COOKING_KEYWORDS = Arrays.asList(
            "food", "kitchen", "cooking", "plate", "dish", "meal",
            "ingredient", "utensil", "stove", "recipe"
    );
    private static final List<String> TRAINING_KEYWORDS = Arrays.asList(
            "person", "gym", "fitness", "exercise", "sport", "training",
            "equipment", "workout", "athletic", "health"
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_proof_upload);

        prefs = getSharedPreferences("fg_prefs", MODE_PRIVATE);

        imgPreview = findViewById(R.id.imgPreview);
        btnTake = findViewById(R.id.btnTakePhoto);
        btnSubmit = findViewById(R.id.btnSubmitProof);
        btnSkip = findViewById(R.id.btnSkip);
        tvInstructions = findViewById(R.id.tvInstructions);
        tvAnalysisResult = findViewById(R.id.tvAnalysisResult);
        progressBar = findViewById(R.id.progressBar);

        btnSubmit.setEnabled(false);

        // ML Kit labeler
        ImageLabelerOptions options = new ImageLabelerOptions.Builder()
                .setConfidenceThreshold(0.6f)
                .build();
        labeler = ImageLabeling.getClient(options);

        btnTake.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        REQ_CAMERA_PERMISSION);
            } else {
                openCamera();
            }
        });

        btnSubmit.setOnClickListener(v -> {
            if (lastBitmap != null) {
                endFocusSession("Great job! Session completed with proof! 🎉");
            } else {
                Toast.makeText(this, "Please take a photo as proof.", Toast.LENGTH_SHORT).show();
            }
        });

        btnSkip.setOnClickListener(v -> endFocusSession("Session ended. Keep it up! 💪"));
    }

    private void openCamera() {
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takeIntent.resolveActivity(getPackageManager()) != null) {
            startActivityForResult(takeIntent, REQ_IMAGE_CAPTURE);
        } else {
            Toast.makeText(this, "No camera available", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            } else {
                Toast.makeText(this, "Camera permission is required.", Toast.LENGTH_LONG).show();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQ_IMAGE_CAPTURE && resultCode == RESULT_OK && data != null) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            if (bitmap != null) {
                lastBitmap = bitmap;
                imgPreview.setImageBitmap(bitmap);
                imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                analyzeImage(bitmap);
            }
        }
    }

    private void analyzeImage(Bitmap bitmap) {
        progressBar.setVisibility(ProgressBar.VISIBLE);
        tvAnalysisResult.setText("Analyzing image...");
        btnSubmit.setEnabled(false);

        InputImage image = InputImage.fromBitmap(bitmap, 0);
        labeler.process(image)
                .addOnSuccessListener(this::handleLabels)
                .addOnFailureListener(e -> {
                    progressBar.setVisibility(ProgressBar.GONE);
                    tvAnalysisResult.setText("❌ Analysis failed. You can still submit.");
                    tvAnalysisResult.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
                    btnSubmit.setEnabled(true);
                    Toast.makeText(this, "Image analysis failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void handleLabels(List<ImageLabel> labels) {
        progressBar.setVisibility(ProgressBar.GONE);

        if (labels.isEmpty()) {
            tvAnalysisResult.setText("❌ No objects detected. Please try again.");
            tvAnalysisResult.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            btnSubmit.setEnabled(false);
            return;
        }

        String focusType = prefs.getString("focus_type", "coding");
        boolean isValid = verifyFocusContext(focusType, labels);

        StringBuilder detectedText = new StringBuilder();
        for (ImageLabel label : labels) {
            detectedText.append(label.getText())
                    .append(" (")
                    .append(Math.round(label.getConfidence() * 100))
                    .append("%), ");
        }

        if (isValid) {
            tvAnalysisResult.setText("✅ Valid proof detected for: " + focusType);
            tvAnalysisResult.setTextColor(getResources().getColor(android.R.color.holo_green_light));
            btnSubmit.setEnabled(true);
            Toast.makeText(this, "Photo verified successfully! 🎉", Toast.LENGTH_LONG).show();
        } else {
            tvAnalysisResult.setText("❌ Photo doesn't match your focus activity (" + focusType + ")\nDetected: " + detectedText);
            tvAnalysisResult.setTextColor(getResources().getColor(android.R.color.holo_red_light));
            btnSubmit.setEnabled(false);
            Toast.makeText(this, "Please take a photo of your " + focusType + " activity", Toast.LENGTH_LONG).show();
        }
    }

    private boolean verifyFocusContext(String focusType, List<ImageLabel> labels) {
        List<String> requiredKeywords;

        switch (focusType.toLowerCase()) {
            case "coding": requiredKeywords = CODING_KEYWORDS; break;
            case "studying": requiredKeywords = STUDYING_KEYWORDS; break;
            case "cooking": requiredKeywords = COOKING_KEYWORDS; break;
            case "training": requiredKeywords = TRAINING_KEYWORDS; break;
            default: return false;
        }

        int matchCount = 0;
        for (ImageLabel label : labels) {
            String detected = label.getText().toLowerCase();
            for (String keyword : requiredKeywords) {
                if (detected.contains(keyword)) {
                    matchCount++;
                    break;
                }
            }
        }

        return matchCount >= 2; // require at least 2 keywords to match
    }

    private void endFocusSession(String message) {
        stopService(new Intent(this, AppMonitorService.class));
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (labeler != null) labeler.close();
    }

    private String bitmapToBase64(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 70, baos);
        return Base64.encodeToString(baos.toByteArray(), Base64.DEFAULT);
    }
}
