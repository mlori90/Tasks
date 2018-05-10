package com.matelorantyahoo.tasks;


import android.app.DatePickerDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.PersistableBundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.util.Log;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AnimationUtils;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.matelorantyahoo.tasks.data.TasksContract.TaskEntry;
import com.matelorantyahoo.tasks.utils.DateUtilities;
import com.matelorantyahoo.tasks.utils.ToastUtilities;


import java.util.Calendar;

import static android.R.attr.data;
import static android.R.attr.id;
import static android.R.attr.switchMinWidth;
import static android.R.attr.viewportHeight;
import static android.R.attr.wallpaperCloseEnterAnimation;
import static android.R.attr.x;
import static android.icu.lang.UCharacter.GraphemeClusterBreak.T;
import static com.matelorantyahoo.tasks.R.id.view;

public class TasksActivity extends AppCompatActivity implements android.app.LoaderManager.LoaderCallbacks<Cursor>,
        TaskAdapter.OnTaskClickedListener {

//     Log tag for debugging
    private static final String LOG_TAG = TasksActivity.class.getName();

//    Key for the date in savedInstanceState

    private static final String DATE_KEY = "date";

//    Loader id
    private static final int LOADER_ID = 498;
    private static final int LOADRE_ID_PAST = 487;

//    State int constants
    private static final int UNFINISHED = 0;
    private static final int FINISHED = 1;

//    String unifier for the extra data in intent

    public static final String INTENT_DATE_EXTRA = "intent_date";

//    TextView for displaying the date and the recycler view
    private TextView mTvDate;
    private RecyclerView mRecyclerView;

//    Empty View and loading indicator
    private View mEmptyView;
    private ProgressBar mProgressBar;

//    Adapter for the RecyclerView
    private TaskAdapter mAdapter;


//    Variables for the unfinished tasks in the past
    private static boolean mUnfinishedTask;
    private static int mCountUnfinishedTask;


//    Variable holding the current date value
    private long mCurrentDateMillis;

//    Variable for loading
    private static boolean isLoading;

//    DateSetListener object for the date picker
    private DatePickerDialog.OnDateSetListener mDateSetListener;

//    SharedPref and sharedPrefListener
    SharedPreferences sharedPref;
    SharedPreferences.OnSharedPreferenceChangeListener listener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_tasks);

//        Setting that there is no unfinished task
        mUnfinishedTask = false;
        mCountUnfinishedTask = 0;

//        Hiding the elevation
        getSupportActionBar().setElevation(0);

//        Checking if there is a saved data in sharedPreferences(in case of device rotation),
//        if not add today as current date and display it. Also if there is a saved date reset to 0
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        long savedDate = sharedPreferences.getLong(DATE_KEY,0);
        if (savedDate ==0){
            mCurrentDateMillis = DateUtilities.getTodayInMillis();
        }
        else mCurrentDateMillis=savedDate;
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putLong(DATE_KEY,0);
        editor.apply();
        mTvDate = findViewById(R.id.tv_date);
        mTvDate.setText(DateUtilities.getDisplayDate(mCurrentDateMillis));
        mProgressBar = findViewById(R.id.pb_task_activity);
        activateLoadingIndicator();

//        Initializing and registering the sharedPref and the listener for the notification
        sharedPref = getSharedPreferences(NotificationActivity.NOTIFICATION_PREF_NAME,MODE_PRIVATE);
        listener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
                if (s == NotificationActivity.NOTIFICATION_STATUS);{
                    invalidateOptionsMenu();
                }
            }
        };
        sharedPref.registerOnSharedPreferenceChangeListener(listener);


