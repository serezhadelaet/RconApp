package com.example.rconapp;

import android.provider.BaseColumns;

public final class SQLContract {

    private SQLContract() {}

    public static class DataEntry implements BaseColumns {
        public static final String TABLE_NAME = "messages_data";
        public static final String COLUMN_MESSAGE = "message";
        public static final String COLUMN_DATE = "date";
        public static final String COLUMN_ISNOTIFY = "isnotify";
        public static final String COLUMN_ISCHAT = "ischat";
    }
}

