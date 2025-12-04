package com.example.focusguardian.data;

import android.content.Context;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;

/**
 * Repository class that handles authentication operations.
 * Combines Firebase Authentication with local SQLite storage for offline support.
 */
public class  AuthRepository {

    private final FirebaseAuth firebaseAuth;
    private final DatabaseHelper databaseHelper;
    private static AuthRepository instance;

    public interface AuthCallback {
        void onSuccess(User user);
        void onError(String errorMessage);
    }

    public static synchronized AuthRepository getInstance(Context context) {
        if (instance == null) {
            instance = new AuthRepository(context);
        }
        return instance;
    }

    private AuthRepository(Context context) {
        firebaseAuth = FirebaseAuth.getInstance();
        databaseHelper = DatabaseHelper.getInstance(context);
    }

    /**
     * Register a new user with email and password.
     */
    public void registerUser(String email, String password, String displayName, AuthCallback callback) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Update display name in Firebase
                            UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(displayName)
                                    .build();
                            
                            firebaseUser.updateProfile(profileUpdates)
                                    .addOnCompleteListener(profileTask -> {
                                        // Create local user
                                        User user = new User(email, displayName);
                                        user.setFirebaseUid(firebaseUser.getUid());
                                        
                                        // Save to SQLite
                                        databaseHelper.insertOrUpdateUser(user);
                                        
                                        callback.onSuccess(user);
                                    });
                        }
                    } else {
                        String errorMessage = task.getException() != null 
                                ? task.getException().getMessage() 
                                : "Registration failed";
                        callback.onError(errorMessage);
                    }
                });
    }

    /**
     * Login user with email and password.
     */
    public void loginUser(String email, String password, AuthCallback callback) {
        firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                        if (firebaseUser != null) {
                            // Check if user exists in local DB
                            User localUser = databaseHelper.getUserByFirebaseUid(firebaseUser.getUid());
                            
                            if (localUser != null) {
                                // Update last login
                                databaseHelper.updateLastLogin(firebaseUser.getUid());
                                localUser.setLastLoginAt(System.currentTimeMillis());
                                callback.onSuccess(localUser);
                            } else {
                                // Create new local user record
                                User user = new User(
                                        firebaseUser.getEmail(),
                                        firebaseUser.getDisplayName() != null 
                                                ? firebaseUser.getDisplayName() 
                                                : email.split("@")[0]
                                );
                                user.setFirebaseUid(firebaseUser.getUid());
                                databaseHelper.insertOrUpdateUser(user);
                                callback.onSuccess(user);
                            }
                        }
                    } else {
                        String errorMessage = task.getException() != null 
                                ? task.getException().getMessage() 
                                : "Login failed";
                        callback.onError(errorMessage);
                    }
                });
    }

    /**
     * Check if user is currently logged in.
     */
    public boolean isLoggedIn() {
        return firebaseAuth.getCurrentUser() != null;
    }

    /**
     * Get the currently logged in user.
     */
    public User getCurrentUser() {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            User localUser = databaseHelper.getUserByFirebaseUid(firebaseUser.getUid());
            if (localUser != null) {
                return localUser;
            }
            // Fallback to Firebase user info
            User user = new User(
                    firebaseUser.getEmail(),
                    firebaseUser.getDisplayName()
            );
            user.setFirebaseUid(firebaseUser.getUid());
            return user;
        }
        return null;
    }

    /**
     * Get Firebase current user directly.
     */
    public FirebaseUser getFirebaseUser() {
        return firebaseAuth.getCurrentUser();
    }

    /**
     * Logout the current user.
     */
    public void logout() {
        firebaseAuth.signOut();
    }

    /**
     * Send password reset email.
     */
    public void sendPasswordResetEmail(String email, AuthCallback callback) {
        firebaseAuth.sendPasswordResetEmail(email)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        callback.onSuccess(null);
                    } else {
                        String errorMessage = task.getException() != null 
                                ? task.getException().getMessage() 
                                : "Failed to send reset email";
                        callback.onError(errorMessage);
                    }
                });
    }

    /**
     * Delete current user account.
     */
    public void deleteAccount(AuthCallback callback) {
        FirebaseUser user = firebaseAuth.getCurrentUser();
        if (user != null) {
            String uid = user.getUid();
            user.delete()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            // Remove from local database
                            databaseHelper.deleteUser(uid);
                            callback.onSuccess(null);
                        } else {
                            String errorMessage = task.getException() != null 
                                    ? task.getException().getMessage() 
                                    : "Failed to delete account";
                            callback.onError(errorMessage);
                        }
                    });
        } else {
            callback.onError("No user logged in");
        }
    }
}
