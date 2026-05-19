package com.example.lostandfoundapp.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import android.database.Cursor;

import com.example.lostandfoundapp.data.LostItem;

import java.util.ArrayList;

public class DatabaseHelper extends SQLiteOpenHelper {

    // Database information
    private static final String DATABASE_NAME = "lost_found.db";
    private static final int DATABASE_VERSION = 2;

    // Table name
    public static final String TABLE_ITEMS = "lost_items";

    // Column names
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_POST_TYPE = "post_type";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE = "phone";
    public static final String COLUMN_DESCRIPTION = "description";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_LOCATION = "location";
    public static final String COLUMN_CATEGORY = "category";
    public static final String COLUMN_IMAGE_URI = "image_uri";
    public static final String COLUMN_LATITUDE = "latitude";
    public static final String COLUMN_LONGITUDE = "longitude";


    // SQL query to create the lost_items table
    private static final String CREATE_TABLE_ITEMS =
            "CREATE TABLE " + TABLE_ITEMS + " (" +
                    COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    COLUMN_POST_TYPE + " TEXT, " +
                    COLUMN_NAME + " TEXT, " +
                    COLUMN_PHONE + " TEXT, " +
                    COLUMN_DESCRIPTION + " TEXT, " +
                    COLUMN_DATE + " TEXT, " +
                    COLUMN_LOCATION + " TEXT, " +
                    COLUMN_CATEGORY + " TEXT, " +
                    COLUMN_IMAGE_URI + " TEXT, " +
                    COLUMN_LATITUDE + " REAL DEFAULT 0, " +
                    COLUMN_LONGITUDE + " REAL DEFAULT 0" +
                    ");";

    // Constructor
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Runs when the database is created for the first time
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_ITEMS);
    }

    // Runs when the database version changes
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            db.execSQL("ALTER TABLE " + TABLE_ITEMS +
                    " ADD COLUMN " + COLUMN_LATITUDE + " REAL DEFAULT 0");
            db.execSQL("ALTER TABLE " + TABLE_ITEMS +
                    " ADD COLUMN " + COLUMN_LONGITUDE + " REAL DEFAULT 0");
        }
    }

    // Insert a new lost & found advert into the database
    public boolean insertItem(String postType, String name, String phone,
                              String description, String date, String location,
                              String category, String imageUri,
                              double latitude, double longitude) {

        SQLiteDatabase db = this.getWritableDatabase();

        // store advert information as key-value pairs
        ContentValues values = new ContentValues();
        values.put(COLUMN_POST_TYPE, postType);
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_PHONE, phone);
        values.put(COLUMN_DESCRIPTION, description);
        values.put(COLUMN_DATE, date);
        values.put(COLUMN_LOCATION, location);
        values.put(COLUMN_CATEGORY, category);
        values.put(COLUMN_IMAGE_URI, imageUri);
        values.put(COLUMN_LATITUDE, latitude);
        values.put(COLUMN_LONGITUDE, longitude);

        long result = db.insert(TABLE_ITEMS, null, values);
        db.close();

        // If insert fails, returns -1
        return result != -1;
    }

    // Get all lost & found adverts from the database
    public ArrayList<LostItem> getAllItems() {
        ArrayList<LostItem> itemList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();

        Cursor cursor = db.rawQuery(
                "SELECT * FROM " + TABLE_ITEMS + " ORDER BY " + COLUMN_ID + " DESC",
                null
        );

        if (cursor.moveToFirst()) {
            do {
                LostItem item = new LostItem();

                item.setId(cursor.getInt(cursor.getColumnIndexOrThrow(COLUMN_ID)));
                item.setPostType(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_POST_TYPE)));
                item.setName(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_NAME)));
                item.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_PHONE)));
                item.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DESCRIPTION)));
                item.setDate(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_DATE)));
                item.setLocation(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_LOCATION)));
                item.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_CATEGORY)));
                item.setImageUri(cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_IMAGE_URI)));
                item.setLatitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LATITUDE)));
                item.setLongitude(cursor.getDouble(cursor.getColumnIndexOrThrow(COLUMN_LONGITUDE)));


                itemList.add(item);

            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();

        return itemList;
    }

    // Delete an advert from the database by id
    public boolean deleteItem(int id) {
        SQLiteDatabase db = this.getWritableDatabase();

        int result = db.delete(
                TABLE_ITEMS,
                COLUMN_ID + " = ?",
                new String[]{String.valueOf(id)}
        );

        db.close();

        // If result is greater than 0, one item was deleted
        return result > 0;
    }
}