package com.example.focusguardian.view;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;

import android.Manifest;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.focusguardian.R;
import com.example.focusguardian.service.AppMonitorService;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ProofUploadActivity extends AppCompatActivity {

    private static final int REQ_CAMERA_PERMISSION = 1002;
    private static final String TAG = "ProofUploadActivity";

    private ImageView imgPreview, statusIcon;
    private Button btnTake, btnSubmit, btnSkip;
    private TextView tvInstructions, tvAnalysisResult, tvAnalysisStatus, tvAnalysisDetail;
    private RelativeLayout analysisContainer;
    private ProgressBar circularProgress;

    private Bitmap lastBitmap;
    private SharedPreferences prefs;
    private ActivityResultLauncher<Intent> cameraLauncher;
    private File photoFile;

    private static final String OCR_API_KEY = "K88433312288957";

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
        tvAnalysisStatus = findViewById(R.id.tvAnalysisStatus);
        tvAnalysisDetail = findViewById(R.id.tvAnalysisDetail);
        analysisContainer = findViewById(R.id.analysisContainer);
        circularProgress = findViewById(R.id.circularProgress);
        statusIcon = findViewById(R.id.statusIcon);

        btnSubmit.setEnabled(false);

        cameraLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == RESULT_OK) {
                        if (photoFile != null && photoFile.exists()) {
                            Bitmap bitmap = BitmapFactory.decodeFile(photoFile.getAbsolutePath());
                            if (bitmap != null) {
                                lastBitmap = bitmap;
                                imgPreview.setImageBitmap(bitmap);
                                imgPreview.setScaleType(ImageView.ScaleType.CENTER_CROP);
                                analyzeImageWithOCR(bitmap);
                            }
                        }
                    }
                }
        );

        btnTake.setOnClickListener(v -> checkCameraPermission());
        btnSubmit.setOnClickListener(v -> submitProof());
        btnSkip.setOnClickListener(v -> endFocusSession("Session ended. Keep it up! 💪"));
    }

    private void checkCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    REQ_CAMERA_PERMISSION);
        } else {
            openCamera();
        }
    }

    private void openCamera() {
        Intent takeIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takeIntent.resolveActivity(getPackageManager()) != null) {
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                Toast.makeText(this, "Error creating image file", Toast.LENGTH_SHORT).show();
                return;
            }

            if (photoFile != null) {
                Uri photoUri = androidx.core.content.FileProvider.getUriForFile(
                        this,
                        getPackageName() + ".provider",
                        photoFile);
                takeIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                cameraLauncher.launch(takeIntent);
            }
        } else {
            Toast.makeText(this, "No camera available", Toast.LENGTH_SHORT).show();
        }
    }

    private File createImageFile() throws IOException {
        String fileName = "proof_" + System.currentTimeMillis();
        File storageDir = new File(getCacheDir(), "images");
        if (!storageDir.exists()) storageDir.mkdirs();
        return File.createTempFile(fileName, ".jpg", storageDir);
    }

    private void submitProof() {
        if (lastBitmap != null) {
            endFocusSession("✅ Great job! Proof accepted.");
        } else {
            Toast.makeText(this, "Please take a photo before submitting.", Toast.LENGTH_SHORT).show();
        }
    }

    private void showAnalysisUI() {
        // Hide image preview
        imgPreview.setVisibility(View.GONE);
        tvAnalysisResult.setVisibility(View.GONE);

        // Show analysis container
        analysisContainer.setVisibility(View.VISIBLE);
        circularProgress.setVisibility(View.VISIBLE);
        statusIcon.setVisibility(View.GONE);

        // Set analyzing text
        tvAnalysisStatus.setText("Analyzing");
        tvAnalysisDetail.setText("Processing image...");

        // Start rotation animation
        Animation rotation = AnimationUtils.loadAnimation(this, R.anim.rotate_circular);
        circularProgress.startAnimation(rotation);
    }

    private void showSuccessUI(String message, String detail) {
        // Stop progress animation
        circularProgress.clearAnimation();
        circularProgress.setVisibility(View.GONE);

        // Show success icon
        statusIcon.setImageResource(R.drawable.ic_verified);
        statusIcon.setVisibility(View.VISIBLE);

        // Animate icon
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_success);
        statusIcon.startAnimation(scaleAnim);

        // Update text
        tvAnalysisStatus.setText("Verified!");
        tvAnalysisStatus.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        tvAnalysisDetail.setText(message);

        // Hide after 2 seconds and show result
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            analysisContainer.setVisibility(View.GONE);
            imgPreview.setVisibility(View.VISIBLE);
            tvAnalysisResult.setVisibility(View.VISIBLE);
            tvAnalysisResult.setText("✅ " + detail);
            tvAnalysisResult.setTextColor(getResources().getColor(android.R.color.holo_green_light));
        }, 2000);
    }

    private void showWarningUI(String message, String detail) {
        // Stop progress animation
        circularProgress.clearAnimation();
        circularProgress.setVisibility(View.GONE);

        // Show warning icon
        statusIcon.setImageResource(R.drawable.ic_warning);
        statusIcon.setVisibility(View.VISIBLE);

        // Animate icon
        Animation scaleAnim = AnimationUtils.loadAnimation(this, R.anim.scale_success);
        statusIcon.startAnimation(scaleAnim);

        // Update text
        tvAnalysisStatus.setText("Unclear");
        tvAnalysisStatus.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        tvAnalysisDetail.setText(message);

        // Hide after 2 seconds and show result
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            analysisContainer.setVisibility(View.GONE);
            imgPreview.setVisibility(View.VISIBLE);
            tvAnalysisResult.setVisibility(View.VISIBLE);
            tvAnalysisResult.setText("⚠️ " + detail);
            tvAnalysisResult.setTextColor(getResources().getColor(android.R.color.holo_orange_light));
        }, 2000);
    }

    private void analyzeImageWithOCR(Bitmap bitmap) {
        showAnalysisUI();
        btnSubmit.setEnabled(false);

        new Thread(() -> {
            try {
                File tempFile = File.createTempFile("ocr_image", ".jpg", getCacheDir());
                FileOutputStream fos = new FileOutputStream(tempFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fos);
                fos.close();

                OkHttpClient client = new OkHttpClient.Builder()
                        .connectTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .writeTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .readTimeout(30, java.util.concurrent.TimeUnit.SECONDS)
                        .build();

                RequestBody requestBody = new MultipartBody.Builder()
                        .setType(MultipartBody.FORM)
                        .addFormDataPart("apikey", OCR_API_KEY)
                        .addFormDataPart("language", "eng")
                        .addFormDataPart("isOverlayRequired", "false")
                        .addFormDataPart("detectOrientation", "true")
                        .addFormDataPart("scale", "true")
                        .addFormDataPart("file", tempFile.getName(),
                                RequestBody.create(MediaType.parse("image/jpeg"), tempFile))
                        .build();

                Request request = new Request.Builder()
                        .url("https://api.ocr.space/parse/image")
                        .post(requestBody)
                        .build();

                Response response = client.newCall(request).execute();
                String jsonResponse = response.body().string();

                JSONObject jsonObject = new JSONObject(jsonResponse);

                if (jsonObject.has("ErrorMessage") && !jsonObject.isNull("ErrorMessage")) {
                    JSONArray errorMessages = jsonObject.getJSONArray("ErrorMessage");
                    if (errorMessages.length() > 0) {
                        runOnUiThread(() -> handleOCRError("Analysis failed"));
                        tempFile.delete();
                        return;
                    }
                }

                if (jsonObject.has("ParsedResults") && !jsonObject.isNull("ParsedResults")) {
                    JSONArray parsedResults = jsonObject.getJSONArray("ParsedResults");
                    if (parsedResults.length() > 0) {
                        JSONObject firstResult = parsedResults.getJSONObject(0);
                        String parsedText = firstResult.optString("ParsedText", "");
                        runOnUiThread(() -> handleOCRResult(parsedText));
                    }
                }

                tempFile.delete();

            } catch (Exception e) {
                Log.e(TAG, "OCR Error", e);
                runOnUiThread(() -> handleOCRError("Processing failed"));
            }
        }).start();
    }

    private void handleOCRError(String errorMessage) {
        showWarningUI("You can still submit", "Analysis unavailable");
        btnSubmit.setEnabled(true);
    }

    private void handleOCRResult(String text) {
        if (text == null || text.trim().isEmpty()) {
            showWarningUI("No text detected", "You can still submit if correct");
            btnSubmit.setEnabled(true);
            return;
        }

        String focusType = prefs.getString("focus_type", "coding");
        boolean isValid = checkFocusText(focusType, text);

        String cleanText = text.trim().replaceAll("\\s+", " ").replaceAll("[\\r\\n]+", "\n");
        String[] lines = cleanText.split("\n");
        StringBuilder preview = new StringBuilder();
        int charCount = 0, lineCount = 0;

        for (String line : lines) {
            if (lineCount >= 3 || charCount >= 150) break;
            if (!line.trim().isEmpty()) {
                preview.append(line.trim()).append("\n");
                charCount += line.length();
                lineCount++;
            }
        }

        if (cleanText.length() > 150 || lines.length > 3) preview.append("...");

        if (isValid) {
            showSuccessUI(
                    focusType + " verified!",
                    "Verified " + focusType
            );
            btnSubmit.setEnabled(true);
        } else {
            showWarningUI(
                    "Image unclear",
                    "Doesn't clearly match " + focusType + "\n\n📄 Detected:\n" +
                            preview.toString() + "\n\nYou can still submit if correct."
            );
            btnSubmit.setEnabled(true);
        }
    }

    private boolean checkFocusText(String focusType, String text) {
        if (text == null || text.isEmpty()) return false;
        String lowerText = text.toLowerCase();

        switch (focusType.toLowerCase()) {
            case "coding":
            case "programming":
                return lowerText.contains("class") || lowerText.contains("public") ||
                        lowerText.contains("void") || lowerText.contains("function") ||
                        lowerText.contains("private") || lowerText.contains("import");
            case "studying":
                return lowerText.contains("chapter") || lowerText.contains("page");
            case "cooking":
                return lowerText.contains("recipe") || lowerText.contains("ingredients");
            case "training":
                return lowerText.contains("reps") || lowerText.contains("exercise");
            default:
                return text.trim().length() > 30;
        }
    }

    private void endFocusSession(String message) {
        stopService(new Intent(this, AppMonitorService.class));
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
        Intent i = new Intent(this, MainActivity.class);
        i.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
        finish();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQ_CAMERA_PERMISSION) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                openCamera();
            }
        }
    }
}