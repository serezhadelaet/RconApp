package com.example.rconapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class SQLData extends SQLiteOpenHelper {
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE IF NOT EXISTS " + SQLContract.DataEntry.TABLE_NAME + " (" +
                    SQLContract.DataEntry.COLUMN_MESSAGE + " TEXT," +
                    SQLContract.DataEntry.COLUMN_DATE + " TEXT," +
                    SQLContract.DataEntry.COLUMN_ISNOTIFY + " INTEGER," +
                    SQLContract.DataEntry.COLUMN_ISCHAT + " INTEGER)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + SQLContract.DataEntry.TABLE_NAME;

    public static final int DATABASE_VERSION = 3;
    public static final String DATABASE_NAME = "messages_data.db";

    private static SQLData Instance;

    public static SQLData getInstance(){
        if (Instance == null){
            Instance = new SQLData(AppService.getInstance());
        }
        return Instance;
    }

    public SQLData(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        getWritableDatabase().execSQL(SQL_CREATE_ENTRIES);
    }

    public void onCreate(SQLiteDatabase db) {

    }

    public void clear(){
        getWritableDatabase().execSQL(SQL_DELETE_ENTRIES);
    }

    private boolean isTableExists(String tableName) {
        Cursor cursor = getReadableDatabase().rawQuery("select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'", null);
        if(cursor != null) {
            if(cursor.getCount() > 0) {
                cursor.close();
                return true;
            }
            cursor.close();
        }
        return false;
    }

    public Cursor getCursor(){

        if (!isTableExists(SQLContract.DataEntry.TABLE_NAME)){
            getWritableDatabase().execSQL(SQL_CREATE_ENTRIES);
        }

        String[] projection = {
            SQLContract.DataEntry.COLUMN_MESSAGE,
            SQLContract.DataEntry.COLUMN_DATE,
            SQLContract.DataEntry.COLUMN_ISNOTIFY,
            SQLContract.DataEntry.COLUMN_ISCHAT
        };

        Cursor cursor = getReadableDatabase().query(
                SQLContract.DataEntry.TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                null               // The sort order
        );

        return cursor;
    }

    // TODO: Limit max amount of rows

    public void insert(ContentValues cv){
        if (!isTableExists(SQLContract.DataEntry.TABLE_NAME)){
            getWritableDatabase().execSQL(SQL_CREATE_ENTRIES);
        }
        Long l = getWritableDatabase().insert(SQLContract.DataEntry.TABLE_NAME, null, cv);
        if (l>300){
            String sql = "delete from " + SQLContract.DataEntry.TABLE_NAME + " where rowid <= " + (l - 300);
        }
        Notifications.updateOnGoingNotification(l);

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
