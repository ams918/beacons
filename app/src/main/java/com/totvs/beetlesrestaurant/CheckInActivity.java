package com.totvs.beetlesrestaurant;

import android.content.Intent;
import android.graphics.Color;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.Firebase;
import com.firebase.client.FirebaseError;
import com.firebase.client.ValueEventListener;
import com.koushikdutta.ion.Ion;
import com.totvs.beetlesrestaurant.drivers.FirebaseConn;
import com.totvs.beetlesrestaurant.models.BeaconModel;
import com.totvs.beetlesrestaurant.models.Company;
import com.totvs.beetlesrestaurant.models.OwnerInfo;
import com.totvs.beetlesrestaurant.models.Product;
import com.totvs.beetlesrestaurant.models.ProductCheckIn;
import com.totvs.beetlesrestaurant.models.RestaurantCheckIn;

import java.util.UUID;


public class CheckInActivity extends ActionBarActivity {

    private OwnerInfo ownerInfo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_check_in);

        // Inicialize Firebase
        Firebase.setAndroidContext(this);

        ownerInfo = new OwnerInfo(this);

        try {

            if (!Company.getInstance().getCompanyCode().isEmpty()) {
                selectCompany(Company.getInstance().getCompanyCode());
            }else{
                Bundle bundle = getIntent().getExtras();
                if(bundle!=null){
                    if (bundle.containsKey(Company.COMPANY_CODE)) {
                        selectCompany(bundle.getString(Company.COMPANY_CODE));
                    }else {
                        Toast.makeText(CheckInActivity.this, "Company not selected!", Toast.LENGTH_LONG).show();
                    }
                }else {
                    Toast.makeText(CheckInActivity.this, "Company not selected!", Toast.LENGTH_LONG).show();
                }
            }

        } catch (Exception e){
            Toast.makeText(CheckInActivity.this, "onCreate: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void selectCompany(String companyCode){

        try {
            Firebase mFirebaseWelcomeCompany = new FirebaseConn().child("restaurant").child("companies").child(companyCode);

            mFirebaseWelcomeCompany.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(final DataSnapshot snapshot) {
                try {

                    Company companySelected = snapshot.getValue(Company.class);
                    Company.getInstance().copyFrom(companySelected);

                    String title = companySelected.getTitle();
                    TextView titleText = (TextView) findViewById(R.id.textViewCheckInTitle);
                    titleText.setText(title);

                    String peopleInLine = "2 " + getResources().getString(R.string.lbl_line_size);
                    TextView peopleInLineText = (TextView) findViewById(R.id.textViewPeopleInLine);
                    peopleInLineText.setText(peopleInLine);

                    String pictureUrl = companySelected.getPictureUrl();
                    ImageView imageImageView =(ImageView) findViewById(R.id.imageViewCheckInLogoRestaurant);
                    Ion.with(imageImageView)
                            .fitCenter()
                            .load(pictureUrl);

                    if (RestaurantCheckIn.getInstance().getTransaction().isEmpty()){
                        RestaurantCheckIn restaurantCheckIn = new RestaurantCheckIn(0
                                , BeaconModel.getInstance().getMacAddress()
                                , "http://dadrix.com.br/apps/image/prof_pic.jpg"
                                , getResources().getString(R.string.lbl_CheckInRequest)
                                , ownerInfo.name
                                , UUID.randomUUID().toString()
                                , 0.00
                                , 1
                                , Company.getInstance().getCompanyCode());
                        RestaurantCheckIn.getInstance().copyFrom(restaurantCheckIn);
                    }

                    Button btnAction =(Button) findViewById(R.id.BtnWaitInLine);

                    btnAction.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            Intent it = new Intent(v.getContext(), OrderActivity.class);

                            it.putExtra(Company.COMPANY_CODE, Company.getInstance().getCompanyCode());

                            Firebase mFirebaseCheckInRequest = new FirebaseConn().child("restaurant").child("checkin").child(RestaurantCheckIn.getInstance().getTransaction());
                            mFirebaseCheckInRequest.setValue(RestaurantCheckIn.getInstance());

                            Firebase mFirebaseProductList = new FirebaseConn().child("restaurant").child("products");

                            mFirebaseProductList.addChildEventListener(new ChildEventListener() {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                                    try {
                                        Product product = dataSnapshot.getValue(Product.class);
                                        if (product.getCompany().equals(Company.getInstance().getCompanyCode())) {
                                            ProductCheckIn productCheckIn = new ProductCheckIn(product.getTitle()
                                                    , product.getDescription()
                                                    , product.getPictureUrl()
                                                    , product.getPrice()
                                                    , product.getCompany()
                                                    , RestaurantCheckIn.getInstance().getTransaction()
                                                    , UUID.randomUUID().toString()
                                                    , false);

                                            Firebase mFirebaseProductCheckIn = new FirebaseConn().child("restaurant").child("productCheckin").child(productCheckIn.getProductTransaction());
                                            mFirebaseProductCheckIn.setValue(productCheckIn);
                                        }
                                    }catch (Exception e){

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

                            v.getContext().startActivity(it);
                        }
                    });

                    ConfigureButtons();

                } catch (Exception e) {
                    Toast.makeText(CheckInActivity.this, "selectCompany: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onCancelled(FirebaseError firebaseError) {
                // do nothing
            }
        });
        }catch (Exception e) {
            Toast.makeText(CheckInActivity.this, "selectCompany: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }

    private void ConfigureButtons(){
        Button btnGroup1 =(Button) findViewById(R.id.btn_group_1);

        btnGroup1.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    RestaurantCheckIn.getInstance().setNumberOfPeople(1);

                    ((Button)v).setBackgroundColor(Color.parseColor("#ff5915"));
                    ((Button)v).setTextColor(Color.parseColor("#ffffff"));

                    Button btnGroup2 = (Button) v.getRootView().findViewById(R.id.btn_group_2);
                    Button btnGroup3 = (Button) v.getRootView().findViewById(R.id.btn_group_3);
                    Button btnGroup4 = (Button) v.getRootView().findViewById(R.id.btn_group_4);

                    btnGroup2.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup2.setTextColor(Color.parseColor("#ff5915"));

                    btnGroup3.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup3.setTextColor(Color.parseColor("#ff5915"));

                    btnGroup4.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup4.setTextColor(Color.parseColor("#ff5915"));
                }catch (Exception e){
                    Toast.makeText(CheckInActivity.this, "btnGroup1.OnClick: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button btnGroup2 =(Button) findViewById(R.id.btn_group_2);

        btnGroup2.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    RestaurantCheckIn.getInstance().setNumberOfPeople(2);

                    ((Button)v).setBackgroundColor(Color.parseColor("#ff5915"));
                    ((Button)v).setTextColor(Color.parseColor("#ffffff"));

                    Button btnGroup2 = (Button) v.getRootView().findViewById(R.id.btn_group_1);
                    Button btnGroup3 = (Button) v.getRootView().findViewById(R.id.btn_group_3);
                    Button btnGroup4 = (Button) v.getRootView().findViewById(R.id.btn_group_4);

                    btnGroup2.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup2.setTextColor(Color.parseColor("#ff5915"));

                    btnGroup3.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup3.setTextColor(Color.parseColor("#ff5915"));

                    btnGroup4.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup4.setTextColor(Color.parseColor("#ff5915"));
                }catch (Exception e){
                    Toast.makeText(CheckInActivity.this, "btnGroup2.OnClick: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button btnGroup3 =(Button) findViewById(R.id.btn_group_3);

        btnGroup3.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    RestaurantCheckIn.getInstance().setNumberOfPeople(3);

                    ((Button)v).setBackgroundColor(Color.parseColor("#ff5915"));
                    ((Button)v).setTextColor(Color.parseColor("#ffffff"));

                    Button btnGroup2 = (Button) v.getRootView().findViewById(R.id.btn_group_1);
                    Button btnGroup3 = (Button) v.getRootView().findViewById(R.id.btn_group_2);
                    Button btnGroup4 = (Button) v.getRootView().findViewById(R.id.btn_group_4);

                    btnGroup2.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup2.setTextColor(Color.parseColor("#ff5915"));

                    btnGroup3.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup3.setTextColor(Color.parseColor("#ff5915"));

                    btnGroup4.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup4.setTextColor(Color.parseColor("#ff5915"));
                }catch (Exception e){
                    Toast.makeText(CheckInActivity.this, "btnGroup3.OnClick: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });

        Button btnGroup4 =(Button) findViewById(R.id.btn_group_4);

        btnGroup4.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    RestaurantCheckIn.getInstance().setNumberOfPeople(4);

                    ((Button)v).setBackgroundColor(Color.parseColor("#ff5915"));
                    ((Button)v).setTextColor(Color.parseColor("#ffffff"));

                    Button btnGroup2 = (Button) v.getRootView().findViewById(R.id.btn_group_1);
                    Button btnGroup3 = (Button) v.getRootView().findViewById(R.id.btn_group_2);
                    Button btnGroup4 = (Button) v.getRootView().findViewById(R.id.btn_group_3);

                    btnGroup2.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup2.setTextColor(Color.parseColor("#ff5915"));

                    btnGroup3.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup3.setTextColor(Color.parseColor("#ff5915"));

                    btnGroup4.setBackgroundColor(Color.parseColor("#ffffff"));
                    btnGroup4.setTextColor(Color.parseColor("#ff5915"));
                }catch (Exception e){
                    Toast.makeText(CheckInActivity.this, "btnGroup4.OnClick: " + e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_check_in, menu);
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
