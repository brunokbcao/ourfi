package br.com.ourfi.ourfi;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.JsonReader;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.google.android.gms.auth.GoogleAuthException;
import com.google.android.gms.auth.GoogleAuthUtil;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.google.android.gms.maps.MapFragment;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;


public class OurFiActivity extends AppCompatActivity implements LocationListener {

    String[] avail_accounts;
    private AccountManager mAccountManager;
    ListView list;
    ArrayAdapter<String> adapter;
    SharedPreferences pref;

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

        /* Servico de autenticacao */
        avail_accounts = getAccountNames();
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,avail_accounts );
        pref = getSharedPreferences("AppPref", MODE_PRIVATE);

        final Dialog accountDialog;

        // TODO Auto-generated method stub
        if (avail_accounts.length != 0) {
            accountDialog = new Dialog(OurFiActivity.this);
            accountDialog.setCanceledOnTouchOutside(false);
            accountDialog.setContentView(R.layout.accounts_dialog);
            accountDialog.setTitle("Select Google Account");
            list = (ListView) accountDialog.findViewById(R.id.list);

            list.setAdapter(adapter);
            list.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {

                    SharedPreferences.Editor edit = pref.edit();
                    //Storing Data using SharedPreferences
                    edit.putString("Email", avail_accounts[position]);
                    edit.commit();
                    new Authenticate().execute();
                    accountDialog.cancel();
                }
            });
            accountDialog.show();
        } else {
            Toast.makeText(getApplicationContext(), "No accounts found, Add a Account and Continue.", Toast.LENGTH_SHORT).show();
        }
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

    /* Pega a lista de contas cadastradas no aparelho */
    private String[] getAccountNames() {
        mAccountManager = AccountManager.get(this);
        Account[] accounts = mAccountManager.getAccountsByType(GoogleAuthUtil.GOOGLE_ACCOUNT_TYPE);
        String[] names = new String[accounts.length];
        for (int i = 0; i < names.length; i++) {
            names[i] = accounts[i].name;
        }
        return names;
    }
    private class Authenticate extends AsyncTask<String, String, String> {
        ProgressDialog pDialog;
        String mEmail;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            pDialog = new ProgressDialog(OurFiActivity.this);
            pDialog.setMessage("Authenticating....");
            pDialog.setIndeterminate(false);
            pDialog.setCancelable(true);
            mEmail= pref.getString("Email", "");
            pDialog.show();
        }
        @Override
        protected void onPostExecute(String token) {
            pDialog.dismiss();
            if(token != null){

                SharedPreferences.Editor edit = pref.edit();
                //Storing Access Token using SharedPreferences
                edit.putString("Access Token", token);
                edit.commit();
                Log.i("Token", "Access Token retrieved:" + token);
                Toast.makeText(getApplicationContext(),"Access Token is " +token, Toast.LENGTH_SHORT).show();
            }

        }

        @Override
        protected String doInBackground(String... arg0) {
            // TODO Auto-generated method stub
            String token = null;

            try {
                token = GoogleAuthUtil.getToken(
                        OurFiActivity.this,
                        mEmail,
                        "oauth2:https://www.googleapis.com/auth/contacts.readonly");
            } catch (IOException transientEx) {
                // Network or server error, try later
                Log.e("IOException", transientEx.toString());
            } catch (UserRecoverableAuthException e) {
                // Recover (with e.getIntent())
                startActivityForResult(e.getIntent(), 1001);

                Log.e("AuthException", e.toString());

            } catch (GoogleAuthException authEx) {
                // The call is not ever expected to succeed
                // assuming you have already verified that
                // Google Play services is installed.
                Log.e("GoogleAuthException", authEx.toString());
            }

            return token;
        }

    }
}
