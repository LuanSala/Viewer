package lsa.viewercloudpoints.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.io.IOException;
import java.util.UUID;

/**
 * Created by Luan Sala on 23/02/2016.
 */
public class ConnectThread extends Thread {
    public final static String TAG = "ConnectThread";


    public ConnectThread(Handler handler, BluetoothDevice btDevice){
        BluetoothSocket tmp = null;
        this.handler = handler;
        mBluetoothDevice = btDevice;
        try {
            //d5db7e8e-4374-465c-869e-2948734503a2
            tmp = mBluetoothDevice.createRfcommSocketToServiceRecord(UUID.fromString("d5db7e8e-4374-465c-869e-2948734503a2"));
            created = true;
        } catch(IOException e){
            Log.d(TAG,"Error in create ConnectThread");
        }
        mBluetoothSocket = tmp;
    }

    @Override
    public void run() {
        BluetoothAdapter.getDefaultAdapter().cancelDiscovery();

        try {
            mBluetoothSocket.connect();
        } catch (IOException e) {
            Log.d(TAG,"Error in connect to server");
            e.printStackTrace();
            try {
                mBluetoothSocket.close();
            } catch (IOException eClose) {}
        }
        if( isConnected() && mOnConnectDeviceListener!=null )
            handler.post(new Runnable() {
                @Override
                public void run() {
                    mOnConnectDeviceListener.onConnected(mBluetoothDevice, mBluetoothSocket);
                }
            });
    }

    public void closeSocket(){
        try {
            mBluetoothSocket.close();
        } catch (IOException e) {}
    }

    public boolean isCreated(){
        return created;
    }

    public boolean isConnected(){
        return mBluetoothSocket.isConnected();
    }

    public String getDeviceAddressConnected(){
        return mBluetoothDevice.getAddress();
    }

    public BluetoothDevice getDeviceConnected() {
        return mBluetoothDevice;
    }

    public interface OnConnectDeviceListener {
        void onConnected(BluetoothDevice btDevice, BluetoothSocket serverSocket);
    }

    public void setOnConnectDeviceListener(OnConnectDeviceListener listener) {
        mOnConnectDeviceListener = listener;
    }

    private boolean created;
    private boolean connected;
    private Handler handler;

    private final BluetoothDevice mBluetoothDevice;
    private final BluetoothSocket mBluetoothSocket;

    private OnConnectDeviceListener mOnConnectDeviceListener;
}
