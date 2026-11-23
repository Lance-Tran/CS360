package com.example.lancetran_firstapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Configuration
    private static final String DATABASE_NAME = "InventoryApp.db";
    private static final int DATABASE_VERSION = 1;

    // Users Table
    public static final String TABLE_USERS = "users";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";

    // Inventory Items Table
    public static final String TABLE_ITEMS = "inventory_items";
    public static final String COL_ITEM_ID = "item_id";
    public static final String COL_ITEM_NAME = "item_name";
    public static final String COL_SKU = "sku";
    public static final String COL_LOCATION = "storage_location";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_NOTES = "notes";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        String createItemsTable = "CREATE TABLE " + TABLE_ITEMS + " (" +
                COL_ITEM_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_ITEM_NAME + " TEXT, " +
                COL_SKU + " TEXT, " +
                COL_LOCATION + " TEXT, " +
                COL_QUANTITY + " INTEGER, " +
                COL_NOTES + " TEXT)";
        db.execSQL(createItemsTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    // ==========================================
    // Authentication Operations
    // ==========================================

    public boolean registerUser(String username, String password) {
        if (username == null || username.trim().isEmpty() || password == null || password.trim().isEmpty()) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username.trim());
        cv.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    public boolean checkUserCredentials(String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                    COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                    new String[]{username.trim(), password}, null, null, null);
            return (cursor != null && cursor.getCount() > 0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public boolean checkUserExists(String username) {
        if (username == null) {
            return false;
        }
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                    COL_USERNAME + "=?",
                    new String[]{username.trim()}, null, null, null);
            return (cursor != null && cursor.getCount() > 0);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    // ==========================================
    // Inventory CRUD Operations
    // ==========================================

    public boolean insertItem(String name, String sku, String location, int quantity, String notes) {
        if (name == null || name.trim().isEmpty() || quantity < 0) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ITEM_NAME, name.trim());
        cv.put(COL_SKU, sku != null ? sku.trim() : "N/A");
        cv.put(COL_LOCATION, (location != null && !location.trim().isEmpty()) ? location.trim() : "General");
        cv.put(COL_QUANTITY, Math.max(0, quantity));
        cv.put(COL_NOTES, notes != null ? notes.trim() : "");
        long result = db.insert(TABLE_ITEMS, null, cv);
        return result != -1;
    }

    // Encapsulates Cursor lifecycle inside try-finally to prevent resource leaks
    public List<InventoryItem> getAllInventoryItems() {
        List<InventoryItem> items = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = null;
        try {
            cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " ORDER BY " + COL_ITEM_ID + " DESC", null);
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int id = cursor.getInt(cursor.getColumnIndexOrThrow(COL_ITEM_ID));
                    String name = cursor.getString(cursor.getColumnIndexOrThrow(COL_ITEM_NAME));
                    String sku = cursor.getString(cursor.getColumnIndexOrThrow(COL_SKU));
                    String location = cursor.getString(cursor.getColumnIndexOrThrow(COL_LOCATION));
                    int qty = cursor.getInt(cursor.getColumnIndexOrThrow(COL_QUANTITY));
                    String notes = cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES));

                    items.add(new InventoryItem(id, name, sku, location, qty, notes));
                } while (cursor.moveToNext());
            }
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
        return items;
    }

    public boolean updateItemQuantity(int itemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_QUANTITY, Math.max(0, newQuantity));
        int rows = db.update(TABLE_ITEMS, cv, COL_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
        return rows > 0;
    }

    public boolean updateItemDetails(int itemId, String name, String sku, String location, int quantity, String notes) {
        if (name == null || name.trim().isEmpty() || quantity < 0) {
            return false;
        }
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ITEM_NAME, name.trim());
        cv.put(COL_SKU, sku != null ? sku.trim() : "N/A");
        cv.put(COL_LOCATION, (location != null && !location.trim().isEmpty()) ? location.trim() : "General");
        cv.put(COL_QUANTITY, Math.max(0, quantity));
        cv.put(COL_NOTES, notes != null ? notes.trim() : "");
        int rows = db.update(TABLE_ITEMS, cv, COL_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
        return rows > 0;
    }

    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_ITEMS, COL_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
        return rows > 0;
    }
}