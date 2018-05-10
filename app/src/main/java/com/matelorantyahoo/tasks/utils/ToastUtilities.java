package com.matelorantyahoo.tasks.utils;

import android.content.Context;
import android.graphics.Color;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.matelorantyahoo.tasks.R;
import com.matelorantyahoo.tasks.TasksActivity;

/**
 * Created by muster on 4/9/2018.
 */

public class ToastUtilities {

    public ToastUtilities(){

    }

    public static void showToast(Context context, String text){
        Toast toast = Toast.makeText(context,text, Toast.LENGTH_SHORT);
        View toastView = toast.getView();
        TextView toastMessage = toastView.findViewById(android.R.id.message);
        toastMessage.setGravity(Gravity.CENTER);
        toastMessage.setTextSize(20);
        toastMessage.setTextColor(Color.WHITE);
        toastMessage.setPadding(80,8,80,8);
        toastView.setBackground(ContextCompat.getDrawable(context,R.drawable.rounded_corner));
        toast.show();
    }

}
