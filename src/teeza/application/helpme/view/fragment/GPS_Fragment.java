package teeza.application.helpme.view.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Document;

import teeza.application.helpme.R;
import teeza.application.helpme.adapter.PlaceAutocompleteAdapter;
import teeza.application.helpme.http.GMapV2Direction;
import teeza.application.helpme.model.ApplicationStatus;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.AutoCompleteTextView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

public class GPS_Fragment extends GMap_Fragment {
	protected GoogleApiClient googleApiClient;

	private PlaceAutocompleteAdapter mAdapter;

	private AutoCompleteTextView mAutocompleteView;

	public static final LatLngBounds BOUNDS_GREATER_SYDNEY = new LatLngBounds(
			new LatLng(-34.041458, 150.790100), new LatLng(-33.682247,
					151.383362));
	private GoogleMap mMap;
	private Marker mMarker;
	private double lat, lng, lat1, lng1, ceilLat, ceilLng, floorLat, floorLng;
	private LatLng point, pointFrom, pointTo, coordinate;
	private AlertDialog.Builder builder;
	private ToggleButton taffic;
	private View rootView;
	private ImageView btsearch, btplacesearch, btnclear;
	// private EditText et, et1, et2;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
	private String search, phonenumber, titlemark, snipmark, stFrom, stTo;
	private String status = "0";
	private String status2 = "0";
	private int stat = 1;
	private ApplicationStatus appStatus;
	private LatLngBounds.Builder latlngBuilder;
	private LatLngBounds bounds;
	private CameraUpdate cu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		StrictMode.setThreadPolicy(policy);
		setRetainInstance(true);

		googleApiClient = new GoogleApiClient.Builder(getActivity())
				.addApi(Places.PLACE_DETECTION_API)
				.addOnConnectionFailedListener(this)
				.addApi(Places.GEO_DATA_API).build();

