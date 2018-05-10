package com.matelorantyahoo.tasks.utils;

import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

/**
 * Created by muster on 4/6/2018.
 */

public class DateUtilities {

    private static String LOG_TAG = DateUtilities.class.getName();


//    Getting the display dateFormat
    public static DateFormat getDisplayDateFormat() {
        DateFormat dateFormat;
//        Display format for hu
        if (Locale.getDefault().getLanguage() == "hu") {
            dateFormat = new SimpleDateFormat("EEE, MMM dd");
        }
//        Standard display format
        else {
            dateFormat = new SimpleDateFormat("EEE, dd MMM");
        }

        return dateFormat;
    }


// Getting the display date
    public static String getDisplayDate(long date){
        DateFormat df = getDisplayDateFormat();
        String returnDate = df.format(date);
        if (Locale.getDefault().getLanguage() == "hu"){
//            Costume format for hu
            returnDate = formatAgain(returnDate);
        }

        return returnDate;
    }

//    Getting the display date for the ListItem
    public static String getDisplayDateForList(long date){
        DateFormat df = DateFormat.getDateInstance(DateFormat.SHORT,Locale.getDefault());
        String returnDate = df.format(date);
        return returnDate;
    }



//    Getting the current days midnight time in millis
    public static long getTodayInMillis(){

//        Getting the current utcTime
        long utcNow = System.currentTimeMillis();

//        Getting the midnight time in millis for today
        long todayInMillis = getDayInMillis(utcNow);

        return todayInMillis;
    }



//    Costume format for hungarians
    private static String formatAgain(String todayDate) {
            String newFormat = null;

//                Monday
            if (todayDate.startsWith("H")) {
                newFormat = todayDate.replace("H", "Hétfő");

//                Tuesday
            } else if (todayDate.startsWith("K")) {
                newFormat = todayDate.replace("K", "Kedd");

//                Wednesday
            } else if (todayDate.startsWith("Sze")) {
                newFormat = todayDate.replace("Sze", "Szerda");

//                Thursday
            } else if (todayDate.startsWith("Cs")) {
                newFormat = todayDate.replace("Cs", "Csütörtök");

//                Friday
            } else if (todayDate.startsWith("P")) {
                newFormat = todayDate.replace("P", "Péntek");

//                Saturday
            } else if (todayDate.startsWith("Szo")) {
                newFormat = todayDate.replace("Szo", "Szombat");

//                Sunday
            } else if (todayDate.startsWith("V")) {
                newFormat = todayDate.replace("V", "Vasárnap");
            }

            return newFormat;
    }

    public static long getDayInMillis(long date){

        //        Getting the timeZone of the user
        TimeZone tz = TimeZone.getDefault();

//        Getting the offset in millis
        long offsetMillis = tz.getOffset(date);

//        Getting the time with offset in millis
        long timeWithOffset = date + offsetMillis;

//        Converting the millis to days
        long days = TimeUnit.MILLISECONDS.toDays(timeWithOffset);

//        Converting the days back to millis
        long dayInMillis = TimeUnit.DAYS.toMillis(days);

//        Returning the midnight millis of the day
        return dayInMillis;
    }


//    Helper method to get the notification date

    public static long getNotificationDate(int hours, int minutes) {


//        Getting the calendar and time zone
        Calendar cal = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();

//        Getting the current date and time
        long currentDateInMillisWithoutOffset = cal.getTimeInMillis();
        long offsetMillisForNow = tz.getOffset(currentDateInMillisWithoutOffset);
        long currentDateInMillis = currentDateInMillisWithoutOffset + offsetMillisForNow;


//        Setting the hours and minutes for the calendar
        cal.set(Calendar.HOUR_OF_DAY,hours);
        cal.set(Calendar.MINUTE,minutes);
        cal.set(Calendar.SECOND,0);

//        Getting the passed date and time
        long dateWithoutOffset = cal.getTimeInMillis();
        long offsetMillisForDate = tz.getOffset(dateWithoutOffset);
        long passedDate = dateWithoutOffset + offsetMillisForDate;


//        Checking if the passed date is in the past, if yes then adding 1 day to the passed date
        long finalDate;
        if (currentDateInMillis>=passedDate){
             finalDate = passedDate + (24*60*60*1000);
        }
        else
             finalDate = passedDate;
        return finalDate;
    }
}
