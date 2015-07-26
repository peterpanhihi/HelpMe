package teeza.application.helpme.adapter;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.AutocompletePrediction;
import com.google.android.gms.location.places.AutocompletePredictionBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLngBounds;

import android.location.Address;
import android.location.Geocoder;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.content.Context;
import android.widget.ArrayAdapter;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import teeza.application.helpme.R;

/**
 * Adapter that handles Autocomplete requests from the Places Geo Data API.
 * Results are encoded as
 * {@link com.example.google.playservices.placecomplete.PlaceAutocompleteAdapter.PlaceAutocomplete}
 * objects that contain both the Place ID and the text description from the
 * autocomplete query.
 * <p>
 * Note that this adapter requires a valid
 * {@link com.google.android.gms.common.api.GoogleApiClient}. The API client
 * must be maintained in the encapsulating Activity, including all lifecycle and
 * connection states. The API client must be connected with the
 * {@link Places#GEO_DATA_API} API.
 */
public class PlaceAutocompleteAdapter extends
		ArrayAdapter<PlaceAutocompleteAdapter.PlaceAutocomplete> implements
		Filterable {

	private static final String TAG = "PlaceAutocompleteAdapter";
	/**
	 * Current results returned by this adapter.
	 */
	private ArrayList<PlaceAutocomplete> mResultList;

	/**
	 * Handles autocomplete requests.
	 */
	private GoogleApiClient mGoogleApiClient;

	/**
	 * The bounds used for Places Geo Data autocomplete API requests.
	 */
	private LatLngBounds mBounds;

	/**
	 * The autocomplete filter used to restrict queries to a specific set of
	 * place types.
	 */
	private AutocompleteFilter mPlaceFilter;
	private Context context;
	private int viewResourceId;

	/**
	 * Initializes with a resource for text rows and autocomplete query bounds.
	 *
	 * @see android.widget.ArrayAdapter#ArrayAdapter(android.content.Context,
	 *      int)
	 */
	public PlaceAutocompleteAdapter(Context context, int resource,
			GoogleApiClient googleApiClient, LatLngBounds bounds,
			AutocompleteFilter filter) {
		super(context, resource);
		mGoogleApiClient = googleApiClient;
		mBounds = bounds;
		mPlaceFilter = filter;
		this.context = context;
		viewResourceId = resource;
	}

	/**
	 * Sets the bounds for all subsequent queries.
	 */
	public void setBounds(LatLngBounds bounds) {
		mBounds = bounds;
	}

	/**
	 * Returns the number of results received in the last autocomplete query.
	 */
	@Override
	public int getCount() {
		return mResultList.size();
	}

	/**
	 * Returns an item from the last autocomplete query.
	 */
	@Override
	public PlaceAutocomplete getItem(int position) {
		return mResultList.get(position);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View v = convertView;
		if (v == null) {
			LayoutInflater vi = (LayoutInflater) getContext().getSystemService(
					Context.LAYOUT_INFLATER_SERVICE);
			v = vi.inflate(viewResourceId, null);
		}
		PlaceAutocomplete place = null;
		if(getCount() > position)
			place = getItem(position);
		if (place != null) {
			TextView title = (TextView) v.findViewById(R.id.title);
			if (title != null) {
				title.setText(place.toString());
			}
		}
		return v;
	}

	/**
	 * Returns the filter for the current set of autocomplete results.
	 */
	@Override
	public Filter getFilter() {
		Filter filter = new Filter() {
			@Override
			protected FilterResults performFiltering(CharSequence constraint) {
				FilterResults results = new FilterResults();
				// Skip the autocomplete query if no constraints are given.
				if (constraint != null) {
					// Query the autocomplete API for the (constraint) search
					// string.
					mResultList = getAutocomplete(constraint);
					if (mResultList != null) {
						// The API successfully returned results.
						results.values = mResultList;
						results.count = mResultList.size();
					}
				}
				return results;
			}

			@Override
			protected void publishResults(CharSequence constraint,
					FilterResults results) {
				if (results != null && results.count > 0) {
					// The API returned at least one result, update the data.
					notifyDataSetChanged();
				} else {
					// The API did not return any results, invalidate the data
					// set.
					notifyDataSetInvalidated();
				}
			}
		};
		return filter;
	}

	/**
	 * Submits an autocomplete query to the Places Geo Data Autocomplete API.
	 * Results are returned as
	 * {@link com.example.google.playservices.placecomplete.PlaceAutocompleteAdapter.PlaceAutocomplete}
	 * objects to store the Place ID and description that the API returns.
	 * Returns an empty list if no results were found. Returns null if the API
	 * client is not available or the query did not complete successfully. This
	 * method MUST be called off the main UI thread, as it will block until data
	 * is returned from the API, which may include a network request.
	 *
	 * @param constraint
	 *            Autocomplete query string
	 * @return Results from the autocomplete API or null if the query was not
	 *         successful.
	 * @see Places#GEO_DATA_API#getAutocomplete(CharSequence)
	 */
	private ArrayList<PlaceAutocomplete> getAutocomplete(CharSequence constraint) {
		if (mGoogleApiClient.isConnected()) {
			Log.i(TAG, "Starting autocomplete query for: " + constraint);

			// Submit the query to the autocomplete API and retrieve a
			// PendingResult that will
			// contain the results when the query completes.
			PendingResult<AutocompletePredictionBuffer> results = Places.GeoDataApi
					.getAutocompletePredictions(mGoogleApiClient,
							constraint.toString(), mBounds, mPlaceFilter);

			// This method should have been called off the main UI thread. Block
			// and wait for at most 60s
			// for a result from the API.
			AutocompletePredictionBuffer autocompletePredictions = results
					.await(60, TimeUnit.SECONDS);

			// Confirm that the query completed successfully, otherwise return
			// null
			final Status status = autocompletePredictions.getStatus();
			if (!status.isSuccess()) {
				Toast.makeText(getContext(),
						"Error contacting API: " + status.toString(),
						Toast.LENGTH_SHORT).show();
				Log.e(TAG, "Error getting autocomplete prediction API call: "
						+ status.toString());
				autocompletePredictions.release();
				return null;
			}

			Log.i(TAG,
					"Query completed. Received "
							+ autocompletePredictions.getCount()
							+ " predictions.");

			// Copy the results into our own data structure, because we can't
			// hold onto the buffer.
			// AutocompletePrediction objects encapsulate the API response
			// (place ID and description).

			Iterator<AutocompletePrediction> iterator = autocompletePredictions
					.iterator();
			ArrayList resultList = new ArrayList(
					autocompletePredictions.getCount());
			while (iterator.hasNext()) {
				AutocompletePrediction prediction = iterator.next();
				// Get the details of this prediction and copy it into a new
				// PlaceAutocomplete object.
				resultList.add(new PlaceAutocomplete(prediction.getPlaceId(),
						prediction.getDescription()));
			}

			// Release the buffer now that all data has been copied.
			autocompletePredictions.release();

			return resultList;
		}
		Log.e(TAG, "Google API client is not connected for autocomplete query.");
		return null;
	}

	/**
	 * Holder for Places Geo Data Autocomplete API results.
	 */
	public class PlaceAutocomplete {

		public CharSequence placeId;
		public CharSequence description;

		PlaceAutocomplete(CharSequence placeId, CharSequence description) {
			this.placeId = placeId;
			this.description = description;
		}

		@Override
		public String toString() {
			return description.toString();
		}
	}
}
