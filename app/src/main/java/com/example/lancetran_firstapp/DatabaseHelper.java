package com.example.lancetran_firstapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

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
        // Create Users Table
        String createUsersTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COL_USER_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_USERNAME + " TEXT UNIQUE, " +
                COL_PASSWORD + " TEXT)";
        db.execSQL(createUsersTable);

        // Create Inventory Items Table
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

    // Register a new user account
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_USERNAME, username.trim());
        cv.put(COL_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, cv);
        return result != -1;
    }

    // Check credentials for existing user login
    public boolean checkUserCredentials(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_USERNAME + "=? AND " + COL_PASSWORD + "=?",
                new String[]{username.trim(), password}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // Check if username is already taken
    public boolean checkUserExists(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USERS, new String[]{COL_USER_ID},
                COL_USERNAME + "=?",
                new String[]{username.trim()}, null, null, null);
        boolean exists = (cursor.getCount() > 0);
        cursor.close();
        return exists;
    }

    // ==========================================
    // Inventory CRUD Operations
    // ==========================================

    // Create: Insert a new stock item
    public boolean insertItem(String name, String sku, String location, int quantity, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ITEM_NAME, name);
        cv.put(COL_SKU, sku);
        cv.put(COL_LOCATION, location);
        cv.put(COL_QUANTITY, quantity);
        cv.put(COL_NOTES, notes);
        long result = db.insert(TABLE_ITEMS, null, cv);
        return result != -1;
    }

    // Read: Fetch all items for RecyclerView grid
    public Cursor getAllItems() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_ITEMS + " ORDER BY " + COL_ITEM_ID + " DESC", null);
    }

    // Update: Modify quantity (for +/- button clicks)
    public boolean updateItemQuantity(int itemId, int newQuantity) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_QUANTITY, Math.max(0, newQuantity));
        int rows = db.update(TABLE_ITEMS, cv, COL_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
        return rows > 0;
    }

    // Update: Full edit of item details
    public boolean updateItemDetails(int itemId, String name, String sku, String location, int quantity, String notes) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_ITEM_NAME, name);
        cv.put(COL_SKU, sku);
        cv.put(COL_LOCATION, location);
        cv.put(COL_QUANTITY, quantity);
        cv.put(COL_NOTES, notes);
        int rows = db.update(TABLE_ITEMS, cv, COL_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
        return rows > 0;
    }

    // Delete: Remove an item from the database
    public boolean deleteItem(int itemId) {
        SQLiteDatabase db = this.getWritableDatabase();
        int rows = db.delete(TABLE_ITEMS, COL_ITEM_ID + "=?", new String[]{String.valueOf(itemId)});
        return rows > 0;
    }
}