
package com.android.settings.beanstalk;

import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.R;

import android.content.ContentResolver;
import android.content.Context;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;

public class RamSettings extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {
    private static final String TAG = "RamSettings";

    private static final String RECENT_MENU_CLEAR_ALL = "recent_menu_clear_all";
    private static final String RECENT_MENU_CLEAR_ALL_LOCATION = "recent_menu_clear_all_location";
    private static final String SHOW_RECENTS_MEMORY_INDICATOR = "show_recents_memory_indicator";
    private static final String RECENTS_MEMORY_INDICATOR_LOCATION =
            "recents_memory_indicator_location";
    private static final String SHOW_RAMBAR_GB =
            "show_rambar_gb";

    private CheckBoxPreference mRecentClearAll;
    private CheckBoxPreference mRambarGB;
    private ListPreference mRecentClearAllPosition;
    private CheckBoxPreference mShowRecentsMemoryIndicator;
    private ListPreference mRecentsMemoryIndicatorPosition;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.ram_settings);

        PreferenceScreen prefSet = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

        mRecentClearAll = (CheckBoxPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL);
        mRecentClearAll.setChecked(Settings.System.getInt(resolver,
                Settings.System.SHOW_CLEAR_RECENTS_BUTTON, 0) == 1);
        mRecentClearAll.setOnPreferenceChangeListener(this);
        mRecentClearAllPosition = (ListPreference) prefSet.findPreference(RECENT_MENU_CLEAR_ALL_LOCATION);
        String recentClearAllPosition = Settings.System.getString(resolver, Settings.System.CLEAR_RECENTS_BUTTON_LOCATION);
        if (recentClearAllPosition != null) {
            mRecentClearAllPosition.setValue(recentClearAllPosition);
        }
        mRecentClearAllPosition.setOnPreferenceChangeListener(this);

        mRambarGB = (CheckBoxPreference) prefSet.findPreference(SHOW_RAMBAR_GB);
        mRambarGB.setChecked(Settings.System.getInt(resolver,
                Settings.System.SHOW_GB_RAMBAR, 0) == 1);
        mRambarGB.setOnPreferenceChangeListener(this);

        mShowRecentsMemoryIndicator = (CheckBoxPreference)
                prefSet.findPreference(SHOW_RECENTS_MEMORY_INDICATOR);
        mShowRecentsMemoryIndicator.setChecked(Settings.System.getInt(resolver,
                Settings.System.SHOW_RECENTS_MEMORY_INDICATOR, 0) == 1);
        mShowRecentsMemoryIndicator.setOnPreferenceChangeListener(this);
        mRecentsMemoryIndicatorPosition = (ListPreference) prefSet
                .findPreference(RECENTS_MEMORY_INDICATOR_LOCATION);
        String recentsMemoryIndicatorPosition = Settings.System.getString(
                resolver, Settings.System.RECENTS_MEMORY_INDICATOR_LOCATION);
        if (recentsMemoryIndicatorPosition != null) {
            mRecentsMemoryIndicatorPosition.setValue(recentsMemoryIndicatorPosition);
        }
        mRecentsMemoryIndicatorPosition.setOnPreferenceChangeListener(this);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return true;
    }

    public boolean onPreferenceChange(Preference preference, Object objValue) {
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mRecentClearAll) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.SHOW_CLEAR_RECENTS_BUTTON, value ? 1 : 0);
	} else if (preference == mRambarGB) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(resolver, Settings.System.SHOW_GB_RAMBAR, value ? 1 : 0);
        } else if (preference == mRecentClearAllPosition) {
            String value = (String) objValue;
            Settings.System.putString(resolver, Settings.System.CLEAR_RECENTS_BUTTON_LOCATION, value);
        } else if (preference == mShowRecentsMemoryIndicator) {
            boolean value = (Boolean) objValue;
            Settings.System.putInt(
                    resolver, Settings.System.SHOW_RECENTS_MEMORY_INDICATOR, value ? 1 : 0);
        } else if (preference == mRecentsMemoryIndicatorPosition) {
            String value = (String) objValue;
            Settings.System.putString(
                    resolver, Settings.System.RECENTS_MEMORY_INDICATOR_LOCATION, value);
        } else {
            return false;
        }

        return true;
    }
}
