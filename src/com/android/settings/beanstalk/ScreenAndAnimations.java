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
import android.preference.PreferenceScreen;
import android.preference.PreferenceCategory;
import android.preference.Preference.OnPreferenceChangeListener;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;

public class ScreenAndAnimations extends SettingsPreferenceFragment implements
        Preference.OnPreferenceChangeListener {

    private static final String KEY_ANIMATION_OPTIONS = "category_animation_options";
    private static final String KEY_LISTVIEW_ANIMATION = "listview_animation";
    private static final String KEY_LISTVIEW_INTERPOLATOR = "listview_interpolator";
    private static final String KEY_POWER_CRT_MODE = "system_power_crt_mode";
    private static final String KEY_TOAST_ANIMATION = "toast_animation";

    private ListPreference mToastAnimation;
    private ListPreference mCrtMode;
    private ListPreference mListViewAnimation;
    private ListPreference mListViewInterpolator;

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

    }

    @Override
    public void onResume() {
        super.onResume();
    }

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
        return false;
    }
}
