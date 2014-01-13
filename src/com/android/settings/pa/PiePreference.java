package com.android.settings.pa;

import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.preference.Preference;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class PiePreference extends SettingsPreferenceFragment {

    //private static final String SLIM_PIE = "slim_pie";
    private static final String PARANOID_PIE = "paranoid_pie";

    Preference mSlimPie;
    Preference mParanoidPie;

    public static class HelpFragment extends DialogFragment {
        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle state) {
            View v = inflater.inflate(R.layout.pie_help, null);
            TextView tv = (TextView) v.findViewById(R.id.help);
            tv.setText(R.string.pie_help_text);
            return v;
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.pie_header);

        //mSlimPie = (Preference) findPreference(SLIM_PIE);
        mParanoidPie = (Preference) findPreference(PARANOID_PIE);

        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.pie_menu, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int choice = item.getItemId();
        switch (choice) {
            case R.id.help:
                DialogFragment df = new HelpFragment();
                df.setStyle(DialogFragment.STYLE_NO_TITLE, 0);
                df.show(getFragmentManager(), "help");
                return true;
            default:
                return false;
        }
    }
}
