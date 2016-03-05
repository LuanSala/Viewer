package lsa.viewercloudpoints;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import lsa.viewercloudpoints.bluetooth.BluetoothScreen;
import lsa.viewercloudpoints.filechooser.FileChooser;


/**
 * Created by Luan Sala on 04/03/2015.
 */
public class MainScreen extends AppCompatActivity {
    private static final String TAG = "MainScreen";

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_mainScreen);
        setSupportActionBar(toolbar);
    }

    public void openFileChooser(View view){
        Intent intent = new Intent(this,FileChooser.class);
        startActivity(intent);
    }

    public void openBluetoothScreen(View view){
        Intent intent = new Intent(this, BluetoothScreen.class);
        startActivity(intent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.app_bar_mainscreen,menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // implementar aqui o que fazer quando se seleciona uma opção presente no app bar.
        /*
        switch (item.getItemId()) {
        case R.id.action_settings:
            // User chose the "Settings" item, show the app settings UI...
            return true;
         */
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( getIntent().getBooleanExtra("EXIT",false) )
            finish();
    }
}
