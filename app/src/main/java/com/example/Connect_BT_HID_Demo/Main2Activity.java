package com.example.Connect_BT_HID_Demo;

import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class Main2Activity extends AppCompatActivity {
    private static final String TAG = "_Main2";
    BluetoothAdapter mBluetoothAdapter;
    BroadcastReceiver mBroadcastReceiver;
    public ArrayList<BluetoothDevice> btDeviceList = new ArrayList<>();
    HidConnectUtil mHidConnectUtil;
    BluetoothDevice mConnectDevice;
    TextView txt;

    /*
    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        System.out.println("KeyDown"+event.getKeyCode());
        int code = event.getKeyCode();
        switch (code){
            case 24:
                mSignage.runShowSingleImage(SLIDESHOW_DIR_PATH+ "/" + "00.png");
                break;
            case 84:
                mSignage.runShowSingleImage(SLIDESHOW_DIR_PATH+ "/" + "01.png");
                break;
            case 82:
                mSignage.runShowSingleImage(SLIDESHOW_DIR_PATH+ "/" + "02.png");
                break;
            default:
                mSignage.runShowSingleImage(SLIDESHOW_DIR_PATH+ "/" + "flower.png");
        }

        return true;
    }
*/
    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mBroadcastReceiver );
    }

    private void openBT() {
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        if(mBluetoothAdapter == null){
            //no BT support;
            return;
        }

        //BT support , enable BT

        if(!mBluetoothAdapter.isEnabled()){
            mBluetoothAdapter.enable();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        txt.setText("["+keyCode+"]");
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        txt = findViewById(R.id.txt);

        //開啟藍牙
        openBT();
        checkBTPermission();
        mBroadcastReceiver = new BlueBroadcastReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BluetoothDevice.ACTION_FOUND);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        intentFilter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        intentFilter.addAction("android.bluetooth.input.profile.action.CONNECTION_STA TE_CHANGED");

        try {
            this.registerReceiver(mBroadcastReceiver,intentFilter);
        } catch (Exception e) {
            Log.d(TAG, "register bt Receiver Error"+e);
            e.printStackTrace();
        }

        mHidConnectUtil = HidConnectUtil.getInstance(this);

        mBluetoothAdapter.startDiscovery();



    }

    private void connectBtDevice(BluetoothDevice device) {

        if(!mHidConnectUtil.isBound(device)){
            mHidConnectUtil.pair(device);
        }
        if(mHidConnectUtil.isBound(device)){
            mHidConnectUtil.connect(device);
        }

    }

    public void btnConnect(View view) {
        DoPairBeforeConnect();

    }

    private void DoPairBeforeConnect() {
        BluetoothDevice  dev = null;
        for(int i = 0; i<btDeviceList.size(); i++){
            dev = btDeviceList.get(i);
            if(dev.getName() !=null){
                if(dev.getName().equals("MOCUTE-052_S23-AUTO")){
                    break;
                }else{
                    dev= null;
                }
            }
        }

        if(dev != null)
        {
            mConnectDevice = dev;
            Log.d(TAG, "Bluetooth start connect to:"+ dev.getName());
            connectBtDevice(mConnectDevice);
        }
        if(dev == null){
            mBluetoothAdapter.startDiscovery();
        }

    }


    private class BlueBroadcastReceiver extends BroadcastReceiver{

        public BlueBroadcastReceiver(){
            super();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent .getAction();
            Log.d(TAG, "--BlueBroadcastReceiver  onReceive action =\n" + action );

            if(action.equals(BluetoothDevice.ACTION_FOUND)){

                final BluetoothDevice localBluetoothDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(btDeviceList == null ){
                    btDeviceList = new ArrayList<BluetoothDevice>();
                }

                //device found
                Log.d(TAG, "--onReceive device: \n"+
                        localBluetoothDevice.getName() +":\n "
                        +localBluetoothDevice.getAddress());
                btDeviceList.add(localBluetoothDevice);

                if("MOCUTE-052_S23-AUTO".equals(localBluetoothDevice.getName())){
                    txt.setText("MOCUTE-052_S23-AUTO has benn found");

                    if(!mHidConnectUtil.isBound(localBluetoothDevice)){
                        mHidConnectUtil.pair(localBluetoothDevice);
                    }
                    if(mHidConnectUtil.isBound(localBluetoothDevice)){
                        mHidConnectUtil.connect(localBluetoothDevice);
                    }
                }
            }else if( action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)){
                BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                if(device.getName().equals("MOCUTE-052_S23-AUTO")){

                    int connState = device.getBondState();
                    switch(connState){
                        case BluetoothDevice.BOND_BONDED:
                            try {
                                Log.d(TAG, "connect to "+device.getName());
                                mHidConnectUtil.connect(device);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                            break;
                    }
                }
            }

        }
    }

    /*
    this method is required for all devices running  API 23+ (Android 6.0 + )
    Android must programmatically check the permission for bluetooth
    only put permission in manifest is not enough
    Note: this will only execute on version > LOLLIPOP because it is not needed otherwise.
    */
    private void checkBTPermission() {
        Log.d(TAG, "checkBTPermission: Start");
        if(Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP){
            int permissionCheck = 0;
            if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                permissionCheck = this.checkSelfPermission("Manifest.permission.ACCESS_FINE_LOCATION");
                permissionCheck += this.checkSelfPermission("Manifest.permission.ACCESS_COARSE_LOCATION");
                if(permissionCheck != 0){
                    this.requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, 1001); //any number
                }else{
                    Log.d(TAG,
                            "checkBTPermissions: No need to check permissions. SDK version < LOLLIPOP.");
                }
            }

        }
        Log.d(TAG, "checkBTPermission: Finish");
    }

}
