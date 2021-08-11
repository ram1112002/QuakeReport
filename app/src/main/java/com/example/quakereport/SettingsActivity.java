package com.example.quakereport;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.view.menu.ListMenuPresenter;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;

import java.util.List;
import java.util.prefs.Preferences;

public class SettingsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_settings);
    }

    public static class EarthquakePreferenceFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.settings_main);

            Preference minMagnitude = findPreference(getString(R.string.settings_min_Magnitude_key));
            bindPreferenceSummeryToValue(minMagnitude);

            Preference orderBy=findPreference(getString(R.string.settings_orderBy_Key));
            bindPreferenceSummeryToValue(orderBy);

            Preference limit=findPreference(getString(R.string.limit_key));
            bindPreferenceSummeryToValue(limit);
        }
        public boolean onPreferenceChange(Preference preference, Object value) {
            String stringValue= value.toString();
            if (preference instanceof ListPreference){
                ListPreference listPreference=(ListPreference)preference;
                int preOfIndex=listPreference.findIndexOfValue(stringValue);
                if(preOfIndex >=0){
                    CharSequence[] labels=listPreference.getEntries();
                    preference.setSummary(labels[preOfIndex]);
                }
                else {
                    preference.setSummary(stringValue);
                }
            }
            preference.setSummary(stringValue);
            return true;
        }
        private void bindPreferenceSummeryToValue(Preference preference){
            preference.setOnPreferenceChangeListener(this);
            SharedPreferences preferences= PreferenceManager.getDefaultSharedPreferences(preference.getContext());
            String preferenceString= preferences.getString(preference.getKey(),"");
            onPreferenceChange(preference, preferenceString);
        }
    }

}