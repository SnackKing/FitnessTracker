package com.harshil.zach.fitnesstracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.fitness.Fitness;
import com.google.android.gms.fitness.data.DataPoint;
import com.google.android.gms.fitness.data.DataSet;
import com.google.android.gms.fitness.data.DataSource;
import com.google.android.gms.fitness.data.DataType;
import com.google.android.gms.fitness.data.Field;
import com.google.android.gms.fitness.request.DataUpdateRequest;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zach on 4/28/2018.
 */

public class AlarmReceiver extends BroadcastReceiver {
    final String TAG = "Receiver";
    public static boolean testFlag = false;

    @Override
    public void onReceive(final Context context, Intent intent) {
        Toast.makeText(context, "Pushing Data...", Toast.LENGTH_LONG).show();
        String action = intent.getAction();
        if(action != null && action.equals("android.intent.action.BOOT_COMPLETED")){
            Log.i(TAG, "boot received, starting alarm");
            setAlarm(context);
        }
            Fitness.getHistoryClient(context, GoogleSignIn.getLastSignedInAccount(context))
                    .readDailyTotal(DataType.TYPE_STEP_COUNT_DELTA)
                    .addOnSuccessListener(
                            new OnSuccessListener<DataSet>() {
                                @Override
                                public void onSuccess(DataSet dataSet) {
                                    long total =
                                            dataSet.isEmpty()
                                                    ? 0
                                                    : dataSet.getDataPoints().get(0).getValue(Field.FIELD_STEPS).asInt();
                                    Log.i(TAG, "Total steps: " + total);
                                    Calendar calendar = Calendar.getInstance();
                                    SimpleDateFormat format1 = new SimpleDateFormat("yyyy-MM-dd");
                                    //calendar.add(Calendar.DAY_OF_YEAR, -7);
                                    String formattedDate = format1.format(calendar.getTime());
                                    FirebaseApp.initializeApp(context);
                                    DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
                                    FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                    mDatabase.child("Users").child(user.getUid()).child("History").child(formattedDate).setValue(total);
                                }
                            })
                    .addOnFailureListener(
                            new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Log.w(TAG, "There was a problem getting the step count.", e);

                                }
                            });



    }
    public void setAlarm(Context context)
    {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent alarmIntent = PendingIntent.getBroadcast(context, 1, intent, 0);
        AlarmManager am =( AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        am.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + (3*AlarmManager.INTERVAL_HOUR),
                (3*AlarmManager.INTERVAL_HOUR), alarmIntent);
    }


}
