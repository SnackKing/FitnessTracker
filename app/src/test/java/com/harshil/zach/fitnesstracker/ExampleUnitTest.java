package com.harshil.zach.fitnesstracker;

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
    public  void invalidEmailInvalidChars() throws Exception{
        boolean isValid = SignUpActivity.isEmailValid("#%@*((*");
    }
}