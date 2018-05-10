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
import com.matelorantyahoo.tasks.utils.DateUtilities;

/**
 * Created by muster on 4/10/2018.
 */

public class UnfinishedTasksAdapter extends RecyclerView.Adapter<UnfinishedTasksAdapter.UnfinishedTaskHolder>{


//    Context and cursor for the adapter
    private Context mContext;
    private Cursor mCursor;

//    Click listener
    private final UnfinishedTaskClickListener mClickListener;

    public interface UnfinishedTaskClickListener{
        void onClick(int itemId, int viewId, int state);
    }

    public UnfinishedTasksAdapter(Context context, UnfinishedTaskClickListener clickListener){

        mContext =context;
        mClickListener = clickListener;
    }

    @Override
    public UnfinishedTaskHolder onCreateViewHolder(ViewGroup parent, int viewType) {

//        Inflate the list item layout and return a new ViewHolder object with the inflated view as parameter
        View view = LayoutInflater.from(mContext).inflate(R.layout.list_item_unfinished,parent,false);
        return new UnfinishedTaskHolder(view);
    }

    @Override
    public void onBindViewHolder(UnfinishedTaskHolder holder, int position) {
        //        Move to the correct position
        mCursor.moveToPosition(position);

//        Getting the column indexes;
        int titleIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_TITLE);
        int idIndex = mCursor.getColumnIndex(TaskEntry._ID);
        int priorityIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_PRIORITY);
        int stateIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_STATE);
        int dateIndex = mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_DATE);

//        Getting the values
        String title = mCursor.getString(titleIndex);
        int id = mCursor.getInt(idIndex);
        float priority = mCursor.getFloat(priorityIndex);
        int state = mCursor.getInt(stateIndex);
        long rowDate = mCursor.getLong(dateIndex);
        String date = DateUtilities.getDisplayDateForList(rowDate);

//        Using the values
        holder.itemView.setTag(id);
        holder.titleTextView.setText(title);
        holder.dateTextView.setText(date);
        holder.priorityRatingBar.setRating(priority);

//        Setting up the checkBox values
        switch (state){
            case 0:
                holder.stateCheckBox.setChecked(false);
                break;
            case 1:
                holder.stateCheckBox.setChecked(true);
                break;
            default: throw new IllegalArgumentException("Value not supported: " +state);
        }


    }


//    Return the size of cursor
    @Override
    public int getItemCount() {
        if (mCursor!=null){
            return mCursor.getCount();
        }
        else return 0;
    }

    public void swapCursor(Cursor cursor){
        mCursor= cursor;
        notifyDataSetChanged();
    }

    public class UnfinishedTaskHolder extends RecyclerView.ViewHolder implements View.OnClickListener{


//        RecyclerView item views
        TextView titleTextView, dateTextView;
        RatingBar priorityRatingBar;
        CheckBox stateCheckBox;


        public UnfinishedTaskHolder(View itemView) {
            super(itemView);

//            Finding the views
            titleTextView = (TextView) itemView.findViewById(R.id.tv_task_name_unfinished);
            dateTextView = itemView.findViewById(R.id.tv_date_unfinished);
            priorityRatingBar = itemView.findViewById(R.id.rb_priority_unfinished);
            stateCheckBox = itemView.findViewById(R.id.cb_state_unfinished);

            itemView.setOnClickListener(this);
            stateCheckBox.setOnClickListener(this);


        }

        @Override
        public void onClick(View view) {

            int viewId = view.getId();
            mCursor.moveToPosition(getAdapterPosition());
            int id = mCursor.getInt(mCursor.getColumnIndex(TaskEntry._ID));
            int state = mCursor.getInt(mCursor.getColumnIndex(TaskEntry.COLUMN_TASK_STATE));
            mClickListener.onClick(id,viewId,state);
        }
    }
}
