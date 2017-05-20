package com.bignerdranch.android.locatr.controller.activity;

import android.app.Dialog;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import com.bignerdranch.android.locatr.R;
import com.bignerdranch.android.locatr.controller.fragment.LocatrFragment;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.GooglePlayServicesUtil;

public class LocatrActivity extends SingleFragmentActivity {
    private static final int REQUEST_CODE = 0;

    @Override
    protected Fragment createFragment() {
        return LocatrFragment.newInstance();
    }

    @Override
    protected void onResume() {
        super.onResume();
        GoogleApiAvailability googleApiAvailability = GoogleApiAvailability.getInstance();

        int errorCode = googleApiAvailability.isGooglePlayServicesAvailable(this);

        if(errorCode != ConnectionResult.SUCCESS){
           if(googleApiAvailability.isUserResolvableError(errorCode)){
               googleApiAvailability.getErrorDialog(this, errorCode,
                       REQUEST_CODE, new DialogInterface.OnCancelListener() {
                           @Override
                           public void onCancel(DialogInterface dialog) {
                               //leave if services are unavailable
                               finish();
                           }
                       }).show();
           }
        }
    }
}
