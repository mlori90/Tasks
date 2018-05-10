package com.matelorantyahoo.tasks;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.RectF;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import com.matelorantyahoo.tasks.data.TasksContract.TaskEntry;
import com.matelorantyahoo.tasks.utils.DateUtilities;
import com.matelorantyahoo.tasks.utils.ToastUtilities;

public class UnfinishedTasks extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>,
        UnfinishedTasksAdapter.UnfinishedTaskClickListener{

    private RecyclerView mRecyclerView;
    private UnfinishedTasksAdapter mAdapter;

    //    State int constants
    private static final int UNFINISHED = 0;
    private static final int FINISHED = 1;

//    Loader id
    private static final int LOADER_ID= 248;

//    Loader indicator
    private ProgressBar mProgressBar;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_unfinished_tasks);


//        Setting up the recyclerView and adapter
        mProgressBar = findViewById(R.id.pb_task_unfinished);
        mProgressBar.setVisibility(View.VISIBLE);
        mRecyclerView = findViewById(R.id.rv_unfinished_tasks);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setHasFixedSize(true);
        mAdapter = new UnfinishedTasksAdapter(this,this);
        mRecyclerView.setAdapter(mAdapter);
        DividerItemDecoration divider = new DividerItemDecoration(mRecyclerView.getContext(),DividerItemDecoration.VERTICAL);
        mRecyclerView.addItemDecoration(divider);
        getLoaderManager().initLoader(LOADER_ID,null,this);

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
                    p.setColor(ContextCompat.getColor(UnfinishedTasks.this,R.color.colorPrimary));

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
                ToastUtilities.showToast(UnfinishedTasks.this,getString(R.string.delete_task));

            }
        }).attachToRecyclerView(mRecyclerView);


    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {

//        Projection for the cursor
        String[] projection = {
                TaskEntry._ID,
                TaskEntry.COLUMN_TASK_TITLE,
                TaskEntry.COLUMN_TASK_DATE,
                TaskEntry.COLUMN_TASK_PRIORITY,
                TaskEntry.COLUMN_TASK_STATE};

//        Selection and selection args for the cursor
        String selection = TaskEntry.COLUMN_TASK_STATE + "=? AND " + TaskEntry.COLUMN_TASK_DATE +"<?";
        String[] selectionArgs = {String.valueOf(UNFINISHED),String.valueOf(DateUtilities.getTodayInMillis())};

//        Describing the sorting order
        String sortingOrder = TaskEntry.COLUMN_TASK_DATE + " DESC, " +
                TaskEntry.COLUMN_TASK_PRIORITY + " DESC, " +
                TaskEntry.COLUMN_TASK_TITLE + " ASC";
        return new CursorLoader(this,TaskEntry.CONTENT_URI,projection,selection,selectionArgs,sortingOrder);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {

//        Updating the cursor
        mAdapter.swapCursor(cursor);
        mRecyclerView.setAdapter(mAdapter);
        mProgressBar.setVisibility(View.GONE);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {

//        Clearing the adapter
        mAdapter.swapCursor(null);
        mRecyclerView.setAdapter(mAdapter);

    }

    @Override
    public void onClick(int itemId, int viewId, int state) {

//        Creating the unique Uri
        Uri uri = Uri.withAppendedPath(TaskEntry.CONTENT_URI,String.valueOf(itemId));

//        Checking if the checkBox was clicked
        if (viewId==R.id.cb_state_unfinished){
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
            Intent intent = new Intent(UnfinishedTasks.this,AddTaskActivity.class);
            intent.setData(uri);
            startActivity(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_unfinished_task,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId()==R.id.menu_item_all_done){

//            Setting the state value of all the tasks in the past which had 0 as state to 1
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
            ToastUtilities.showToast(UnfinishedTasks.this,updateMessage);
            finish();

        }
        return super.onOptionsItemSelected(item);
    }
}
