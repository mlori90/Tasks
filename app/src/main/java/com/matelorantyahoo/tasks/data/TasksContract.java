package com.matelorantyahoo.tasks.data;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by muster on 4/7/2018.
 */

public class TasksContract {

    public static final String CONTENT_AUTHORITY ="com.matelorantyahoo.tasks";
    public static final Uri BASE_URI = Uri.parse("content://"+ CONTENT_AUTHORITY);
    public static final String PATH_TABLE = "tasks";

    public static abstract class TaskEntry implements BaseColumns{

        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_URI,PATH_TABLE);

//        Table name
        public static final String TABLE_NAME = "tasks";
//        Unique id of the task
        public static final String _ID = BaseColumns._ID;
//        Date of the task
        public static final String COLUMN_TASK_DATE = "date";
//        Priority of the task
        public static final String COLUMN_TASK_PRIORITY = "priority";
//        Title of the task
        public static final String COLUMN_TASK_TITLE = "title";
//        Optional description of the task
        public static final String COLUMN_TASK_DESCRIPTION ="description";
//        State of the task
        public static final String COLUMN_TASK_STATE = "state";

    }
}
