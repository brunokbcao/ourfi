package br.com.ourfi.ourfi;

import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class OurFiActivity extends AppCompatActivity implements LocationListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_our_fi);

        Utils.initServices(this);
        Utils.setupMap(this);
        Utils.enableLocation(this);
        Utils.setupNotification(this);

//        try {
////            String body = "{\"Authentication\":{\"Login\":\"bruno.silva@neurotech.com.br\",\"Password\":\"Ciab@2015\",\"Properties\":{\"Key\":\"PRODUTO\",\"Value\":\"DD\"}}}";
////            String test = ServiceUtils.httpRequest("http://wt9.cloudapp.net/riskpack-webservice/services/rest/accessControl/login", body);
////
////            JSONObject jo = new JSONObject(test);
////            Log.e("outfi", "Status: " + jo.getString("StatusCode"));
////            Log.e("outfi", "Token: " + jo.getJSONObject("Token").getString("Key"));
////
////            Toast.makeText(this, test, Toast.LENGTH_LONG);
////            Log.e("ourfi", test);
//        } catch (IOException e) {
//            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG);
//            Log.e("ourfi", e.toString());
//        } catch (JSONException e) {
//            e.printStackTrace();
//        }
    }

    @Override
    protected void onDestroy() {
        Utils.finish(this);
        super.onDestroy();
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

    @Override
    public void onLocationChanged(Location location) {
        Toast.makeText(this, "Location Changed: " + location.toString(), Toast.LENGTH_LONG).show();
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        System.out.println("Location onStatusChanged: " + provider
                + "\n" + status
                + "\n" + extras.toString());
    }

    @Override
    public void onProviderEnabled(String provider) {
        System.out.println("Location onProviderEnabled: " + provider);
    }

    @Override
    public void onProviderDisabled(String provider) {
        System.out.println("Location onProviderDisabled: " + provider);
    }
}