//        Setting up the recyclerView and the adapter
        mRecyclerView = findViewById(R.id.rw_tasks);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new TaskAdapter(this,this);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration divider = new DividerItemDecoration(mRecyclerView.getContext(),DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(divider);
        mEmptyView = findViewById(R.id.emptyView);
        checkEmptyView();




//        Swipe to delete
        new ItemTouchHelper(new ItemTouchHelper.SimpleCallback(0,ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onChildDraw(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {

                if(actionState == ItemTouchHelper.ACTION_STATE_SWIPE){

                    View itemView = viewHolder.itemView;
                    Paint p = new Paint();
                    p.setColor(ContextCompat.getColor(TasksActivity.this,R.color.colorPrimary));

                    if(dX > 0){
                        RectF background = new RectF((float) itemView.getLeft(), (float) itemView.getTop(), dX,(float) itemView.getBottom());
                        c.drawRect(background,p);

                    } else {

                        RectF background = new RectF((float) itemView.getRight() + dX, (float) itemView.getTop(),(float) itemView.getRight(), (float) itemView.getBottom());
                        c.drawRect(background,p);
                    }
                }
                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

//                Getting the task unique id, creating a new uri, and calling delete with the content resolver
                int id = (int) viewHolder.itemView.getTag();
                Uri uri = Uri.withAppendedPath(TaskEntry.CONTENT_URI,String.valueOf(id));
                getContentResolver().delete(uri,null,null);

//                Showing a costume toast message
                ToastUtilities.showToast(TasksActivity.this,getString(R.string.delete_task));

            }
        }).attachToRecyclerView(mRecyclerView);



//        Initializing the date set listener
        mDateSetListener = setDateSetListener();

//       Setting up the onClickListener for the displayed date text view, and within creating a DatePickerDialog
        mTvDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(mCurrentDateMillis);
                int year=cal.get(Calendar.YEAR);
                int month = cal.get(Calendar.MONTH);
                int day = cal.get(Calendar.DAY_OF_MONTH);

                DatePickerDialog dialog = new DatePickerDialog(
                        TasksActivity.this,
                        R.style.DatePickerCostume,
                        mDateSetListener,
                        year,
                        month,
                        day);
                dialog.show();
            }
        });


//        Finding the navigation arrows and setting the onClickListener;
        ImageView ivLeftArrow = findViewById(R.id.iv_leftArrow);
        ivLeftArrow.setOnClickListener(new NaviOnClickListener());
        ImageView ivRightArrow = findViewById(R.id.iv_rightArrow);
        ivRightArrow.setOnClickListener(new NaviOnClickListener());

//        Setting up the floating bottom
        FloatingActionButton fabAddTask = findViewById(R.id.fab_addTask);
        fabAddTask.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(TasksActivity.this,AddTaskActivity.class);
//                Putting the current date as extra
                intent.putExtra(INTENT_DATE_EXTRA,mCurrentDateMillis);
                startActivity(intent);
            }
        });

//        Starting the loader
        Log.i(LOG_TAG,"Day = " + mCurrentDateMillis);
        getLoaderManager().initLoader(LOADER_ID,null,this);



    }

    private void checkEmptyView() {
        if (mAdapter.getItemCount()==0 && !isLoading){
            mEmptyView.setVisibility(View.VISIBLE);
        }else {
            mEmptyView.setVisibility(View.GONE);

        }
    }

    private void activateLoadingIndicator(){
        isLoading=true;
        mProgressBar.setVisibility(View.VISIBLE);

    }

    private void deactivateLoadingIndicator(){
        isLoading=false;
        mProgressBar.setVisibility(View.GONE);
    }


    //      Helper method for creating the OnDateSetListener
    private DatePickerDialog.OnDateSetListener setDateSetListener() {
        return new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                Calendar cal = Calendar.getInstance();
                cal.set(year,month,day);
                long time = cal.getTimeInMillis();
                mCurrentDateMillis = DateUtilities.getDayInMillis(time);
                mTvDate.setText(DateUtilities.getDisplayDate(mCurrentDateMillis));
                Log.i(LOG_TAG,"Day = " + mCurrentDateMillis);
                getLoaderManager().restartLoader(LOADER_ID,null,TasksActivity.this);

            }
        };
    }

    @NonNull
    @Override
    public android.content.Loader<Cursor> onCreateLoader(int id, @Nullable Bundle args) {

//          Selecting the action according to the loader id
        switch (id) {
            case LOADER_ID:

                activateLoadingIndicator();
//          Setting up the wanted columns
                String[] projection = {
                        TaskEntry._ID,
                        TaskEntry.COLUMN_TASK_TITLE,
                        TaskEntry.COLUMN_TASK_DATE,
                        TaskEntry.COLUMN_TASK_PRIORITY,
                        TaskEntry.COLUMN_TASK_STATE};

//        Choosing the wanted tasks
                String selection = TaskEntry.COLUMN_TASK_DATE + "=?";
                String[] selectionArgs = {String.valueOf(mCurrentDateMillis)};

//        Describing the sorting order
                String sortingOrder = TaskEntry.COLUMN_TASK_STATE + " ASC, " +
                        TaskEntry.COLUMN_TASK_PRIORITY + " DESC, " +
                        TaskEntry.COLUMN_TASK_TITLE + " ASC";

//        Returning a new Cursor
                return new android.content.CursorLoader(
                        this,
                        TaskEntry.CONTENT_URI,
                        projection,
                        selection,
                        selectionArgs,
                        sortingOrder);

            case LOADRE_ID_PAST:
                Log.i(LOG_TAG, "THe count is on CreateLoader :" + mCountUnfinishedTask);

//                Returning a new cursor
                String[] projectionPast = {TaskEntry._ID, TaskEntry.COLUMN_TASK_STATE};
                String selectionPast = TaskEntry.COLUMN_TASK_STATE + "=? AND " + TaskEntry.COLUMN_TASK_DATE +"<?";
                String[] selectionArgsPast = {String.valueOf(UNFINISHED),String.valueOf(mCurrentDateMillis)};
                return new android.content.CursorLoader(
                        this,
                        TaskEntry.CONTENT_URI,
                        projectionPast,
                        selectionPast,
                        selectionArgsPast,
                        null);

            default: throw new UnsupportedOperationException("Loader is not supported: " + id);

        }
    }

    @Override
    public void onLoadFinished(android.content.Loader<Cursor> loader, Cursor data) {

        switch (loader.getId()) {
            case LOADER_ID:
//        Updating the cursor in the adapter
                mAdapter.swapCursor(data);
                mRecyclerView.setAdapter(mAdapter);
                deactivateLoadingIndicator();
                checkEmptyView();
                getLoaderManager().initLoader(LOADRE_ID_PAST,null,TasksActivity.this);
                break;
            case LOADRE_ID_PAST:
                getCount(data);
                Log.i(LOG_TAG, "Number of unfinished tasks: " + mCountUnfinishedTask);
                if (mCountUnfinishedTask>0){
                    mUnfinishedTask = true;
                    invalidateOptionsMenu();
                }
                else {
                    mUnfinishedTask = false;
                    invalidateOptionsMenu();
                }
                break;
        }
    }

