package com.example.mystockwatch;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String TAG = "DatabaseHelper";
    private static final String name  = "stocks";
    private static final String col1  = "ID";
    private static final String col2 = "stocks";

    public DatabaseHelper(@Nullable Context context) {
        super(context, name, null, 2);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE "+ name + "(ID INTEGER PRIMARY KEY AUTOINCREMENT, "+col2 + " TEXT)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS "+ name);
        onCreate(db);
    }

    public boolean addData(String data){
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(col2,data);


        long result = db.insert(name,null, contentValues);
        db.close();
        if(result == -1){
            return false;
        }
        return true;
    }
    public boolean exists(String id) {
        SQLiteDatabase db = getWritableDatabase();
        String selectString = "SELECT * FROM " + name + " WHERE stocks " + " =?";

        // Add the String you are searching by here.
        // Put it in an array to avoid an unrecognized token error
        Cursor cursor = db.rawQuery(selectString, new String[] {id});

        boolean hasObject = false;
        int count = 0;
        if(cursor.moveToFirst()){
            hasObject = true;

            //region if you had multiple records to check for, use this region.


            while(cursor.moveToNext()){
                count++;
            }
            //here, count is records found
            Log.d("SQL", String.format("%d records found", count));

            //endregion

        }


        cursor.close();          // Dont forget to close your cursor
        db.close();
        Log.d("SQL", "count: "+ count);
        if(count == 0){
            return false;
        }
        return true;
    }

    public boolean delete(String name) {
        SQLiteDatabase dbase = this.getWritableDatabase();
        boolean a =  dbase.delete(this.name, col2 + "=?", new String[]{name}) > 0;
        dbase.close();
        return a;
    }
}
