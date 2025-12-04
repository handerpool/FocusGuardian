package com.example.focusguardian.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite Database Helper for local user storage.
 * Stores user information locally for offline access and caching.
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "focusguardian.db";
    private static final int DATABASE_VERSION = 1;

    // Users table
    public static final String TABLE_USERS = "users";
    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_FIREBASE_UID = "firebase_uid";
    public static final String COLUMN_EMAIL = "email";
    public static final String COLUMN_DISPLAY_NAME = "display_name";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_LAST_LOGIN_AT = "last_login_at";

    // Create table SQL
    private static final String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + " ("
            + COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, "
            + COLUMN_FIREBASE_UID + " TEXT UNIQUE, "
            + COLUMN_EMAIL + " TEXT NOT NULL, "
            + COLUMN_DISPLAY_NAME + " TEXT, "
            + COLUMN_CREATED_AT + " INTEGER, "
            + COLUMN_LAST_LOGIN_AT + " INTEGER"
            + ")";

    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    // Insert or update user
    public long insertOrUpdateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_FIREBASE_UID, user.getFirebaseUid());
        values.put(COLUMN_EMAIL, user.getEmail());
        values.put(COLUMN_DISPLAY_NAME, user.getDisplayName());
        values.put(COLUMN_CREATED_AT, user.getCreatedAt());
        values.put(COLUMN_LAST_LOGIN_AT, user.getLastLoginAt());

        // Try to update first
        int rowsAffected = db.update(TABLE_USERS, values,
                COLUMN_FIREBASE_UID + " = ?", new String[]{user.getFirebaseUid()});

        if (rowsAffected == 0) {
            // Insert new record
            return db.insert(TABLE_USERS, null, values);
        }
        return rowsAffected;
    }

    // Get user by Firebase UID
    public User getUserByFirebaseUid(String firebaseUid) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COLUMN_FIREBASE_UID + " = ?", new String[]{firebaseUid},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    // Get user by email
    public User getUserByEmail(String email) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null,
                COLUMN_EMAIL + " = ?", new String[]{email},
                null, null, null);

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    // Update last login time
    public void updateLastLogin(String firebaseUid) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_LAST_LOGIN_AT, System.currentTimeMillis());
        db.update(TABLE_USERS, values, COLUMN_FIREBASE_UID + " = ?", new String[]{firebaseUid});
    }

    // Delete user
    public void deleteUser(String firebaseUid) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, COLUMN_FIREBASE_UID + " = ?", new String[]{firebaseUid});
    }

    // Get current logged in user (most recent login)
    public User getCurrentUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, null, null, null,
                null, null, COLUMN_LAST_LOGIN_AT + " DESC", "1");

        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = cursorToUser(cursor);
            cursor.close();
        }
        return user;
    }

    private User cursorToUser(Cursor cursor) {
        int idIndex = cursor.getColumnIndex(COLUMN_ID);
        int firebaseUidIndex = cursor.getColumnIndex(COLUMN_FIREBASE_UID);
        int emailIndex = cursor.getColumnIndex(COLUMN_EMAIL);
        int displayNameIndex = cursor.getColumnIndex(COLUMN_DISPLAY_NAME);
        int createdAtIndex = cursor.getColumnIndex(COLUMN_CREATED_AT);
        int lastLoginAtIndex = cursor.getColumnIndex(COLUMN_LAST_LOGIN_AT);

        return new User(
                idIndex >= 0 ? cursor.getLong(idIndex) : 0,
                firebaseUidIndex >= 0 ? cursor.getString(firebaseUidIndex) : null,
                emailIndex >= 0 ? cursor.getString(emailIndex) : null,
                displayNameIndex >= 0 ? cursor.getString(displayNameIndex) : null,
                createdAtIndex >= 0 ? cursor.getLong(createdAtIndex) : 0,
                lastLoginAtIndex >= 0 ? cursor.getLong(lastLoginAtIndex) : 0
        );
    }
}
