package lsa.viewercloudpoints.bluetooth;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;

import lsa.viewercloudpoints.R;

/**
 * Created by Luan Sala on 26/02/2016.
 */
public class ConnectDeviceDialog extends DialogFragment
        implements DialogInterface.OnClickListener {
    private static final String TAG = "ConnectDeviceDialog";

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(R.string.title_dialog_select_device);
        builder.setItems(listBondedDevices, this);

        return builder.create();
    }

    @Override
    public void onClick(DialogInterface dialog, int which) {
        if ( mOnItemClickListener!=null )
            mOnItemClickListener.onItemClick(dialog, which);
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        mOnItemClickListener = listener;
    }

    public interface OnItemClickListener {
        void onItemClick(DialogInterface dialog, int which);
    }

    public void updateListOfDevices(CharSequence[] list) {
        listBondedDevices = list.clone();
    }

    private OnItemClickListener mOnItemClickListener = null;

    private CharSequence[] listBondedDevices;

}
