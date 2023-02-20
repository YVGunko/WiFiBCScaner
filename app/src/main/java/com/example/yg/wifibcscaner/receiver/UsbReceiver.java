package com.example.yg.wifibcscaner.receiver;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;

import com.example.yg.wifibcscaner.MainActivity;
import com.example.yg.wifibcscaner.receiver.Config;

import static com.example.yg.wifibcscaner.receiver.Config.ACTION_USB_PERMISSION;

public class UsbReceiver extends BroadcastReceiver {
    private static final String TAG = "UsbReceiver";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            synchronized (this) {
                mdevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if(mdevice != null){
                    //
                    Log.d(TAG,"USB device is unplaged -" + mdevice);
                }
            }
        }
        //
        if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            synchronized (this) {
                mdevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                    if(mdevice != null){
                        //

                        Log.d(TAG,"USB device is plaged on -" + mdevice);
                    }
                }
                else {
                    PendingIntent mPermissionIntent;
                    mPermissionIntent = PendingIntent.getBroadcast(MainActivity.this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_ONE_SHOT);
                    mUsbManager.requestPermission(mdevice, mPermissionIntent);

                }

            }
        }
//
        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                mdevice = (UsbDevice)intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {

                    if(mdevice != null){
                        //
                        Log.d("1","USB устройство разрешено-" + mdevice);
                        showMessage("USB устройство разрешено");
                    }
                }

            }
        }

    }
}
