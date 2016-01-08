package lsa.viewercloudpoints;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.Toast;

import lsa.viewercloudpoints.filechooser.FileChooser;
import lsa.viewercloudpoints.navigation_drawer.MovementSpeedPreference;

public class Viewer extends Activity {
    private static final String TAG = "Viewer";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Global.setStateProgram(Global.STATE_INIT_PROGRAM);
        Global.setStateProgram(Global.STATE_RENDER_POINTS);
        Global.setViewingStyle(Global.VIEW_USING_TRACKBALL);
        setContentView(R.layout.layout_viewer);
        if(savedInstanceState!=null)
            mGLView = new MyGLSurfaceView(this,savedInstanceState);
        else {
            Global.centralizeTrackball();
            mGLView = new MyGLSurfaceView(this);
        }
        global = new Global(mGLView,this);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        drawerLayout.addView(mGLView, 0);  //Não remover esta linha...

        drawerLayout.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if(mGLView.getRenderer().getAxisTrackball()!=null && hasFocus) {
                            mGLView.getRenderer().getAxisTrackball().unPress();
                            mGLView.requestRender();
                        }
                    }
                }
        );

        PreferenceFragment preferenceFragment;
        preferenceFragment = (PreferenceFragment)getFragmentManager().findFragmentByTag(getString(R.string.tag_nav_drawer_fragment));
        switchFullscreen = (SwitchPreference)preferenceFragment.findPreference(getString(R.string.key_full_screen));

        preferenceFragment.findPreference(getString(R.string.key_mov_speed)).
                setOnPreferenceChangeListener(mGLView);
        preferenceFragment.findPreference(getString(R.string.key_show_axis_trackball)).
                setOnPreferenceChangeListener(mGLView);
        mGLView.setSpeedMultiTouchInitial(
                ((MovementSpeedPreference)preferenceFragment.
                findPreference(getString(R.string.key_mov_speed))).getValue()
        );

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

    }

    public void openFileChooser(View view){
        drawerLayout.closeDrawer(findViewById(R.id.linear_layout_drawerLayout));
        Intent render = new Intent(this,FileChooser.class);
        boolean[] bool = {true};
        render.putExtra(getString(R.string.isActivityForResult), bool);
        startActivityForResult(render, Global.PICK_FILE);
    }

    public void exitApplication(View view) {
        Intent exit = new Intent(this,MainScreen.class);
        exit.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        exit.putExtra("EXIT", true);
        startActivity(exit);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode==Global.PICK_FILE ){
            if( resultCode==RESULT_OK ){
                Global.file = data.getStringExtra("fileSelected");
                mGLView.getRenderer().updatePoints();
                mGLView.requestRender();
                drawerLayout.closeDrawer(findViewById(R.id.linear_layout_drawerLayout));
            }
        }
    }

    @Override
    public void onBackPressed() {
        if( drawerLayout.isDrawerOpen(findViewById(R.id.linear_layout_drawerLayout)) )
            drawerLayout.closeDrawer(findViewById(R.id.linear_layout_drawerLayout));
        else
            super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putFloatArray(getString(R.string.key_save_virtual_trackball),
                mGLView.getRenderer().getVirtualTrackball().getForSaveInstanceState());
        outState.putFloatArray(getString(R.string.key_save_camera),
                mGLView.getRenderer().getCamera().getForSaveInstanceState());
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if(hasFocus)
            if (switchFullscreen.isChecked())
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(global!=null) global.destroy();
    }

    private MyGLSurfaceView mGLView;
    private Global global;

    // Variavel da navigation drawer
    private DrawerLayout drawerLayout;

    //Variável utilizada para chegar quando o modo FullScreen está ativado ou não.
    private SwitchPreference switchFullscreen;

}



