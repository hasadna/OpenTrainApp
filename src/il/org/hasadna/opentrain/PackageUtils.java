package il.org.hasadna.opentrain;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;

final class PackageUtils {
    private PackageUtils() {
    }

    static String getAppVersion(Context context) {
        PackageManager pm = context.getPackageManager();
        try {
            return pm.getPackageInfo("il.org.hasadna.opentrain", 0).versionName;
        } catch (NameNotFoundException e) {
            throw new IllegalArgumentException(e);
        }
    }

    static String getMetaDataString(Context context, String name) {
        PackageManager pm = context.getPackageManager();
        String packageName = context.getPackageName();

        ApplicationInfo ai;
        try {
            ai = pm.getApplicationInfo(packageName, PackageManager.GET_META_DATA);
        } catch (NameNotFoundException e) {
            throw new IllegalArgumentException(e);
        }

        return (String) ai.metaData.get(name);
    }
}
