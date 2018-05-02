package com.harshil.zach.fitnesstracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.github.lzyzsd.circleprogress.DonutProgress;
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
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.lang.reflect.Array;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.concurrent.TimeUnit;

/**
 * Created by Zach on 4/28/2018.
 */

public class AlarmReceiver extends BroadcastReceiver {

    public class CustomComparator implements Comparator<Rank> {
        @Override
        public int compare(Rank o1, Rank o2) {
            return o1.level() - (o2.level());
        }
    }
    final String TAG = "Receiver";
    public static boolean testFlag = false;
    int userExp;
    long lastCheckedSteps;
    int userRank;
    int totalSteps;
    ArrayList<Rank> ranks = new ArrayList<>();
    ArrayList<Challenge> challenges = new ArrayList<>();
    ArrayList<String> friendIds = new ArrayList<>();
    Rank userNextRank;
    FirebaseUser user;
    String userName;

    @Override
    public void onReceive(final Context context, Intent intent) {
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
                                    addExperience(total,context);
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
    public void addExperience(final long total, final Context context){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
       final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();

        mDatabase.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Integer> completed = new ArrayList<>();
                int userRank = dataSnapshot.child("Users").child(user.getUid()).child("rank").getValue(Integer.class);
                userName = dataSnapshot.child("Users").child(user.getUid()).child("name").getValue(String.class);
                //get completed challenges
                for (DataSnapshot postSnapshot : dataSnapshot.child("Users").child(user.getUid()).child("Completed").getChildren()) {
                    Challenge currentCompleted = postSnapshot.getValue(Challenge.class);
                    completed.add(currentCompleted.getId());
                }
                //get list of ranks
                for (DataSnapshot postSnapshot : dataSnapshot.child("Ranks").getChildren()) {
                    Rank current = postSnapshot.getValue(Rank.class);
                    ranks.add(current);
                }
                Collections.sort(ranks,new AlarmReceiver.CustomComparator());
                //go through all challenges and only consider incomplete challenges.
                for (DataSnapshot postSnapshot : dataSnapshot.child("Challenges").getChildren()) {
                    Challenge currentChallenge = postSnapshot.getValue(Challenge.class);
                    if(!completed.contains(currentChallenge.getId())){
                        challenges.add(currentChallenge);
                    }
                }
                for(DataSnapshot postSnapshot: dataSnapshot.child("Users").child(user.getUid()).child("FriendedBy").getChildren()){
                    String friendId = postSnapshot.getValue(String.class);
                    friendIds.add(friendId);
                }
                String update = dataSnapshot.child("Users").child(user.getUid()).child("Updates").child("latest").getValue(String.class);
                if(update == null){
                    update = "No new updates";
                }

                if(ranks.size() != 0){
                    userNextRank = ranks.get(userRank);
                }





                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
                //get day of last check in
                SharedPreferences sharedPref= context.getSharedPreferences("mypref", 0);
                long lastCheckedSteps = sharedPref.getLong("lastChecked", 0);
                String datePlaced = sharedPref.getString("datePlaced","");
                Date strDate = new Date();
                Date now = null;
                String nowString = sdf.format(new Date());
                if(datePlaced.equals("")){
                    datePlaced = sdf.format(new Date());
                }
                //convert dates to simple date format
                try {
                    strDate = sdf.parse(datePlaced);
                    now = sdf.parse(nowString);
                } catch (ParseException e) {
                    e.printStackTrace();
                    strDate = new Date();
                }

                //last time steps were checked in was yesterday. Reset counter
                if (now.after(strDate)) {
                    lastCheckedSteps = 0;
                }
                int exp = (int) total - (int)lastCheckedSteps;
                //subtract different in steps to prevent duplicate xp awards
                int totalSteps = dataSnapshot.child("Users").child(user.getUid()).child("totalSteps").getValue(Integer.class);
                userExp = dataSnapshot.child("Users").child(user.getUid()).child("xp").getValue(Integer.class);
                mDatabase.child("Users").child(user.getUid()).child("totalSteps").setValue(totalSteps + exp);
                totalSteps += exp;
                //currently 100 steps is 1 xp
                exp = exp/100;
                mDatabase.child("Users").child(user.getUid()).child("xp").setValue(userExp + exp);
                userExp += exp;

                //update UI and potentially rank
               boolean over100 =  calculatePercent();
               if(over100){
                   increaseRank();
               }
                Date c = Calendar.getInstance().getTime();
                String formattedDate = sdf.format(c);
                //update counter for next check in
                SharedPreferences.Editor editor= sharedPref.edit();
                editor.putLong("lastChecked", total);
                editor.putString("datePlaced",formattedDate);
                editor.apply();
                //check challenges
                checkChallenges(total);

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

    }
    private boolean calculatePercent(){
        boolean over100 = false;
        float decimal = (float)userExp/userNextRank.getXp();
        float percent = decimal * 100;
        if(percent >= 100){
            over100 = true;
        }
        return over100;
    }
    private void increaseRank(){
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        mDatabase.child("Users").child(user.getUid()).child("rank").setValue(userNextRank.level());
        String notification = userName + " just reached rank " + Integer.toString(userNextRank.level());
        for(String userId: friendIds){
            mDatabase.child("Users").child(userId).child("Updates").child("latest").setValue(notification);
        }

    }

    private void checkChallenges(long total){
        int i = 0;
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        final DatabaseReference mDatabase = FirebaseDatabase.getInstance().getReference();
        float maxProgress = -1;
        Challenge closestChallenge = null;
        while(i < challenges.size()){
            Challenge current = challenges.get(i);
            int requirement = current.getNumSteps();
            float currentProgress = (float) total/requirement;
            float percent = currentProgress * 100;
            //current challenge is closest to completion
            //challenge completed
            if(total >= requirement){
                challenges.remove(i);
                int exp = current.getXp();
                mDatabase.child("Users").child(user.getUid()).child("xp").setValue(userExp + exp);
                mDatabase.child("Users").child(user.getUid()).child("Completed").child("Challenge" + current.getId()).setValue(current);
                userExp += exp;
                calculatePercent();

            }
            i++;
        }

    }



}
