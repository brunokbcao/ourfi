package br.com.ourfi.ourfi;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.widget.Toast;

/**
 * Created by Christian on 01/08/2015.
 */
public class LocationHandler implements LocationListener {

    private Context context;
    private LocationManager locationManager;

    private Location lastLocation;

    public LocationHandler (Context context) {
        this.context = context;
        this.locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        enableLocateNow();
    }

    public void enableLocateNow() {
        this.disable();
        this.locationManager.requestLocationUpdates(500, 1, new Criteria(), this, null);
    }

    public void enableLocateLive() {
        this.disable();
        this.locationManager.requestLocationUpdates(5000, 1, new Criteria(), this, null);
    }

    public void disable() {
        this.locationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        this.lastLocation = location;
        Toast.makeText(context, "Location Changed: " + location.toString(), Toast.LENGTH_LONG).show();
        Intent maps = new Intent(context, MapsActivity.class);
        context.startActivity(maps);
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
