package com.bignerdranch.android.locatr.controller.fragment;

import android.Manifest;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bignerdranch.android.locatr.R;
import com.bignerdranch.android.locatr.model.GalleryItem;
import com.bignerdranch.android.locatr.utils.FlickrFetchr;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.List;

/**
 * Created by TINH HUYNH on 5/20/2017.
 */

public class LocatrFragment extends Fragment {
    private static final String TAG = "LocatrFragment";

    private ImageView mImageView;
    private GoogleApiClient mClient;

    public static LocatrFragment newInstance() {
        return new LocatrFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        mClient = new GoogleApiClient.Builder(getActivity())
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        getActivity().invalidateOptionsMenu();
                    }

                    @Override
                    public void onConnectionSuspended(int i) {
                        getActivity().invalidateOptionsMenu();
                    }
                })
                .build();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_locatr, container, false);

        mImageView = (ImageView) view.findViewById(R.id.image);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        mClient.connect();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.fragment_locatr, menu);

        MenuItem searchItem = menu.findItem(R.id.action_locate);
        searchItem.setEnabled(mClient.isConnected());
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_locate:
                findImage();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void findImage() {
        LocationRequest request = LocationRequest.create();
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        request.setNumUpdates(1);
        request.setInterval(0);
        if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        LocationServices.FusedLocationApi.requestLocationUpdates(mClient, request, new LocationListener() {
            @Override
            public void onLocationChanged(Location location) {
                Log.i(TAG, "Got a fix: " + location);
                new SearchTask().execute(location);
            }
        });
    }

    private class SearchTask extends AsyncTask<Location, Void, Void>{
        private GalleryItem mGalleryItem;
        private Bitmap mBitmap;
        private ProgressDialog mProgressDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressDialog = new ProgressDialog(getActivity());
            mProgressDialog.setIndeterminate(true);
            mProgressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
            mProgressDialog.setMessage("Loading data for you");
            mProgressDialog.show();
        }

        @Override
        protected Void doInBackground(Location... params) {
            FlickrFetchr flickrFetchr = new FlickrFetchr();
            List<GalleryItem> items = flickrFetchr.searchPhotos(params[0]);
            if(items.isEmpty()){
                return null;
            }
            mGalleryItem = items.get(0);

            try {
                byte[] bytes = flickrFetchr.getUrlBytes(mGalleryItem.getUrl());
                mBitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
            } catch (IOException e) {
                Log.i(TAG, "Unable to download bitmap", e);
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            mProgressDialog.dismiss();
            mImageView.setImageBitmap(mBitmap);
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        mClient.disconnect();
    }
}
