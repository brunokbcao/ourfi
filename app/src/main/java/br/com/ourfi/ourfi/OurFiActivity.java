package br.com.ourfi.ourfi;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import java.util.List;


public class OurFiActivity extends AppCompatActivity {

    private static final String TAG = "ourfi";
    private WifiManager wifiManager;
    private String connectedSsidName;
    private List<ScanResult> scanResults;
    private int res;

    public void connectToAP(String networkSSID, String networkPass) {
        Log.i(TAG, "* connectToAP");

        WifiConfiguration wifiConfiguration = new WifiConfiguration();

        Log.d(TAG, "# password " + networkPass);

        for (ScanResult result : scanResults) {
            if (result.SSID.equals(networkSSID)) {

                String securityMode = getScanResultSecurity(result);

                if (securityMode.equalsIgnoreCase("OPEN")) {

                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    int res = wifiManager.addNetwork(wifiConfiguration);
                    Log.d(TAG, "# add Network returned " + res);

                    boolean b = wifiManager.enableNetwork(res, true);
                    Log.d(TAG, "# enableNetwork returned " + b);

                    wifiManager.setWifiEnabled(true);

                } else if (securityMode.equalsIgnoreCase("WEP")) {

                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.wepKeys[0] = "\"" + networkPass + "\"";
                    wifiConfiguration.wepTxKeyIndex = 0;
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    int res = wifiManager.addNetwork(wifiConfiguration);
                    Log.d(TAG, "### 1 ### add Network returned " + res);

                    boolean b = wifiManager.enableNetwork(res, true);
                    Log.d(TAG, "# enableNetwork returned " + b);

                    wifiManager.setWifiEnabled(true);
                }

                wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                wifiConfiguration.preSharedKey = "\"" + networkPass + "\"";
                wifiConfiguration.hiddenSSID = true;
                wifiConfiguration.status = WifiConfiguration.Status.ENABLED;
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                wifiConfiguration.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                wifiConfiguration.allowedProtocols.set(WifiConfiguration.Protocol.WPA);

                res = wifiManager.addNetwork(wifiConfiguration);
                Log.d(TAG, "### 2 ### add Network returned " + res);

                wifiManager.enableNetwork(res, true);

                /*boolean changeHappen = wifiManager.saveConfiguration();

                if(res != -1 && changeHappen){
                    Log.d(TAG, "### Change happen");

                    connectedSsidName = networkSSID;

                }else{
                    Log.d(TAG, "*** Change NOT happen");
                }*/

                wifiManager.setWifiEnabled(true);
            }
        }
    }

    public String getScanResultSecurity(ScanResult scanResult) {
        Log.i(TAG, "* getScanResultSecurity");

        final String cap = scanResult.capabilities;
        final String[] securityModes = { "WEP", "PSK", "EAP" };

        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }

        return "OPEN";
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_our_fi);

        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        wifiManager.startScan();
        scanResults = wifiManager.getScanResults();
        for (ScanResult sc : scanResults) {
            if (sc.SSID.equals("NT_INTERNA")) {
                connectToAP("NT_INTERNA", "neurotechciti");
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiManager.removeNetwork(res);
        System.out.println("onDestroy");
    }

    @Override
    protected void onPause() {
        super.onPause();
        System.out.println("onPause");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_our_fi, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
