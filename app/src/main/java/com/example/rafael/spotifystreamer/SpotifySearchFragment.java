package com.example.rafael.spotifystreamer;

import android.content.SharedPreferences;
import android.net.Uri;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.text.format.Time;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Locale;

import butterknife.ButterKnife;
import butterknife.InjectView;


/**
 * A placeholder fragment containing a simple view.
 */
public class SpotifySearchFragment extends Fragment {
    @InjectView(R.id.spotify_search_text) EditText spotifySearch;
    @InjectView(R.id.spotify_search_list) ListView spotifyList;

    public SpotifySearchFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_main, container, false);
        ButterKnife.inject(this, rootView);

        spotifySearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                searchArtist();

            }
        });

        return rootView;
    }

    private void searchArtist() {
        retrieveSpotifyData weatherTask = new retrieveSpotifyData();
        weatherTask.execute(spotifySearch.getText().toString());
    }

    public class retrieveSpotifyData extends AsyncTask<String, Void, String[]> {

        private final String LOG_TAG = retrieveSpotifyData.class.getSimpleName();

        @Override
        protected String[] doInBackground(String... params) {
            // These two need to be declared outside the try/catch
            // so that they can be closed in the finally block.
            HttpURLConnection urlConnection = null;
            BufferedReader reader = null;

            // Will contain the raw JSON response as a string.
            String spotifyResponse = null;

            String type = "artist";

            try {
                // Construct the URL for the Spotify search query
                // Possible parameters are available at Search for an Item
                // http://openweathermap.org/API#forecast
                final String FORECAST_BASE_URL = "https://api.spotify.com/v1/search";
                final String QUERY_PARAM = "q";
                final String TYPE_PARAM = "type";

                Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon()
                        .appendQueryParameter(QUERY_PARAM, params[0])
                        .appendQueryParameter(TYPE_PARAM, type)
                        .build();


                URL url = new URL(builtUri.toString());

                // Create the request to OpenWeatherMap, and open the connection
                urlConnection = (HttpURLConnection) url.openConnection();
                urlConnection.setRequestMethod("GET");
                urlConnection.connect();

                // Read the input stream into a String
                InputStream inputStream = urlConnection.getInputStream();
                StringBuffer buffer = new StringBuffer();
                if (inputStream == null) {
                    // Nothing to do.
                    return null;
                }
                reader = new BufferedReader(new InputStreamReader(inputStream));

                String line;
                while ((line = reader.readLine()) != null) {
                    // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
                    // But it does make debugging a *lot* easier if you print out the completed
                    // buffer for debugging.
                    buffer.append(line + "\n");
                }

                if (buffer.length() == 0) {
                    // Stream was empty.  No point in parsing.
                    return null;
                }
                spotifyResponse = buffer.toString();
                Log.d("JSON", spotifyResponse);
            } catch (IOException e) {
                Log.e("PlaceholderFragment", "Error ", e);
                // If the code didn't successfully get the weather data, there's no point in attempting
                // to parse it.
                return null;
            } finally{
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (final IOException e) {
                        Log.e("PlaceholderFragment", "Error closing stream", e);
                    }
                }
            }
            try {
                String[] resultForecast = getArtistDataFromJson(spotifyResponse);
                return resultForecast;
            } catch (JSONException e) {
                e.printStackTrace();
            }

            return null;
        }

        @Override
        protected void onPostExecute(String[] result) {
            super.onPostExecute(result);

            /*if (result != null){
                if (weekForecast == null){
                    weekForecast = new ArrayList<String>();
                }else {
                    weekForecast.clear();
                }

                for (int i = 0; i<result.length; i++){
                    weekForecast.add(result[i]);
                    Log.v("forecast", result[i]);
                }
                fanciAdapter = new FancyAdapter();
                forecastList.setAdapter(fanciAdapter);
                fanciAdapter.notifyDataSetChanged();
            }*/




        }

        /* The date/time conversion code is going to be moved outside the asynctask later,
         * so for convenience we're breaking it out into its own method now.
         */
        private String getReadableDateString(long time){
            // Because the API returns a unix timestamp (measured in seconds),
            // it must be converted to milliseconds in order to be converted to valid date.
            SimpleDateFormat shortenedDateFormat = new SimpleDateFormat("EEEE/MMMM dd", Locale.US);
            return shortenedDateFormat.format(time);
        }


        /**
         * Take the String representing the complete forecast in JSON Format and
         * pull out the data we need to construct the Strings needed for the wireframes.
         *
         * Fortunately parsing is easy:  constructor takes the JSON string and converts it
         * into an Object hierarchy for us.
         */
        private String[] getArtistDataFromJson(String forecastJsonStr)
                throws JSONException {

            Log.d("spotifyJson", forecastJsonStr);

            // These are the names of the JSON objects that need to be extracted.
            final String OWM_LIST = "items";
            final String OWM_WEATHER = "weather";
            final String OWM_TEMPERATURE = "temp";
            final String OWM_MAX = "max";
            final String OWM_MIN = "min";
            final String OWM_DESCRIPTION = "main";

            JSONObject forecastJson = new JSONObject(forecastJsonStr);
            JSONArray weatherArray = forecastJson.getJSONArray(OWM_LIST);

            // OWM returns daily forecasts based upon the local time of the city that is being
            // asked for, which means that we need to know the GMT offset to translate this data
            // properly.

            // Since this data is also sent in-order and the first day is always the
            // current day, we're going to take advantage of that to get a nice
            // normalized UTC date for all of our weather.

            Time dayTime = new Time();
            dayTime.setToNow();

            // we start at the day returned by local time. Otherwise this is a mess.
            int julianStartDay = Time.getJulianDay(System.currentTimeMillis(), dayTime.gmtoff);

            // now we work exclusively in UTC
            dayTime = new Time();

            // Data is fetched in Celsius by default.
            // If user prefers to see in Fahrenheit, convert the values here.
            // We do this rather than fetching in Fahrenheit so that the user can
            // change this option without us having to re-fetch the data once
            // we start storing the values in a database.


            String[] resultStrs = new String[weatherArray.length()];
            for(int i = 0; i < weatherArray.length(); i++) {
                // For now, using the format "Day, description, hi/low"
                String day;
                String description;
                String highAndLow;

                // Get the JSON object representing the day
                JSONObject dayForecast = weatherArray.getJSONObject(i);

                // The date/time is returned as a long.  We need to convert that
                // into something human-readable, since most people won't read "1400356800" as
                // "this saturday".
                long dateTime;
                // Cheating to convert this to UTC time, which is what we want anyhow
                dateTime = dayTime.setJulianDay(julianStartDay+i);
                day = getReadableDateString(dateTime);

                // description is in a child array called "weather", which is 1 element long.
                JSONObject weatherObject = dayForecast.getJSONArray(OWM_WEATHER).getJSONObject(0);
                description = weatherObject.getString(OWM_DESCRIPTION);

                // Temperatures are in a child object called "temp".  Try not to name variables
                // "temp" when working with temperature.  It confuses everybody.
                JSONObject temperatureObject = dayForecast.getJSONObject(OWM_TEMPERATURE);
                double high = temperatureObject.getDouble(OWM_MAX);
                double low = temperatureObject.getDouble(OWM_MIN);

                int humidity = dayForecast.getInt("humidity");
                double pressure = dayForecast.getDouble("pressure");
                double wind = dayForecast.getDouble("speed");

                //highAndLow = formatHighLows(high, low, pressure, wind, unitType);
                resultStrs[i] = day + "/" + description + "/" + Integer.toString(humidity);
            }

            /*for (String s : resultStrs) {
                Log.v(LOG_TAG, "Forecast entry: " + s);
            }*/
            return resultStrs;

        }
    }
}
