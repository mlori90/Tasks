package com.matelorantyahoo.tasks;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.TextView;
import com.matelorantyahoo.tasks.data.TasksContract.TaskEntry;

/**
 * Created by muster on 4/8/2018.
 */

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder>{

    private Cursor mCursor;
    private Context mContext;

//    Click Listener object
    final private OnTaskClickedListener mOnTaskClicked;


//    Interface for the clickListener
    public interface OnTaskClickedListener{
        void onClick(int itemId, int viewId, int state);
    }

    public TaskAdapter(Context context, OnTaskClickedListener handler){
        mContext = context;

//        Initializing the click listener object
        mOnTaskClicked = handler;
    }

    @Override
    public TaskViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

//        Inflate the list item layout and return a new ViewHolder object with the inflated view as parameter
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item,parent,false);

        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(TaskViewHolder holder, int position) {

//        Move to the correct position
        mCursor.moveToPosition(position);

//        Getting the column indexes;
        int titleIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
        int idIndex = mCursor.getColumnIndex(TaskEntry._ID);
        int priorityIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_PRIORITY);
        int stateIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_STATE);

//        Getting the values
        String title = mCursor.getString(titleIndex);
        int id = mCursor.getInt(idIndex);
        float priority = mCursor.getFloat(priorityIndex);
        int state = mCursor.getInt(stateIndex);

//        Using the values
        holder.itemView.setTag(id);
        holder.titleTextView.setText(title);
        holder.priorityRatingBar.setRating(priority);
//        Setting up the checkBox value, and if the task is finished setting the alpha at 0.4
        switch (state){
            case 0:
                holder.stateBox.setChecked(false);
                holder.itemView.setAlpha(1f);
                break;
            case 1:
                holder.stateBox.setChecked(true);
                holder.itemView.setAlpha(0.4f);
                break;
            default: throw new IllegalArgumentException("Value not supported: " +state);
        }

    }




//    Returns de size of the cursor
    @Override
    public int getItemCount() {
        if (mCursor == null){
            return 0;
        }
        return mCursor.getCount();

    }

//    Swaps the cursor
    public void swapCursor(Cursor cursor){
        mCursor = cursor;
        notifyDataSetChanged();
    }

    public class TaskViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{

//        Rating bar, textView and the checkBox for the list items
        TextView titleTextView;
        RatingBar priorityRatingBar;
        CheckBox stateBox;

        public TaskViewHolder(View itemView) {
            super(itemView);

//            Finding the views in the list_item
            titleTextView = itemView.findViewById(R.id.tv_task_name);
            priorityRatingBar = itemView.findViewById(R.id.rb_priority);
            stateBox = itemView.findViewById(R.id.cb_state);

//            Setting up the on click listener for the list Item
            itemView.setOnClickListener(this);
            stateBox.setOnClickListener(this);

        }

        @Override
        public void onClick(View view) {
            int viewId = view.getId();
            mCursor.moveToPosition(getAdapterPosition());
            int id = mCursor.getInt(mCursor.getColumnIndex(TaskEntry._ID));
            int state = mCursor.getInt(mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_STATE));
            mOnTaskClicked.onClick(id, viewId, state);
        }
    }
}
