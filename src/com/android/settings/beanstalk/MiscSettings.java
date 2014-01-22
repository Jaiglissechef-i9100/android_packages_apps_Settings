/*
 * Copyright (C) 2013 SlimRoms
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

import android.app.Activity;
import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.preference.CheckBoxPreference;
import android.os.Bundle;
import android.content.ContentResolver;
import android.provider.Settings;
import android.content.res.Resources;
import android.preference.ListPreference;
import android.preference.Preference;
import android.content.DialogInterface;
import android.view.WindowManagerGlobal;
import android.content.Intent;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceActivity;
import android.preference.ListPreference;
import android.text.Spannable;
import android.provider.Settings.SettingNotFoundException;
import android.util.Log;
import android.text.Editable;
import android.util.Slog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

import java.util.List;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;

public class MiscSettings extends SettingsPreferenceFragment
        implements OnPreferenceChangeListener {

    private static final String TAG = "MiscSettings";
    private static final String PREF_MEDIA_SCANNER_ON_BOOT = "media_scanner_on_boot";
    private static final String PREF_CUSTOM_CARRIER_LABEL = "custom_carrier_label";
    private static final String STATUS_BAR_CARRIER = "status_bar_carrier";
    private static final String PREF_VIBRATE_NOTIF_EXPAND = "vibrate_notif_expand";

    private static final String KEY_LCD_DENSITY = "lcd_density";
    private static final int DIALOG_CUSTOM_DENSITY = 101;
    private static final String DENSITY_PROP = "persist.sys.lcd_density";

    private static ListPreference mLcdDensity;
    private static Activity mActivity;

    private ListPreference mMsob;
    private Preference mCustomLabel;
    private String mCustomLabelText = null;
    CheckBoxPreference mVibrateOnExpand;
    private CheckBoxPreference mStatusBarCarrier;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
	mActivity = getActivity();

        updateSettings();
    }

    private void updateSettings() {
        setPreferenceScreen(null);
        addPreferencesFromResource(R.xml.misc_settings);
	PreferenceScreen prefScreen = getPreferenceScreen();
        ContentResolver resolver = getActivity().getContentResolver();

	mStatusBarCarrier = (CheckBoxPreference) findPreference(STATUS_BAR_CARRIER);
	mStatusBarCarrier.setChecked((Settings.System.getInt(resolver, Settings.System.STATUS_BAR_CARRIER, 0) == 1));
	mStatusBarCarrier.setOnPreferenceChangeListener(this);

	mCustomLabel = findPreference(PREF_CUSTOM_CARRIER_LABEL);
        mMsob = (ListPreference) findPreference(PREF_MEDIA_SCANNER_ON_BOOT);
        mMsob.setValue(String.valueOf(Settings.System.getInt(getActivity().getContentResolver(),
                Settings.System.MEDIA_SCANNER_ON_BOOT, 0)));
        mMsob.setSummary(mMsob.getEntry());
        mMsob.setOnPreferenceChangeListener(this);

	updateCustomLabelTextSummary();

	mVibrateOnExpand = (CheckBoxPreference) findPreference(PREF_VIBRATE_NOTIF_EXPAND);
        mVibrateOnExpand.setChecked(Settings.System.getBoolean(mContext.getContentResolver(),
                Settings.System.VIBRATE_NOTIF_EXPAND, true));

	mLcdDensity = (ListPreference) findPreference(KEY_LCD_DENSITY);
        String current = SystemProperties.get(DENSITY_PROP,
                SystemProperties.get("ro.sf.lcd_density"));
        final ArrayList<String> array = new ArrayList<String>(
                Arrays.asList(getResources().getStringArray(R.array.lcd_density_entries)));
        if (array.contains(current)) {
            mLcdDensity.setValue(current);
        } else {
            mLcdDensity.setValue("custom");
        }
        mLcdDensity.setSummary(getResources().getString(R.string.current_lcd_density) + current);
        mLcdDensity.setOnPreferenceChangeListener(this);

        mReverseDefaultAppPicker = (CheckBoxPreference) findPreference(KEY_REVERSE_DEFAULT_APP_PICKER);
        mReverseDefaultAppPicker.setChecked(Settings.System.getInt(getContentResolver(),
                    Settings.System.REVERSE_DEFAULT_APP_PICKER, 0) != 0);
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
//        String value = (String) newValue;
        ContentResolver resolver = getActivity().getContentResolver();
        if (preference == mMsob) {
            String value = (String) newValue;
            Settings.System.putInt(getActivity().getContentResolver(),
                    Settings.System.MEDIA_SCANNER_ON_BOOT,
                    Integer.valueOf(value));

            mMsob.setValue(String.valueOf(value));
            mMsob.setSummary(mMsob.getEntry());
            return true;
        } else if (preference == mStatusBarCarrier) {
            boolean value = (Boolean) newValue;
            Settings.System.putInt(resolver, Settings.System.STATUS_BAR_CARRIER, value ? 1 : 0);
            return true;
	} else if (preference == mLcdDensity) {
            String density = (String) newValue;
            if (SystemProperties.get(DENSITY_PROP) != density) {
                if ((density).equals(getResources().getString(R.string.custom_density))) {
                    showDialogInner(DIALOG_CUSTOM_DENSITY);
                } else {
                    setDensity(Integer.parseInt(density));
                }
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	if (preference == mVibrateOnExpand) {
            Settings.System.putBoolean(mContext.getContentResolver(),
                    Settings.System.VIBRATE_NOTIF_EXPAND,
                    ((CheckBoxPreference) preference).isChecked());
         //   Helpers.restartSystemUI();
        } else if (preference == mCustomLabel) {
            AlertDialog.Builder alert = new AlertDialog.Builder(getActivity());

            alert.setTitle(R.string.custom_carrier_label_title);
            alert.setMessage(R.string.custom_carrier_label_explain);

            // Set an EditText view to get user input
            final EditText input = new EditText(getActivity());
            input.setText(mCustomLabelText != null ? mCustomLabelText : "");
            alert.setView(input);
            alert.setPositiveButton(getResources().getString(R.string.ok), new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {
                    String value = ((Spannable) input.getText()).toString();
                    Settings.System.putString(getActivity().getContentResolver(),
                            Settings.System.CUSTOM_CARRIER_LABEL, value);
                    updateCustomLabelTextSummary();
                    Intent i = new Intent();
                    i.setAction("com.android.settings.LABEL_CHANGED");
                    mContext.sendBroadcast(i);
                }
            });
            alert.setNegativeButton(getResources().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    // Canceled.
                }
            });
            alert.show();
        } else {
            // If we didn't handle it, let preferences handle it.
            return super.onPreferenceTreeClick(preferenceScreen, preference);

         }
        return true;
     }

    private void updateCustomLabelTextSummary() {
        mCustomLabelText = Settings.System.getString(getActivity().getContentResolver(),
                Settings.System.CUSTOM_CARRIER_LABEL);
        if (mCustomLabelText == null || mCustomLabelText.length() == 0) {
            mCustomLabel.setSummary(R.string.custom_carrier_label_notset);
        } else {
            mCustomLabel.setSummary(mCustomLabelText);
        }
     }

    private static void setDensity(int density) {
        int max = mActivity.getResources().getInteger(R.integer.lcd_density_max);
        int min = mActivity.getResources().getInteger(R.integer.lcd_density_min);
        int navbarHeight = Settings.System.getIntForUser(mActivity.getContentResolver(),
                Settings.System.NAVIGATION_BAR_HEIGHT, mActivity.getResources()
                .getDimensionPixelSize(com.android.internal.R.dimen.navigation_bar_height),
                UserHandle.USER_CURRENT);
        if (density < min && density > max) {
            mLcdDensity.setSummary(mActivity.getResources().getString(
                                            R.string.custom_density_summary_invalid));
        }
        SystemProperties.set(DENSITY_PROP, Integer.toString(density));
        Settings.System.putInt(mActivity.getContentResolver(),
                Settings.System.LCD_DENSITY, density);

        killCurrentLauncher();
        Configuration mConfiguration = new Configuration();
        mConfiguration.setToDefaults();
        try {
            ActivityManagerNative.getDefault().updateConfiguration(mConfiguration);
        } catch (RemoteException e) {
            Slog.w(TAG, "Failure communicating with activity manager", e);
        }
        mActivity.recreate();
        try {
            Thread.sleep(2000);
        } catch (Exception e){}
        Settings.System.putInt(mActivity.getContentResolver(),
                Settings.System.NAVIGATION_BAR_HEIGHT, navbarHeight);
    }

    private static void killCurrentLauncher() {
        ComponentName defaultLauncher = mActivity.getPackageManager().getHomeActivities(
                        new ArrayList<ResolveInfo>());
                ActivityManager am = (ActivityManager) mActivity.getSystemService(
                        Context.ACTIVITY_SERVICE);
                am.killBackgroundProcesses(defaultLauncher.getPackageName());
    }

    private void showDialogInner(int id) {
        DialogFragment newFragment = MyAlertDialogFragment.newInstance(id);
        newFragment.setTargetFragment(this, 0);
        newFragment.show(getFragmentManager(), "dialog " + id);
    }

    public static class MyAlertDialogFragment extends DialogFragment {

        public static MyAlertDialogFragment newInstance(int id) {
            MyAlertDialogFragment frag = new MyAlertDialogFragment();
            Bundle args = new Bundle();
            args.putInt("id", id);
            frag.setArguments(args);
            return frag;
        }

        InterfaceSettings getOwner() {
            return (InterfaceSettings) getTargetFragment();
        }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            LayoutInflater factory = LayoutInflater.from(getActivity());
            int id = getArguments().getInt("id");
            switch (id) {
                case DIALOG_CUSTOM_DENSITY:
                    final View textEntryView = factory.inflate(
                            R.layout.alert_dialog_text_entry, null);
                    return new AlertDialog.Builder(getActivity())
                            .setTitle(getResources().getString(R.string.set_custom_density_title))
                            .setView(textEntryView)
                            .setPositiveButton(getResources().getString(
                                    R.string.set_custom_density_set),
                                    new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    EditText dpi = (EditText)
                                            textEntryView.findViewById(R.id.dpi_edit);
                                    Editable text = dpi.getText();
                                    dialog.dismiss();
                                    setDensity(Integer.parseInt(text.toString()));

                                }
                            })
                            .setNegativeButton(getResources().getString(R.string.cancel),
                                    new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int whichButton) {
                                    dialog.dismiss();
                                }
                            })
                            .create();
            }
            throw new IllegalArgumentException("unknown id " + id);
        }

        @Override
        public void onCancel(DialogInterface dialog) {
        }
    }
}
