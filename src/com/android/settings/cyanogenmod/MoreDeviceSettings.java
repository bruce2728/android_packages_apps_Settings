/*
 * Copyright (C) 2013 The CyanogenMod project
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

package com.android.settings.cyanogenmod;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Vibrator;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceGroup;
import android.preference.PreferenceManager;
import android.preference.PreferenceScreen;
import android.provider.Settings;
import android.util.Log;

import com.android.settings.R;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.hardware.DisplayColor;
import com.android.settings.hardware.DisplayGamma;
import com.android.settings.hardware.VibratorIntensity;

import org.cyanogenmod.hardware.UsbFastCharging;

public class MoreDeviceSettings extends SettingsPreferenceFragment {
    private static final String TAG = "MoreDeviceSettings";

    private static final String KEY_SENSORS_MOTORS_CATEGORY = "sensors_motors_category";
    private static final String KEY_DISPLAY_CALIBRATION_CATEGORY = "display_calibration_category";
    private static final String KEY_DISPLAY_COLOR = "color_calibration";
    private static final String KEY_DISPLAY_GAMMA = "gamma_tuning";
    private static final String KEY_USB_FAST_CHARGING_CATEGORY = "usb_fast_charging_category";
    private static final String KEY_USB_FAST_CHARGING = "usb_fast_charging";

    private CheckBoxPreference mUsbFastCharging;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.more_device_settings);
        ContentResolver resolver = getContentResolver();

        Vibrator vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        if (!VibratorIntensity.isSupported() || vibrator == null || !vibrator.hasVibrator()) {
            removePreference(KEY_SENSORS_MOTORS_CATEGORY);
        }

        final PreferenceGroup calibrationCategory =
                (PreferenceGroup) findPreference(KEY_DISPLAY_CALIBRATION_CATEGORY);

        if (!DisplayColor.isSupported() && !DisplayGamma.isSupported()) {
            getPreferenceScreen().removePreference(calibrationCategory);
        } else {
            if (!DisplayColor.isSupported()) {
                calibrationCategory.removePreference(findPreference(KEY_DISPLAY_COLOR));
            }
            if (!DisplayGamma.isSupported()) {
                calibrationCategory.removePreference(findPreference(KEY_DISPLAY_GAMMA));
            }
        }

        final PreferenceGroup usbFastChargingCategory =
                (PreferenceGroup) findPreference(KEY_USB_FAST_CHARGING_CATEGORY);
        mUsbCharging = (CheckBoxPreference) findPreference(KEY_USB_FAST_CHARGING);

        if (isUsbFastChargingSupported()) {
            mUsbFastCharging.setChecked(UsbFastCharging.isEnabled());
        } else {
            getPreferenceScreen().removePreference(usbFastChargingCategory);
        }
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {

        if (preference == mUsbFastCharging) {
            return UsbFastCharging.setEnabled(mUsbFastCharging.isChecked());
        }

        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    /**
     * Restore the properties associated with this preference on boot
     * @param context A valid context
     */
    public static void restore(Context context) {
        if (isUsbFastChargingSupported()) {
            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            final boolean enabled = prefs.getBoolean(KEY_USB_FAST_CHARGING, true);
            if (!UsbFastCharging.setEnabled(enabled)) {
                Log.e(TAG, "Failed to restore USB fast charging settings.");
            } else {
                Log.d(TAG, "USB fast charging settings restored.");
            }
        }
    }

    private static boolean isUsbFastChargingSupported() {
        try {
            return UsbFastCharging.isSupported();
        } catch (NoClassDefFoundError e) {
            // Hardware abstraction framework not installed
            return false;
        }
    }

}
