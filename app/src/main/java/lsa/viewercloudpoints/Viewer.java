package lsa.viewercloudpoints;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import lsa.viewercloudpoints.filechooser.FileChooser;
import lsa.viewercloudpoints.navigation_drawer.MovementSpeedPreference;

public class Viewer extends Activity
    implements DialogInterface.OnClickListener {
    private static final String TAG = "Viewer";

    private MyGLSurfaceView mGLView;
    private Global global;

    // Variavel da navigation drawer
    DrawerLayout drawerLayout;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Global.setStateProgram(Global.STATE_INIT_PROGRAM);
        Global.setStateProgram(Global.STATE_RENDER_POINTS);
        Global.setViewingStyle(Global.VIEW_USING_TRACKBALL);
        setContentView(R.layout.layout_viewer);
        mGLView = new MyGLSurfaceView(this);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        drawerLayout.addView(mGLView,0);

        ((PreferenceFragment)getFragmentManager().findFragmentByTag(getString(R.string.nav_drawer_TAG))).
                findPreference(getString(R.string.key_mov_speed)).
                setOnPreferenceChangeListener(mGLView);
        mGLView.setSpeedMultiTouchInitial(
                ((MovementSpeedPreference)((PreferenceFragment)getFragmentManager().
                        findFragmentByTag(getString(R.string.nav_drawer_TAG))).
                        findPreference(getString(R.string.key_mov_speed))).getValue() );

        global = new Global(mGLView,this);

        //Point p = new Point();
        //getWindowManager().getDefaultDisplay().getSize(p);
        //System.out.println("Size = "+p.x+" "+p.y);

        findViewById(R.id.Button_exit_application).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Sair do aplicativo", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        findViewById(R.id.Button_open_file_nav_drawer).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getApplicationContext(), "Abrir um novo arquivo", Toast.LENGTH_SHORT).show();
                return true;
            }
        });

        PreferenceFragment preferenceFragment;
        preferenceFragment = (PreferenceFragment)getFragmentManager().findFragmentByTag(getString(R.string.nav_drawer_TAG));
        switchFullScreen = (SwitchPreference)preferenceFragment.findPreference(getString(R.string.key_full_screen));
    }

    public void openFileChooser(View view){
        Intent render = new Intent(this,FileChooser.class);
        drawerLayout.closeDrawer(findViewById(R.id.linear_layout_drawerLayout));
        this.startActivityForResult(render,Global.PICK_FILE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode==Global.PICK_FILE ){
            if( resultCode==RESULT_OK ){
                Global.file = data.getStringExtra("fileSelected");
                ((MyGLRenderer)mGLView.getRenderer()).updatePoints();
                mGLView.requestRender();
                drawerLayout.closeDrawer(findViewById(R.id.linear_layout_drawerLayout));
            }
        }
        if( switchFullScreen.isChecked() )
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                    View.SYSTEM_UI_FLAG_FULLSCREEN |
                    View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
    }

    @Override
    public void onBackPressed() {
        if( drawerLayout.isDrawerOpen(findViewById(R.id.linear_layout_drawerLayout)) )
            drawerLayout.closeDrawer(findViewById(R.id.linear_layout_drawerLayout));
        else {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage(R.string.message_dialog_backButton_exit)
                    .setTitle(R.string.title_dialog_backButton_exit);
            builder.setPositiveButton(R.string.positiveButton_dialog_backButton_exit, this);
            builder.setNegativeButton(R.string.negativeButton_dialog_backButton_exit, this);
            builder.show();
        }
    }

    public void exitApplication(View view) {
        super.onBackPressed();
    }

    public void onClick(DialogInterface dialog, int which) {
        if(which==DialogInterface.BUTTON_POSITIVE)
            super.onBackPressed();
    }

    //@TODO: configurar apropriadamente os métodos de saida do aplicativo nas classes corretas.

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(global!=null) global.destroy();
    }
    //Variável utilizada para chegar quando o modo FullScreen está ativado ou não.
    SwitchPreference switchFullScreen;
}



