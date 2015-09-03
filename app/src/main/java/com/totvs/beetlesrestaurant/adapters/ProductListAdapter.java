package com.totvs.beetlesrestaurant.adapters;

import android.app.Activity;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.client.Firebase;
import com.firebase.client.Query;
import com.koushikdutta.ion.Ion;
import com.totvs.beetlesrestaurant.drivers.FirebaseConn;
import com.totvs.beetlesrestaurant.models.Product;
import com.totvs.beetlesrestaurant.models.ProductCheckIn;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by rond.borges on 01/09/2015.
 */
public class ProductListAdapter extends FirebaseListAdapter<ProductCheckIn> {

    public ProductListAdapter(Query ref, Activity activity, int layout, String fireBase_url) {
        super(ref, ProductCheckIn.class, layout, activity, fireBase_url);
    }

    @Override
    protected void populateView(View view, ProductCheckIn model) {
        try {
            String title = model.getTitle();
            TextView titleText = (TextView) view.findViewWithTag("product_title");
            titleText.setText(title);

            String description = model.getDescription();
            TextView descriptionText = (TextView) view.findViewWithTag("product_description");
            descriptionText.setText(description);

            String price = String.format("%1$,.2f", model.getPrice());
            TextView priceText = (TextView) view.findViewWithTag("product_price");
            priceText.setText(price);

            Boolean checked = model.getChecked();
            CheckBox checkedText =(CheckBox) view.findViewWithTag("product_checked");
            checkedText.setChecked(checked);

            // Used by che ckeck
            String product_transaction = model.getProductTransaction();
            TextView productTransactionText = (TextView) view.findViewWithTag("product_product_transaction");
            productTransactionText.setText(product_transaction);

            checkedText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        TextView productTransactionText = (TextView) v.getRootView().findViewWithTag("product_product_transaction");
                        CheckBox checkedText =(CheckBox) (CheckBox)v;

                        Firebase mFirebaseProductUpdate = new FirebaseConn().child("restaurant").child("productCheckin").child(productTransactionText.getText().toString());
                        Map<String, Object> updates = new HashMap<String, Object>();

                        updates.put("checked", checkedText.isChecked());

                        mFirebaseProductUpdate.updateChildren(updates);
                        //Toast.makeText(v.getContext(), "transaction: " + productTransactionText.getText(), Toast.LENGTH_LONG).show();
                    }catch (Exception e){
                        Toast.makeText(v.getContext(), "checkedText.OnClick: " + e.toString(), Toast.LENGTH_LONG).show();
                    }
                }
            });

            String pictureUrl = model.getPictureUrl();
            ImageView imageImageView = (ImageView) view.findViewWithTag("product_picture");
            Ion.with(imageImageView)
                    .fitCenter()
                    .load(pictureUrl);
        } catch (Exception e) {
            Toast.makeText(view.getContext(), "listProducts: " + e.toString(), Toast.LENGTH_LONG).show();
        }
    }
}
