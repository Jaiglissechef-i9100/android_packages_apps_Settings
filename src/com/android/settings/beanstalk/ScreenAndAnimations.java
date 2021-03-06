package com.android.settings.beanstalk;

import android.widget.Toast;
import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Resources;
import android.os.Bundle;
import android.provider.Settings;
import android.content.ContentResolver;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.text.TextUtils;
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;
import com.android.settings.beanstalk.AppMultiSelectListPreference;
import com.android.settings.beanstalk.SeekBarPreferenceChOS;

import java.util.HashSet;
import java.util.Set;
import java.lang.Thread;
import java.util.ArrayList;
import java.util.Arrays;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class ScreenAndAnimations extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_ANIMATION_OPTIONS = "category_animation_options";
    private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
    private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";
    private static final String KEY_POWER_CRT_MODE = "system_power_crt_mode";
    private static final String KEY_TOAST_ANIMATION = "toast_animation";
    private static final String PREF_ENABLE_APP_CIRCLE_BAR = "enable_app_circle_bar";
    private static final String PREF_INCLUDE_APP_CIRCLE_BAR_KEY = "app_circle_bar_included_apps";
    private static final String KEY_TRIGGER_WIDTH = "trigger_width";
    private static final String KEY_TRIGGER_TOP = "trigger_top";
    private static final String KEY_TRIGGER_BOTTOM = "trigger_bottom";

    private AppMultiSelectListPreference mIncludedAppCircleBar;
    private ListPreference mToastAnimation;
    private ListPreference mCrtMode;
    private CheckBoxPreference mEnableAppCircleBar;
    private ListPreference mListViewAnimation;
    private ListPreference mListViewInterpolator;
    private SeekBarPreferenceChOS mTriggerWidthPref;
    private SeekBarPreferenceChOS mTriggerTopPref;
    private SeekBarPreferenceChOS mTriggerBottomPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.screen_and_animations);

	PreferenceScreen prefSet = getPreferenceScreen();

	// respect device default configuration
        // true fades while false animates
        boolean electronBeamFadesConfig = getResources().getBoolean(
                com.android.internal.R.bool.config_animateScreenLights);
        PreferenceCategory animationOptions =
            (PreferenceCategory) prefSet.findPreference(KEY_ANIMATION_OPTIONS);
        mCrtMode = (ListPreference) prefSet.findPreference(KEY_POWER_CRT_MODE);
        if (!electronBeamFadesConfig && mCrtMode != null) {
            int crtMode = Settings.System.getInt(getContentResolver(),
                    Settings.System.SYSTEM_POWER_CRT_MODE, 1);
            mCrtMode.setValue(String.valueOf(crtMode));
            mCrtMode.setSummary(mCrtMode.getEntry());
            mCrtMode.setOnPreferenceChangeListener(this);

        } else if (mCrtMode != null & animationOptions != null) {
            animationOptions.removePreference(mCrtMode);
        }

	mListViewAnimation = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_ANIMATION);
        int listviewanimation = Settings.System.getInt(getContentResolver(),
                Settings.System.LISTVIEW_ANIMATION, 0);
	if(mListViewAnimation != null){

        	mListViewAnimation.setValue(String.valueOf(listviewanimation));
        	mListViewAnimation.setSummary(mListViewAnimation.getEntry());
        	mListViewAnimation.setOnPreferenceChangeListener(this);
	}

        mListViewInterpolator = (ListPreference) prefSet.findPreference(KEY_LISTVIEW_INTERPOLATOR);
        int listviewinterpolator = Settings.System.getInt(getContentResolver(),
                Settings.System.LISTVIEW_INTERPOLATOR, 0);
	if(mListViewInterpolator != null)
	{
        	mListViewInterpolator.setValue(String.valueOf(listviewinterpolator));
        	mListViewInterpolator.setSummary(mListViewInterpolator.getEntry());
        	mListViewInterpolator.setOnPreferenceChangeListener(this);
        	mListViewInterpolator.setEnabled(listviewanimation > 0);
	}

	mToastAnimation = (ListPreference) findPreference(KEY_TOAST_ANIMATION);
	mToastAnimation.setSummary(mToastAnimation.getEntry());
	int CurrentToastAnimation = Settings.System.getInt(getContentResolver(), Settings.System.ACTIVITY_ANIMATION_CONTROLS[10], 1);
	mToastAnimation.setValueIndex(CurrentToastAnimation); //set to index of default value
	mToastAnimation.setSummary(mToastAnimation.getEntries()[CurrentToastAnimation]);
	mToastAnimation.setOnPreferenceChangeListener(this);

	// App circle bar
	mEnableAppCircleBar = (CheckBoxPreference) prefSet.findPreference(PREF_ENABLE_APP_CIRCLE_BAR);
	mEnableAppCircleBar.setChecked((Settings.System.getInt(getContentResolver(),
	Settings.System.ENABLE_APP_CIRCLE_BAR, 0) == 1));

	mIncludedAppCircleBar = (AppMultiSelectListPreference) prefSet.findPreference		(PREF_INCLUDE_APP_CIRCLE_BAR_KEY);
	Set<String> includedApps = getIncludedApps();
	if (includedApps != null) mIncludedAppCircleBar.setValues(includedApps);
	mIncludedAppCircleBar.setOnPreferenceChangeListener(this);

	mTriggerWidthPref = (SeekBarPreferenceChOS) findPreference(KEY_TRIGGER_WIDTH);
	mTriggerWidthPref.setValue(Settings.System.getInt(getContentResolver(),
	Settings.System.APP_CIRCLE_BAR_TRIGGER_WIDTH, 10));
	mTriggerWidthPref.setOnPreferenceChangeListener(this);

	mTriggerTopPref = (SeekBarPreferenceChOS) findPreference(KEY_TRIGGER_TOP);
	mTriggerTopPref.setValue(Settings.System.getInt(getContentResolver(),
	Settings.System.APP_CIRCLE_BAR_TRIGGER_TOP, 0));
	mTriggerTopPref.setOnPreferenceChangeListener(this);

	mTriggerBottomPref = (SeekBarPreferenceChOS) findPreference(KEY_TRIGGER_BOTTOM);
	mTriggerBottomPref.setValue(Settings.System.getInt(getContentResolver(),
	Settings.System.APP_CIRCLE_BAR_TRIGGER_HEIGHT, 100));
	mTriggerBottomPref.setOnPreferenceChangeListener(this);

    }

    @Override
    public void onPause() {
	super.onPause();
	Settings.System.putInt(getContentResolver(),
		Settings.System.APP_CIRCLE_BAR_SHOW_TRIGGER, 0);
    }

    @Override
    public void onResume() {
        super.onResume();
	Settings.System.putInt(getContentResolver(),
		Settings.System.APP_CIRCLE_BAR_SHOW_TRIGGER, 1);
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
	ContentResolver resolver = getActivity().getContentResolver();
	boolean value;
	if (preference == mEnableAppCircleBar) {
	    boolean checked = ((CheckBoxPreference)preference).isChecked();
	    Settings.System.putInt(resolver,
		Settings.System.ENABLE_APP_CIRCLE_BAR, checked ? 1:0);
	} else {
	    return super.onPreferenceTreeClick(preferenceScreen, preference);
	}

	return true;
    }

    @Override
    public boolean onPreferenceChange(Preference preference, Object newValue) {
	final String key = preference.getKey();

	if (KEY_POWER_CRT_MODE.equals(key)) {
            int value = Integer.parseInt((String) newValue);
            int index = mCrtMode.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.SYSTEM_POWER_CRT_MODE,
                    value);
            mCrtMode.setSummary(mCrtMode.getEntries()[index]);
        }

	if (preference == mToastAnimation) {
            int index = mToastAnimation.findIndexOfValue((String) newValue);
            Settings.System.putString(getContentResolver(), Settings.System.ACTIVITY_ANIMATION_CONTROLS[10], (String) newValue);
            mToastAnimation.setSummary(mToastAnimation.getEntries()[index]);
            Toast.makeText(mContext, "Toast Test", Toast.LENGTH_SHORT).show();
            return true;
	}

	if (preference == mTriggerWidthPref) {
	    int width = ((Integer)newValue).intValue();
	    Settings.System.putInt(getContentResolver(),
		Settings.System.APP_CIRCLE_BAR_TRIGGER_WIDTH, width);
	    return true;
	}

    	if (preference == mTriggerTopPref) {
	    int top = ((Integer)newValue).intValue();
	    Settings.System.putInt(getContentResolver(),
		Settings.System.APP_CIRCLE_BAR_TRIGGER_TOP, top);
	    return true;
	}

	if (preference == mTriggerBottomPref) {
	    int bottom = ((Integer)newValue).intValue();
	    Settings.System.putInt(getContentResolver(),
		Settings.System.APP_CIRCLE_BAR_TRIGGER_HEIGHT, bottom);
	    return true;
	}

	if (KEY_LISTVIEW_ANIMATION.equals(key)) {
            int value = Integer.parseInt((String) newValue);
            int index = mListViewAnimation.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LISTVIEW_ANIMATION,
                    value);
            mListViewAnimation.setSummary(mListViewAnimation.getEntries()[index]);
            mListViewInterpolator.setEnabled(value > 0);
        }
        if (KEY_LISTVIEW_INTERPOLATOR.equals(key)) {
            int value = Integer.parseInt((String) newValue);
            int index = mListViewInterpolator.findIndexOfValue((String) newValue);
            Settings.System.putInt(getContentResolver(),
                    Settings.System.LISTVIEW_INTERPOLATOR,
                    value);
            mListViewInterpolator.setSummary(mListViewInterpolator.getEntries()[index]);
        }
	if (preference == mIncludedAppCircleBar) {
	    storeIncludedApps((Set<String>) newValue);
	}
        return false;
    }

    private Set<String> getIncludedApps() {
	String included = Settings.System.getString(getActivity().getContentResolver(),
			Settings.System.WHITELIST_APP_CIRCLE_BAR);
	if (TextUtils.isEmpty(included)) {
		return null;
	}
	return new HashSet<String>(Arrays.asList(included.split("\\|")));
    }

    private void storeIncludedApps(Set<String> values) {
	StringBuilder builder = new StringBuilder();
	String delimiter = "";
	for (String value : values) {
		builder.append(delimiter);
		builder.append(value);
		delimiter = "|";
	}
	Settings.System.putString(getActivity().getContentResolver(),
		Settings.System.WHITELIST_APP_CIRCLE_BAR, builder.toString());
    }
}
