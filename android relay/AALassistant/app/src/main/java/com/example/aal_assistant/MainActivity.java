package com.example.aal_assistant;

import android.Manifest;
import android.content.DialogInterface;
import android.support.v4.app.ActivityCompat;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity implements inter.updateAction {
    private static final int PERMISSIONS_REQUEST = 100;
    private Fragment currentFragment;
    private FragmentManager fragmentManager;
    private SwipeRefreshLayout refresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        refresh = (SwipeRefreshLayout) findViewById(R.id.swipeContainer);
       refresh.setEnabled(false);


        // exit if the device doesn't have BLE
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, R.string.no_ble, Toast.LENGTH_SHORT).show();
            finish();
        }
        //Check whether GPS tracking is enabled//
        LocationManager lm = (LocationManager) getSystemService(LOCATION_SERVICE);
        if (!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
        }
//Check whether this app has access to the location permission//
        int permission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION);
//If the locationCon permission has been granted, then start the TrackerService//
        if (permission != PackageManager.PERMISSION_GRANTED) {

//If the app doesn’t currently have access to the user’s locationCon, then request access//
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST);
        }
        fragmentManager = getSupportFragmentManager();
        currentFragment = Accelerometer_frag.newInstance("54:6C:0E:53:01:1B"); //enter MAC
        fragmentManager.beginTransaction().replace(R.id.container, currentFragment).commit();
    }

    @Override
    public void onListFragmentInteraction(String address) {
        currentFragment = Accelerometer_frag.newInstance(address);

    }

    @Override
    public void onShowProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing(true);
            }
        });
    }

    @Override
    public void onHideProgress() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                refresh.setRefreshing(false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_about:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.about_title);
                builder.setMessage(R.string.about_message);
                builder.setNegativeButton(R.string.github, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setData(Uri.parse(getString(R.string.github_url)));
                        startActivity(intent);
                    }
                });
                builder.setPositiveButton(R.string.close, null);
                builder.show();
                break;
        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[]
            grantResults) {
        if (requestCode == PERMISSIONS_REQUEST && grantResults.length == 1
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

        } else {
            Toast.makeText(this, "Please enable location to allow tracking", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.information, menu);
        return super.onCreateOptionsMenu(menu);
    }
}
