package com.example.homeserviceapp.Customer;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.homeserviceapp.R;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class CustomerBottomSheetDialog extends BottomSheetDialogFragment {

    String serviceProviderName, serviceProviderPhone, serviceProviderImage, serviceProviderServices;
    Context context;

    public CustomerBottomSheetDialog() {
    }

    public CustomerBottomSheetDialog(String serviceProviderName, String serviceProviderPhone, String serviceProviderImage, String serviceProviderServices, Context context) {
        this.serviceProviderName = serviceProviderName;
        this.serviceProviderPhone = serviceProviderPhone;
        this.serviceProviderImage = serviceProviderImage;
        this.serviceProviderServices = serviceProviderServices;
        this.context = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.bottom_sheet_layout, container, false);

        CircleImageView image = v.findViewById(R.id.service_provider_image);
        TextView nameTextView = v.findViewById(R.id.service_provider_name);
        TextView servicesTextView = v.findViewById(R.id.service_provider_services);
        LinearLayout phone = v.findViewById(R.id.service_provider_phone);
        TextView moreTextView = v.findViewById(R.id.service_provider_more);


        Picasso.get().load(this.serviceProviderImage).into(image);
        nameTextView.setText(this.serviceProviderName);
        servicesTextView.setText("Service: " + this.serviceProviderServices);

        phone.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MakeCall();
            }
        });

        moreTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, MakeRequestActivity.class);
                intent.putExtra("providerID", serviceProviderPhone);
                startActivity(intent);
            }
        });

        return v;
    }

    public void MakeCall() {
        if (this.serviceProviderPhone.trim().length() > 0) {
            if (ContextCompat.checkSelfPermission(this.context,
                    Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {

                ActivityCompat.requestPermissions((Activity) context,
                        new String[]
                                {
                                        Manifest.permission.CALL_PHONE
                                }, 1);

            } else {
                String dial = "tel:" + this.serviceProviderPhone;
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(dial)));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                MakeCall();
            } else {
                Toast.makeText(this.context, "Permission DENIED", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
