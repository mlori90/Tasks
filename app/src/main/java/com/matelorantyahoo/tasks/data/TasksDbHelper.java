package com.matelorantyahoo.tasks.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.matelorantyahoo.tasks.data.TasksContract.TaskEntry;

/**
 * Created by muster on 4/7/2018.
 */

public class TasksDbHelper extends SQLiteOpenHelper {

//    Database name
    private static final String DATABASE_NAME = "tasks.db";

//    Database version
    private static final int DATABASE_VERSION = 1;


    public TasksDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {

//        Creating the database
        String SQL_CREATE_DATABASE_TABLE = "CREATE TABLE " + TaskEntry.TABLE_NAME + " (" +
                TaskEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                TaskEntry.COLUMN_TASK_TITLE + " TEXT NOT NULL, " +
                TaskEntry.COLUMN_TASK_DATE + " INTEGER NOT NULL, " +
                TaskEntry.COLUMN_TASK_PRIORITY + " REAL NOT NULL DEFAULT 0, " +
                TaskEntry.COLUMN_TASK_DESCRIPTION + " TEXT, " +
                TaskEntry.COLUMN_TASK_STATE + " INTEGER NOT NULL DEFAULT 0);";

        sqLiteDatabase.execSQL(SQL_CREATE_DATABASE_TABLE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {
//        On upgrade deleting the old table and creating a new one
        sqLiteDatabase.execSQL("DROP TABLE IF EXISTS " +TaskEntry.TABLE_NAME);
        onCreate(sqLiteDatabase);

    }
}
