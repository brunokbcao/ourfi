package br.com.ourfi.ourfi;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Classe que conterá os métodos utilitários que precisarmos
 */
public class Utils {

    private static WifiManager wifiManager;
    private static LocationManager locationManager;
    private static NotificationManager notificationManager;

    private static int notificationId;
    private static int networkId;

    public static void initServices(Context context) {
        wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }

    public static List<ScanResult> listWifiNetworks() {
        if (wifiManager != null) {
            wifiManager.startScan();
            return wifiManager.getScanResults();
        }
        return new ArrayList<>();
    }

    public static int connectToAP(String networkSSID, String networkPass) {
        int res = -1;

        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        for (ScanResult result : listWifiNetworks()) {
            if (result.SSID.equals(networkSSID)) {

                String securityMode = getScanResultSecurity(result);

                if (securityMode.equalsIgnoreCase("OPEN")) {
                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    res = wifiManager.addNetwork(wifiConfiguration);
                    boolean b = wifiManager.enableNetwork(res, true);
                    wifiManager.setWifiEnabled(true);
                } else if (securityMode.equalsIgnoreCase("WEP")) {
                    wifiConfiguration.SSID = "\"" + networkSSID + "\"";
                    wifiConfiguration.wepKeys[0] = "\"" + networkPass + "\"";
                    wifiConfiguration.wepTxKeyIndex = 0;
                    wifiConfiguration.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
                    wifiConfiguration.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                    res = wifiManager.addNetwork(wifiConfiguration);
                    boolean b = wifiManager.enableNetwork(res, true);
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
                wifiManager.enableNetwork(res, true);
                wifiManager.setWifiEnabled(true);
            }
        }
        return res;
    }

    private static String getScanResultSecurity(ScanResult scanResult) {
        final String cap = scanResult.capabilities;
        final String[] securityModes = { "WEP", "PSK", "EAP" };

        for (int i = securityModes.length - 1; i >= 0; i--) {
            if (cap.contains(securityModes[i])) {
                return securityModes[i];
            }
        }
        return "OPEN";
    }

    public static void disconnectFromAP(int res) {
        if (wifiManager != null) {
            wifiManager.removeNetwork(res);
        }
    }

    public static void enableLocation(LocationListener locationListener) {
        if (locationManager != null) {
            locationManager.requestSingleUpdate(new Criteria(), locationListener, Looper.getMainLooper());
        }
    }

    public static void disableLocation(LocationListener locationListener) {
        if (locationManager != null) {
            locationManager.removeUpdates(locationListener);
        }
    }

    public static void setupMap(final Activity context) {
        if (locationManager != null) {
            GoogleMap map = ((MapFragment) context.getFragmentManager().findFragmentById(R.id.fragment)).getMap();
            Location myLocation = locationManager.getLastKnownLocation(locationManager.getBestProvider(new Criteria(), true));
            LatLng latLng = new LatLng(myLocation.getLatitude(), myLocation.getLongitude());
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(false);
            map.getUiSettings().setAllGesturesEnabled(false);
            map.getUiSettings().setMapToolbarEnabled(false);

            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
            final Circle c =  map.addCircle(new CircleOptions().center(latLng).radius(30).fillColor(0x2FFF0000).strokeWidth(1f).strokeColor(0xFFFF0000));
            map.addMarker(new MarkerOptions()
                    .position(latLng)
                    .alpha(0)
                    .infoWindowAnchor(0.5f, 0.5f));

            map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                @Override
                public boolean onMarkerClick(Marker marker) {
                    LatLng position = marker.getPosition();
                    marker.setTitle("Lista de redes nesta posição...");
                    marker.setSnippet("Rede A\nRede B");
                    marker.showInfoWindow();
                    System.out.println(position);
                    LatLng center = c.getCenter();
                    double radius = c.getRadius();
                    float[] distance = new float[1];
                    Location.distanceBetween(position.latitude, position.longitude, center.latitude, center.longitude, distance);
                    boolean clicked = distance[0] < radius;
                    System.out.println("Circulo clicado? = " + clicked);
                    return false;
                }
            });

            map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                @Override
                public View getInfoWindow(Marker arg0) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    TextView info = new TextView(context);
                    info.setTextColor(Color.DKGRAY);
                    info.setText(marker.getSnippet());
                    return info;
                }
            });

            map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                @Override
                public boolean onMyLocationButtonClick() {
                    System.out.println("Aproveitar para atualizar as redes ao meu redor...");
                    return false;
                }
            });
        }
    }

    public static int setupNotification(Context context) {
        if (notificationManager != null) {
            Intent notificationIntent = new Intent(context, context.getClass());
            notificationIntent.setAction(Intent.ACTION_MAIN);
            notificationIntent.addCategory(Intent.CATEGORY_LAUNCHER);

            PendingIntent pi = PendingIntent.getActivity(context, 0, notificationIntent, 0);

            Notification not = new Notification.Builder(context)
                    .setContentTitle("OurFi")
                    .setOngoing(true)
                    .setContentText("Free WiFi's around you")
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setContentIntent(pi)
                    .setPriority(Notification.PRIORITY_MIN)
                    .build();

            notificationId = 1;
            notificationManager.notify(notificationId, not);
        }
        return notificationId;
    }

    public static void finish(Context context) {
        if (notificationManager != null) {
            notificationManager.cancel(notificationId);
        }

        if (locationManager != null) {
            locationManager.removeUpdates((LocationListener) context);
        }

        if (wifiManager != null) {
            wifiManager.removeNetwork(networkId);
        }
    }

}
