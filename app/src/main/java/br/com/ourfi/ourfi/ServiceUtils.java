package br.com.ourfi.ourfi;

import android.location.Location;
import android.net.wifi.ScanResult;
import android.os.AsyncTask;
import android.util.JsonReader;

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

    public ResultListWifis listWiFis() throws IOException {
        final String urlPath = "listWifi/";

        ResultListWifis resultListWifis = new ResultListWifis();

        URL url = new URL(R.string.SERVER_URL + urlPath);

        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        JsonReader jsr = new JsonReader(new InputStreamReader(urlConnection.getInputStream()));

        return resultListWifis;
    }

    public static class ResultListWifis extends Result {

        List<Wifi> WiFis;

        public ResultListWifis () {
            WiFis = new ArrayList<Wifi>();
        }

    }

    public static class Result {
        Boolean success;
        String message;
        Date timestamp;
    }

    public static class Wifi {

        String SSID;
        String Password;
        Double Latitude;
        Double Longitude;

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

/*
//Listando Wifis
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