package com.matelorantyahoo.tasks;

import android.app.DatePickerDialog;
import android.app.LoaderManager;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Color;
import android.net.Uri;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matelorantyahoo.tasks.utils.DateUtilities;
import com.matelorantyahoo.tasks.data.TasksContract.TaskEntry;
import com.matelorantyahoo.tasks.utils.ToastUtilities;

import java.util.Calendar;

import static android.R.attr.priority;

public class AddTaskActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private static final String LOG_TAG = AddTaskActivity.class.getName();

    private EditText mEtAddTaskTitle, mEtAddTaskDesc;
    private TextView mTvAddTaskDate;
    private RatingBar mRbPriority;
    private long mCurrentDate;
    private DatePickerDialog.OnDateSetListener mDateSetListener;
    private Uri mCurrentTaskUri;

    private static final int LOADER_ID = 148;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_task);

//        Setting up the action bar back bottom
        ActionBar actionBar = getSupportActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

//        Initializing the onDateSetListener, the RatingBar the EditText fields and the TextView for the date
        mDateSetListener = setDateSetListener();
        mEtAddTaskTitle = findViewById(R.id.et_add_task_title);
        mEtAddTaskDesc = findViewById(R.id.et_add_task_desc);
        mRbPriority = findViewById(R.id.rb_add_priority);
        mTvAddTaskDate = findViewById(R.id.tv_add_task_date);

//        Getting the date from the intent(if exists) displaying the value in the textView and setting the activity title
        Intent intent = getIntent();
        long date = intent.getLongExtra(TasksActivity.INTENT_DATE_EXTRA, DateUtilities.getTodayInMillis());
        if (date!=0){
            mCurrentDate = date;
            setTitle(getString(R.string.add_task_activity));
            mTvAddTaskDate.setText(DateUtilities.getDisplayDate(mCurrentDate));
        }


        Uri uri = intent.getData();
        if (uri != null){
            mCurrentTaskUri=uri;
            setTitle(getString(R.string.edit_task_activity));
            getLoaderManager().initLoader(LOADER_ID,null,this);
        }





//         Setting up the onClickListener for the displayed date text view, and within creating a DatePickerDialog
        mTvAddTaskDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(mCurrentDate);
                int year=cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        AddTaskActivity.this,
                        R.style.DatePickerCostume,
                        mDateSetListener,
                        year,
                        month,
                        day);
                dialog.show();
            }
        });
    }


//    Helper method for creating the OnDateSetListener
    private DatePickerDialog.OnDateSetListener setDateSetListener() {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar cal = Calendar.getInstance();
                cal.set(year,month,day);
                long time = cal.getTimeInMillis();
                mCurrentDate = DateUtilities.getDayInMillis(time);
                mTvAddTaskDate.setText(DateUtilities.getDisplayDate(mCurrentDate));

            }
        };
    }


//    Inflating the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_add_task,menu);
        return super.onCreateOptionsMenu(menu);
    }

//    Setting up the menu items
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()){

//            Save item
            case R.id.menu_item_save_task:
                if (mCurrentTaskUri==null){
                    if (saveTask()){
                        finish();
                    }
                    else return true;

                }
                else {
                    if (updateTask()){
                        finish();
                    }
                    else return true;
                }
                break;

//            Delete item
            case R.id.menu_item_delete_task:
                if (mCurrentTaskUri!=null){
                    int rowsDeleted = getContentResolver().delete(mCurrentTaskUri,null,null);
                    if (rowsDeleted!=0){
                        ToastUtilities.showToast(this,getString(R.string.delete_task));
                        finish();
                    }
                    else ToastUtilities.showToast(this,getString(R.string.delete_task_fail));
                    return true;
                }
                return true;

//            Home Item
            case android.R.id.home:
                finish();
        }
        return super.onOptionsItemSelected(item);
    }

