package lsa.viewercloudpoints.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * Created by Luan Sala on 29/02/2016.
 */
public class ReadWriteStream extends Thread {
    private static final String TAG = ReadWriteStream.class.getSimpleName();

    private static final int READ_BUFFER_LENGTH = 4096;
    private static final int MAX_LENGTH_FILE_NAME = 32;

    public ReadWriteStream(Handler handler, BluetoothSocket socket) {
        mSocket = socket;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        mHandler = handler;
        runnableSocketClose = new Runnable() {
            @Override
            public void run() {
                mOnSocketClosedListener.onSocketClosed();
            }
        };

        try {
            inputStream = mSocket.getInputStream();
            outputStream = mSocket.getOutputStream();
        } catch (IOException e) {}

        mInputStream = inputStream;
        mOutputStream = outputStream;
    }

    @Override
    public void run() {
        final int SIGNAL_IN_BLOCK = 0x7e7e7e7e;
        byte[]     readBuffer = new byte[READ_BUFFER_LENGTH];
        boolean    canReceiveFile = false;
        ByteBuffer byteBuffer = ByteBuffer.allocate(48);
        byteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        String     fileName;
        int        sizeFileToReceiver = 0;
        int        bytesRead = 0;
        while(true) {
            try {
                bytesRead = mInputStream.read(readBuffer, 0, 48);
            } catch(IOException e) {
                if (mOnSocketClosedListener!=null)
                    mHandler.post(runnableSocketClose);
                break;
            }
            if (bytesRead==48) {
                byteBuffer.position(0);
                byteBuffer.put(readBuffer, 0, 48);

                if( byteBuffer.getInt(0)==SIGNAL_IN_BLOCK ) {
                    byte[] tag = new byte[4];
                    byteBuffer.position(4);
                    byteBuffer.get(tag, 0, 4);
                    String tagType = new String(tag);

                    if( tagType.equals("SIZE") ) {
                        sizeFileToReceiver = byteBuffer.getInt(8);
                        byte[] bytesFileName = new byte[MAX_LENGTH_FILE_NAME];
                        byteBuffer.position(12);
                        byteBuffer.get(bytesFileName, 0, MAX_LENGTH_FILE_NAME);
                        fileName = new String(bytesFileName).split("\0")[0];
                        mSaverFile = new SaverFile(fileName);
                        mHandler.obtainMessage(BluetoothScreen.MESSAGE_INIT_PROGRESS_BAR,sizeFileToReceiver,0,fileName).sendToTarget();
                        canReceiveFile = true;
                    }
                }
            }
            int totalBytesReceived = 0;
            while(canReceiveFile) {
                try {
                    bytesRead = mInputStream.read(readBuffer, 0, READ_BUFFER_LENGTH);
                    totalBytesReceived += bytesRead;
                    mSaverFile.write(readBuffer, bytesRead);
                    mHandler.obtainMessage(BluetoothScreen.MESSAGE_UPDATE_PROGRESS_BAR, totalBytesReceived, 0).sendToTarget();
                    if (totalBytesReceived==sizeFileToReceiver) {
                        canReceiveFile = false;
                        mSaverFile.close();
                    }
                } catch (IOException e) {
                    if (mOnSocketClosedListener!=null)
                        mHandler.post(runnableSocketClose);
                    break;
                }
            }
        }
    }

    public void write(byte[] data) {
        try {
            mOutputStream.write(data);
        } catch (IOException e) {}
    }

    public void setOnSocketCloseListener(OnSocketCloseListener listener) {
        mOnSocketClosedListener = listener;
    }

    public interface OnSocketCloseListener {
        void onSocketClosed();
    }

    private OnSocketCloseListener mOnSocketClosedListener;

    private SaverFile mSaverFile;

    private Runnable runnableSocketClose;
    private final BluetoothSocket mSocket;
    private final InputStream mInputStream;
    private final OutputStream mOutputStream;
    private final Handler mHandler;
}
