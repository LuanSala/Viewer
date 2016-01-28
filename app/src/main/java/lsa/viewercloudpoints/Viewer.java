package lsa.viewercloudpoints;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Toast;

import java.io.File;

import lsa.viewercloudpoints.filechooser.FileChooser;
import lsa.viewercloudpoints.navigation_drawer.MovementSpeedPreference;

public class Viewer extends AppCompatActivity {
    private static final String TAG = "Viewer";

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        //Global.setStateProgram(Global.STATE_INIT_PROGRAM);
        Global.setStateProgram(Global.STATE_RENDER_POINTS);
        Global.setViewingStyle(Global.VIEW_USING_TRACKBALL);
        setContentView(R.layout.layout_viewer);
        drawerLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        layoutNavigationDrawer = findViewById(R.id.layout_nav_drawer);
        setSupportActionBar((Toolbar) findViewById(R.id.toolbar_viewer));
        actionBar = getSupportActionBar();
        actionBar.setTitle(new File(Global.file).getName());

        if(savedInstanceState!=null)
            mGLView = new MyGLSurfaceView(this,savedInstanceState);
        else {
            Global.centralizeTrackball();
            mGLView = new MyGLSurfaceView(this);
        }
        global = new Global(mGLView,this);
        ((RelativeLayout)findViewById(R.id.layout_viewer)).addView(mGLView, 0, buildLayoutParamsGLView()); //Não remover esta linha...

        drawerToogle = new ActionBarDrawerToggle(
                this,drawerLayout,(Toolbar) findViewById(R.id.toolbar_viewer),
                R.string.drawer_viewer_opened,R.string.drawer_viewer_closed);
        actionBar.setHomeButtonEnabled(true);
        actionBar.setDisplayHomeAsUpEnabled(true);
        drawerLayout.setDrawerListener(drawerToogle);

        setupPreferenceFragment();

        drawerLayout.setOnFocusChangeListener(
                new View.OnFocusChangeListener() {
                    @Override
                    public void onFocusChange(View v, boolean hasFocus) {
                        if (mGLView.getRenderer().getAxisTrackball() != null && hasFocus) {
                            mGLView.getRenderer().getAxisTrackball().unPress();
                            mGLView.requestRender();
                        }
                    }
                }
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

    private void setupPreferenceFragment(){
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
    }

    private RelativeLayout.LayoutParams buildLayoutParamsGLView(){
        RelativeLayout.LayoutParams layoutParams;
        layoutParams = new RelativeLayout.LayoutParams(
                RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.toolbar_viewer);
        return layoutParams;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        drawerToogle.syncState();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        drawerToogle.onConfigurationChanged(newConfig);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return (drawerToogle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if( requestCode==Global.PICK_FILE ){
            if( resultCode==RESULT_OK ){
                Global.file = data.getStringExtra("fileSelected");
                mGLView.getRenderer().updatePoints();
                mGLView.requestRender();
                actionBar.setTitle(new File(Global.file).getName());
                drawerLayout.closeDrawer(layoutNavigationDrawer);
            }
        }
    }

    @Override
    public void onBackPressed() {
        if( drawerLayout.isDrawerOpen(layoutNavigationDrawer) )
            drawerLayout.closeDrawer(layoutNavigationDrawer);
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
            if (switchFullscreen.isChecked()) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                actionBar.hide();
            }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(global!=null) global.destroy();
    }

    public void openFileChooser(View view){
        drawerLayout.closeDrawer(layoutNavigationDrawer );
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

    private MyGLSurfaceView mGLView;
    private Global global;

    // Variavel da navigation drawer
    private DrawerLayout drawerLayout;
    private View layoutNavigationDrawer;

    private ActionBar actionBar;
    private ActionBarDrawerToggle drawerToogle;

    //Variável utilizada para chegar quando o modo FullScreen está ativado ou não.
    private SwitchPreference switchFullscreen;

}



