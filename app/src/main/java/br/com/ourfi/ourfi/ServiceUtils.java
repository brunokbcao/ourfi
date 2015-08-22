package br.com.ourfi.ourfi;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.util.JsonReader;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutionException;

/**
 * Created by Christian on 15/08/2015.
 */
public class ServiceUtils {

    private static final String SERVER_URL = "http://192.168.1.104:8080/services/ourfi/";

            /*
//Listando Wifis
{
	"Authentication": {

		"Mod": "Google"
	},
	"Location": {
		"Lat": 0.0,
		"Lon": -0.1,
		"Alt": 10.3
	}
}

//Registrando/Deregistrando Wifi
{
	"Authentication": {
		"User": "billao@gmail.com",
		"Token": "asdfghjkl2345789",
		"Mod": "Google"
	},
	"Location": {
		"Lat": 0.0,
		"Lon": -0.1,
		"Alt": 10.3
	},
	"WiFi": {
		"SSID": "NT_INTERNA",
		"Password": "passsss"
	}
}
 */

    public static ResultListWifis listWiFis(Location loc) {
        final String urlPath = "list";

        try {
            JSONObject req = createJsonListWifi("billao@gmail.com", "asdfghjkl2345789", loc);
            String resp = httpRequest(SERVER_URL + urlPath, req.toString());

            return new ResultListWifis(new JSONObject(resp));

        } catch (Exception e) {
            Log.e("ourfi", e.getMessage(), e);
            ResultListWifis ret = new ResultListWifis();
            ret.Success = false;
            ret.Message = e.getMessage();
            return ret;
        }
    }

    public static class ResultListWifis extends Result {

        List<Wifi> WiFis;

        public ResultListWifis () {
            WiFis = new ArrayList<Wifi>();
        }

        public ResultListWifis(JSONObject json) throws JSONException {
            super(json);
            this.WiFis = new ArrayList<Wifi>();
            JSONArray wifis = json.getJSONArray("Wifis");
            if (wifis != null && wifis.length() > 0) {
                for (int i = 0; i < wifis.length(); i++) {
                    JSONObject jsWf = wifis.getJSONObject(i);
                    Wifi wf = new Wifi(jsWf);
                    this.WiFis.add(wf);
                }
            }
        }
    }

    public static class Result {
        Boolean Success;
        String Message;
//        String Timestamp;

        public Result() {}
        public Result(JSONObject json) throws JSONException {
            this.Success = json.getBoolean("Success");
            this.Message = json.getString("Message");
//            this.Timestamp = json.getString("Timestamp");
        }
    }

    public static class Wifi {
        String SSID;
        String Password;
        Location Location;

        public Wifi() {}
        public Wifi(JSONObject json) throws JSONException {
            this.SSID = json.getString("SSID");
            this.Password = json.getString("Password");
            this.Location = new Location(json.getJSONObject("Location"));
        }

        public JSONObject toJson() throws JSONException {
            JSONObject wf = new JSONObject();
            wf.put("SSID", this.SSID);
            wf.put("Password", this.Password);
            wf.put("Location", this.Location.toJson());
            return wf;
        }
    }

    public static class Location {
        Double Latitude;
        Double Longitude;
        Double Altitude;

        public Location() {}

        public Location(Double latitude, Double longitude, Double altitude) {
            Latitude = latitude;
            Longitude = longitude;
            Altitude = altitude;
        }

        public Location (JSONObject json) throws JSONException {
            this.Latitude = json.getDouble("Lat");
            this.Longitude = json.getDouble("Lon");
            this.Altitude = json.getDouble("Alt");
        }

        public JSONObject toJson() throws JSONException {
            JSONObject loc = new JSONObject();
            loc.put("Lat", this.Latitude);
            loc.put("Lon", this.Longitude);
            loc.put("Alt", this.Altitude);
            return loc;
        }
    }

    private static JSONObject createJsonListWifi(String user, String token, Location location) throws JSONException {
        JSONObject ret = createJsonRequest(user, token);
        ret.put("Location", location.toJson());
        return ret;
    }

    private JSONObject createJsonRegisterWifi(String user, String token, Wifi wifi) throws JSONException {
        JSONObject ret = createJsonListWifi(user, token, wifi.Location);
        ret.put("WiFi", wifi.toJson());
        return ret;
    }

    private static JSONObject createJsonRequest(String user, String token) throws JSONException {
        JSONObject auth = new JSONObject();
        auth.put("User", user);
        auth.put("Token", token);
        auth.put("Mod", "Google");

        JSONObject ret = new JSONObject();
        ret.put("Authentication", auth);

        return ret;
    }

    public static String httpRequest (final String urlPath, final String body) throws IOException {


        AsyncTask<String, Void, String> asyncTask = new AsyncTask<String, Void, String>() {
            @Override
            protected String doInBackground(String... params) {
                try {
                    URL url = new URL(urlPath);
                    HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
                    urlConnection.setDoInput(true);
                    urlConnection.setRequestMethod("GET");

                    if (body != null && !body.isEmpty()) {
                        urlConnection.setDoOutput(true);
                        urlConnection.setRequestMethod("POST");
                        urlConnection.setRequestProperty("Content-Type", "application/json;charset=utf-8");
                        byte[] bytes = body.getBytes();
                        urlConnection.setFixedLengthStreamingMode(bytes.length);
                        OutputStream os = urlConnection.getOutputStream();
                        os.write(bytes);
                        os.flush();
                    }

                    InputStream in;

                    int responseCode = urlConnection.getResponseCode();
                    if (responseCode >= 400) {
                        in = urlConnection.getErrorStream();
                    } else {
                        in = urlConnection.getInputStream();
                    }

                    ByteArrayOutputStream buffer = new ByteArrayOutputStream();
                    int nRead;
                    byte[] data = new byte[16384];
                    while ((nRead = in.read(data, 0, data.length)) != -1) {
                        buffer.write(data, 0, nRead);
                    }
                    buffer.flush();
                    return new String(buffer.toByteArray());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return null;
            }
        };

        asyncTask.execute();

        try {
            return asyncTask.get();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        return null;
    }
}