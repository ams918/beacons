package com.totvs.beetlesrestaurant.services;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.RemoteException;
import android.support.v7.app.NotificationCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
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
import com.koushikdutta.ion.Ion;
import com.totvs.beetlesrestaurant.CheckInActivity;
import com.totvs.beetlesrestaurant.MainActivity;
import com.totvs.beetlesrestaurant.R;
import com.totvs.beetlesrestaurant.drivers.FirebaseConn;
import com.totvs.beetlesrestaurant.models.BeaconModel;
import com.totvs.beetlesrestaurant.models.Company;
import com.totvs.beetlesrestaurant.models.OwnerInfo;
import com.totvs.beetlesrestaurant.models.RestaurantCheckIn;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by rond.borges on 01/09/2015.
 */
public class PushNotification extends IntentService{

    Handler handler;

    private OwnerInfo ownerInfo;

    private BeaconManager beaconManager;
    private Nearable selectedNearable = null;
    private static final Region ALL_ESTIMOTE_BEACONS_REGION = new Region("rid", null, null, null);
    private Beacon selectedBeacon = null;
    private BeaconModel beaconModel = null;

    public static String URL_SURVEY = "https://pt.surveymonkey.com/r/X559Y8M";

    public PushNotification(){
        super("PushNotification");
    }

    @Override
    protected void onHandleIntent(Intent intent) {

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

    @Override
    public void onCreate() {
        handler = new Handler();

        //Initialize Beacon Manager
        beaconManager = new BeaconManager(this);

        //Default values are 5s of scanning and 25s of waiting time to save CPU cycles.
        beaconManager.setBackgroundScanPeriod(TimeUnit.SECONDS.toMillis(30), 0);

        //Initialize Selected Beacon
        selectedNearable = null;

        if (beaconManager.isBluetoothEnabled()) {
            connectToService();
        }

        ownerInfo = new OwnerInfo(this);

        super.onCreate();
    }

    public static void pushNotification(Context context, String title, String content, String companyCode, String url){
        try {
            NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

            NotificationCompat.Builder builder = new NotificationCompat.Builder(context);
            builder.setContentTitle(title);
            builder.setContentText(content);
            builder.setSmallIcon(R.drawable.beetle_clip_art_hight_color);
            builder.setAutoCancel(true);

            // OPTIONAL create soundUri and set sound:
            //builder.setSound(soundUri);

            Intent newIntent = null;
            if (url.isEmpty()) {
                newIntent = new Intent(context, CheckInActivity.class);
                newIntent.putExtra(context.getString(R.string.navigation_from_notification), true);
                newIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK |
                        Intent.FLAG_ACTIVITY_CLEAR_TASK);
                newIntent.putExtra(Company.COMPANY_CODE, companyCode);
            }else{
                if (!url.startsWith("http://") && !url.startsWith("https://"))
                    url = "http://" + url;
                newIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            }

            PendingIntent pi = PendingIntent.getActivity(context, 0, newIntent, 0);

            builder.setContentIntent(pi);

            notificationManager.notify("Beetle's Restaurant", 0, builder.build());
        }catch (Exception e){
            Toast.makeText(context, "NotificationManager " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void runOnUiThread(Runnable runnable) {
        handler.post(runnable);
    }

    private void connectToService() {

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {

                // Start to discovery nearables in region
                beaconManager.startNearableDiscovery();

                beaconManager.setNearableListener(new BeaconManager.NearableListener() {
                    @Override
                    public void onNearablesDiscovered(final List<Nearable> rangedNearables) {
                        // Run in background, or else will make the app crash
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Nearable foundNearable = null;
                                for (Nearable rangedNearable : rangedNearables) {
                                    foundNearable = rangedNearable;
                                    //updateNearableFound(foundNearable);
                                }
                            }
                        });
                    }
                });

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

    private void updateNearableFound(Nearable foundNearable){
        if (selectedNearable == null) {

            pushNotification(PushNotification.this, "Hi, " + ownerInfo.name, "Welcome to our restaurant, would you like to come in?", "", "");

            selectedNearable = foundNearable;
        }else if (Utils.computeProximity(foundNearable).toString().equals("IMMEDIATE") && (selectedNearable != null)){
            if (foundNearable.identifier.equals(selectedNearable.identifier)) {
                return;
            }

            //pushNotification("Hi, "+ownerInfo.name, "I found a new Immediate Nearable: " + selectedNearable.identifier);
        }
    }

    private void updateBeaconFound(Beacon foundBeacon){
        if ((Utils.computeAccuracy(foundBeacon) < 0.20)) {

            selectedBeacon = foundBeacon;

            Firebase mFirebaseWelcomeBeacon = new FirebaseConn().child("restaurant").child("beacons").child(selectedBeacon.getMacAddress());

            mFirebaseWelcomeBeacon.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot snapshot) {
                    try {

                        beaconModel = snapshot.getValue(BeaconModel.class);

                        Firebase mFirebaseWelcomeCompany = new FirebaseConn().child("restaurant").child("companies").child(beaconModel.getCompany());

                        mFirebaseWelcomeCompany.addValueEventListener(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot snapshot) {
                                try {
                                    Company companySelected = snapshot.getValue(Company.class);

                                    if (Company.getInstance().getCompanyCode().isEmpty()) {
                                        pushNotification(PushNotification.this, "Hi, " + ownerInfo.name, "Welcome to " + companySelected.getTitle() + ", would you want to come in?", companySelected.getCompanyCode(), "");
                                    }else if (!Company.getInstance().getCompanyCode().equals(companySelected.getCompanyCode())){
                                        pushNotification(PushNotification.this, "Hi, " + ownerInfo.name, "Welcome to " + companySelected.getTitle() + ", would you want to come in?", companySelected.getCompanyCode(), "");
                                    }

                                } catch (Exception e) {
                                    //Toast.makeText(NearablePlayProximityActivity.this, "updateNearableFoundFromFirebase: " + e.toString(), Toast.LENGTH_LONG).show();
                                }
                            }

                            @Override
                            public void onCancelled(FirebaseError firebaseError) {
                                // do nothing
                            }
                        });

                    } catch (Exception e) {
                        //Toast.makeText(NearablePlayProximityActivity.this, "updateNearableFoundFromFirebase: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(FirebaseError firebaseError) {
                    // do nothing
                }
            });
        }else if ((Utils.computeAccuracy(foundBeacon) > 20)){
            //if (!RestaurantCheckIn.getInstance().getStatus().equals(getResources().getString(R.string.lbl_CheckInRequest))) {
                //if (foundBeacon.getMacAddress().equals(BeaconModel.getInstance().getMacAddress())) {
                    pushNotification(PushNotification.this, "Hi, " + ownerInfo.name, "How was your experiency with us?", "Please let us know if we miss anything.", URL_SURVEY);
                //}
            //}
        }
    }
}