//    Helper method to update the task
    private boolean updateTask() {

//        Getting the contentValues and validating them
        ContentValues cv = getCv();
        if(cv==null){
            return false;
        }
//        Updating the task
        int rows = getContentResolver().update(mCurrentTaskUri,cv,null,null);

//        Checking if update successful and showing toast accordingly
        if (rows !=0){
            ToastUtilities.showToast(this,getString(R.string.edit_task_successful));
            return true;
        }
        else {
            ToastUtilities.showToast(this,getString(R.string.edit_task_unsuccessful));
            return false;
        }
    }

//    Hiding the delete menu item if there is no uri
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (mCurrentTaskUri ==null){
            MenuItem deleteItem = menu.findItem(R.id.menu_item_delete_task);
            deleteItem.setVisible(false);
        }
        return true;
    }

   @Override
    public void onBackPressed() {
        mEtAddTaskTitle.setText("");
        mEtAddTaskDesc.setText("");
        mRbPriority.setRating(1);
        mCurrentTaskUri = null;
        super.onBackPressed();
    }

    //    Helper method to save or update the task
    private boolean saveTask() {

//        Getting the contentValues and validating them
        ContentValues cv = getCv();
        if(cv==null){
            return false;
        }

//        Saving the data in the database
        Uri uri = getContentResolver().insert(TaskEntry.CONTENT_URI,cv);
        if (uri == null){
//            If the new content URI is null show error message, if successful show successful message
            ToastUtilities.showToast(this,getString(R.string.add_task_failed));
            return false;
        } else {
            ToastUtilities.showToast(this,getString(R.string.add_task_successful));
            return true;
        }

    }


//  Helper method to validate the date and get the contentValues
    private ContentValues getCv() {

//        Getting the title and check if there exist a valid title, if not set red color error hint end return
        String title = mEtAddTaskTitle.getText().toString().trim();
        if (TextUtils.isEmpty(title)) {
            mEtAddTaskTitle.setHint(getString(R.string.add_title_error));
            mEtAddTaskTitle.setHintTextColor(Color.RED);
            return null;
        }

//        Getting the description and priority
        String description = mEtAddTaskDesc.getText().toString().trim();
        float priority =  mRbPriority.getRating();

//        Saving the data in ContentValue object
        ContentValues cv = new ContentValues();
        cv.put(TaskEntry.COLUMN_TASK_TITLE,title);
        cv.put(TaskEntry.COLUMN_TASK_DATE,mCurrentDate);
        cv.put(TaskEntry.COLUMN_TASK_DESCRIPTION,description);
        cv.put(TaskEntry.COLUMN_TASK_PRIORITY,priority);
        return cv;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {


//        Setting up the wanted columns
        String [] projection = {
                TaskEntry._ID,
                TaskEntry.COLUMN_TASK_TITLE,
                TaskEntry.COLUMN_TASK_DATE,
                TaskEntry.COLUMN_TASK_DESCRIPTION,
                TaskEntry.COLUMN_TASK_PRIORITY};


        return new CursorLoader(this,mCurrentTaskUri,projection,null,null,null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

        if (cursor == null || cursor.getCount() < 1) {
            return;
        }

        if (cursor.moveToFirst()){

//            Getting the column indexes;
            int titleIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
            int idIndex = cursor.getColumnIndex(TaskEntry._ID);
            int priorityIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_PRIORITY);
            int descriptionIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DESCRIPTION);
            int dateIndex = cursor.getColumnIndex(TaskEntry.COLUMN_TASK_DATE);

//            Getting the values

            String title = cursor.getString(titleIndex);
            String description = cursor.getString(descriptionIndex);
//            int id = cursor.getInt(idIndex);
            float priority = cursor.getFloat(priorityIndex);
            long date = cursor.getLong(dateIndex);

//            Giving the values to the views
            mCurrentDate = date;
            mTvAddTaskDate.setText(DateUtilities.getDisplayDate(mCurrentDate));
            mEtAddTaskTitle.setText(title);
            mEtAddTaskDesc.setText(description);
            mRbPriority.setRating(priority);

        }

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

        mCurrentDate = DateUtilities.getTodayInMillis();
        mTvAddTaskDate.setText(DateUtilities.getDisplayDate(mCurrentDate));
        mEtAddTaskTitle.setText("");
        mEtAddTaskDesc.setText("");
        mRbPriority.setRating(1);
    }
}