//    Helper method to get the unfinished tasks count
    private void getCount(Cursor data) {
        if (data != null){
            int x = 0;
            int stateIndex = data.getColumnIndex(TaskEntry.COLUMN_TASK_STATE);
            for (data.moveToFirst();!data.isAfterLast();data.moveToNext()){
                int finished = data.getInt(stateIndex);
                if (finished ==0){
                    x++;
                }
            }
            mCountUnfinishedTask = x;
        }
    }

    @Override
    public void onLoaderReset(android.content.Loader<Cursor> loader) {


        switch(loader.getId()){

            case LOADER_ID:
//              Clearing the adapter
                mAdapter.swapCursor(null);
                mRecyclerView.setAdapter(mAdapter);
                break;
            case LOADRE_ID_PAST:
//                Resetting the values
                mUnfinishedTask = false;
                mCountUnfinishedTask = 0;
                break;
        }
    }

//    Implementing the list item click listener, opens the AddTaskActivity with the Task unique Uri
    @Override
    public void onClick(int itemId, int viewId, int state) {

//        Creating the unique Uri
        Uri uri = Uri.withAppendedPath(TaskEntry.CONTENT_URI,String.valueOf(itemId));

//        Checking if the checkBox was clicked
        if (viewId==R.id.cb_state){
            ContentValues cv  = new ContentValues();
//            Updating the state of the task
            switch (state){
                case UNFINISHED:
                    cv.put(TaskEntry.COLUMN_TASK_STATE, FINISHED);
                    getContentResolver().update(uri,cv,null,null);
                    break;
                case FINISHED:
                    cv.put(TaskEntry.COLUMN_TASK_STATE, UNFINISHED);
                    getContentResolver().update(uri,cv,null,null);
                    break;
                default: throw new IllegalArgumentException("Unknown state: " + state);
            }

        }
//        If the item was click opening the AddTaskActivity and passing the Uri of the task
        else {
        Intent intent = new Intent(TasksActivity.this,AddTaskActivity.class);
        intent.setData(uri);
        startActivity(intent);
        }
    }


    //      Costume OnClickListener object for the navigation arrows
    class NaviOnClickListener implements View.OnClickListener{

        @Override
        public void onClick(View view) {
            int id = view.getId();
            long day;
            switch(id){
//                Setting the value of the day to -1 in milliseconds
                case R.id.iv_leftArrow:
                    day = -1*(1000*60*60*24);
                    view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.arrow_anim));
                    break;
//                Setting the value of the day to 1 in milliseconds
                case R.id.iv_rightArrow:
                    day = 1000*60*60*24;
                    view.startAnimation(AnimationUtils.loadAnimation(getApplicationContext(),R.anim.arrow_anim));
                    break;
                default:
                    throw new IllegalArgumentException("Id not supported: " + id);
            }
            mCurrentDateMillis +=day;
            mTvDate.setText(DateUtilities.getDisplayDate(mCurrentDateMillis));
            getLoaderManager().restartLoader(LOADER_ID,null,TasksActivity.this);

        }
    }


    @Override
    protected void onPause() {
        super.onPause();

//        Checking if the device is rotated, if so saving the currentDate in sharedPref
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (isChangingConfigurations()){
            editor.putLong(DATE_KEY,mCurrentDateMillis);
        }
        editor.apply();
    }