		resetLatLng();
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.gps_map, container, false);
		builder = new AlertDialog.Builder(getActivity());
		appStatus = ApplicationStatus.getInstance();

		if (appStatus.isOnline(getActivity())) {
			if (mMap == null)
				mMap = ((SupportMapFragment) getFragmentManager()
						.findFragmentById(R.id.map3)).getMap();
			mMap.getUiSettings().setMapToolbarEnabled(false);
			mMap.getUiSettings().setZoomControlsEnabled(false);
		} else {
			appStatus.setNetwork(getActivity());
		}

		mAutocompleteView = (AutoCompleteTextView) rootView
				.findViewById(R.id.autocomplete_places);

		mAutocompleteView.setOnItemClickListener(mAutocompleteClickListener);

		mAdapter = new PlaceAutocompleteAdapter(getActivity(),
				R.layout.autocomplete_list, googleApiClient,
				BOUNDS_GREATER_SYDNEY, null);
		mAutocompleteView.setAdapter(mAdapter);
		mAutocompleteView
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView arg0, int arg1,
							KeyEvent arg2) {
						appStatus.checkLocation(getActivity());
						if (mAutocompleteView.getText().toString().trim()
								.equals("")) {
							Toast.makeText(
									getActivity().getApplicationContext(),
									"Please Location", Toast.LENGTH_SHORT)
									.show();
						} else {
							search();
							mAutocompleteView.dismissDropDown();
							hideSoftKeyboard(getActivity());
						}
						return false;
					}
				});

		btnclear = (ImageView) rootView.findViewById(R.id.imageView_close);
		btnclear.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				mAutocompleteView.setText("");
				status = "0";
				resetLatLng();
				updateUI();
				
				mMap.clear();
				mMap.addMarker(new MarkerOptions()
						.position(new LatLng(lat, lng))
						.title("คุณ")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.marker_user)));
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						coordinate, 14));
				
			}
		});

		btplacesearch = (ImageView) rootView
				.findViewById(R.id.imageView_placesearchgps);
		btplacesearch.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				appStatus.checkLocation(getActivity());
				mAutocompleteView.setText("");

				builder = new AlertDialog.Builder(new ContextThemeWrapper(
						getActivity(), R.style.AppTheme));
				@SuppressWarnings("static-access")
				LayoutInflater inflater2 = (LayoutInflater) getActivity()
						.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
				View layout = inflater2.inflate(R.layout.gpsdialog_input, null);
				final AutoCompleteTextView from = (AutoCompleteTextView) layout
						.findViewById(R.id.from);
				from.setOnItemClickListener(autoOnClickPlaceSearch);
				from.setAdapter(mAdapter);

				final AutoCompleteTextView to = (AutoCompleteTextView) layout
						.findViewById(R.id.to);
				to.setOnItemClickListener(autoOnClickPlaceSearch);
				to.setAdapter(mAdapter);

				builder.setView(layout);
				builder.setCancelable(true);
				builder.setPositiveButton("ไม่ต้องการ",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {
								dialog.cancel();
							}
						});
				builder.setNegativeButton("ต้องการ",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog,
									int whichButton) {

								stFrom = from.getText().toString().trim();
								stTo = to.getText().toString().trim();
								if (stFrom.equals("") || stTo.equals("")) {

								} else {
									placesearch(stFrom, stTo);
									from.setText("");
									to.setText("");
									dialog.cancel();
									status2 = "1";
								}
							}
						});
				builder.show();
			}
		});

		// et = (EditText) rootView.findViewById(R.id.editText_searchgps);
		taffic = (ToggleButton) rootView.findViewById(R.id.toggleButton1);
		mMap.setTrafficEnabled(false);
		taffic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				if (stat == 0) {
					taffic.setChecked(false);
					mMap.setTrafficEnabled(false);
					stat = 1;
				} else {
					taffic.setChecked(true);
					mMap.setTrafficEnabled(true);
					stat = 0;
				}
			}
		});

		return rootView;
	}

	/**
	 * Listener that handles selections from suggestions from the
	 * AutoCompleteTextView that displays Place suggestions. Gets the place id
	 * of the selected item and issues a request to the Places Geo Data API to
	 * retrieve more details about the place.
	 *
	 * @see com.google.android.gms.location.places.GeoDataApi#getPlaceById(com.google.android.gms.common.api.GoogleApiClient,
	 *      String...)
	 */
	protected AdapterView.OnItemClickListener mAutocompleteClickListener = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			appStatus.checkLocation(getActivity());
			/*
			 * Retrieve the place ID of the selected item from the Adapter. The
			 * adapter stores each Place suggestion in a PlaceAutocomplete
			 * object from which we read the place ID.
			 */
			final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter
					.getItem(position);
			final String placeId = String.valueOf(item.placeId);

			/*
			 * Issue a request to the Places Geo Data API to retrieve a Place
			 * object with additional details about the place.
			 */
			PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
					.getPlaceById(googleApiClient, placeId);
			placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

			mAutocompleteView.setText(item.description);
			search();
			hideSoftKeyboard(getActivity());

			Log.i("GSP", "Called getPlaceById to get Place details for "
					+ item.placeId);
		}
	};

	protected AdapterView.OnItemClickListener autoOnClickPlaceSearch = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			appStatus.checkLocation(getActivity());
			final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter
					.getItem(position);
			final String placeId = String.valueOf(item.placeId);
			PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
					.getPlaceById(googleApiClient, placeId);
			placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

		}
	};

	/**
	 * Callback for results from a Places Geo Data API query that shows the
	 * first place result in the details view on screen.
	 */
	private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
		@Override
		public void onResult(PlaceBuffer places) {
			appStatus.checkLocation(getActivity());
			if (!places.getStatus().isSuccess()) {
				// Request did not complete successfully
				Log.e("GSP", "Place query did not complete. Error: "
						+ places.getStatus().toString());
				places.release();
				return;
			}
			// Get the Place object from the buffer.
			final Place place = places.get(0);

			// Display the third party attributions if set.
			final CharSequence thirdPartyAttribution = places.getAttributions();

			places.release();
		}
	};

	@Override
	public void updateUI() {
		if (mCurrentLocation != null) {
			appStatus.checkLocation(getActivity());
			lat = mCurrentLocation.getLatitude();
			lng = mCurrentLocation.getLongitude();

			if ((lat > ceilLat || lat < floorLat)
					|| (lng > ceilLng || lng < floorLng)) {
				coordinate = new LatLng(lat, lng);

				if (mMarker != null)
					mMarker.remove();
				mMap.clear();
				mMap.addMarker(new MarkerOptions()
						.position(new LatLng(lat, lng))
						.title("คุณ")
						.icon(BitmapDescriptorFactory
								.fromResource(R.drawable.marker_user)));
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						coordinate, 14));
				if (status.equals("1")) {
					markpoint();
				}
				if (status2.equals("1")) {
					placesearch(stFrom, stTo);
				}
				updateRange();
			}
		}
		super.updateUI();
	}

	public void updateRange() {
		ceilLat = Math.ceil(lat * 10000) / 10000;
		ceilLng = Math.ceil(lng * 10000) / 10000;
		floorLat = Math.floor(lat * 10000) / 10000;
		floorLng = Math.floor(lng * 10000) / 10000;
	}

	public void resetLatLng() {
		ceilLat = Double.MIN_VALUE;
		ceilLng = Double.MIN_VALUE;
		floorLat = Double.MAX_VALUE;
		floorLng = Double.MAX_VALUE;
	}

	public void search() {

		search = mAutocompleteView.getText().toString();

		if (search != null && !search.equals("")) {
			appStatus.checkLocation(getActivity());
			Geocoder geoc = new Geocoder(this.getActivity(),
					Locale.getDefault());
			try {
				Address addr = null;
				String addrText = null;
				List<Address> addresses = geoc.getFromLocationName(search, 1);
				if (addresses.size() > 0) {
					mMap.clear();
					for (int i = 0; i < addresses.size(); i++) {
						addr = (Address) addresses.get(i);
						phonenumber = addr.getPhone();
						point = new LatLng(addr.getLatitude(),
								addr.getLongitude());
						lat1 = addr.getLatitude();
						lng1 = addr.getLongitude();
						addrText = "";
						for (int j = 0; j < addr.getMaxAddressLineIndex(); j++)
							addrText += addr.getAddressLine(j) + " ";
					}
					mMap.addMarker(new MarkerOptions()
							.position(new LatLng(lat, lng))
							.title("คุณ")
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.marker_user)));
					mMap.addMarker(new MarkerOptions()
							.position(point)
							.title(addr.getAddressLine(0))
							.snippet(addrText)
							.icon(BitmapDescriptorFactory
									.fromResource(R.drawable.marker_destination)));

					latlngBuilder = new LatLngBounds.Builder();
					latlngBuilder.include(new LatLng(lat, lng));
					latlngBuilder.include(point);
					bounds = latlngBuilder.build();

					cu = CameraUpdateFactory.newLatLngBounds(
							bounds, 500);
					mMap.animateCamera(cu);

					mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
						@Override
						public boolean onMarkerClick(Marker mMarker) {
							if (!mMarker.getTitle().equals("คุณ")) {
								Log.d("mMarker getTitle", mMarker.getTitle());

								builder = new AlertDialog.Builder(
										new ContextThemeWrapper(getActivity(),
												R.style.AppTheme));
								@SuppressWarnings("static-access")
								LayoutInflater inflater2 = (LayoutInflater) getActivity()
										.getSystemService(
												getActivity().LAYOUT_INFLATER_SERVICE);
								View layout = inflater2.inflate(
										R.layout.dialog_gps, null);
								TextView title = (TextView) layout
										.findViewById(R.id.gpsDialog_title);
								TextView detail = (TextView) layout
										.findViewById(R.id.gpsDialog_detail);
								final TextView phone = (TextView) layout
										.findViewById(R.id.gpsDialog_phone);

								titlemark = mMarker.getTitle();
								snipmark = mMarker.getSnippet();

								title.setText(titlemark);
								detail.setText(snipmark);
								phone.setText(phonenumber);

								point = new LatLng(
										mMarker.getPosition().latitude, mMarker
												.getPosition().longitude);

								builder.setCancelable(true);
								builder.setPositiveButton("ไม่ต้องการ",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												dialog.cancel();
											}
										});
								builder.setNegativeButton("ต้องการ",
										new DialogInterface.OnClickListener() {
											public void onClick(
													DialogInterface dialog,
													int whichButton) {
												markpoint();
												status = "1";
												dialog.cancel();
											}
										});
								builder.setView(layout);
								builder.show();
							}
							return false;
						}
					});
				} else {
					Toast.makeText(getActivity(), "Not Found Location",
							Toast.LENGTH_SHORT).show();
					mAutocompleteView.setText("");
					status = "0";
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	protected void markpoint() {
		appStatus.checkLocation(getActivity());
		mMap.clear();
		mMap.addMarker(new MarkerOptions()
				.position(coordinate)
				.title("เธ�เธธเธ�")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.marker_user)));
		mMap.addMarker(new MarkerOptions()
				.position(point)
				.title(titlemark)
				.snippet(snipmark)
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.marker_destination)));
		GMapV2Direction md = new GMapV2Direction();
		Document doc = md.getDocument(coordinate, point,
				GMapV2Direction.MODE_DRIVING);
		ArrayList<LatLng> directionPoint = md.getDirection(doc);
		PolylineOptions rectLine = new PolylineOptions().width(14).color(
				Color.BLUE);

		for (int i = 0; i < directionPoint.size(); i++) {
			rectLine.add(directionPoint.get(i));
		}

		latlngBuilder = new LatLngBounds.Builder();
		latlngBuilder.include(coordinate);
		latlngBuilder.include(point);
		bounds = latlngBuilder.build();

		cu = CameraUpdateFactory.newLatLngBounds(bounds, 500);
		mMap.animateCamera(cu);

		mMap.setOnMarkerClickListener(new OnMarkerClickListener() {
			@Override
			public boolean onMarkerClick(Marker mMarker) {
				appStatus.checkLocation(getActivity());
				if (mMarker.getTitle() != null
						&& !mMarker.getTitle().equals("คุณ")) {

					builder = new AlertDialog.Builder(new ContextThemeWrapper(
							getActivity(), R.style.AppTheme));
					@SuppressWarnings("static-access")
					LayoutInflater inflater2 = (LayoutInflater) getActivity()
							.getSystemService(
									getActivity().LAYOUT_INFLATER_SERVICE);
					View layout = inflater2.inflate(R.layout.dialog_gps, null);

					TextView title = (TextView) layout
							.findViewById(R.id.gpsDialog_title);
					TextView detail = (TextView) layout
							.findViewById(R.id.gpsDialog_detail);
					final TextView phone = (TextView) layout
							.findViewById(R.id.gpsDialog_phone);
					title.setText(mMarker.getTitle());
					detail.setText(mMarker.getSnippet());
					phone.setText(phonenumber);
					// point = new LatLng(mMarker.getPosition().latitude,
					// mMarker
					// .getPosition().longitude);

					builder.setCancelable(true);
					builder.setNeutralButton("ตกลง",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									
									latlngBuilder = new LatLngBounds.Builder();
									latlngBuilder.include(coordinate);
									latlngBuilder.include(point);
									LatLngBounds bounds = latlngBuilder.build();

									cu = CameraUpdateFactory.newLatLngBounds(bounds, 500);
									mMap.animateCamera(cu);
									
									dialog.cancel();
								}
							});
					builder.setView(layout);
					builder.show();
				}
				return false;
			}
		});
		@SuppressWarnings("unused")
		Polyline polylin = mMap.addPolyline(rectLine);
	}

	public void placesearch(String search1, String search2) {
		mMap.clear();
		pointFrom = markDestination(search1);
		pointTo = markDestination(search2);
		GMapV2Direction md = new GMapV2Direction();
		try {
			Document doc = md.getDocument(pointFrom, pointTo,
					GMapV2Direction.MODE_DRIVING);
			ArrayList<LatLng> directionPoint = md.getDirection(doc);
			PolylineOptions rectLine = new PolylineOptions().width(14).color(
					Color.BLUE);
			for (int i = 0; i < directionPoint.size(); i++) {
				rectLine.add(directionPoint.get(i));
			}

			@SuppressWarnings("unused")
			Polyline polylin = mMap.addPolyline(rectLine);
			
			latlngBuilder = new LatLngBounds.Builder();
			latlngBuilder.include(pointFrom);
			latlngBuilder.include(pointTo);
			LatLngBounds bounds = latlngBuilder.build();

			cu = CameraUpdateFactory.newLatLngBounds(bounds, 500);
			mMap.animateCamera(cu);
			
		} catch (Exception e) {
			Log.e("Search Place", "NOT FOUND LOCATION");
		}
	}

	public LatLng markDestination(String search) {
		appStatus.checkLocation(getActivity());
		LatLng pointDes = null;
		Geocoder geoc = new Geocoder(this.getActivity(), Locale.getDefault());
		try {
			List<Address> addresses = geoc.getFromLocationName(search, 1);
			if (addresses.size() > 0) {
				for (int i = 0; i < addresses.size(); i++) {
					Address addr = (Address) addresses.get(i);
					pointDes = new LatLng(addr.getLatitude(),
							addr.getLongitude());
					lat1 = addr.getLatitude();
					lng1 = addr.getLongitude();
					String addrText = "";
					for (int j = 0; j < addr.getMaxAddressLineIndex(); j++)
						addrText += addr.getAddressLine(j) + "\n";
					mMap.addMarker(
							new MarkerOptions()
									.position(pointDes)
									.title(addr.getAddressLine(0))
									.snippet(addrText)
									.icon(BitmapDescriptorFactory
											.fromResource(R.drawable.marker_destination)))
							.showInfoWindow();
				}
			} else {
				Toast.makeText(getActivity(), "Not Found Location",
						Toast.LENGTH_SHORT).show();
				status2 = "0";
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return pointDes;
	}

	@Override
	public void onStart() {
		super.onStart();
		googleApiClient.connect();
	}

	@Override
	public void onStop() {
		googleApiClient.disconnect();
		super.onStop();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			SupportMapFragment fragment = (SupportMapFragment) getActivity()
					.getSupportFragmentManager().findFragmentById(R.id.map3);
			if (fragment != null)
				getFragmentManager().beginTransaction().remove(fragment)
						.commit();

		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public static void hideSoftKeyboard(Activity activity) {
		InputMethodManager inputMethodManager = (InputMethodManager) activity
				.getSystemService(Activity.INPUT_METHOD_SERVICE);
		inputMethodManager.hideSoftInputFromWindow(activity.getCurrentFocus()
				.getWindowToken(), 0);
	}

}