/*
 * Copyright (C) 2013 SlimRoms Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settings.beanstalk;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.util.Random;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.content.Context;
import android.content.res.Resources;
import android.content.DialogInterface;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.graphics.Rect;
import android.net.Uri;
import android.content.ContentResolver;
import android.database.ContentObserver;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Handler;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceScreen;
import android.provider.MediaStore;
import android.provider.Settings;
import android.text.Spannable;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.view.Display;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;
import com.android.settings.util.Helpers;

import net.margaritov.preference.colorpicker.ColorPickerPreference;

import com.android.internal.util.beanstalk.DeviceUtils;

public class StatusBar extends SettingsPreferenceFragment implements OnPreferenceChangeListener {

    private static final String TAG = "StatusBarSettings";

    private static final String KEY_STATUS_BAR_CLOCK = "clock_style_pref";
    private static final String STATUS_BAR_BRIGHTNESS_CONTROL = "status_bar_brightness_control";
    private static final String STATUS_BAR_TRAFFIC_STYLE = "status_bar_traffic_style";

    private ColorPickerPreference mColorPicker;
    private PreferenceScreen mClockStyle;
    private CheckBoxPreference mStatusBarBrightnessControl;
    private ListPreference mStatusBarTraffic;

    ListPreference mDbmStyletyle;
    CheckBoxPreference mHideSignal;
    CheckBoxPreference mBatteryIndicator, mBatteryIndicatorPlugged;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.status_bar_settings);

	PreferenceScreen prefSet = getPreferenceScreen();

        ContentResolver resolver = getActivity().getContentResolver();

	// Start observing for changes on auto brightness
        StatusBarBrightnessChangedObserver statusBarBrightnessChangedObserver =
            new StatusBarBrightnessChangedObserver(new Handler());
        statusBarBrightnessChangedObserver.startObserving();

        mClockStyle = (PreferenceScreen) prefSet.findPreference(KEY_STATUS_BAR_CLOCK);
        if (mClockStyle != null) {
            updateClockStyleDescription();
        }

	mStatusBarBrightnessControl =
            (CheckBoxPreference) prefSet.findPreference(STATUS_BAR_BRIGHTNESS_CONTROL);
        mStatusBarBrightnessControl.setChecked((Settings.System.getInt(getContentResolver(),
                            Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL, 0) == 1));
        mStatusBarBrightnessControl.setOnPreferenceChangeListener(this);

        // Statusbar Traffic
        mStatusBarTraffic = (ListPreference) findPreference(STATUS_BAR_TRAFFIC_STYLE);
        int trafficStyle = Settings.System.getInt(resolver, Settings.System.STATUS_BAR_TRAFFIC_STYLE, 0);
        mStatusBarTraffic.setValue(String.valueOf(trafficStyle));
        mStatusBarTraffic.setSummary(mStatusBarTraffic.getEntry());
        mStatusBarTraffic.setOnPreferenceChangeListener(this);


	    mDbmStyletyle = (ListPreference) findPreference("signal_style");
        mDbmStyletyle.setOnPreferenceChangeListener(this);
        mDbmStyletyle.setValue(Integer.toString(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_SIGNAL_TEXT,
                0)));

        mColorPicker = (ColorPickerPreference) findPreference("signal_color");
        mColorPicker.setOnPreferenceChangeListener(this);

        mHideSignal = (CheckBoxPreference) findPreference("hide_signal");
        mHideSignal.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.STATUSBAR_HIDE_SIGNAL_BARS,
                0) != 0);

        mBatteryIndicator = (CheckBoxPreference) findPreference("battery_percentage_indicator");
        mBatteryIndicator.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.BATTERY_PERCENTAGE_INDICATOR,
                0) != 0);

        mBatteryIndicatorPlugged = (CheckBoxPreference) findPreference("battery_percentage_indicator_plugged");
        mBatteryIndicatorPlugged.setChecked(Settings.System.getInt(getActivity()
                .getContentResolver(), Settings.System.BATTERY_PERCENTAGE_INDICATOR_PLUGGED,
                0) != 0);

    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        ContentResolver resolver = getActivity().getContentResolver();
	if (preference == mStatusBarBrightnessControl) {
            Settings.System.putInt(getContentResolver(),
                    Settings.System.STATUS_BAR_BRIGHTNESS_CONTROL,
                    (Boolean) newValue ? 1 : 0);
            return true;
        } else if (preference == mStatusBarTraffic) {
            int trafficStyle = Integer.valueOf((String) newValue);
            int index = mStatusBarTraffic.findIndexOfValue((String) newValue);
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_TRAFFIC_STYLE, trafficStyle);
            mStatusBarTraffic.setSummary(mStatusBarTraffic.getEntries()[index]);
            return true;
	    } else if (preference == mDbmStyletyle) {
            int val = Integer.parseInt((String) newValue);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_SIGNAL_TEXT, val);
            return true;
        } else if (preference == mColorPicker) {
            String hex = ColorPickerPreference.convertToARGB(Integer.valueOf(String
                    .valueOf(newValue)));
            preference.setSummary(hex);
            int intHex = ColorPickerPreference.convertToColorInt(hex);
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_SIGNAL_TEXT_COLOR, intHex);
            return true;
        }
        return false;
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        boolean value;

        if (preference == mHideSignal) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.STATUSBAR_HIDE_SIGNAL_BARS,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }
        else if (preference == mBatteryIndicator) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BATTERY_PERCENTAGE_INDICATOR,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }
        else if (preference == mBatteryIndicatorPlugged) {
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.BATTERY_PERCENTAGE_INDICATOR_PLUGGED,
                    ((CheckBoxPreference) preference).isChecked() ? 1 : 0);
            return true;
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    @Override
    public void onResume() {
        super.onResume();
    	updateClockStyleDescription();
	updateStatusBarBrightnessControl();
    }

    private void updateStatusBarBrightnessControl() {
        try {
            if (mStatusBarBrightnessControl != null) {
                int mode = Settings.System.getIntForUser(getContentResolver(),
                    Settings.System.SCREEN_BRIGHTNESS_MODE,
                    Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);

                if (mode == Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC) {
                    mStatusBarBrightnessControl.setEnabled(false);
                    mStatusBarBrightnessControl.setSummary(R.string.status_bar_toggle_info);
                } else {
                    mStatusBarBrightnessControl.setEnabled(true);
                    mStatusBarBrightnessControl.setSummary(
                        R.string.status_bar_toggle_brightness_summary);
                }
            }
        } catch (SettingNotFoundException e) {
        }
    }

    private void updateClockStyleDescription() {
        if (Settings.System.getInt(getContentResolver(),
               Settings.System.STATUS_BAR_CLOCK, 1) == 1) {
            mClockStyle.setSummary(getString(R.string.enabled));
        } else {
            mClockStyle.setSummary(getString(R.string.disabled));
        }
    }

    private class StatusBarBrightnessChangedObserver extends ContentObserver {
        public StatusBarBrightnessChangedObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange) {
            updateStatusBarBrightnessControl();
        }

        public void startObserving() {
            getContentResolver().registerContentObserver(
                    Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE),
                    false, this);
        }
    }
}