//    Unregister the sharedPrefListener
    @Override
    protected void onDestroy() {
        sharedPref.unregisterOnSharedPreferenceChangeListener(listener);
        super.onDestroy();
    }

    //    Inflating the menu
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_tasks_activity,menu);
        return super.onCreateOptionsMenu(menu);
    }


//    Hiding the menu item if there are no unfinished tasks in the past
    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem infoItem = menu.findItem(R.id.menu_item_info);
        if (!mUnfinishedTask){
            infoItem.setVisible(false);
        }
        else if (mUnfinishedTask){
            infoItem.setVisible(true);
        }
        MenuItem notificationItem = menu.findItem(R.id.menu_item_notifications);

        boolean notify = sharedPref.getBoolean(NotificationActivity.NOTIFICATION_STATUS,
                getResources().getBoolean(R.bool.notification_status));
        if (notify) {
            notificationItem.setIcon(R.drawable.ic_notifications_active_svg_24dp);
        }else {
            notificationItem.setIcon(R.drawable.ic_notifications_off_svg_24dp);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
//        Creating AlertDialog when the user clicks on the icon
        if (item.getItemId()==R.id.menu_item_info){
            final AlertDialog.Builder infoDialog = new AlertDialog.Builder(TasksActivity.this);

//            Getting the string resources according to the number of unfinished task
            String message = getResources().getQuantityString(R.plurals.message_num_task,mCountUnfinishedTask,mCountUnfinishedTask);
            String setBottomString = getResources().getQuantityString(R.plurals.message_bottom_set,mCountUnfinishedTask);

//            Setting the alertDialog message
            infoDialog.setMessage(message);

//            Setting up the exit bottom
            infoDialog.setPositiveButton(getString(R.string.alert_exit_bottom), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    dialogInterface.cancel();
                }
            });

//            Setting up the set to finished bottom
            infoDialog.setNegativeButton(setBottomString, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {

//                    Setting the state value of all the tasks in the past which had 0 as state to 1
                    ContentValues cv = new ContentValues();
                    cv.put(TaskEntry.COLUMN_TASK_STATE,FINISHED);
                    String selection = TaskEntry.COLUMN_TASK_STATE + "=? AND " + TaskEntry.COLUMN_TASK_DATE +"<?";
                    String[] selectionArgs = {String.valueOf(UNFINISHED),String.valueOf(DateUtilities.getTodayInMillis())};
                    int rows = getContentResolver().update(
                            TaskEntry.CONTENT_URI,
                            cv,
                            selection,
                            selectionArgs);

//                    Displaying a toast with the number of updated tasks
                    String updateMessage = getResources().getQuantityString(R.plurals.toast_updated_task,rows,rows);
                    ToastUtilities.showToast(TasksActivity.this,updateMessage);

                }
            });

//            Setting up the show bottom
            infoDialog.setNeutralButton(getString(R.string.alert_show_bottom), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
//                    Starting the UnfinishedTasks activity
                    Intent intent = new Intent(TasksActivity.this,UnfinishedTasks.class);
                    startActivity(intent);
                }
            });

//            Creating and showing the alertDialog
            infoDialog.create();
            infoDialog.show();
        }

        if (item.getItemId() == R.id.menu_item_notifications){
            Intent intent = new Intent(TasksActivity.this,NotificationActivity.class);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }
}
