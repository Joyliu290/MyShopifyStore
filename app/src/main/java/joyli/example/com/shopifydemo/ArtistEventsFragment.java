package joyli.example.com.shopifydemo;

import android.content.Intent;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import com.squareup.picasso.Picasso;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.regex.Pattern;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Created by Joyli on 2018-09-08.
 */

public class ArtistEventsFragment extends Fragment {

    private static final String[] KPOP = new String[]{
            "AOA", "APink", "Astro",
            "BAP", "B1A4", "Berry Good", "Black Pink", "The Boyz", "Block B", "BtoB", "Brown Eyed Girls",
            "CLC", "Cosmic Girls", "Crayon Pop", "Chocolat", "Cleo", "C-Clown", "CNBLUE", "Cross Gene", "Winner"
    };
    public static ArtistEventsFragment newInstance() {
        return new ArtistEventsFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.artist_events, container, false);
        AutoCompleteTextView inputArtistName= (AutoCompleteTextView) view.findViewById(R.id.searchArtistName);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(this.getContext(), R.layout.autocomplete_single_item, R.id.autocompleteSingleItemTextView, KPOP);
        inputArtistName.setAdapter(adapter);

        ImageView clearInputArtistName = (ImageView)view.findViewById(R.id.clearSearch);
        // View.setOnClickListener calls onClick when image is being clicked, this would execute the body of the lambda expression
        clearInputArtistName.setOnClickListener((View currentView) -> inputArtistName.setText(""));

        GridLayout gridLayoutConcert = (GridLayout)view.findViewById(R.id.gridLayoutConcert);
        ProgressBar progressOfRequest = (ProgressBar)view.findViewById(R.id.progressBarForArtistEvent);

        inputArtistName.setOnKeyListener((View currentView, int i, KeyEvent keyEvent) -> {
                if ((keyEvent.getAction() == KeyEvent.ACTION_DOWN) && (i == KeyEvent.KEYCODE_ENTER)){
                    gridLayoutConcert.removeAllViews();
                    progressOfRequest.setVisibility(view.VISIBLE);
                    String APIKey = "r1JG9bG41koJH64nFPFuXvEzlMTlcAn4";
                    // Get Request using OKHttpClient
                    OkHttpClient client = new OkHttpClient();
                    Request requestForArtistEvent = new Request.Builder()
                            .url("https://app.ticketmaster.com/discovery/v2/events.json?keyword="+inputArtistName.getText().toString()+"&apikey="+APIKey)
                            .build();
                    client.newCall(requestForArtistEvent).enqueue(new Callback() {
                        @Override
                        public void onFailure(Call call, IOException e) {
                            e.printStackTrace();
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            if (response.isSuccessful()){

                                getActivity().runOnUiThread(()-> progressOfRequest.setVisibility(view.INVISIBLE));
                                String JSONResponse = response.body().string();
                                try {
                                    JSONObject jsonObject = new JSONObject(JSONResponse);
                                    JSONObject innerJsonObject = jsonObject.getJSONObject("_embedded");
                                    JSONArray eventJson = innerJsonObject.getJSONArray("events");

                                    for (int i = 0; i<eventJson.length(); i++){
                                        JSONObject innerEventJson = eventJson.getJSONObject(i);
                                        String concertName = innerEventJson.getString("name");
                                        String concertURL = innerEventJson.getString("url");

                                        // parsing the concertURL since it's not a valid URL right now
                                        String[] properConcertURLArray = concertURL.split(Pattern.quote("\\"));
                                        String finalConcertURL = "";
                                        for (int j = 0; j<properConcertURLArray.length; j++){
                                            finalConcertURL = finalConcertURL + properConcertURLArray[j];
                                        }

                                        String concertURLFinal = finalConcertURL;

                                        // getting the location of concert by parsing further more into the JSON object
                                        JSONObject venueObject = innerEventJson.getJSONObject("_embedded");
                                        JSONArray venueArray = venueObject.getJSONArray("venues");
                                        JSONObject venueArrayObject = venueArray.getJSONObject(0);
                                        String venueName = venueArrayObject.getString("name");
                                        JSONObject venueCity = venueArrayObject.getJSONObject("city");
                                        String cityName = venueCity.getString("name");
                                        JSONObject venueCountry = venueArrayObject.getJSONObject("country");
                                        String country = venueCountry.getString("name");

                                        // form the location name that will be passed in
                                        String locationName = venueName + " in "+ cityName+ ", "+ country;
                                        // parsing JSON to get image URL
                                        JSONArray imageArray = innerEventJson.getJSONArray("images");
                                        JSONObject imageObject = imageArray.getJSONObject(8);
                                        String imageURL = imageObject.getString("url");

                                        // parse the JSON to get the concert time
                                        JSONObject dateObj = innerEventJson.getJSONObject("dates");
                                        JSONObject timeObj = dateObj.getJSONObject("start");
                                        String date = timeObj.getString("localDate");
                                        String time = timeObj.getString("localTime");
                                        String timeZone = dateObj.getString("timezone");
                                        String overallEventTime = time + " in " + timeZone.split("/")[1] + " Timezone";

                                        JSONArray priceArray = innerEventJson.getJSONArray("priceRanges");
                                        JSONObject priceObj = priceArray.getJSONObject(0);
                                        String currency = priceObj.getString("currency");
                                        String minPrice = priceObj.getString("min");
                                        String maxPrice = priceObj.getString("max");
                                        String priceRangeAndCurrency = minPrice + " - " + maxPrice + " in " + currency;

                                        getActivity().runOnUiThread(() -> addConcertItem(concertName, locationName, " " + date, overallEventTime,
                                                priceRangeAndCurrency, concertURLFinal, imageURL, gridLayoutConcert));
                                    }

                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                // the OkHttp enqueue method is executing the network call in the background thread
                                // we can only display texts in the main thread, so use runOnUiThread
                            }
                        }
                    });

                    return true;
                }
                else{
                    return false;
                }
        });

        return view;
    }

