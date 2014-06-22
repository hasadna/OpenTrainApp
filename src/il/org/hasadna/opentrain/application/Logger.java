package il.org.hasadna.opentrain.application;

import android.content.Context;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import static android.os.Environment.MEDIA_MOUNTED;

/**
 * Created by Noam.m on 22/06/2014.
 */
public class Logger {

    public static boolean logFlag = true;

    private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private static FileWriter mFileWriter;

    public static void init(Context context) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            String currentDate = sdf.format(new Date());
            String mFileName = currentDate + ".txt";
            File directory = getLogsDir(context);
            File logFile = new File(directory, mFileName);
            if (!logFile.exists()) {
                logFile.createNewFile();
            }
            mFileWriter = new FileWriter(logFile, true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static File getLogsDir(Context context) {
        File appCacheDir = null;
        if (MEDIA_MOUNTED
                .equals(Environment.getExternalStorageState())) {
            appCacheDir = getExternalCacheDir(context);
        }
        if (appCacheDir == null) {
            appCacheDir = context.getCacheDir();
        }
        if (appCacheDir == null) {
            String cacheDirPath = "/data/data/" + context.getPackageName() + "/logs/";
            appCacheDir = new File(cacheDirPath);
        }
        return appCacheDir;
    }

    private static File getExternalCacheDir(Context context) {
        File dataDir = new File(new File(Environment.getExternalStorageDirectory(), "Android"), "data");
        File appCacheDir = new File(new File(dataDir, context.getPackageName()), "logs");
        if (!appCacheDir.exists()) {
            if (!appCacheDir.mkdirs()) {
                return null;
            }
            try {
                new File(appCacheDir, ".nomedia").createNewFile();
            } catch (IOException e) {

            }
        }
        return appCacheDir;
    }

    public static void wifi(String str) {
        try {
            str = sdf.format(new Date(System.currentTimeMillis())) + " wifi : " + str + "\n";
            Log.d("FileLog", str);
            mFileWriter.write(str);
            mFileWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void location(String str) {
        try {
            str = sdf.format(new Date(System.currentTimeMillis())) + " location : " + str + "\n";
            Log.d("FileLog", str);
            mFileWriter.write(str);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void submitter(String str) {
        //Log.d("FileLog", "report : " + str);
    }
}
