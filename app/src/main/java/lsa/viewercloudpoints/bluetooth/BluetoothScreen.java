package lsa.viewercloudpoints.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import java.util.Iterator;
import java.util.NoSuchElementException;

import lsa.viewercloudpoints.R;

/**
 * Created by Luan Sala on 18/02/2016.
 */
public class BluetoothScreen extends AppCompatActivity
        implements MyBroadcastReceiver.OnBroadcastReceiverListener, ConnectDeviceDialog.OnItemClickListener,
        ConnectThread.OnConnectDeviceListener, ReadWriteStream.OnSocketCloseListener, Handler.Callback {
    private static final String TAG = "BluetoothScreen";

    private static final int REQUEST_ENABLE_BT = 1;

    public static final int MESSAGE_INIT_PROGRESS_BAR = 1;
    public static final int MESSAGE_UPDATE_PROGRESS_BAR = 2;

    private static final int DEFAULT_NUMBER_VIEWS_LAYOUT = 3;

    private static final String ACTIVITY_RESTORED = "Activity Restored";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.bluetooth_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_bluetoothScreen);
        setSupportActionBar(toolbar);
        activated = getString(R.string.activated);
        disabled = getString(R.string.disabled);
        linearLayoutBluetoothScreen = (LinearLayout)findViewById(R.id.linearLayout_bluetoothScreen);
        viewSwitcher = (ViewSwitcher) findViewById(R.id.viewSwitcher_bluetoothScreen);
        connectedDevice = (TextView) findViewById(R.id.textView_connected_device_bluetoothScreen);
        switchBarText = (TextView) findViewById(R.id.switchBar_text);
        switchBarSwitch = (Switch) findViewById(R.id.switchBar_switch);
        connectButton = (Button) findViewById(R.id.button_connect);
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        mHandler = new Handler(Looper.myLooper(),this);

        connectDeviceDialog = new ConnectDeviceDialog();
        connectDeviceDialog.setOnItemClickListener(this);
        mBroadcastReceiver = new MyBroadcastReceiver();
        mBroadcastReceiver.setOnBroadcastReceiverListener(this);

        if (savedInstanceState==null)
            startService(new Intent(this, BluetoothService.class));
        else
            savedInstanceState.getBoolean(ACTIVITY_RESTORED);
        bindService(new Intent(this, BluetoothService.class), mConnection, Context.BIND_AUTO_CREATE);

        if ( mBluetoothAdapter==null )
            connectedDevice.setText(getString(R.string.bluetooth_unavailable));
        else if (mBluetoothAdapter.isEnabled())
            connectedDevice.setText(getString(R.string.no_device_connected));

        switchBarSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    switchBarText.setText(activated);
                    if ( mBluetoothAdapter!=null ) {
                        if( mBluetoothAdapter.isEnabled() )
                            connectButton.setEnabled(true);
                        else
                            if(!activityRestored)
                                enableBluetooth();
                    }
                } else {
                    activityRestored = false;
                    switchBarText.setText(disabled);
                    if ( connectionThread!=null && connectionThread.isConnected() )
                        connectionThread.closeSocket();
                    if (mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled() )
                        connectedDevice.setText(getString(R.string.no_device_connected));
                }
                viewSwitcher.showNext();
            }
        });
    }


    private ServiceConnection mConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            BluetoothService.LocalBinder binder = (BluetoothService.LocalBinder) service;
            mBluetoothService = binder.getService();
            connectionThread = mBluetoothService.getConnectionThread();
            mReadWriteStream = mBluetoothService.getReadWriteStream();
            if (connectionThread!=null && connectionThread.isConnected()) {
                addProgressBarForTransfer();
                connectedDevice.setText(connectionThread.getDeviceConnected().getName());
                if (mReadWriteStream!=null) {
                    mReadWriteStream.updateHandler(mHandler);
                    mReadWriteStream.requestInitialInfo();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName arg0) {
        }
    };

    @Override
    protected void onResume() {
        saveInstance = false;
        registerReceiver(mBroadcastReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));
