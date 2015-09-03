package com.totvs.beetlesrestaurant.adapters;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Query;
import com.koushikdutta.ion.Ion;
import com.totvs.beetlesrestaurant.CheckInActivity;
import com.totvs.beetlesrestaurant.models.Company;
import com.totvs.beetlesrestaurant.models.RestaurantCheckIn;

/**
 * Created by rond.borges on 01/09/2015.
 */
public class CompanyListAdapter extends FirebaseListAdapter<Company>{

    public CompanyListAdapter(Query ref, Activity activity, int layout, String fireBase_url) {
        super(ref, Company.class, layout, activity, fireBase_url);
    }

    @Override
    protected void populateView(View view, Company model) {

        String title = model.getTitle();
        TextView titleText = (TextView) view.findViewWithTag("company_title");
        titleText.setText(title);

        String phone = model.getPhone();
        TextView phoneText =(TextView) view.findViewWithTag("company_phone");
        phoneText.setText(phone);

        String location = model.getLocation();
        TextView locationText =(TextView) view.findViewWithTag("company_location");
        locationText.setText(location);

        String companyCode = model.getCompanyCode();
        TextView companyCodeText =(TextView) view.findViewWithTag("company_code");
        companyCodeText.setText(companyCode);

        String pictureUrl = model.getPictureUrl();
        ImageView imageImageView =(ImageView) view.findViewWithTag("company_picture");
        Ion.with(imageImageView)
                .fitCenter()
                .load(pictureUrl);

        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                try {
                    RestaurantCheckIn.getInstance().clear();
                    Company.getInstance().clear();

                    TextView companyCodeText =(TextView) v.findViewWithTag("company_code");

                    Intent it = new Intent(v.getContext(), CheckInActivity.class);

                    it.putExtra(Company.COMPANY_CODE, companyCodeText.getText().toString());

                    v.getContext().startActivity(it);
                }catch (Exception e){
                    Toast.makeText(v.getContext(), "OnClick: "+e.toString(), Toast.LENGTH_LONG).show();
                }
            }
        });
    }
}
