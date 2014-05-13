package il.org.hasadna.opentrain;

interface ScannerServiceInterface {
   
    boolean isScanning();
    
    void startScanning();
   	void stopScanning();
    
//    int getLocationCount();
//    int getAPCount();
}
