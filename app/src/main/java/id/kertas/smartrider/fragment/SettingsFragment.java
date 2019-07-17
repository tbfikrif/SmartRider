package id.kertas.smartrider.fragment;

import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;

import id.kertas.smartrider.R;

public class SettingsFragment extends PreferenceFragmentCompat {
    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        addPreferencesFromResource(R.xml.settings_pref);

    }
}