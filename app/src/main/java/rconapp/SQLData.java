package rconapp;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SQLData extends SQLiteOpenHelper {

    // TODO create config field to control this
    private static final int maxMessagesAmount = 1000;

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
        Cursor cursor = getReadableDatabase().rawQuery(
                "select DISTINCT tbl_name from sqlite_master where tbl_name = '"+tableName+"'",
                null);
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
                SQLContract.DataEntry.TABLE_NAME,
                projection,null, null,
                null,null,null
        );

        return cursor;
    }

    public void insert(ContentValues cv){
        if (!isTableExists(SQLContract.DataEntry.TABLE_NAME)){
            getWritableDatabase().execSQL(SQL_CREATE_ENTRIES);
        }
        Long messagesAmount = getWritableDatabase().insert(SQLContract.DataEntry.TABLE_NAME, null, cv);
        if (messagesAmount > maxMessagesAmount){
            String sql = "delete from " + SQLContract.DataEntry.TABLE_NAME + " where rowid <= " + (messagesAmount - maxMessagesAmount);
            getWritableDatabase().execSQL(sql);
        }
        Notifications.updateOnGoingNotification(messagesAmount);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }

}
