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
        startActivity(render);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( getIntent().getBooleanExtra("EXIT",false) )
            finish();
    }
}
