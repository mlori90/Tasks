package com.matelorantyahoo.tasks.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;

import com.matelorantyahoo.tasks.data.TasksContract.TaskEntry;

/**
 * Created by muster on 4/7/2018.
 */

public class TasksProvider extends ContentProvider {

    private static final String LOG_TAG = TasksProvider.class.getName();

    private TasksDbHelper mOpenHelper;

//    Table code for the uriMatcher
    private static final int TASKS = 900;

//    Task code for the uriMatcher
    private static final int TASKS_ID = 901;

//    Setting up the uriMatcher
    private static final UriMatcher mUriMatcher =new UriMatcher(UriMatcher.NO_MATCH);
    static {
        mUriMatcher.addURI(TasksContract.CONTENT_AUTHORITY,TasksContract.PATH_TABLE,TASKS);
        mUriMatcher.addURI(TasksContract.CONTENT_AUTHORITY,TasksContract.PATH_TABLE + "/#",TASKS_ID);
    }

    @Override
    public boolean onCreate() {

//        Initializing the TasksDbHelper
        mOpenHelper = new TasksDbHelper(getContext());
        return true;
    }

//    Getting data from the database
    @Nullable
    @Override
    public Cursor query(@NonNull Uri uri, @Nullable String[] projection, @Nullable String selection,
                        @Nullable String[] selectionArgs, @Nullable String sortOrder) {

//        Get readable database
        SQLiteDatabase database = mOpenHelper.getReadableDatabase();

//        This cursor will hold the result of the query
        Cursor cursor;

//        Matching the Uri
        int match = mUriMatcher.match(uri);
        switch (match){

//            Query for the whole table
            case TASKS:
                cursor = database.query(
                        TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder);
                break;

//            Query for single tasks
            case TASKS_ID:
                selection = TaskEntry._ID + "=?";
                String id = uri.getLastPathSegment();
                selectionArgs = new String[]{id};
                cursor = database.query(
                        TaskEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        null);
                break;

            default: throw new IllegalArgumentException("Uri not supported for query: " + uri);

        }
//        Setting up the notifications for data change
        cursor.setNotificationUri(getContext().getContentResolver(), uri);

//        Returning the cursor
        return cursor;
    }

//    This method is not necessary
    @Nullable
    @Override
    public String getType(@NonNull Uri uri) {
        return null;
    }

//    Adding data to the database
    @Nullable
    @Override
    public Uri insert(@NonNull Uri uri, @Nullable ContentValues contentValues) {

        if (mUriMatcher.match(uri) == TASKS){
            return insertTask(uri,contentValues);
        }
        else{
            throw new IllegalArgumentException("Uri not supported for insert: " +uri);
        }
    }

//    Helper method for inserting the task
    private Uri insertTask(Uri uri, ContentValues contentValues) {

//        Checking if the task have a title, if not throw exception
        String title = contentValues.getAsString(TaskEntry.COLUMN_TASK_TITLE);
        if (title == null && title.isEmpty()){
            throw new IllegalArgumentException("Task requires a title!");
        }

//        Getting the writable database
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

//        Inserting the values
        long id = db.insert(TaskEntry.TABLE_NAME,null,contentValues);

//        Checking if the insert was successful, if not return null
        if (id ==-1){
            Log.e(LOG_TAG,"Failed to insert for the uri: " + uri);
            return null;
        }
//        Notifying the change in the database
        getContext().getContentResolver().notifyChange(uri,null);

//        Returning the uri with the appended id number of the task
        return ContentUris.withAppendedId(uri,id);
    }


//    Deleting from the database
    @Override
    public int delete(@NonNull Uri uri, @Nullable String selection, @Nullable String[] selectionArgs) {

//        Getting the writable database
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

//        Checking if the uri code
        if (mUriMatcher.match(uri)==TASKS_ID){

//            Creating the selection and the selection arguments
            selection =TaskEntry._ID + "=?";
            selectionArgs= new String[] {String.valueOf(ContentUris.parseId(uri))};

//            Deleting the task, if successful notifying the change and returning the rows count which must be 1
            int rowsDeleted = db.delete(TaskEntry.TABLE_NAME,selection,selectionArgs);
            if (rowsDeleted!=0){
                getContext().getContentResolver().notifyChange(uri,null);
            }
            return rowsDeleted;
        }

//        Throwing exception for the unsupported uris
        else throw new IllegalArgumentException("Delete not supported for the following uri: " + uri);
    }


    @Override
    public int update(@NonNull Uri uri, @Nullable ContentValues contentValues, @Nullable String selection,
                      @Nullable String[] selectionArgs) {

//        Checking if the uri code
        switch (mUriMatcher.match(uri)){
            case TASKS_ID:
//            Creating the selection and the selection arguments
                selection =TaskEntry._ID + "=?";
                String id = uri.getLastPathSegment();
                selectionArgs= new String[] {id};
                return updateTask(uri,contentValues,selection,selectionArgs);
            case TASKS:
                SQLiteDatabase db = mOpenHelper.getWritableDatabase();
                int rows = db.update(TaskEntry.TABLE_NAME,contentValues,selection,selectionArgs);
                if (rows !=0){
                    getContext().getContentResolver().notifyChange(uri,null);
                }
                return rows;
        default: throw new IllegalArgumentException("Update not supported for the following uri: " + uri);
        }
    }

    private int updateTask(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {

//        Checking if the content values are empty
        if (contentValues.size()==0){
            return 0;
        }

//        Checking if there is a title for the task
        if (contentValues.containsKey(TaskEntry.COLUMN_TASK_TITLE)){
            String title = contentValues.getAsString(TaskEntry.COLUMN_TASK_TITLE);
            if (title==null){
                throw new IllegalArgumentException("Task requires a title!");
            }
        }

//        Get the writable database
        SQLiteDatabase db = mOpenHelper.getWritableDatabase();

//        Update the data and get the updated rows number
        int rows = db.update(TaskEntry.TABLE_NAME,contentValues,selection,selectionArgs);

//        If the update was successful notify
        if (rows !=0){
            getContext().getContentResolver().notifyChange(uri,null);
        }
        return rows;
    }
}
