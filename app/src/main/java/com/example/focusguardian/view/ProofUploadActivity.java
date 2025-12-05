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
        AnalysisResult result = analyzeProofContent(focusType, text);

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

        if (result.isValid && result.confidence >= 70) {
            showSuccessUI(
                    focusType + " verified! (" + result.confidence + "% match)",
                    "✓ " + result.reason
            );
            btnSubmit.setEnabled(true);
        } else if (result.confidence >= 40) {
            showWarningUI(
                    "Partial match (" + result.confidence + "%)",
                    result.reason + "\n\n📄 Detected:\n" +
                            preview.toString() + "\n\nYou can still submit."
            );
            btnSubmit.setEnabled(true);
        } else {
            showWarningUI(
                    "Low confidence (" + result.confidence + "%)",
                    "Doesn't clearly match " + focusType + "\n\n📄 Detected:\n" +
                            preview.toString() + "\n\nYou can still submit if correct."
            );
            btnSubmit.setEnabled(true);
        }
    }

    // Analysis result class to hold validation data
    private static class AnalysisResult {
        boolean isValid;
        int confidence;
        String reason;
        
        AnalysisResult(boolean isValid, int confidence, String reason) {
            this.isValid = isValid;
            this.confidence = confidence;
            this.reason = reason;
        }
    }

    private AnalysisResult analyzeProofContent(String focusType, String text) {
        if (text == null || text.isEmpty()) {
            return new AnalysisResult(false, 0, "No content detected");
        }
        
        String lowerText = text.toLowerCase();
        int score = 0;
        int maxScore = 100;
        StringBuilder matchedItems = new StringBuilder();

        switch (focusType.toLowerCase()) {
            case "coding":
            case "programming":
                // Programming keywords - primary indicators (high weight)
                String[] codeKeywordsPrimary = {
                    "function", "class", "public", "private", "protected", "static",
                    "void", "return", "import", "package", "interface", "extends",
                    "implements", "const", "let", "var", "def", "async", "await"
                };
                
                // Secondary indicators (medium weight)
                String[] codeKeywordsSecondary = {
                    "if", "else", "for", "while", "switch", "case", "try", "catch",
                    "throw", "new", "null", "true", "false", "this", "super"
                };
                
                // Code patterns (high weight)
                String[] codePatterns = {
                    "(){", "() {", "};", ");", "=>", "->", "==", "!=", "<=", ">=",
                    "++", "--", "+=", "-=", "[]", "{}", "()", "/**", "//", "/*"
                };
                
                // IDE/Editor indicators
                String[] ideIndicators = {
                    "android", "studio", "intellij", "vscode", "visual studio",
                    "eclipse", "sublime", "atom", "terminal", "console", "debug",
                    "build", "run", "compile", "error", "warning", "git"
                };
                
                int primaryMatches = 0;
                for (String keyword : codeKeywordsPrimary) {
                    if (lowerText.contains(keyword)) {
                        primaryMatches++;
                        if (primaryMatches <= 3) matchedItems.append(keyword).append(", ");
                    }
                }
                score += Math.min(primaryMatches * 12, 48); // Max 48 points
                
                int secondaryMatches = 0;
                for (String keyword : codeKeywordsSecondary) {
                    if (lowerText.contains(keyword)) secondaryMatches++;
                }
                score += Math.min(secondaryMatches * 5, 20); // Max 20 points
                
                int patternMatches = 0;
                for (String pattern : codePatterns) {
                    if (text.contains(pattern)) patternMatches++;
                }
                score += Math.min(patternMatches * 4, 20); // Max 20 points
                
                int ideMatches = 0;
                for (String indicator : ideIndicators) {
                    if (lowerText.contains(indicator)) ideMatches++;
                }
                score += Math.min(ideMatches * 6, 12); // Max 12 points
                
                // Check for code structure patterns using regex
                if (text.matches("(?s).*\\b\\w+\\s*\\([^)]*\\)\\s*\\{.*")) score += 10;
                if (text.matches("(?s).*\\b(int|string|boolean|float|double|void)\\b.*")) score += 5;
                
                String reason = matchedItems.length() > 0 ? 
                    "Found: " + matchedItems.toString().replaceAll(", $", "") : "Code patterns detected";
                return new AnalysisResult(score >= 70, Math.min(score, 100), reason);
                
            case "studying":
            case "reading":
                // Academic/Study keywords
                String[] studyKeywordsPrimary = {
                    "chapter", "page", "section", "paragraph", "introduction",
                    "conclusion", "summary", "abstract", "thesis", "hypothesis",
                    "bibliography", "reference", "citation", "figure", "table"
                };
                
                String[] studyKeywordsSecondary = {
                    "definition", "example", "note", "important", "key", "concept",
                    "theory", "practice", "exercise", "question", "answer", "test",
                    "exam", "quiz", "study", "learn", "understand", "explain"
                };
                
                String[] academicSubjects = {
                    "math", "science", "history", "english", "physics", "chemistry",
                    "biology", "literature", "economics", "psychology", "sociology",
                    "philosophy", "calculus", "algebra", "geometry", "statistics"
                };
                
                int studyPrimaryMatches = 0;
                for (String keyword : studyKeywordsPrimary) {
                    if (lowerText.contains(keyword)) {
                        studyPrimaryMatches++;
                        if (studyPrimaryMatches <= 3) matchedItems.append(keyword).append(", ");
                    }
                }
                score += Math.min(studyPrimaryMatches * 15, 45);
                
                int studySecondaryMatches = 0;
                for (String keyword : studyKeywordsSecondary) {
                    if (lowerText.contains(keyword)) studySecondaryMatches++;
                }
                score += Math.min(studySecondaryMatches * 8, 32);
                
                int subjectMatches = 0;
                for (String subject : academicSubjects) {
                    if (lowerText.contains(subject)) subjectMatches++;
                }
                score += Math.min(subjectMatches * 8, 16);
                
                // Check for numbered lists or bullet points (common in notes)
                if (text.matches("(?s).*(\\d+\\.\\s+|•\\s+|\\-\\s+).*")) score += 7;
                
                reason = matchedItems.length() > 0 ? 
                    "Found: " + matchedItems.toString().replaceAll(", $", "") : "Study content detected";
                return new AnalysisResult(score >= 70, Math.min(score, 100), reason);
                
            case "cooking":
            case "baking":
                // Recipe keywords
                String[] cookingKeywordsPrimary = {
                    "recipe", "ingredients", "instructions", "directions", "step",
                    "cook", "bake", "fry", "boil", "simmer", "roast", "grill",
                    "mix", "stir", "blend", "chop", "slice", "dice", "mince"
                };
                
                String[] cookingKeywordsSecondary = {
                    "preheat", "oven", "pan", "pot", "bowl", "cup", "tablespoon",
                    "teaspoon", "gram", "ounce", "pound", "liter", "minute", "hour",
                    "temperature", "degrees", "fahrenheit", "celsius", "serve"
                };
                
                String[] commonIngredients = {
                    "salt", "pepper", "sugar", "flour", "butter", "oil", "egg",
                    "milk", "water", "chicken", "beef", "onion", "garlic", "tomato",
                    "cheese", "cream", "sauce", "spice", "herb", "vegetable"
                };
                
                int cookingPrimaryMatches = 0;
                for (String keyword : cookingKeywordsPrimary) {
                    if (lowerText.contains(keyword)) {
                        cookingPrimaryMatches++;
                        if (cookingPrimaryMatches <= 3) matchedItems.append(keyword).append(", ");
                    }
                }
                score += Math.min(cookingPrimaryMatches * 12, 48);
                
                int cookingSecondaryMatches = 0;
                for (String keyword : cookingKeywordsSecondary) {
                    if (lowerText.contains(keyword)) cookingSecondaryMatches++;
                }
                score += Math.min(cookingSecondaryMatches * 6, 24);
                
                int ingredientMatches = 0;
                for (String ingredient : commonIngredients) {
                    if (lowerText.contains(ingredient)) ingredientMatches++;
                }
                score += Math.min(ingredientMatches * 4, 20);
                
                // Check for measurements pattern
                if (text.matches("(?s).*\\d+\\s*(cup|tbsp|tsp|oz|g|ml|lb).*")) score += 8;
                
                reason = matchedItems.length() > 0 ? 
                    "Found: " + matchedItems.toString().replaceAll(", $", "") : "Recipe content detected";
                return new AnalysisResult(score >= 70, Math.min(score, 100), reason);
                
            case "training":
            case "workout":
            case "exercise":
                // Fitness keywords
                String[] fitnessKeywordsPrimary = {
                    "exercise", "workout", "training", "reps", "sets", "rest",
                    "weight", "cardio", "strength", "endurance", "flexibility",
                    "warmup", "cooldown", "stretch", "routine", "program"
                };
                
                String[] fitnessKeywordsSecondary = {
                    "squat", "deadlift", "bench", "press", "curl", "row", "pull",
                    "push", "lunge", "plank", "crunch", "burpee", "run", "jog",
                    "sprint", "jump", "lift", "muscle", "core", "arm", "leg"
                };
                
                String[] fitnessMetrics = {
                    "minute", "second", "mile", "kilometer", "pound", "kilogram",
                    "heart", "rate", "calories", "burned", "distance", "time",
                    "speed", "pace", "duration", "interval", "recovery"
                };
                
                int fitnessPrimaryMatches = 0;
                for (String keyword : fitnessKeywordsPrimary) {
                    if (lowerText.contains(keyword)) {
                        fitnessPrimaryMatches++;
                        if (fitnessPrimaryMatches <= 3) matchedItems.append(keyword).append(", ");
                    }
                }
                score += Math.min(fitnessPrimaryMatches * 12, 48);
                
                int fitnessSecondaryMatches = 0;
                for (String keyword : fitnessKeywordsSecondary) {
                    if (lowerText.contains(keyword)) fitnessSecondaryMatches++;
                }
                score += Math.min(fitnessSecondaryMatches * 6, 30);
                
                int metricMatches = 0;
                for (String metric : fitnessMetrics) {
                    if (lowerText.contains(metric)) metricMatches++;
                }
                score += Math.min(metricMatches * 4, 16);
                
                // Check for rep/set patterns like "3x10" or "10 reps"
                if (text.matches("(?s).*\\d+\\s*[xX]\\s*\\d+.*")) score += 6;
                if (text.matches("(?s).*\\d+\\s*(reps?|sets?).*")) score += 6;
                
                reason = matchedItems.length() > 0 ? 
                    "Found: " + matchedItems.toString().replaceAll(", $", "") : "Fitness content detected";
                return new AnalysisResult(score >= 70, Math.min(score, 100), reason);
                
            default:
                // Generic content check - just verify there's meaningful text
                int wordCount = text.split("\\s+").length;
                if (wordCount >= 50) score = 80;
                else if (wordCount >= 30) score = 65;
                else if (wordCount >= 15) score = 50;
                else if (wordCount >= 5) score = 30;
                else score = 10;
                
                return new AnalysisResult(score >= 60, score, wordCount + " words detected");
        }
    }

    // Keep old method for backward compatibility but it's no longer used
    private boolean checkFocusText(String focusType, String text) {
        return analyzeProofContent(focusType, text).isValid;
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