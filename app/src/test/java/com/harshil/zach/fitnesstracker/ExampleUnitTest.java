package com.harshil.zach.fitnesstracker;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;

import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
public class ExampleUnitTest {
    @Test
    public void validEmail() throws Exception {
        boolean isValid = SignUpActivity.isEmailValid("allegretti.3@osu.edu");
        assertTrue(isValid);
    }
    @Test
    public  void invalidEmailMissingAt() throws Exception{
        boolean isValid = SignUpActivity.isEmailValid("allegretti.3osu.edu");
        assertFalse(isValid);
    }
    @Test
    public void invalidEmailInvalidChars() throws Exception{
        boolean isValid = SignUpActivity.isEmailValid("#%@*((*");
    }
    @Test
    public void timeDifferentialNormal() throws Exception{
        String timeDiff = RunningResultsActivity.getStringTimeTaken("3:20","10:00");
        System.out.println(timeDiff);
        assertTrue(timeDiff.equals("6:40"));
    }
    @Test
    public void timeDifferentialDoubleDigits() throws Exception{
        String timeDiff = RunningResultsActivity.getStringTimeTaken("10:30","15:00");
        System.out.println(timeDiff);
        assertTrue(timeDiff.equals("4:30"));
    }
    @Test
    public void timeDifferentialZeroSecondsLeft() throws Exception{
        String timeDiff = RunningResultsActivity.getStringTimeTaken("5:00","15:00");
        System.out.println(timeDiff);
        assertTrue(timeDiff.equals("10:00"));
    }
    @Test
    public void timeDifferentialDoubleDigitsPassed() throws Exception{
        String timeDiff = RunningResultsActivity.getStringTimeTaken("1:30","15:00");
        System.out.println(timeDiff);
        assertTrue(timeDiff.equals("13:30"));
    }
    @Test
    public void timeDifferentialUnder1MinuteAlmostExpired() throws Exception{
        String timeDiff = RunningResultsActivity.getStringTimeTaken("0:01","1:00");
        System.out.println(timeDiff);
        assertTrue(timeDiff.equals("0:59"));
    }
    @Test
    public void timeDifferentialUnder1Minute() throws Exception{
        String timeDiff = RunningResultsActivity.getStringTimeTaken("0:13","1:00");
        System.out.println(timeDiff);
        assertTrue(timeDiff.equals("0:47"));
    }

}