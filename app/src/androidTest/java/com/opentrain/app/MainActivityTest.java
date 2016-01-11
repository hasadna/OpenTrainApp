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
    private ImageComparator mComparator;

    @Before
    public void setUp() {
        mActivity = mActivityRule.getActivity();
        mComparator = new ImageComparator(mActivity);
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
    public void testOpenMenuAndClickToCompareImages() {


        Bitmap bitmap = null, bitmapAfter = null;

        Bitmap mCurrentBeforeLoadBitmap, mCurrentAfterLoadBitmap;

        // initial screenshot
        File beforeFile = Spoon.screenshot(mActivity, "Before_Menu_Click");

        //open menu
        openActionBarOverflowOrOptionsMenu(getInstrumentation().getTargetContext());

        // perform a click on the option
        onView(withText(R.string.action_test_trip)).perform(click());

        // Now we wait for 10 sec
        IdlingResource idlingResource = new ElapsedTimeIdlingResource(10000);
        Espresso.registerIdlingResources(idlingResource);

        // check for string mentioned in the Descendant of Recycler View
        onView(withId(R.id.recyclerView));

        // final screenshot
        File afterFile = Spoon.screenshot(mActivity, "After_RecyclerView_loads");

        mCurrentBeforeLoadBitmap = getBitmapFromAsset(mActivity, "golden_dir/1452171737275_Before_Menu_Click.png");
        mCurrentAfterLoadBitmap = getBitmapFromAsset(mActivity, "golden_dir/1452171748544_After_RecyclerView_loads.png");

        if (mCurrentAfterLoadBitmap != null && mCurrentBeforeLoadBitmap != null) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(beforeFile), null, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
            try {
                bitmapAfter = BitmapFactory.decodeStream(new FileInputStream(afterFile), null, options);
            } catch (Exception e) {
                e.printStackTrace();
            }
            double compare = mComparator.doCompare(bitmap, mCurrentBeforeLoadBitmap);
            double compareAfter = mComparator.doCompare(bitmapAfter, mCurrentAfterLoadBitmap);

            if (compare > 0 && compare < 1000) {
                Log.d("diff ", "Images may be same");
            } else if (compare == 0) {
                Log.d("diff ", "Images are same");
            } else {
                Log.d("diff ", "Images are not duplicates");
            }

            if (compareAfter > 0 && compareAfter < 1000) {
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
            istr = assetManager.open(filePath);
            bitmap = BitmapFactory.decodeStream(istr);
        } catch (Exception e) {
            // handle exception
            e.printStackTrace();
        }
        return bitmap;
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
