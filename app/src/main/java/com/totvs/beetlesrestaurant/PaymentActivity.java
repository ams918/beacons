package com.totvs.beetlesrestaurant;

import android.content.Intent;
import android.database.DataSetObserver;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.totvs.beetlesrestaurant.adapters.ProductListAdapter;
import com.totvs.beetlesrestaurant.drivers.FirebaseConn;
import com.totvs.beetlesrestaurant.models.Company;
import com.totvs.beetlesrestaurant.models.Product;
import com.totvs.beetlesrestaurant.models.ProductCheckIn;
import com.totvs.beetlesrestaurant.models.RestaurantCheckIn;

import java.util.HashMap;
import java.util.Map;


public class PaymentActivity extends ActionBarActivity {

    private ProductListAdapter productListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment);

        // Inicialize Firebase
        Firebase.setAndroidContext(this);

        try {
            if (!Company.getInstance().getCompanyCode().isEmpty()) {
                listProducts();
            }else{
                Toast.makeText(PaymentActivity.this, "Company not selected!", Toast.LENGTH_LONG).show();
            }
        } catch (Exception e){
            Toast.makeText(PaymentActivity.this, "onCreate: " + e.toString(), Toast.LENGTH_LONG).show();
        }

        initializeButtons();

    }

    private void listProducts(){
        Firebase mFirebaseProductList = new FirebaseConn().child("restaurant").child("productCheckin");
        final ListView listViewProducts = (ListView) findViewById(R.id.listViewPayment);
        productListAdapter = new ProductListAdapter(mFirebaseProductList.orderByChild("transaction").equalTo(RestaurantCheckIn.getInstance().getTransaction()), PaymentActivity.this, R.layout.activity_order_item, FirebaseConn.getFirebase_url());
        listViewProducts.setAdapter(productListAdapter);
        productListAdapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                listViewProducts.setSelection(productListAdapter.getCount() - 1);
            }
        });

        RestaurantCheckIn.getInstance().setBill(0.0);

        Firebase mFirebaseProductListUpdate = new FirebaseConn().child("restaurant").child("productCheckin");
        mFirebaseProductListUpdate.orderByChild("transaction").equalTo(RestaurantCheckIn.getInstance().getTransaction()).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                ProductCheckIn productCheckIn = dataSnapshot.getValue(ProductCheckIn.class);
                if (productCheckIn.getChecked()){
                    RestaurantCheckIn.getInstance().setBill(RestaurantCheckIn.getInstance().getBill()+productCheckIn.getPrice());

                    Firebase mFirebaseCheckInUpdate = new FirebaseConn().child("restaurant").child("checkin").child(RestaurantCheckIn.getInstance().getTransaction());
                    Map<String, Object> updates = new HashMap<String, Object>();

                    updates.put("bill", RestaurantCheckIn.getInstance().getBill());

                    mFirebaseCheckInUpdate.updateChildren(updates);

                    String bill = "$ "+String.format("%1$,.2f", RestaurantCheckIn.getInstance().getBill());
                    TextView priceText = (TextView) PaymentActivity.this.findViewById(R.id.lbl_payment_order_bill);
                    priceText.setText(bill);
                }else{
                    Firebase mFirebaseProductListRemoveUnchecked = new FirebaseConn().child("restaurant").child("productCheckin").child(productCheckIn.getProductTransaction());
                    mFirebaseProductListRemoveUnchecked.removeValue();
                }
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {

            }
        });
    }

    private void initializeButtons(){
        Button btnAction =(Button) findViewById(R.id.btnPayNow);

        btnAction.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Firebase mFirebaseCheckInUpdate = new FirebaseConn().child("restaurant").child("checkin").child(RestaurantCheckIn.getInstance().getTransaction());
                Map<String, Object> updates = new HashMap<String, Object>();

                updates.put("status", getResources().getString(R.string.lbl_CheckOutSucess));

                mFirebaseCheckInUpdate.updateChildren(updates);

                Intent it = new Intent(v.getContext(), ReceiptActivity.class);

                v.getContext().startActivity(it);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_payment, menu);
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
