package com.nghichty.hoangloc.doorbluetooth;
import android.app.ListActivity;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.Handler;
import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothManager;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class ScanDevice extends AppCompatActivity {
    private BluetoothAdapter myBluetoothAdapter;
    private BluetoothLeScanner myBluetoothLeScanner;
    List<BluetoothDevice> listBluetoothDevice_temp;
    ArrayList listBluetoothDevice;
    private Handler mHandler = new Handler();
    BluetoothManager btManager;
    BluetoothAdapter btAdapter;
    ListAdapter listAdapterLE;
    Button Button_Scan;
    private Boolean myScanning;
    private static final long SCAN_PERIOD = 10000;
    ListView myListView;
    private static final int ENABLE_BT_REQUEST_ID = 1;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scandevice);
        Button_Scan = (Button)findViewById(R.id.button_scan);
        myListView = (ListView)findViewById(R.id.listView);

        if (ContextCompat.checkSelfPermission(this,Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED)
        {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {
            } else {
                ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
            }
        }

        // Use this check to determine whether BLE is supported on the device.  Then you can
        // selectively disable BLE-related features.
        if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            Toast.makeText(this, "Bluetooth LE Is Not Supported In This Device!", Toast.LENGTH_SHORT).show();
            finish();
        }

        // Check if Bluetooth is ready open
         btManager = (BluetoothManager)getSystemService(BLUETOOTH_SERVICE);
         btAdapter = btManager.getAdapter();
         myBluetoothLeScanner = btAdapter.getBluetoothLeScanner();

        if (btAdapter != null && !btAdapter.isEnabled()) {
            Intent TurnOnBluetooth = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE); //Request enable
            startActivityForResult(TurnOnBluetooth, ENABLE_BT_REQUEST_ID);
        }

        listBluetoothDevice_temp = new ArrayList<>();// add device

        listBluetoothDevice = new ArrayList<>();//to add string
        listAdapterLE = new ArrayAdapter<BluetoothDevice>(this, android.R.layout.simple_list_item_1, listBluetoothDevice);
        myListView.setAdapter(listAdapterLE);

        myListView.setOnItemClickListener(scanResultOnItemClickListener);
        Button_Scan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                scanLeDevice(true);
            }
        });
    }

    AdapterView.OnItemClickListener scanResultOnItemClickListener = new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
          //  final BluetoothDevice device = mDevicesListAdapter.getDevice(position);
            // Get the device MAC address, the last 17 chars in the View
            String info = ((TextView)view).getText().toString();//Take all text on textView
            String adress = info.substring(info.length()-17);   //Take only address
            String name = info.substring(0,info.length()-18);
            Intent intent = new Intent(ScanDevice.this,Control.class);
            intent.putExtra(Control.EXTRAS_DEVICE_NAME,name);
            intent.putExtra(Control.EXTRAS_DEVICE_ADDRESS,adress);
     //       intent.putExtra(Control.EXTRA_DEVICE_RSSI,mDevicesListAdapter.getRssi(position);

            if (myScanning) {
                myBluetoothLeScanner.stopScan(scanCallback);
                myScanning = false;
            }
            scanLeDevice(false);// will stop after first device detection
            startActivity(intent);
        }
    };

    private String getBTDevieType(BluetoothDevice d){
        String type = "";

        switch (d.getType()){
            case BluetoothDevice.DEVICE_TYPE_CLASSIC:
                type = "DEVICE_TYPE_CLASSIC";
                break;
            case BluetoothDevice.DEVICE_TYPE_DUAL:
                type = "DEVICE_TYPE_DUAL";
                break;
            case BluetoothDevice.DEVICE_TYPE_LE:
                type = "DEVICE_TYPE_LE";
                break;
            case BluetoothDevice.DEVICE_TYPE_UNKNOWN:
                type = "DEVICE_TYPE_UNKNOWN";
                break;
            default:
                type = "unknown...";
        }

        return type;
    }

     @Override
     protected void onActivityResult(int requestCode, int resultCode, Intent data)
     {
         // user didn't want to turn on BT
         if (requestCode == ENABLE_BT_REQUEST_ID && resultCode == Activity.RESULT_CANCELED)
         {
             finish();
             return;
         }
         getBluetoothAdapterAndLeScanner();
         super.onActivityResult(requestCode, resultCode, data);
     }
    /*
    @Override
    protected void onResume()
    {
        super.onResume();
        if (btAdapter == null || !btAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, ENABLE_BT_REQUEST_ID);
        } else {
            if (Build.VERSION.SDK_INT >= 21) {
                myBluetoothLeScanner = myBluetoothAdapter.getBluetoothLeScanner();
                settings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                filters = new ArrayList<ScanFilter>();
            }
            scanLeDevice(true);
        }
    }*/

    private void getBluetoothAdapterAndLeScanner()
    {
        final BluetoothManager bluetoothManager = (BluetoothManager)getSystemService(Context.BLUETOOTH_SERVICE);
        myBluetoothAdapter = bluetoothManager.getAdapter();
        myBluetoothLeScanner = myBluetoothAdapter.getBluetoothLeScanner();
    }

    /*
   to call startScan (ScanCallback callback),
   Requires BLUETOOTH_ADMIN permission.
   Must hold ACCESS_COARSE_LOCATION or ACCESS_FINE_LOCATION permission to get results.
    */
    private void scanLeDevice(final boolean enable) {
        if (enable) {
            // Stops scanning after a pre-defined scan period.
            mHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    myScanning = false;
                    myBluetoothLeScanner.startScan(scanCallback);
                    invalidateOptionsMenu();
                }
            }, SCAN_PERIOD);

            myScanning = true;
            myBluetoothLeScanner.startScan(scanCallback);
        } else {
            myBluetoothLeScanner.stopScan(scanCallback);
            myScanning = false;
            Button_Scan.setEnabled(true);
        }
        invalidateOptionsMenu();
    }

        private ScanCallback scanCallback = new ScanCallback() {
            @Override
            public void onScanResult(int callbackType, ScanResult result) {
                super.onScanResult(callbackType, result);
                addBluetoothDevice(result.getDevice());
            }

            @Override
            public void onBatchScanResults(List<ScanResult> results) {
                super.onBatchScanResults(results);
                for (ScanResult result : results) {
                    addBluetoothDevice(result.getDevice());
                }
            }

            @Override
            public void onScanFailed(int errorCode) {
                super.onScanFailed(errorCode);
                Toast.makeText(ScanDevice.this,
                        "onScanFailed: " + String.valueOf(errorCode),
                        Toast.LENGTH_LONG).show();
            }

            private void addBluetoothDevice(BluetoothDevice device) {
                if (!listBluetoothDevice_temp.contains(device)) {
                    listBluetoothDevice_temp.add(device);
                    listBluetoothDevice.add(device.getName()+"\n"+device.getAddress());
                    myListView.invalidateViews();
                }
            }
        };

}
