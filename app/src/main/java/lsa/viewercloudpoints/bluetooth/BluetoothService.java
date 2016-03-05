package lsa.viewercloudpoints.bluetooth;

import android.app.Service;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

/**
 * Created by Luan Sala on 04/03/2016.
 */
public class BluetoothService extends Service {
    private static final String TAG = BluetoothService.class.getSimpleName();
    private final Binder mBinder = new LocalBinder();

    public class LocalBinder extends Binder {
        BluetoothService getService() {
            // Return this instance of LocalService so clients can call public methods
            return BluetoothService.this;
        }
    }

    @Override
    public void onCreate() {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_REDELIVER_INTENT;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return mBinder;
    }

    @Override
    public boolean onUnbind(Intent intent) {
        return true;
    }

    @Override
    public void onRebind(Intent intent) {
    }

    @Override
    public void onDestroy() {
    }


    public ConnectThread getConnectionThread() {
        return connectionThread;
    }

    public ReadWriteStream getReadWriteStream() {
        return mReadWriteStream;
    }

    public ConnectThread createSocket(Handler handler, BluetoothDevice btDevice) {
        connectionThread = new ConnectThread(handler,btDevice);
        return connectionThread;
    }

    public ReadWriteStream createReadWriteStream(Handler handler,BluetoothSocket socket) {
        mReadWriteStream = new ReadWriteStream(handler, socket);
        return mReadWriteStream;
    }

    private ConnectThread connectionThread;
    private ReadWriteStream mReadWriteStream;
}
