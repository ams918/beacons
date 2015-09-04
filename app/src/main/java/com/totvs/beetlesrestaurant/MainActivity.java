package com.totvs.beetlesrestaurant;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.database.DataSetObserver;
import android.os.RemoteException;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.totvs.beetlesrestaurant.adapters.CompanyListAdapter;
import com.totvs.beetlesrestaurant.drivers.FirebaseConn;
import com.totvs.beetlesrestaurant.models.BeaconModel;
import com.totvs.beetlesrestaurant.models.Company;
import com.totvs.beetlesrestaurant.models.RestaurantCheckIn;
import com.totvs.beetlesrestaurant.services.PushNotification;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class MainActivity extends ActionBarActivity {

    private BeaconManager beaconManager;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private static final int REQUEST_ENABLE_BT = 1234;

    private CompanyListAdapter companyListAdapter;

    private Beacon beaconSelected = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicialize Firebase
        Firebase.setAndroidContext(this);

        //Initialize Beacon Manager
        beaconManager = new BeaconManager(this);

        //Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(30), 0);

        // Check if device supports Bluetooth Low Energy.
        if (!beaconManager.hasBluetooth()) {
            Toast.makeText(this, "Device does not have Bluetooth Low Energy", Toast.LENGTH_LONG).show();
            return;
        }

        // If Bluetooth is not enabled, let user enable it.
        if (!beaconManager.isBluetoothEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
        }else{
            connectToService();
        }
    }

    @Override
    public void onDestroy(){
        // Destroy beacon manager
        beaconManager.disconnect();
        super.onDestroy();
    }

    @Override
    public void onStart(){
        super.onStart();

        Firebase mFirebaseCompanyList = new FirebaseConn().child("restaurant").child("companies");
        final ListView listViewCompanies = (ListView) findViewById(R.id.listViewMainCompanies);
        companyListAdapter = new CompanyListAdapter(mFirebaseCompanyList.limit(50), MainActivity.this, R.layout.activity_main_item, FirebaseConn.getFirebase_url());
        listViewCompanies.setAdapter(companyListAdapter);
        companyListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listViewCompanies.setSelection(companyListAdapter.getCount() - 1);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode == Activity.RESULT_OK) {
                connectToService();
            } else {
                Toast.makeText(this, "Bluetooth not enabled", Toast.LENGTH_LONG).show();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void connectToService() {

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

                // Start to discovery beacons in region
                try {
                    beaconManager.startRanging(ALL_ESTIMOTE_BEACONS_REGION);
                } catch (RemoteException e) {
                    Log.e("PushNotification", "Cannot start ranging", e);
                }

                beaconManager.setRangingListener(new BeaconManager.RangingListener() {
                    @Override
                    public void onBeaconsDiscovered(Region region, final List<Beacon> rangedBeacons) {
                        // Run in background, or else will make the app crash
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Beacon foundBeacon = null;
                                for (Beacon rangedBeacon : rangedBeacons) {
                                    foundBeacon = rangedBeacon;
                                    updateBeaconFound(foundBeacon);
                                }
                            }
                        });
                    }
                });
            }
        });
    }

    private void updateBeaconFound(Beacon foundBeacon){
        if ((Utils.computeAccuracy(foundBeacon) < 0.20) || beaconSelected == null){
            beaconSelected = foundBeacon;

            try {
                beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
            } catch (RemoteException e) {
                Log.d("Main Activity", "Error while stopping ranging", e);
            }

            Firebase mFirebaseBeaconFound = new FirebaseConn().child("restaurant").child("beacons").child(foundBeacon.getMacAddress());

            mFirebaseBeaconFound.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        BeaconModel beaconModel = snapshot.getValue(BeaconModel.class);

                        BeaconModel.getInstance().copyFrom(beaconModel);

                        Intent it = new Intent(MainActivity.this, CheckInActivity.class);

                        it.putExtra(Company.COMPANY_CODE, beaconModel.getCompany());

                        startActivity(it);

                    }catch(Exception e){
                        Toast.makeText(MainActivity.this, "updateBeaconFound: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    // do nothing
                }
            });
        }
    }

    @Override
    public void onPause(){
        super.onPause();

        /*if (beaconManager.isBluetoothEnabled()) {
            Intent msgIntent = new Intent(MainActivity.this, PushNotification.class);
            startService(msgIntent);
        }*/
    }
}
