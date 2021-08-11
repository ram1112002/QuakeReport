/*
 * Copyright (C) 2016 The Android Open Source Project
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
package com.example.quakereport;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;

import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.loader.app.LoaderManager;
import androidx.loader.content.Loader;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;


public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<List<Earthquake>> {

    private TextView mEmptyStateTextView;
//    private ImageView mImageView;

    private EarthquakeAdapter mAdapter;
    /**
     * Constant value for the earthquake loader ID. We can choose any integer.
     * This really only comes into play if you're using multiple loaders.
     */
    private static final int EARTHQUAKE_LOADER_ID = 1;

    public static final String USGS_REQUEST_URL = "https://earthquake.usgs.gov/fdsnws/event/1/query";

    public static final String LOG_TAG = MainActivity.class.getName();
    private androidx.loader.content.Loader<List<Earthquake>> loader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
//        Log.i(LOG_TAG,"TEST: Main Activity onCreate() called");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.earthquake_activity);

        // Find a reference to the {@link ListView} in the layout
        ListView earthquakeListView = (ListView) findViewById(R.id.list);

        // Create a new adapter that takes an empty list of earthquakes as input
        mAdapter = new EarthquakeAdapter(this, new ArrayList<Earthquake>());
        //Set the adapter on the {@link ListView}
        // so the list can be populated in the user interface
        earthquakeListView.setAdapter(mAdapter);

        // Start the AsyncTask to fetch the earthquake data


        // Set an item click listener on the ListView, which sends an intent to a web browser
        // to open a website with more information about the selected earthquake.
        earthquakeListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l) {
                // Find the current earthquake that was clicked on
                Earthquake currentEarthquake = mAdapter.getItem(position);

                // Convert the String URL into a URI object (to pass into the Intent constructor)
                Uri earthquakeUri = Uri.parse(currentEarthquake.getUrl());

                // Create a new intent to view the earthquake URI
                Intent websiteIntent = new Intent(Intent.ACTION_VIEW, earthquakeUri);

                // Send the intent to launch a new activity
                startActivity(websiteIntent);
            }
        });

        //        mImageView=(ImageView)findViewById(R.id.empty_image);
        mEmptyStateTextView=(TextView) findViewById(R.id.empty_view);
        earthquakeListView.setEmptyView(mEmptyStateTextView);
//        earthquakeListView.setEmptyView(mImageView);

        // Get a reference to the ConnectivityManager to check state of network connectivity
        ConnectivityManager connMana=(ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        // Get details on the currently active default data network
        NetworkInfo networkInfo=connMana.getActiveNetworkInfo();

        // If there is a network connection, fetch data
        if (networkInfo!=null && networkInfo.isConnected()){
            // Get a reference to the LoaderManager, in order to interact with loaders.
            LoaderManager loaderManager= getSupportLoaderManager();
            // Initialize the loader. Pass in the int ID constant defined above and pass in null for
            // the bundle. Pass in this activity for the LoaderCallbacks parameter (which is valid
            // because this activity implements the LoaderCallbacks interface).
//        Log.i(LOG_TAG, "TEST: calling initLoader...");
            loaderManager.initLoader(EARTHQUAKE_LOADER_ID, null, this);
        }
        else {
            // Otherwise, display error
            // First, hide loading indicator so error message will be visible
            View loadingIndicator=findViewById(R.id.LoadingIndicator);
            loadingIndicator.setVisibility(View.GONE);
            // Update empty state with no connection error message
            mEmptyStateTextView.setText(R.string.no_Internet);
        }



    }

    /**
     * This is all of AsyncTask we used and hoe to use AsyncTask but we know that Loader is better then AsyncTask so we used this
     * on this project and made this as a comment.
     */
//            private class EarthquakeAsyncTask extends AsyncTask<String, Void, List<Earthquake>> {
//
//                @Override
//                protected List<Earthquake> doInBackground(String... urls) {
//                    // Don't perform the request if there are no URLs, or the first URL is null.
//                    if (urls.length<1 || urls[0]==null){
//                        return null;
//                    }
//                    List<Earthquake> result = QueryUtils.fetchEarthquakeData(urls[0]);
//                    return result;
//                }
//
//                @Override
//                protected void onPostExecute(List<Earthquake> data) {
//                    // Clear the adapter of previous earthquake data
//                    mAdapter.clear();
//
//                    // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
//                    // data set. This will trigger the ListView to update.
//                    if (data !=null && !data.isEmpty()){
//                        mAdapter.addAll(data);
//                    }
//                }



    @Override
    public Loader<List<Earthquake>> onCreateLoader(int i, Bundle bundle) {
        // Create a new loader for the given URL
//        Log.i(LOG_TAG,"TEST: onCreateLoader() called...");
        SharedPreferences sharedPrefs= PreferenceManager.getDefaultSharedPreferences(this);
        String minMagnitude=sharedPrefs.getString(
                getString(R.string.settings_min_Magnitude_key),
                getString(R.string.settings_min_Magnitude_default));

        String orderBy=sharedPrefs.getString(
                getString(R.string.settings_orderBy_Key),
                getString(R.string.settings_orderBy_default)
        );
        String limit=sharedPrefs.getString(
                getString(R.string.limit_key),
                getString(R.string.limit_default)
        );
        Uri baseUri= Uri.parse(USGS_REQUEST_URL);
        Uri.Builder uriBuilder=baseUri.buildUpon();

        uriBuilder.appendQueryParameter("format","geojson");
        uriBuilder.appendQueryParameter("limit",limit);
        uriBuilder.appendQueryParameter("minmag",minMagnitude);
        uriBuilder.appendQueryParameter("orderby",orderBy);

        return new EarthquakeLoader(this, uriBuilder.toString());
    }

    @Override
    public void onLoadFinished(Loader<List<Earthquake>> loader, List<Earthquake> earthquakes) {
//        Log.i(LOG_TAG,"TEST: onLoadFinished() called...");

        View loadingIndicator=findViewById(R.id.LoadingIndicator);
        loadingIndicator.setVisibility(View.GONE);

        // Set empty state text to display "No earthquakes found."
        mEmptyStateTextView.setText(R.string.no_earthquakes);
//        mImageView.setImageResource(R.drawable._9234680_mother_and_baby_stylized_vector_symbol_mom_hugs_her_child_icon_design_happy_mother_s_day_concept_ill);

        // Clear the adapter of previous earthquake data
        mAdapter.clear();

        // If there is a valid list of {@link Earthquake}s, then add them to the adapter's
        // data set. This will trigger the ListView to update.


        if (earthquakes != null && !earthquakes.isEmpty()) {
            mAdapter.addAll(earthquakes);
        }
    }

        @Override
        public void onLoaderReset
        (Loader < List < Earthquake >> loader)
        {
// Loader reset, so we can clear out our existing data.
//            Log.i(LOG_TAG,"TEST: onLoaderReset() called...");

            mAdapter.clear();
        }

        @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.main,menu);
        return true;
        }
        @Override
    public boolean onOptionsItemSelected(MenuItem item){
        int id=item.getItemId();
        if (id==R.id.action_settings){
            Intent settingsIntent=new Intent(this,SettingsActivity.class);
            startActivity(settingsIntent);
            return true;
        }
        return super.onOptionsItemSelected(item);
        }

    }





