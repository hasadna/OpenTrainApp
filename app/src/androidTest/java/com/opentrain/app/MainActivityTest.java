package com.opentrain.app;

import android.os.Environment;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.opentrain.app.model.Settings;
import com.opentrain.app.view.MainActivity;
import com.squareup.spoon.Spoon;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.InstrumentationRegistry.getInstrumentation;
import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.hasDescendant;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

/**
 * Created by Wide Vision on 28/12/15.
 */

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule =
            new ActivityTestRule<>(MainActivity.class);

    private MainActivity mActivity;

    @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();

    }

    // Test method should starts with test (Mandatory for spoon_dependencies file to execute )
    @Test
    public void testOpenMenuAndClick() {

        // initial screenshot
        Spoon.screenshot(mActivity, "Before_Menu_Click");

        //open menu
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // perform a click on the option
        onView(withText(R.string.action_test_trip)).perform(click());

        // Now we wait for 10 sec
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(10000);
        Espresso.registerIdlingResources(idlingResource);

        // check for string mentioned in the Descendant of Recycler View
        onView(withId(R.id.recyclerView))
                .check(matches(hasDescendant(withText(Settings.TEST_TIME_BASE))));

        // final screenshot
        Spoon.screenshot(mActivity, "After_RecyclerView_loads");

    }

    @Test
    public void testOpenMenuAndClickForFailure() {

        // initial screenshot
        Spoon.screenshot(mActivity, "Before_Menu_Click");

        //open menu
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // perform a click on the option
        onView(withText(R.string.action_test_trip)).perform(click());

        // Now we wait for 10 sec
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(10000);
        Espresso.registerIdlingResources(idlingResource);

        // check for string mentioned in the Descendant of Recycler View
        onView(withId(R.id.recyclerView))
                .check(matches(hasDescendant(withText(Settings.TEST_TIME_BASE_FOR_FAILURE))));

        // final screenshot
        Spoon.screenshot(mActivity, "After_RecyclerView_loads");

    }

    // class to wait the test runner
    public class ElapsedTimeIdlingResource implements IdlingResource {
        private final long startTime;
        private final long waitingTime;
        private ResourceCallback resourceCallback;

        public ElapsedTimeIdlingResource(long waitingTime) {
            this.startTime = System.currentTimeMillis();
            this.waitingTime = waitingTime;
        }

        @Override
        public String getName() {
            return ElapsedTimeIdlingResource.class.getName() + ":" + waitingTime;
        }

        @Override
        public boolean isIdleNow() {
            long elapsed = System.currentTimeMillis() - startTime;
            boolean idle = (elapsed >= waitingTime);
            if (idle) {
                resourceCallback.onTransitionToIdle();
            }
            return idle;
        }

        @Override
        public void registerIdleTransitionCallback(
                ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }
    }

}
