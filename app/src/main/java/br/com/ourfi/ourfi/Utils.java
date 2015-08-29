package br.com.ourfi.ourfi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Looper;
import android.support.v4.app.NotificationCompat;
import android.text.InputType;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.List;

/**
 * Classe que conterá os métodos utilitários que precisarmos
 */
public class Utils {

    private static WifiManager wifiManager;
    private static LocationManager locationManager;
    private static NotificationManager notificationManager;

    private static int notificationId;
    private static int networkId;
    private static String userEmail;
    private static Location lastKnownLocation;

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

    public static int connectToAP(String networkSSID, String networkPass, boolean permanent) {
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
                if (res > 0 && permanent) {
                    wifiManager.saveConfiguration();
                }
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

    private static GoogleMap map = null;

    public static void setupMap(final Activity context, final Location location) {
        if (locationManager != null) {
            lastKnownLocation = location;
            if (map == null) {
                map = ((MapFragment) context.getFragmentManager().findFragmentById(R.id.fragment)).getMap();
                map.setMyLocationEnabled(true);
                map.getUiSettings().setAllGesturesEnabled(false);
                map.getUiSettings().setZoomControlsEnabled(false);
                map.getUiSettings().setZoomGesturesEnabled(true);
                map.getUiSettings().setMapToolbarEnabled(false);
                map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                    @Override
                    public View getInfoWindow(Marker marker) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {
                        TextView info = new TextView(context);
                        info.setTextColor(Color.DKGRAY);
                        info.setText(marker.getTitle());
                        return info;
                    }
                });

                map.setOnMyLocationButtonClickListener(new GoogleMap.OnMyLocationButtonClickListener() {
                    @Override
                    public boolean onMyLocationButtonClick() {
                        ServiceUtils.Location loc = new ServiceUtils.Location(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude(), lastKnownLocation.getAltitude());
                        ServiceUtils.ResultListWifis resultListWifis = ServiceUtils.listWiFis(userEmail, loc);
                        map.clear();
                        if (resultListWifis.Success) {
                            for (ServiceUtils.Wifi wf : resultListWifis.WiFis) {
                                map.addMarker(new MarkerOptions()
                                        .position(new LatLng(wf.Location.Latitude, wf.Location.Longitude))
                                        .alpha(1)
                                        .title(wf.SSID)
                                        .snippet(wf.Password)
                                        .infoWindowAnchor(0.5f, 0.5f));
                            }
                        }
                        return false;
                    }
                });

                map.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(Marker marker) {
                        Button b = (Button) context.findViewById(R.id.buttonWifi);
                        b.setText(context.getString(R.string.str_button_connect_wifi));
                        b.setOnClickListener(Utils.createClickListener(context, marker.getTitle(), marker.getSnippet()));
                        return false;
                    }
                });

                map.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
                    @Override
                    public void onMapClick(LatLng latLng) {
                        Button b = (Button) context.findViewById(R.id.buttonWifi);
                        b.setText(context.getString(R.string.str_button_share_wifi));
                        b.setOnClickListener(Utils.createClickListener(context, null, null));
                    }
                });
            }

            LatLng latLng = new LatLng(location.getLatitude(), location.getLongitude());
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 17.0f));
        }
    }

    public static View.OnClickListener createClickListener(final Context context, final String SSID, final String Password) {
        return new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (SSID == null && Password == null) {
                    final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
                    if (connectionInfo.getSupplicantState() != SupplicantState.COMPLETED) {
                        Toast.makeText(context, "Você não está conectado a uma rede WiFi!", Toast.LENGTH_LONG).show();
                    } else {
                        AlertDialog.Builder builder = new AlertDialog.Builder(context);
                        final String ssid = connectionInfo.getSSID().replaceAll("\"", "");
                        builder.setTitle("Insira a senha para a WiFi " + ssid);
                        final EditText input = new EditText(context);
                        input.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                        builder.setView(input);
                        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                String editText = input.getText().toString();

                                networkId = connectToAP(ssid, editText, true);
                                if (networkId < 0) {
                                    dialog.cancel();
                                    Toast.makeText(context, "Senha inválida, tente novamente!", Toast.LENGTH_LONG).show();
                                    return;
                                }

                                ServiceUtils.Wifi wifi = new ServiceUtils.Wifi();
                                wifi.Location = new ServiceUtils.Location();
                                wifi.Location.Latitude = lastKnownLocation.getLatitude();
                                wifi.Location.Longitude = lastKnownLocation.getLongitude();
                                wifi.Location.Altitude = lastKnownLocation.getAltitude();
                                wifi.SSID = ssid;
                                wifi.Password = editText.isEmpty() ? "" : editText;

                                ServiceUtils.Result result = ServiceUtils.shareWiFi(userEmail, wifi);
                                Toast.makeText(context, result.Message, Toast.LENGTH_LONG).show();
                            }
                        });
                        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });

                        builder.show();
                    }
                } else {
                    networkId = connectToAP(SSID, Password, false);
                    String msg = "Conectado à rede " + SSID + " com sucesso!";
                    if (networkId < 0) {
                        boolean found = false;
                        for (ScanResult sc : listWifiNetworks()) {
                            if (sc.SSID.equals(SSID)) {
                                found = true;
                                break;
                            }
                        }
                        if (found) {
                            msg = "Não foi possível conectar à rede " + SSID + "...";
                        } else {
                            msg = "A rede " + SSID + " está fora de alcance no momento...";
                        }
                    }
                    Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
                }
            }
        };
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

    public static void setupUserAccount(Context context) {
        final SharedPreferences pref = context.getSharedPreferences("AppPref", Context.MODE_PRIVATE);
        userEmail = pref.getString("Email", null);
        if (userEmail == null) {
            /* Pega a lista de contas cadastradas no aparelho */
            AccountManager mAccountManager = AccountManager.get(context);
            Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
            final String[] names = new String[accounts.length];
            for (int i = 0; i < names.length; i++) {
                names[i] = accounts[i].name;
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, names);

            if (names.length != 0) {
                final Dialog accountDialog = new Dialog(context);
                accountDialog.setCanceledOnTouchOutside(false);
                accountDialog.setContentView(R.layout.accounts_dialog);
                accountDialog.setTitle("Select Google Account");
                ListView list = (ListView) accountDialog.findViewById(R.id.list);

                list.setAdapter(adapter);
                list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view,
                                            int position, long id) {
                        accountDialog.cancel();
                        userEmail = names[position];

                        SharedPreferences.Editor edit = pref.edit();
                        edit.putString("Email", names[position]);
                        edit.commit();
                    }
                });
                accountDialog.show();
            } else {
                userEmail = "anonymous";
            }
        }
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
