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

//        Getting the calendar and setting the hours, minutes, seconds and milliseconds to 0
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

//        Getting the midnight time from the calendar and returning it
        long todayInMillies = calendar.getTimeInMillis();

        return todayInMillies;

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

//    Gets the starting time in milliseconds for the passed date
    public static long getDayInMillis(long date){

//        Getting the calendar and setting the time to the passed time
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);

//        Setting the hours, minutes, seconds and milliseconds to 0
        calendar.set(Calendar.HOUR_OF_DAY,0);
        calendar.set(Calendar.MINUTE,0);
        calendar.set(Calendar.SECOND,0);
        calendar.set(Calendar.MILLISECOND,0);

//        Getting from the modified calendar and returning the passed date starting time
        long dayInMillis = calendar.getTimeInMillis();

        return dayInMillis;

    }


//    Helper method to get the notification date

    public static long getNotificationDate(int hours, int minutes) {


//        Getting the calendar and time zone
        Calendar cal = Calendar.getInstance();
        TimeZone tz = TimeZone.getDefault();

//        Getting the current date and time
        long currentDateInMillis = cal.getTimeInMillis();


//        Setting the hours and minutes for the calendar
        cal.set(Calendar.HOUR_OF_DAY,hours);
        cal.set(Calendar.MINUTE,minutes);
        cal.set(Calendar.SECOND,0);
        cal.set(Calendar.MILLISECOND,0);

//        Getting the passed date and time
        long passedDate = cal.getTimeInMillis();


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
