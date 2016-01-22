package lsa.viewercloudpoints.navigation_drawer;

import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.View;
import android.widget.RelativeLayout;

import lsa.viewercloudpoints.Global;
import lsa.viewercloudpoints.R;

/**
 * Created by Luan Sala on 09/08/2015.
 */
public class NavigationDrawerFragment extends PreferenceFragment
        implements Preference.OnPreferenceChangeListener, Preference.OnPreferenceClickListener {
    private static final String TAG = "NavigationDrawerFrag";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preference_nav_drawer);

        modeVision = (ListPreference)findPreference(getString(R.string.key_mode_vision));
        modeVision.setOnPreferenceChangeListener(this);
        findPreference(getString(R.string.key_full_screen)).setOnPreferenceChangeListener(this);

        centerCloud = findPreference(getString(R.string.key_centralize_trackball));
        centerCloud.setOnPreferenceClickListener(this);

        showAxisTrackball = (SwitchPreference)findPreference(getString(R.string.key_show_axis_trackball));
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if( preference.getKey().equals(getString(R.string.key_mode_vision)) ) {
            synchronized (this) {
                if (newValue.equals(getString(R.string.value_mode_virtual_trackball))) {
                    centerCloud.setEnabled(true);
                    showAxisTrackball.setEnabled(true);
                    Global.setViewingStyle(Global.VIEW_USING_TRACKBALL);
                } else {
                    centerCloud.setEnabled(false);
                    showAxisTrackball.setEnabled(false);
                    Global.setViewingStyle(Global.VIEW_USING_CAMERA);
                }
                notifyAll();
            }
            ((DrawerLayout) getActivity().findViewById(R.id.drawerLayout)).closeDrawer(layoutParent);
        } else if( preference.getKey().equals(getString(R.string.key_full_screen)) ) {
            if(newValue.equals(true)) {
                //getActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getActivity().getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_HIDE_NAVIGATION |
                                View.SYSTEM_UI_FLAG_FULLSCREEN |
                                View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY );
                layoutParent.setPadding(0,0,0,0);
            } else {
                //getActivity().getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                getActivity().getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                layoutParent.setPadding(0,72,0,0);
            }
        }
        return true;
    }

    @Override
    public boolean onPreferenceClick(Preference preference) {
        if (preference.getKey().equals(getString(R.string.key_centralize_trackball))) {
            synchronized (this) {
                Global.centralizeTrackball();
                notifyAll();
            }
            ((DrawerLayout) getActivity().findViewById(R.id.drawerLayout)).closeDrawer(layoutParent);
        }
        return true;
    }

    @Override
    public void onResume() {
        super.onResume();
        synchronized (this) {
            if (modeVision.getValue().equals(getString(R.string.value_mode_virtual_trackball))) {
                centerCloud.setEnabled(true);
                showAxisTrackball.setEnabled(true);
                Global.setViewingStyle(Global.VIEW_USING_TRACKBALL);
            } else {
                centerCloud.setEnabled(false);
                showAxisTrackball.setEnabled(false);
                Global.setViewingStyle(Global.VIEW_USING_CAMERA);
            }
            notifyAll();
        }
        layoutParent = getActivity().findViewById(R.id.layout_nav_drawer);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        synchronized (this) {
            notifyAll();
        }
    }

    private Preference centerCloud;
    private ListPreference modeVision;
    private SwitchPreference showAxisTrackball;

    private View layoutParent;

}
