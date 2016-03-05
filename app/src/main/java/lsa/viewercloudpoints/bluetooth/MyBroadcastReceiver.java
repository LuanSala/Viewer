package lsa.viewercloudpoints.bluetooth;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Luan Sala on 23/02/2016.
 */
public class MyBroadcastReceiver extends BroadcastReceiver {

    public MyBroadcastReceiver(){}

    @Override
    public void onReceive(Context context, Intent intent) {
        if ( mOnBroadcastReceiverListener!=null ) {
            mOnBroadcastReceiverListener.onBroadcastReceived(context, intent);
        }
    }

    public void setOnBroadcastReceiverListener(OnBroadcastReceiverListener listener) {
        mOnBroadcastReceiverListener = listener;
    }

    public interface OnBroadcastReceiverListener {

        void onBroadcastReceived(Context context, Intent intent);
    }


    private OnBroadcastReceiverListener mOnBroadcastReceiverListener;
}
