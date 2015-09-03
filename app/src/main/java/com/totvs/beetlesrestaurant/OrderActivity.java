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
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.BeaconManager;
import com.estimote.sdk.Nearable;
import com.estimote.sdk.Region;
import com.estimote.sdk.Utils;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.totvs.beetlesrestaurant.adapters.ProductListAdapter;
import com.totvs.beetlesrestaurant.drivers.FirebaseConn;
import com.totvs.beetlesrestaurant.models.BeaconModel;
import com.totvs.beetlesrestaurant.models.Company;
import com.totvs.beetlesrestaurant.models.RestaurantCheckIn;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class OrderActivity extends ActionBarActivity {

    private ProductListAdapter productListAdapter;

    private BeaconManager beaconManager;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private static final int REQUEST_ENABLE_BT = 1234;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order);

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

        try {

            if (!Company.getInstance().getCompanyCode().isEmpty()) {

                TextView titleText = (TextView) findViewById(R.id.textViewMenuTable);
                titleText.setText("Tracking your table!");

                listProducts();
            }else{
                Toast.makeText(OrderActivity.this, "Company not selected!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){
            Toast.makeText(OrderActivity.this, "onCreate: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        initializeButtons();

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

    @Override
    public void onDestroy(){
        beaconManager.disconnect();
        try {
            beaconManager.stopRanging(ALL_ESTIMOTE_BEACONS_REGION);
        } catch (RemoteException e) {
            Log.d("PushNotification", "Error while stopping ranging", e);
        }
        super.onDestroy();
    }

    private void listProducts(){
        try {
            Firebase mFirebaseProductList = new FirebaseConn().child("restaurant").child("productCheckin");
            final ListView listViewProducts = (ListView) findViewById(R.id.listViewOrder);
            productListAdapter = new ProductListAdapter(mFirebaseProductList.orderByChild("transaction").equalTo(RestaurantCheckIn.getInstance().getTransaction()), OrderActivity.this, R.layout.activity_order_item, FirebaseConn.getFirebase_url());
            listViewProducts.setAdapter(productListAdapter);
            productListAdapter.registerDataSetObserver(new DataSetObserver() {
                @Override
                public void onChanged() {
                    super.onChanged();
                    listViewProducts.setSelection(productListAdapter.getCount() - 1);
                }
            });
        }catch (Exception e){
            Toast.makeText(OrderActivity.this, "listProducts: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void initializeButtons(){
        Button btnAction =(Button) findViewById(R.id.btnPlaceOrder);

        btnAction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Firebase mFirebaseCheckInUpdate = new FirebaseConn().child("restaurant").child("checkin").child(RestaurantCheckIn.getInstance().getTransaction());
                Map<String, Object> updates = new HashMap<String, Object>();

                updates.put("status", getResources().getString(R.string.lbl_CheckOutRequest));
                updates.put("bill", 17.90);

                mFirebaseCheckInUpdate.updateChildren(updates);

                Intent it = new Intent(v.getContext(), PaymentActivity.class);

                v.getContext().startActivity(it);
            }
        });
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
        if ((Utils.computeAccuracy(foundBeacon) < 0.20)){
            if (foundBeacon.getMacAddress().equals(BeaconModel.getInstance().getMacAddress())) {
                return;
            }

            Firebase mFirebaseBeaconFound = new FirebaseConn().child("restaurant").child("beacons").child(foundBeacon.getMacAddress());

            mFirebaseBeaconFound.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {
                        BeaconModel beaconModel = snapshot.getValue(BeaconModel.class);

                        BeaconModel.getInstance().copyFrom(beaconModel);

                        Firebase mFirebaseWelcomeCompany = new FirebaseConn().child("restaurant").child("companies").child(beaconModel.getCompany());

                        mFirebaseWelcomeCompany.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                try {

                                    Company companySelected = snapshot.getValue(Company.class);
                                    Company.getInstance().copyFrom(companySelected);

                                    BeaconModel.getInstance().setCompany(companySelected.getCompanyCode());

                                    RestaurantCheckIn.getInstance().setTable(BeaconModel.getInstance().getTable());
                                    RestaurantCheckIn.getInstance().setCompanyCode(companySelected.getCompanyCode());
                                    RestaurantCheckIn.getInstance().setBeaconIdentifier(BeaconModel.getInstance().getMacAddress());

                                    Firebase mFirebaseCheckInUpdate = new FirebaseConn().child("restaurant").child("checkin").child(RestaurantCheckIn.getInstance().getTransaction());
                                    Map<String, Object> updates = new HashMap<String, Object>();

                                    updates.put("status", getResources().getString(R.string.lbl_CheckInSucess));
                                    updates.put("table", RestaurantCheckIn.getInstance().getTable());
                                    updates.put("companyCode", RestaurantCheckIn.getInstance().getCompanyCode());
                                    updates.put("beaconIdentifier", RestaurantCheckIn.getInstance().getBeaconIdentifier());

                                    TextView titleText = (TextView) findViewById(R.id.textViewMenuTable);
                                    titleText.setText("Your are on table: " + RestaurantCheckIn.getInstance().getTable());

                                    mFirebaseCheckInUpdate.updateChildren(updates);

                                } catch (Exception e) {
                                    //Toast.makeText(NearablePlayProximityActivity.this, "updateNearableFoundFromFirebase: " + e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                // do nothing
                            }
                        });

                        }catch(Exception e){
                            //Toast.makeText(NearablePlayProximityActivity.this, "updateNearableFoundFromFirebase: " + e.toString(), Toast.LENGTH_LONG).show();
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_order, menu);
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
