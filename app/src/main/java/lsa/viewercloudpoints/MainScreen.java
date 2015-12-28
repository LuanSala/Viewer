package lsa.viewercloudpoints;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import lsa.viewercloudpoints.filechooser.FileChooser;


/**
 * Created by Luan Sala on 04/03/2015.
 */
public class MainScreen extends Activity {
    private static final String TAG = "MainScreen";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
    }

    public void openFileChooser(View view){
        Intent render = new Intent(this,FileChooser.class);
        //this.startActivityForResult(render,Global.PICK_FILE);
        startActivity(render);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode==Global.PICK_FILE ){
            if( resultCode==RESULT_OK ){
                Global.file = data.getStringExtra("fileSelected");
                startActivity(new Intent(this,Viewer.class));
                finish();
            }
        }
    }
}
