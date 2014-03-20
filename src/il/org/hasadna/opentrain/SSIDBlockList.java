package il.org.hasadna.opentrain;

import android.net.wifi.ScanResult;

final class SSIDBlockList {
    private static final String[] PREFIX_LIST = {
        // Mobile devices
        "AndroidAP",
        "AndroidHotspot",
        "Android Hotspot",
        "barnacle", // Android tether app
        "Galaxy Note",
        "Galaxy S",
        "Galaxy Tab",
        "HTC ",
        "iPhone",
        "LG-MS770",
        "LG-MS870",
        "LG VS910 4G",
        "LG Vortex",
        "MIFI",
        "MiFi",
        "myLGNet",
        "myTouch 4G Hotspot",
        "NOKIA Lumia",
        "PhoneAP",
        "SCH-I",
        "Sprint MiFi",
        "Verizon ",
        "Verizon-",
        "VirginMobile MiFi",
        "VodafoneMobileWiFi-",
        "FirefoxHotspot",

        // Transportation Wi-Fi
        /*"ac_transit_wifi_bus",
        "AmtrakConnect",
        "Amtrak_",
        "amtrak_",
        "GBUS",
        "GBusWifi",
        "gogoinflight", // Gogo in-flight WiFi
        "SF Shuttle Wireless",
        "ShuttleWiFi",
        "Southwest WiFi", // Southwest Airlines in-flight WiFi
        "SST-PR-1", // Sears Home Service van hotspot?!
        "wifi_rail", // BART
        "egged.co.il", // Egged transportation services (Israel)
        "gb-tours.com", // GB Tours transportation services (Israel)
        "ISRAEL-RAILWAYS",
        "Omni-WiFi", // Omnibus transportation services (Israel)*/
    };

    private static final String[] TRAIN_INDICATORS = {
        // Transportation Wi-Fi
        "ISRAEL-RAILWAYS",
        "S-ISRAEL-RAILWAYS",
        "keydars", // TODO: Remove before launch!
        "Bartals", // TODO: Remove before launch!
        "CampusGuest", //TODO: Remove before launch!
        "zooni" ,//TODO: Remove before launch!
        "PeerSport",////TODO: Remove before launch!
        "Galina"//TODO: Remove before launch!
    };

    private static final String[] SUFFIX_LIST = {
        // Mobile devices
        "iPhone",
        "iphone",
        "MIFI",
        "MIFI",
        "MiFi",
        "Mifi",
        "mifi",
        "mi-fi",
        "MyWi",
        "Phone",
        "Portable Hotspot",
        "Tether",
        "tether",

        // Google's SSID opt-out
        "_nomap",
    };

    private SSIDBlockList() {
    }

    static boolean contains(ScanResult scanResult) {
        String SSID = scanResult.SSID;
        if (SSID == null) {
            return true; // no SSID?
        }

        for (String prefix : PREFIX_LIST) {
            if (SSID.startsWith(prefix)) {
                return true; // blocked!
            }
        }

        for (String suffix : SUFFIX_LIST) {
            if (SSID.endsWith(suffix)) {
                return true; // blocked!
            }
        }

        return false; // OK
    }
    
    static boolean trainIndicatorsContain(ScanResult scanResult) {
    	//android.os.Debug.waitForDebugger();
        String SSID = scanResult.SSID;
        if (SSID == null) {
            return false; // no SSID?
        }
        for (String prefix : TRAIN_INDICATORS) {
            if (SSID.startsWith(prefix)) {
                return true; // Train indicator!
            }
        }
        return false; // Not train indicator
    }
}