    public void addConcertItem(String concertName, String concertLocation, String concertDate, String concertTime, String concertPriceRange, String ticketURL, String imageURL, GridLayout layout){
            getActivity().runOnUiThread(() -> {
                    View concertSingleView = getActivity().getLayoutInflater().inflate(R.layout.single_artist_event,null);

                    // initializing and setting all the textview variables
                    TextView concertNameTV = (TextView)concertSingleView.findViewById(R.id.concertName);
                    concertNameTV.setText(concertName);
                    concertNameTV.setTypeface(null, Typeface.BOLD);
                    TextView concertDateTV = (TextView)concertSingleView.findViewById(R.id.concertDate);
                    concertDateTV.setText(concertDate);
                    TextView concertTimeTV = (TextView)concertSingleView.findViewById(R.id.concertTime);
                    concertTimeTV.setText(concertTime);
                    TextView concertLocationTV = (TextView)concertSingleView.findViewById(R.id.concertLocation);
                    concertLocationTV.setText(concertLocation);
                    TextView concertPriceRangeTV = (TextView)concertSingleView.findViewById(R.id.concertPrice);
                    concertPriceRangeTV.setText(concertPriceRange);
                    Button buyTicketButton = (Button)concertSingleView.findViewById(R.id.buyTicketButton);
                    buyTicketButton.setOnClickListener((View currentView) -> {
                            Intent intent = new Intent();
                            intent.setAction(Intent.ACTION_VIEW);
                            intent.addCategory(Intent.CATEGORY_BROWSABLE);
                            intent.setData(Uri.parse(ticketURL));
                            startActivity(intent);
                        });

                    ImageView img = (ImageView)concertSingleView.findViewById(R.id.concertImage);
                    Picasso.with(getContext())
                            .load(imageURL)
                            .into(img);
                    layout.addView(concertSingleView);
                });
    }
}
