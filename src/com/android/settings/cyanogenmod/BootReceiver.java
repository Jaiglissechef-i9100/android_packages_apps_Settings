
package com.android.settings.cyanogenmod;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.android.settings.DisplaySettings;
import com.android.settings.R;
import com.android.settings.Utils;
import com.android.settings.hardware.DisplayColor;
import com.android.settings.hardware.DisplayGamma;
import com.android.settings.hardware.VibratorIntensity;
import com.android.settings.location.LocationSettings;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context ctx, Intent intent) {

        /* Restore hardware tunable values */
        DisplayColor.restore(ctx);
        DisplayGamma.restore(ctx);
        VibratorIntensity.restore(ctx);
        DisplaySettings.restore(ctx);
        LocationSettings.restore(ctx);
    }
}
