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

@RunWith(AndroidJUnit4.class)
public class MainActivityTest {

    @Rule
    public ActivityTestRule<MainActivity> mActivityRule = new ActivityTestRule<>(MainActivity.class);
    private MainActivity mActivity;
    private ImageComparator mComparator;
    private static final String GOLDEN_DIR = "golden_dir";
    private static final int COMPARE_VALUE = 1000;
    private static final int SCREEN_WAIT_TIME = 10; //In sec.

    @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();
        mComparator = new ImageComparator(mActivity);
    }

    // Test method should starts with test (Mandatory for spoon_dependencies file to execute )
    @Test
    public void testOpenMenuAndClick() {

        // Initial screenshot
        Spoon.screenshot(mActivity, "Before_Menu_Click");

        //Open menu
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Perform a click on the option
        onView(withText(R.string.action_test_trip)).perform(click());

        // Now we wait for 10 sec
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(TimeUnit.SECONDS.toMillis(SCREEN_WAIT_TIME));
        Espresso.registerIdlingResources(idlingResource);

        // Check for string mentioned in the Descendant of Recycler View
        onView(withId(R.id.recyclerView))
                .check(matches(hasDescendant(withText(Settings.TEST_TIME_BASE))));

        // Final screenshot
        Spoon.screenshot(mActivity, "After_RecyclerView_loads");
    }

    @Test
    public void testOpenMenuAndClickToCompareImages() {

        Bitmap mCurrentBeforeLoadBitmap, mCurrentAfterLoadBitmap;

        // Initial screenshot
        File beforeMenuClickFile = Spoon.screenshot(mActivity, "Before_Menu_Click");

        //Open menu
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // Perform a click on the option
        onView(withText(R.string.action_test_trip)).perform(click());

        // Now we wait for 10 sec
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(TimeUnit.SECONDS.toMillis(SCREEN_WAIT_TIME));
        Espresso.registerIdlingResources(idlingResource);

        // Check for string mentioned in the Descendant of Recycler View
        onView(withId(R.id.recyclerView));

        // Final screenshot
        File afterRecyclerViewLoadFile = Spoon.screenshot(mActivity, "After_RecyclerView_loads");

        mCurrentBeforeLoadBitmap = getBitmapFromAsset(mActivity, "1452171737275_Before_Menu_Click.png");
        mCurrentAfterLoadBitmap = getBitmapFromAsset(mActivity, "1452171748544_After_RecyclerView_loads.png");

        if (mCurrentAfterLoadBitmap != null && mCurrentBeforeLoadBitmap != null) {
            Bitmap beforeMenuClickBitmap = null, afterRecyclerViewLoadsBitmap = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                beforeMenuClickBitmap = BitmapFactory.decodeStream(new FileInputStream(beforeMenuClickFile), null, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                afterRecyclerViewLoadsBitmap = BitmapFactory.decodeStream(new FileInputStream(afterRecyclerViewLoadFile), null, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
            double compare = mComparator.doCompare(beforeMenuClickBitmap, mCurrentBeforeLoadBitmap);
            double compareAfter = mComparator.doCompare(afterRecyclerViewLoadsBitmap, mCurrentAfterLoadBitmap);

            if (compare > 0 && compare < COMPARE_VALUE) {
                Log.d("diff ", "Images may be same");
            } else if (compare == 0) {
                Log.d("diff ", "Images are same");
            } else {
                Log.d("diff ", "Images are not duplicates");
            }

            if (compareAfter > 0 && compareAfter < COMPARE_VALUE) {
                Log.d("diff ", "After_Images may be same");
            } else if (compareAfter == 0) {
                Log.d("diff ", "After_Images are same");
            } else {
                Log.d("diff ", "After_Images are not duplicates");
            }
        } else {
            Log.d("No bitmap found", "No bitmap found");
        }
    }

    /**
     * Get Bitmap from Assets.
     *
     * @param context
     * @param filePath
     * @return Bitmap of the image.
     **/
    public static Bitmap getBitmapFromAsset(Activity context, String filePath) {
        AssetManager assetManager = context.getAssets();

        InputStream istr;
        Bitmap bitmap = null;
        try {
            istr = assetManager.open(GOLDEN_DIR + "/" + filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (Exception e) {
            // Handle exception
            e.printStackTrace();
        }
        return bitmap;
    }

    // Class to wait the test runner
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
