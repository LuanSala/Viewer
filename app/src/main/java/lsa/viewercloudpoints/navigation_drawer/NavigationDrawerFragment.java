package lsa.viewercloudpoints.navigation_drawer;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.WindowManager;

import lsa.viewercloudpoints.Global;
import lsa.viewercloudpoints.R;

/**
 * Created by Luan Sala on 09/08/2015.
 */
public class NavigationDrawerFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener {
    private static final String TAG = "NavigationDrawerFragment";

    private SwitchPreference centerCloud;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_nav_drawer);

        findPreference(getString(R.string.key_mode_vision)).setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.key_full_screen)).setOnPreferenceChangeListener(this);
        //findPreference(getString(R.string.key_mov_speed)).setOnPreferenceChangeListener(this);

        centerCloud = (SwitchPreference)findPreference(getString(R.string.key_center_trackball));
        centerCloud.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if( preference.getKey().equals(getString(R.string.key_mode_vision)) ) {
            synchronized (this) {
                if (newValue.equals(getString(R.string.value_mode_virtual_trackball))) {
                    centerCloud.setEnabled(true);
                    Global.setViewingStyle(Global.VIEW_USING_TRACKBALL);
                } else {
                    centerCloud.setEnabled(false);
                    Global.setViewingStyle(Global.VIEW_USING_CAMERA);
                }
                notifyAll();
            }
            ((DrawerLayout) getActivity().findViewById(R.id.drawerLayout)).closeDrawer(
                    getActivity().findViewById(R.id.linear_layout_drawerLayout));
        }
        if( preference.getKey().equals(getString(R.string.key_center_trackball)) ){
            synchronized (this){
                Global.useTrackballCentered((boolean)newValue);
                notifyAll();
            }
            ((DrawerLayout) getActivity().findViewById(R.id.drawerLayout)).closeDrawer(
                    getActivity().findViewById(R.id.linear_layout_drawerLayout));
        }
        if( preference.getKey().equals(getString(R.string.key_full_screen)) ) {
            if(newValue.equals(true)) {
                //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getActivity().getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                        View.SYSTEM_UI_FLAG_FULLSCREEN |
                        View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
            } else {
                //getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
            }
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            notifyAll();
        }
    }
}
