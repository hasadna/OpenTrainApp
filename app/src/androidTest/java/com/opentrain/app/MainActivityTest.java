package com.opentrain.app;

import android.app.Activity;

import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.test.espresso.Espresso;
import android.support.test.espresso.IdlingResource;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import com.opentrain.app.model.Settings;
import com.opentrain.app.utils.ImageComparator;
import com.opentrain.app.view.MainActivity;
import com.squareup.spoon.Spoon;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.File;
import java.io.FileInputStream;

import java.io.InputStream;
import java.util.concurrent.TimeUnit;

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

// Note: Test methods should start with test in order for Spoon to run the test.
@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    private MainActivity mActivity;
    private ImageComparator mComparator;
    private static final String GOLDEN_DIR = "golden_dir";
    private static final int IMAGE_PERCENTAGE_MATCH = 100;
    private static final int SCREEN_WAIT_TIME_SECONDS = 10;
    private static final BitmapFactory.Options BITMAP_OPTIONS = new BitmapFactory.Options();

    @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();
        mComparator = new ImageComparator();
    }

    @Test
    public void testOpenMenuAndClick() {

        // Initial screenshot
        Spoon.screenshot(mActivity, "Before_Menu_Click");

        // Open menu
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Perform a click on the option
        onView(withText(R.string.action_test_trip)).perform(click());

        // Now we wait for SCREEN_WAIT_TIME_SECONDS
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(TimeUnit.SECONDS.toMillis(SCREEN_WAIT_TIME_SECONDS));
        Espresso.registerIdlingResources(idlingResource);

        // Check for string mentioned in the Descendant of Recycler View
        onView(withId(R.id.recyclerView))
                .check(matches(hasDescendant(withText(Settings.TEST_TIME_BASE))));

        // Final screenshot
        Spoon.screenshot(mActivity, "After_RecyclerView_loads");
    }

    @Test
    public void testOpenMenuAndClickToCompareImages() {

        Bitmap mBeforeMenuClickGoldenBitmap, mAfterRecyclerViewLoadsGoldenBitmap;

        // Initial screenshot
        Bitmap mBeforeMenuClickBitmap = takeScreenshot("Before_Menu_Click");

        //Open menu
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Perform a click on the option
        onView(withText(R.string.action_test_trip)).perform(click());

        // Now we wait for SCREEN_WAIT_TIME_SECONDS
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(TimeUnit.SECONDS.toMillis(SCREEN_WAIT_TIME_SECONDS));
        Espresso.registerIdlingResources(idlingResource);

        // Check for string mentioned in the Descendant of Recycler View
        onView(withId(R.id.recyclerView));

        // Final screenshot
        Bitmap mAfterRecyclerViewLoadsBitmap = takeScreenshot("After_RecyclerView_loads");

        mBeforeMenuClickGoldenBitmap = getBitmapFromAsset(mActivity, "1452171737275_Before_Menu_Click.png");
        mAfterRecyclerViewLoadsGoldenBitmap = getBitmapFromAsset(mActivity, "1452171748544_After_RecyclerView_loads.png");

        if (mAfterRecyclerViewLoadsGoldenBitmap != null && mBeforeMenuClickGoldenBitmap != null) {
            BITMAP_OPTIONS.inPreferredConfig = Bitmap.Config.ARGB_8888;

            double match = mComparator.percentageMatch(mBeforeMenuClickBitmap, mBeforeMenuClickGoldenBitmap);
            double matchAfter = mComparator.percentageMatch(mAfterRecyclerViewLoadsBitmap, mAfterRecyclerViewLoadsGoldenBitmap);

            if (match == IMAGE_PERCENTAGE_MATCH) {
                Log.d("diff ", "Images same");
            } else {
                Log.d("diff ", "Images are not duplicates");
            }

            if (matchAfter == IMAGE_PERCENTAGE_MATCH) {
                Log.d("diff ", "After_Images same");
            } else {
                Log.d("diff ", "After_Images are not duplicates");
            }
        } else {
            Log.d("No bitmap found", "No bitmap found");
        }
    }

    public static Bitmap getBitmapFromAsset(Activity context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream inputStream;
        Bitmap bitmap = null;
        try {
            inputStream = assetManager.open(GOLDEN_DIR + "/" + filePath);
            bitmap = BitmapFactory.decodeStream(inputStream);
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace();
        }
        return bitmap;
    }

    private Bitmap takeScreenshot(String name) {
        File file = Spoon.screenshot(mActivity, name);
        Bitmap bitmap = null;
        try {
            bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, BITMAP_OPTIONS);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return bitmap;
    }

    // This class makes the test wait for a specified amount of time.
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
        public void registerIdleTransitionCallback(ResourceCallback resourceCallback) {
            this.resourceCallback = resourceCallback;
        }
    }
}