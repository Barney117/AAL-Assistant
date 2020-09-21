package com.example.aal_assistant;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import java.util.Calendar;
import java.util.UUID;

public class Accelerometer_frag extends Fragment {


    private DatabaseReference databaseReference;
    locationCon mloc = new locationCon();
    private static final String Address = "Address";
    private String macAddress;
    private boolean isConnected = true;
    private inter.updateAction Listener;
    private Calendar lastVal;
    private static double L1, L2;
    private String locAddress;
    private BluetoothAdapter BluetoothAdapter;
    private BluetoothGatt gattProfile;
    private BluetoothGattService gattService;
    private BluetoothGattCharacteristic ReadCharacteristic, Start, Time;
    private TextView xAxis, yAxis, zAxis, lat, lon, textAddress;

    public Accelerometer_frag() {
    }

    /**
     * Returns a new instance of this Fragment.
     *
     * @param address the MAC address of the device to connect
     * @return A new instance of {@link Accelerometer_frag}
     */
    public static Accelerometer_frag newInstance(String address) {
        Accelerometer_frag fragment = new Accelerometer_frag();
        Bundle args = new Bundle();
        args.putString(Address, address);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            macAddress = getArguments().getString(Address);
        }
        databaseReference = FirebaseDatabase.getInstance().getReference();
        BluetoothManager manager = (BluetoothManager) getActivity().getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter = manager.getAdapter();
    }



    @Override
    public void onResume() {
        super.onResume();
        connectToSensor(macAddress);
    }

    @Override
    public void onPause() {
        deviceDisconnected();
        super.onPause();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof inter.updateAction) {
            Listener = (inter.updateAction) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnStatusListener");
        }
    }

    private void connectToSensor(String address) {
        if (!BluetoothAdapter.isEnabled()) {
            BluetoothAdapter.getDefaultAdapter().enable();
            Toast.makeText(getActivity(), R.string.enable, Toast.LENGTH_SHORT).show();
            getActivity();
        }
        Listener.onShowProgress();
        BluetoothDevice device = BluetoothAdapter.getRemoteDevice(address);
        gattProfile = device.connectGatt(getActivity(), true, callback);
    }

    private void deviceConnected() {
        startTrackerService();
        Listener.onHideProgress();
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });

        // start connection watcher thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean Connected = true;
                while (Connected) {
                    long diff = Calendar.getInstance().getTimeInMillis() - lastVal.getTimeInMillis();
                    if (diff > 2000) {
                        Connected = false;
                        stopTrackerService();
                        new Thread(new Runnable() {
                            @Override
                            public void run() {
                            }
                        });
                    }
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
    private void startTrackerService() {
        getContext().startService(new Intent(getContext(), location_service.class));

    }

    public void stopTrackerService() {
        getContext().stopService(new Intent(getContext(), location_service.class));

    }

    private void deviceDisconnected() {
        if (gattProfile != null) gattProfile.disconnect();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View layout = inflater.inflate(R.layout.accelerometer_fragment, container, false);
        xAxis = layout.findViewById(R.id.XAxis);
        yAxis = layout.findViewById(R.id.YAxis);
        zAxis = layout.findViewById(R.id.ZAxis);
        lat = layout.findViewById(R.id.latitude);
        lon = layout.findViewById(R.id.longitude);
        textAddress = (TextView) layout.findViewById(R.id.StringAdd);

        return layout;
    }

    /**
     * makes connection with the gatt profile of the CC2650
     * requests connection
     * Looks for Accelerometer
     * Reads Accelerometer
     * Writes to Firebase
     */
    private BluetoothGattCallback callback = new BluetoothGattCallback() {
        double axisVals[];

        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            super.onConnectionStateChange(gatt, status, newState);
            switch (newState) {
                case BluetoothGatt.STATE_CONNECTED:
                    gatt.discoverServices();
                    break;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            super.onServicesDiscovered(gatt, status);
            gattService = gattProfile.getService(UUID.fromString("F000AA80-0451-4000-B000-000000000000"));
            Start = gattService.getCharacteristic(UUID.fromString("F000AA82-0451-4000-B000-000000000000"));
            Start.setValue(0b1000111000, BluetoothGattCharacteristic.FORMAT_UINT16, 0);
            gattProfile.writeCharacteristic(Start);
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicWrite(gatt, characteristic, status);
            if (characteristic == Start) {
                Time = gattService.getCharacteristic(UUID.fromString("F000AA83-0451-4000-B000-000000000000"));

                Time.setValue(0x0A, BluetoothGattCharacteristic.FORMAT_UINT8, 0);
                gattProfile.writeCharacteristic(Time);
            } else if (characteristic == Time) {
                ReadCharacteristic = gattService.getCharacteristic(UUID.fromString("F000AA81-0451-4000-B000-000000000000"));

                lastVal = Calendar.getInstance();
                gattProfile.readCharacteristic(ReadCharacteristic);
                deviceConnected();
            }
        }

        @Override
        public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            super.onCharacteristicRead(gatt, characteristic, status);
            axisVals = representations.convertAccelerometerdata(characteristic.getValue());
            if (isConnected) {
                double X = axisVals[0];
                double Y = axisVals[1];
                double Z = axisVals[2];
                sensor fire = new sensor(X, Y, Z);//here
                databaseReference.child("Accelerometer").setValue(fire);

                L1 = mloc.getLatitude();
                L2 = mloc.getLongitude();
                locAddress = mloc.getAddress();

                databaseReference.child("Location").child("latitude").setValue(L1);
                databaseReference.child("Location").child("longitude").setValue(L2);
                databaseReference.child("Location").child("address").setValue(locAddress);
            }
            getActivity().runOnUiThread(new Runnable() {

                @Override
                public void run() {
                    if (isAdded()) {

                        // update current acceleration readings
                        xAxis.setText(String.format(getString(R.string.xAxis), Math.abs(axisVals[0])));
                        yAxis.setText(String.format(getString(R.string.yAxis), Math.abs(axisVals[1])));
                        zAxis.setText(String.format(getString(R.string.zAxis), Math.abs(axisVals[2])));
                        lat.setText(String.format(getString(R.string.Lat), mloc.getLatitude()));
                        lon.setText(String.format(getString(R.string.Lon), mloc.getLongitude()));
                        textAddress.setText(mloc.getAddress());

                    }
                }
            });
            gattProfile.readCharacteristic(ReadCharacteristic);
        }
    };


}

