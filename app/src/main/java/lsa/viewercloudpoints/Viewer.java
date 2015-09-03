package lsa.viewercloudpoints;

import android.app.Activity;
import android.graphics.Point;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;

import lsa.viewercloudpoints.navigation_drawer.MovementSpeedPreference;

public class Viewer extends Activity {
    private static final String TAG = "Viewer";
    private MyGLSurfaceView mGLView;
    private Global global;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        //getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
        //        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION);
        //getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);

        //Global.setStateProgram(Global.STATE_INIT_PROGRAM);
        Global.setStateProgram(Global.STATE_RENDER_POINTS);
        Global.setViewingStyle(Global.VIEW_USING_TRACKBALL);
        setContentView(R.layout.layout_viewer);
        mGLView = new MyGLSurfaceView(this);
        DrawerLayout rLayout = (DrawerLayout)findViewById(R.id.drawerLayout);
        rLayout.addView(mGLView,0);

        ((PreferenceFragment)getFragmentManager().findFragmentByTag(getString(R.string.nav_drawer_TAG))).
                findPreference(getString(R.string.key_mov_speed)).
                setOnPreferenceChangeListener(mGLView);
        mGLView.setSpeedMultiTouchInitial(
                ((MovementSpeedPreference)((PreferenceFragment)getFragmentManager().
                        findFragmentByTag(getString(R.string.nav_drawer_TAG))).
                        findPreference(getString(R.string.key_mov_speed))).getValue() );

        global = new Global(mGLView,this);

        Point p = new Point();
        getWindowManager().getDefaultDisplay().getSize(p);
        System.out.println("Size = "+p.x+" "+p.y);

        //rLayout.setStatusBarBackgroundColor(getResources().getColor(android.R.color.transparent));
        //rLayout.setFitsSystemWindows(true);
    }


    /*@Override
    public boolean onKeyDown(int keyCode, KeyEvent event){
        if( keyCode == KeyEvent.KEYCODE_VOLUME_UP ) {
            Global.setViewingStyle( Global.VIEW_USING_CAMERA );
            ((MyGLRenderer)(mGLView.getRenderer())).refreshMVP();
            mGLView.requestRender();
            return true;
        }else if( keyCode == KeyEvent.KEYCODE_VOLUME_DOWN ) {
            Global.setViewingStyle( Global.VIEW_USING_TRACKBALL );
            ((MyGLRenderer)(mGLView.getRenderer())).refreshMVP();
            mGLView.requestRender();
            return true;
        }
        return super.onKeyDown(keyCode,event);
    }*/

    //@TODO: configurar apropriadamente os métodos de saida do aplicativo nas classes corretas.

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(global!=null) global.destroy();
    }
}