//        if (mBluetoothAdapter!=null && mBluetoothAdapter.isEnabled()) {
//            connectedDevice.setText(getString(R.string.no_device_connected));
//            //if (connectionThread != null && !connectionThread)
//                if (!connectionThread.isConnected())
//                    connectedDevice.setText(getString(R.string.no_device_connected));
//        }
        super.onResume();
    }

    private boolean saveInstance = false;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(ACTIVITY_RESTORED,true);
        saveInstance = true;
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onDestroy() {
        if (!saveInstance) {
            unbindService(mConnection);
            stopService(new Intent(this, BluetoothService.class));
        } else
            unbindService(mConnection);
        unregisterReceiver(mBroadcastReceiver);
        if(!saveInstance)
            if ( connectionThread!=null && connectionThread.isConnected() )
                connectionThread.closeSocket();
        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_bluetoothscreen, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch( item.getItemId() ){
            case R.id.action_settings_bluetooth_btScreen:
                startActivity(new Intent(Settings.ACTION_BLUETOOTH_SETTINGS));
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleMessage(Message msg) {
        if (linearLayoutBluetoothScreen.getChildCount()>DEFAULT_NUMBER_VIEWS_LAYOUT) {
            if (msg.what == MESSAGE_INIT_PROGRESS_BAR) {
                infoBluetoothTransfer.setText(getString(R.string.receiving_file) + ": " + msg.obj);
                bluetoothTransfer.setMax(msg.arg1);
                bluetoothTransfer.setProgress(0);
            } else if (msg.what == MESSAGE_UPDATE_PROGRESS_BAR) {
                bluetoothTransfer.setProgress(msg.arg1);
                if (bluetoothTransfer.getProgress() == bluetoothTransfer.getMax())
                    infoBluetoothTransfer.setText(getString(R.string.receiving_file) + ": ");
            }
        }
        return false;
    }

    @Override
    public void onBroadcastReceived(Context context, Intent intent) {
        if ( intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)==BluetoothAdapter.STATE_OFF ) {
            connectedDevice.setText(getString(R.string.bluetooth_off));
            if( connectionThread!=null && connectionThread.isConnected() )
                connectionThread.closeSocket();
            connectButton.setEnabled(false);
        } else if ( intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, -1)==BluetoothAdapter.STATE_ON ) {
            connectedDevice.setText(getString(R.string.no_device_connected));
            connectButton.setEnabled(true);
        }
    }

    @Override
    public void onItemClick(DialogInterface dialog, int which) {
        if ( connectionThread!=null)
            connectionThread.closeSocket();

        connectionThread = mBluetoothService.createSocket(mHandler,mBluetoothAdapter.getRemoteDevice(bondedDevices[which].getDeviceAddress()));
        connectionThread.setOnConnectDeviceListener(this);
        connectionThread.start();
//        connectionThread = new ConnectThread( mHandler ,
//                mBluetoothAdapter.getRemoteDevice(bondedDevices[which].getDeviceAddress()) );
//        connectionThread.setOnConnectDeviceListener(this);
//        connectionThread.start();
    }

    @Override
    public void onConnected(final BluetoothDevice btDevice, BluetoothSocket socket) {
        connectedDevice.setText(btDevice.getName());
        mReadWriteStream = mBluetoothService.createReadWriteStream(mHandler,socket);
        mReadWriteStream.write(mBluetoothAdapter.getName().getBytes());
        mReadWriteStream.setOnSocketCloseListener(this);
        mReadWriteStream.start();
//        mReadWriteStream = new ReadWriteStream(mHandler, socket);
//        mReadWriteStream.write(mBluetoothAdapter.getName().getBytes());
//        mReadWriteStream.setOnSocketCloseListener(this);
//        mReadWriteStream.start();
//        if (linearLayoutBluetoothScreen.getChildCount()==DEFAULT_NUMBER_VIEWS_LAYOUT) {
//            linearLayoutBluetoothScreen.addView(getLayoutInflater().inflate(R.layout.bluetooth_transfer_progressbar, null),
//                    DEFAULT_NUMBER_VIEWS_LAYOUT);
//            bluetoothTransfer = (ProgressBar) findViewById(R.id.progressBar_bluetooth_transfer);
//            infoBluetoothTransfer = (TextView) findViewById(R.id.textView_info_bluetooth_transfer);
//            infoBluetoothTransfer.setText(getString(R.string.receiving_file)+": ");
//        }
        addProgressBarForTransfer();
    }

    @Override
    public void onSocketClosed() {
        connectedDevice.setText(getString(R.string.no_device_connected));
        if (linearLayoutBluetoothScreen.getChildCount()>DEFAULT_NUMBER_VIEWS_LAYOUT)
            linearLayoutBluetoothScreen.removeViewAt(DEFAULT_NUMBER_VIEWS_LAYOUT);
    }

    public void addProgressBarForTransfer() {
        if (linearLayoutBluetoothScreen.getChildCount()==DEFAULT_NUMBER_VIEWS_LAYOUT) {
            linearLayoutBluetoothScreen.addView(getLayoutInflater().inflate(R.layout.bluetooth_transfer_progressbar, null),
                    DEFAULT_NUMBER_VIEWS_LAYOUT);
            bluetoothTransfer = (ProgressBar) findViewById(R.id.progressBar_bluetooth_transfer);
            infoBluetoothTransfer = (TextView) findViewById(R.id.textView_info_bluetooth_transfer);
            infoBluetoothTransfer.setText(getString(R.string.receiving_file)+": ");
        }
    }

    private void getBondedDevices() {
        Iterator<BluetoothDevice> it = mBluetoothAdapter.getBondedDevices().iterator();
        bondedDevices = new BondedDevice[mBluetoothAdapter.getBondedDevices().size()];
        for(int i=0; ; i++) {
            try {
                BluetoothDevice bt = it.next();
                bondedDevices[i] = new BondedDevice(bt.getName(), bt.getAddress());
            } catch (NoSuchElementException e) {
                break;
            }
        }
    }

    public void switchBarOnClick(View view){
        switchBarSwitch.toggle();
    }

    public void chooseDeviceForConnect(View view){
        getBondedDevices();
        CharSequence[] list = new CharSequence[bondedDevices.length];
        for(int i=0; i< list.length; i++)
            list[i] = bondedDevices[i].getDeviceName();
        connectDeviceDialog.updateListOfDevices(list);
        connectDeviceDialog.show(getFragmentManager(), "dialogChooseBondedDevice");
    }

    private void enableBluetooth(){
        Intent enableBTIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        startActivityForResult(enableBTIntent, REQUEST_ENABLE_BT);
    }


    private String activated;
    private String disabled;
    private boolean activityRestored;

    private BluetoothAdapter mBluetoothAdapter;
    private ViewSwitcher viewSwitcher;
    private ProgressBar bluetoothTransfer;
    private TextView switchBarText;
    private TextView connectedDevice;
    private TextView infoBluetoothTransfer;
    private Switch switchBarSwitch;
    private Button connectButton;
    private LinearLayout linearLayoutBluetoothScreen;

    private Handler mHandler;
    private ConnectDeviceDialog connectDeviceDialog;
    private MyBroadcastReceiver mBroadcastReceiver;
    private BondedDevice[] bondedDevices;
    private BluetoothService mBluetoothService;
    private ConnectThread connectionThread;
    private ReadWriteStream mReadWriteStream;
}
