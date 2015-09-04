package com.totvs.beetlesrestaurant;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.database.DataSetObserver;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.totvs.beetlesrestaurant.adapters.ProductListAdapter;
import com.totvs.beetlesrestaurant.drivers.FirebaseConn;
import com.totvs.beetlesrestaurant.models.Company;
import com.totvs.beetlesrestaurant.models.RestaurantCheckIn;
import com.totvs.beetlesrestaurant.services.PushNotification;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


public class ReceiptActivity extends ActionBarActivity {

    private ProductListAdapter productListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receipt);

        // Inicialize Firebase
        Firebase.setAndroidContext(this);

        try {
            if (!Company.getInstance().getCompanyCode().isEmpty()) {
                listProducts();

                String messageReceipt = "This is your receipt with "+Company.getInstance().getTitle()+" on "+RestaurantCheckIn.getInstance().getDate();
                TextView messageReceiptText = (TextView) ReceiptActivity.this.findViewById(R.id.lblReceiptMessage);
                messageReceiptText.setText(messageReceipt);
            }else{
                Toast.makeText(ReceiptActivity.this, "Company not selected!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){
            Toast.makeText(ReceiptActivity.this, "onCreate: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        initializeButtons();
    }

    private void listProducts(){
        Firebase mFirebaseProductList = new FirebaseConn().child("restaurant").child("productCheckin");
        final ListView listViewProducts = (ListView) findViewById(R.id.listViewReceipt);
        productListAdapter = new ProductListAdapter(mFirebaseProductList.orderByChild("transaction").equalTo(RestaurantCheckIn.getInstance().getTransaction()), ReceiptActivity.this, R.layout.activity_order_item, FirebaseConn.getFirebase_url());
        listViewProducts.setAdapter(productListAdapter);
        productListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listViewProducts.setSelection(productListAdapter.getCount() - 1);
            }
        });
    }

    private void initializeButtons(){
        Button btnAction =(Button) findViewById(R.id.btnNewOrder);

        btnAction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent it = new Intent(v.getContext(), CheckInActivity.class);

                RestaurantCheckIn.getInstance().clear();

                v.getContext().startActivity(it);
            }
        });

        Button btnFinish =(Button) findViewById(R.id.btnFinish);

        btnFinish.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent msgIntent = new Intent(getApplicationContext(), PushNotification.class);
                startService(msgIntent);

                PushNotification.pushNotification(ReceiptActivity.this, "Hi, " + RestaurantCheckIn.getInstance().getCustomerName(), "How was your experiency with us?", "Please let us know if we miss anything.", PushNotification.URL_SURVEY);

                Intent it = new Intent(v.getContext(), CheckInActivity.class);

                RestaurantCheckIn.getInstance().clear();

                v.getContext().startActivity(it);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_receipt, menu);
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
