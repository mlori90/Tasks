package com.matelorantyahoo.tasks;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.TimePicker;

import com.matelorantyahoo.tasks.receiver.AlarmReceiver;
import com.matelorantyahoo.tasks.utils.DateUtilities;

import java.text.SimpleDateFormat;

import static com.matelorantyahoo.tasks.R.id.view;

public class NotificationActivity extends AppCompatActivity {


//    Variables containing the time and the status of the notifications
    private static int mHours;
    private static int mMinutes;
    private static boolean mStatus;

//    Constants containing the pref name and key values
    public static final String HOURS = "hours";
    public static final String MINUTES = "minutes";
    public static final String NOTIFICATION_STATUS="not_status";
    public static final String NOTIFICATION_PREF_NAME = "not_pref_name";

//    Constant for the pending intent request code
    public static final int PENDING_INTENT_CODE = 7954;

//    Constants for notification status
    public static final boolean NOTIFICATIONS_ON = true;
    public static final boolean NOTIFICATIONS_OFF= false;

//    Log
    private static final String LOG_TAG = NotificationActivity.class.getName();


//    Views
    Switch mSwitch;
    TextView mTvTime;

//    TimePicker listener

    private TimePickerDialog.OnTimeSetListener listener;

//    Alarm manager
    AlarmManager alarmManager;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notification);

//        Setting up the pending intent for the BroadcastReceiver
        Intent intent = new Intent(NotificationActivity.this, AlarmReceiver.class);
        final PendingIntent pendingIntent = PendingIntent.getBroadcast(
                NotificationActivity.this,PENDING_INTENT_CODE,intent,PendingIntent.FLAG_UPDATE_CURRENT);


//        Getting the shared pref and the prefEditor
         final SharedPreferences prefApp = getSharedPreferences(NOTIFICATION_PREF_NAME,MODE_PRIVATE);
         final SharedPreferences.Editor editor = prefApp.edit();

//        Getting the values from sharedPref
        mStatus = prefApp.getBoolean(NOTIFICATION_STATUS,getResources().getBoolean(R.bool.notification_status));
        mHours = prefApp.getInt(HOURS,getResources().getInteger(R.integer.hours_default));
        mMinutes = prefApp.getInt(MINUTES,getResources().getInteger(R.integer.minutes_default));

        //        Initializing the alarm manager
        alarmManager = (AlarmManager) getSystemService(ALARM_SERVICE);

//        Setting up the switch view
        mSwitch = findViewById(R.id.sw_notification);
        mSwitch.setChecked(mStatus);
        mSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b==true){
                    startNotifications(pendingIntent);
                    editor.putBoolean(NOTIFICATION_STATUS,NOTIFICATIONS_ON);
                    editor.apply();
                    mStatus = true;
                }
                else if (b==false){
                    alarmManager.cancel(pendingIntent);
                    editor.putBoolean(NOTIFICATION_STATUS,NOTIFICATIONS_OFF);
                    editor.apply();
                    mStatus = false;
                }
            }
        });

//        Setting up the time textView

        String time = getDisplayTime();
        mTvTime = findViewById(R.id.tv_time_notification);
        mTvTime.setText(time);



//        Initializing the timePicker listener

        listener = new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker timePicker, int hours, int minutes) {
//                Updating the sharedpref, the time variables and the text view
                SharedPreferences.Editor editorTime = prefApp.edit();
                editorTime.putInt(HOURS,hours);
                editorTime.putInt(MINUTES,minutes);
                editorTime.apply();
                mHours = hours;
                mMinutes = minutes;
                String date = getDisplayTime();
                mTvTime.setText(date);

//             Canceling the alarm and starting a new one
                if (mStatus) {
                    alarmManager.cancel(pendingIntent);
                    startNotifications(pendingIntent);
                }

            }
        };

//        Setting up the time picker dialog
        mTvTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                TimePickerDialog dialog = new TimePickerDialog(
                        NotificationActivity.this,
                        listener,
                        mHours,
                        mMinutes,
                        true
                );
                dialog.show();
            }
        });


    }
//    Helper method to start the alarm
    private void startNotifications(PendingIntent pendingIntent) {
        long date = DateUtilities.getNotificationDate(mHours,mMinutes);

//        Check build version and start alarm accordingly
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, date, pendingIntent);

        }
        else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, date, pendingIntent);
        }
        else {
            alarmManager.set(AlarmManager.RTC_WAKEUP, date, pendingIntent);
        }

    }

    //      Helper method to return the display time format
    public String getDisplayTime() {
        String hrs = getString(R.string.hrs);
        String hours;
        if (mHours<10){
            hours ="0"+mHours;
        } else hours = String.valueOf(mHours);
        String minutes;
        if (mMinutes<10){
            minutes = "0"+mMinutes;
        } else minutes = String.valueOf(mMinutes);

        String displayTime = hours + ":" + minutes + " " + hrs;
        return displayTime;
    }
}
