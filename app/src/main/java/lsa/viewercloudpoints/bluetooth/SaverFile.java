package lsa.viewercloudpoints.bluetooth;

import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Created by Luan Sala on 02/03/2016.
 */
public class SaverFile {
    private static final String TAG = "SaverFile";

    private static final String directory = "Point Cloud";

    public SaverFile(String fileName) {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
            File baseDirectory = new File(Environment.getExternalStorageDirectory().getAbsolutePath() +
                    File.separator + directory);
            if (!baseDirectory.exists()) {
                if (!baseDirectory.mkdir())
                    baseDirectory = new File(File.separator, "sdcard"+File.separator+directory);
            }
            try {
                receivedFile = new FileOutputStream(baseDirectory.getAbsolutePath() + File.separator + fileName);
            } catch(FileNotFoundException e){
                e.printStackTrace();
            }

        }
    }

    public void write(byte[] buffer, int byteCount) {
        try {
            receivedFile.write(buffer, 0, byteCount);
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    public void close(){
        try {
            receivedFile.close();
        } catch(IOException e){
            e.printStackTrace();
        }
    }

    FileOutputStream receivedFile;
}
