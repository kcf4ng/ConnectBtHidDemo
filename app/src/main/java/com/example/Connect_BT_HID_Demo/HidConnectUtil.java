package com.example.Connect_BT_HID_Demo;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.util.Log;

import java.lang.reflect.Method;

public class HidConnectUtil {
    private static final String TAG = "HidConnectUtil";

    Context context;
    private final int INPUT_DEVICE = 4;
    BluetoothAdapter mBtAdapter;
    BluetoothProfile    mBluetoothProfile;
    static HidConnectUtil instance;


    public static HidConnectUtil getInstance(Context context){
        if(instance == null){
            instance = new HidConnectUtil(context);
        }
        return instance;
    }

    private BluetoothProfile.ServiceListener mListener = new BluetoothProfile.ServiceListener() {
        @Override
        public void onServiceConnected(int profile, BluetoothProfile proxy) {
            Log.d(TAG, "mConnectListener onServiceConnected profidle: "+profile);

            try {
                if(profile == INPUT_DEVICE){
                    //set BluetoothProfile as INPUT_DEVICE profile.
                    mBluetoothProfile = proxy;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(int profile) {
            Log.d(TAG, "mConnectListener onServiceDisconnected: ");
        }
    };

    public HidConnectUtil(Context mContext) {
        this.context = mContext;
        mBtAdapter = BluetoothAdapter.getDefaultAdapter();

        try {
            mBtAdapter.getProfileProxy(context,mListener, INPUT_DEVICE);
        } catch (Exception e) {
            Log.d(TAG, "HidConnectUtil: get Progfile Proxy error ");
            e.printStackTrace();
        }

    }

    public boolean isBound(BluetoothDevice dev){
        return (dev.getBondState() == BluetoothDevice.BOND_BONDED);
    }

    public void pair(BluetoothDevice device){
        Log.d(TAG, "pair device : "+device);
        Method createBondMethod;
        try {
            createBondMethod = BluetoothDevice.class.getMethod("createBond");
             createBondMethod.invoke(device);
             } catch (Exception e) {
             e.printStackTrace();
             }
     }

    public void connect(final BluetoothDevice device) {
        Log.d(TAG, "connect device: "+device);
             try {
                 Method method = mBluetoothProfile.getClass().getMethod("connect", new Class[] { BluetoothDevice.class });
                 method.invoke(mBluetoothProfile, device);

                  } catch (Exception e) {
                  e.printStackTrace();
                  }
             }

    public void disConnect(BluetoothDevice device) {
        Log.d(TAG, "disConnect device:"+device);
             try {
                 if (device != null) {
                      Method method = mBluetoothProfile.getClass().getMethod("disconnect", new Class[] { BluetoothDevice.class });
                      method.invoke(mBluetoothProfile, device);}
             } catch (Exception e) {

                 e.printStackTrace();
             }
    }
    }



