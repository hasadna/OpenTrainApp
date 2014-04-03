package il.org.hasadna.opentrain;

interface ScannerServiceInterface {
    boolean isScanning();
    void startScanning();
    void startWifiScanningOnly();
    void pauseScanning();
    void stopScanning();
    int getLocationCount();
    int getAPCount();
  //  long getLastUploadTime();
  //  long getReportsSent();
 //   long getLastTrainIndicationTime();
}
