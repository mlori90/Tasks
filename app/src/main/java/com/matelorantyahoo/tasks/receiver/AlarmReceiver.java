package com.matelorantyahoo.tasks.receiver;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.matelorantyahoo.tasks.NotificationActivity;
import com.matelorantyahoo.tasks.R;
import com.matelorantyahoo.tasks.TasksActivity;
import com.matelorantyahoo.tasks.data.TasksContract.TaskEntry;
import com.matelorantyahoo.tasks.utils.DateUtilities;

import static android.os.Build.VERSION_CODES.N;

/**
 * Created by muster on 4/16/2018.
 */

public class AlarmReceiver extends BroadcastReceiver{

    private static final String CHANNEL_ID= "chanel_id";
    private static final int PENDING_INTENT_ID = 326;
    private static final int NOTIFICATION_ID = 795;
//    Log
    private static final String LOG_TAG = AlarmReceiver.class.getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(LOG_TAG,"OnRecieved");

        SharedPreferences sharedPreferences =
                context.getSharedPreferences(NotificationActivity.NOTIFICATION_PREF_NAME,Context.MODE_PRIVATE);

        if (sharedPreferences.getBoolean(NotificationActivity.NOTIFICATION_STATUS,
                context.getResources().getBoolean(R.bool.notification_status))){

//        Getting the cursor for the current day where there are unfinished tasks
        String [] projection = {TaskEntry._ID};
        String selection = TaskEntry.COLUMN_TASK_STATE + "=? AND " + TaskEntry.COLUMN_TASK_DATE +"<?";
        String[] selectionArgs = {String.valueOf(0),String.valueOf(DateUtilities.getTodayInMillis())};
        Cursor cursor = context.getContentResolver().query(TaskEntry.CONTENT_URI,projection,selection,selectionArgs,null);


        if (cursor!=null){
            int count = cursor.getCount();
//            If the cursor has at least one item send notification
            if (count >0){
                NotificationManager notificationManager = (NotificationManager)
                        context.getSystemService(Context.NOTIFICATION_SERVICE);

//                Checking if the Build version is equal or later the O, if so creating a notification channel
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    NotificationChannel mChannel = new NotificationChannel(
                            CHANNEL_ID,
                            context.getString(R.string.notification_chanel_name),
                            NotificationManager.IMPORTANCE_HIGH);
                    notificationManager.createNotificationChannel(mChannel);
                }

//                Building the notification
                String title = context.getResources().getQuantityString(R.plurals.notification_title,count);
                String text = context.getResources().getQuantityString(R.plurals.notification_text,count,count);
                Intent i = new Intent(context, TasksActivity.class);
                PendingIntent pendingIntent = PendingIntent.getActivity(context,PENDING_INTENT_ID,i,PendingIntent.FLAG_UPDATE_CURRENT);
                NotificationCompat.Builder builder = new NotificationCompat.Builder(context,CHANNEL_ID)
                        .setSmallIcon(R.drawable.ic_notification_icon)
                        .setAutoCancel(true)
                        .setColor(ContextCompat.getColor(context,R.color.colorPrimaryDark))
                        .setContentTitle(title)
                        .setContentText(text)
                        .setContentIntent(pendingIntent)
                        .setPriority(NotificationCompat.PRIORITY_HIGH);

//                 Sending the notification
                notificationManager.notify(NOTIFICATION_ID,builder.build());

                }

//            Closing the cursor
                cursor.close();

            }

//            Getting the hour and minute from shared pref
            int hours = sharedPreferences.getInt(NotificationActivity.HOURS,context.getResources().getInteger(R.integer.hours_default));
            int minutes = sharedPreferences.getInt(NotificationActivity.MINUTES,context.getResources().getInteger(R.integer.minutes_default));

//            Creating the notification date
            long date = DateUtilities.getNotificationDate(hours,minutes);
            Intent restartIntent = new Intent(context,AlarmReceiver.class);
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    context,NotificationActivity.PENDING_INTENT_CODE,restartIntent,PendingIntent.FLAG_UPDATE_CURRENT);

//            Getting the alarm manager

            AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);

//            Check build version and start alarm accordingly
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date, pendingIntent);

            }
            else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                alarmManager.cancel(pendingIntent);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, date, pendingIntent);
            }
            else {
                alarmManager.cancel(pendingIntent);
                alarmManager.set(AlarmManager.RTC_WAKEUP, date, pendingIntent);
            }

        }
    }
}
