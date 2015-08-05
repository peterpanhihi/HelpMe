package teeza.application.helpme.view.fragment;

import java.util.ArrayList;
import java.util.HashMap;

import org.w3c.dom.Document;

import teeza.application.helpme.R;
import teeza.application.helpme.http.GMapV2Direction;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.ArrayMarker;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import app.akexorcist.gdaplibrary.GooglePlaceSearch;
import app.akexorcist.gdaplibrary.GooglePlaceSearch.OnBitmapResponseListener;
import app.akexorcist.gdaplibrary.PlaceType;
import app.akexorcist.gdaplibrary.GooglePlaceSearch.OnPlaceResponseListener;

public class SearchPlace_Fragment extends GMap_Fragment {
	// using a browser key,server key <- Don't change
	public final String API_KEY = "AIzaSyBoC4w1t1mr8HE5Y_8IvsJg1ZOK8_k0cdY";

	private GooglePlaceSearch gp = new GooglePlaceSearch(API_KEY);
	private GoogleMap mMap;
	private Marker mMarker;
	private double lat, lng, lat2, lng2, last_lat, last_lng, ceilLat, ceilLng,
			floorLat, floorLng;
	private LatLng point, pos, coordinate;
	private AlertDialog.Builder builder;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
	private int radius = 800;
	private String language = "en";
	private View rootView;
	private ArrayAdapter<String> StrNameAdap1;
	private Spinner spin;
	private ArrayMarker arrayMarker;
	private int status;
	private int statusmark;
	private ArrayList<ArrayMarker> Listmarker;
	private String markTitle, markPhone;
	private ApplicationStatus appStatus;
	private HashMap<String, Integer> markerID;
	private LatLngBounds.Builder latlngBuilder;
	private LatLngBounds bounds;
	private CameraUpdate cu;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		setHasOptionsMenu(true);
		markerID = new HashMap();

		ceilLat = Double.MIN_VALUE;
		ceilLng = Double.MIN_VALUE;
		floorLat = Double.MAX_VALUE;
		floorLng = Double.MAX_VALUE;

		StrictMode.setThreadPolicy(policy);
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.search_place, container, false);

		appStatus = ApplicationStatus.getInstance();
		appStatus.checkLocation(getActivity());

		if (appStatus.isOnline(getActivity())) {
			if (mMap == null)
				mMap = ((SupportMapFragment) getFragmentManager()
						.findFragmentById(R.id.map2)).getMap();
			mMap.getUiSettings().setMapToolbarEnabled(false);
			mMap.getUiSettings().setZoomControlsEnabled(false);
			search();
		} else
			appStatus.setNetwork(getActivity());
		return rootView;
	}

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

				if (status == 1)
					markPlace(PlaceType.LODGING, R.drawable.marker_hostel);
				else if (status == 2)
					markPlace(PlaceType.RESTAURANT,
							R.drawable.marker_restaurant);
				else if (status == 3)
					markPlace(PlaceType.GAS_STATION, R.drawable.marker_gasoline);
				else if (statusmark == 4)
					markLinePlace(R.drawable.marker_hostel);
				else if (statusmark == 5)
					markLinePlace(R.drawable.marker_restaurant);
				else if (statusmark == 6)
					markLinePlace(R.drawable.marker_gasoline);
				else {
					mMarker = mMap.addMarker(new MarkerOptions()
							.position(new LatLng(lat, lng))
							.title("คุณ")
							.icon(BitmapDescriptorFactory.fromResource(R.drawable.marker_user)));
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(coordinate, 14));
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

	private void search() {
		final ArrayList<String> types = new ArrayList<String>();
		spin = (Spinner) rootView.findViewById(R.id.spinner1);
		StrNameAdap1 = new ArrayAdapter<String>(getActivity(),
				R.layout.spiner_layout, R.id.textspin, types);
		types.add("---เลือกสถานที่---");
		types.add("โรงแรมและที่พัก");
		types.add("ร้านอาหาร");
		types.add("ปั้มน้ำมัน");
		spin.setAdapter(StrNameAdap1);
		spin.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> arg0, View arg1,
					int arg2, long arg3) {
				mMap.clear();
				switch (arg2) {
				case 1:
					markPlace(PlaceType.LODGING, R.drawable.marker_hostel);
					status = 1;
					break;
				case 2:
					markPlace(PlaceType.RESTAURANT,
							R.drawable.marker_restaurant);
					status = 2;
					break;
				case 3:
					markPlace(PlaceType.GAS_STATION, R.drawable.marker_gasoline);
					status = 3;
					break;
				default:
					break;
				}
				updateUI();
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				Toast.makeText(getActivity(), "Please Select new",
						Toast.LENGTH_LONG).show();
			}
		});
	}

	public void markPlace(String type, final int resID) {
		appStatus.checkLocation(getActivity());
		Listmarker = new ArrayList<ArrayMarker>();
		latlngBuilder = new LatLngBounds.Builder();
		mMap.clear();
		mMarker = mMap.addMarker(new MarkerOptions()
				.position(new LatLng(lat, lng))
				.title("คุณ")
				.icon(BitmapDescriptorFactory
						.fromResource(R.drawable.marker_user)));
		markerID.put(mMarker.getTitle(), Integer.MAX_VALUE);
		mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat, lng),
				15));
		latlngBuilder.include(new LatLng(lat, lng));
		
		gp.setLogging(true);
		gp.setOnPlaceResponseListener(new OnPlaceResponseListener() {
			public void onResponse(String status,
					ArrayList<ContentValues> arr_data, Document doc) {

				Toast.makeText(getActivity(), status, Toast.LENGTH_LONG).show();

				if (status.equals(GooglePlaceSearch.STATUS_OK)) {
					for (int i = 0; i < arr_data.size(); i++) {
						arrayMarker = new ArrayMarker();
						String title = arr_data.get(i).getAsString(
								GooglePlaceSearch.PLACE_NAME);
						markerID.put(title, i);
						arrayMarker.setname(title);
						arrayMarker.setphone(arr_data.get(i).getAsString(
								GooglePlaceSearch.PLACE_PHONENUMBER));
						arrayMarker.setpic(arr_data.get(i).getAsString(
								GooglePlaceSearch.PLACE_PHOTO));
						arrayMarker.setaddress(arr_data.get(i).getAsString(
								GooglePlaceSearch.PLACE_ADDRESS));
						lat2 = arr_data.get(i).getAsDouble(
								GooglePlaceSearch.PLACE_LATITUDE);
						lng2 = arr_data.get(i).getAsDouble(
								GooglePlaceSearch.PLACE_LONGITUDE);
						arrayMarker.setlati(lat2);
						arrayMarker.setlongi(lng2);
						pos = new LatLng(lat2, lng2);
						mMap.addMarker(new MarkerOptions()
								.position(pos)
								.title(arrayMarker.getname())
								.snippet(arrayMarker.getaddress())
								.icon(BitmapDescriptorFactory
										.fromResource(resID)));
						latlngBuilder.include(pos);
						Listmarker.add(arrayMarker);
					}
					
					bounds = latlngBuilder.build();

					cu = CameraUpdateFactory.newLatLngBounds(bounds, 200);
					mMap.animateCamera(cu);

					mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {

						@Override
						public void onInfoWindowClick(Marker mMarker) {

							try {
								int indexmaker = markerID.get(mMarker
										.getTitle());
								if (indexmaker != markerID.get("คุณ")) {
									builder = new AlertDialog.Builder(
											new ContextThemeWrapper(
													getActivity(),
													R.style.AppTheme));
									@SuppressWarnings("static-access")
									LayoutInflater inflater = (LayoutInflater) getActivity()
											.getSystemService(
													getActivity().LAYOUT_INFLATER_SERVICE);
									View layout = inflater.inflate(
											R.layout.dialog_searchplace, null);
									TextView title = (TextView) layout
											.findViewById(R.id.Title);
									TextView detail = (TextView) layout
											.findViewById(R.id.Detail);
									TextView phone = (TextView) layout
											.findViewById(R.id.Phone);
									final ImageView pic = (ImageView) layout
											.findViewById(R.id.picplace);

									markTitle = Listmarker.get(indexmaker)
											.getname();
									markPhone = Listmarker.get(indexmaker)
											.getphone();

									title.setText(markTitle);
									detail.setText(Listmarker.get(indexmaker)
											.getaddress());
									phone.setText(markPhone);
									String ref = Listmarker.get(indexmaker)
											.getpic();
									gp.getPhotoBitmapByWidth(ref, 300,
											"First Place Photo",
											new OnBitmapResponseListener() {
												public void onResponse(
														Bitmap bm, String tag) {
													// Do something
													pic.setImageBitmap(bm);
												}
											});
									point = new LatLng(
											mMarker.getPosition().latitude,
											mMarker.getPosition().longitude);
									builder.setView(layout);
									builder.setCancelable(true);
									builder.setPositiveButton(
											"ยกเลิก",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													dialog.cancel();
												}
											});
									builder.setNegativeButton(
											"เส้นทาง",
											new DialogInterface.OnClickListener() {
												public void onClick(
														DialogInterface dialog,
														int whichButton) {
													markLinePlace(resID);
												}
											});
									builder.show();
								}
							} catch (Exception e) {
								Log.e("Marker Error", "Marker Place");
							}

						}
					});
				}
			}
		});
		if (last_lat != lat || last_lng != lng)
			gp.getNearby(lat, lng, radius, type, language);
	}

	public void markLinePlace(int resID) {
		appStatus.checkLocation(getActivity());
		setStatus(resID);
		mMap.clear();
		mMap.addMarker(new MarkerOptions().position(coordinate).icon(
				BitmapDescriptorFactory.fromResource(R.drawable.marker_user)));
		mMap.addMarker(new MarkerOptions().position(point).title(markTitle)
				.snippet(markPhone)
				.icon(BitmapDescriptorFactory.fromResource(resID)));
		
		GMapV2Direction md = new GMapV2Direction();
		Document doc = md.getDocument(coordinate, point,
				GMapV2Direction.MODE_DRIVING);
		ArrayList<LatLng> directionPoint = md.getDirection(doc);
		PolylineOptions rectLine = new PolylineOptions().width(8).color(
				Color.BLUE);
		for (int i = 0; i < directionPoint.size(); i++) {
			rectLine.add(directionPoint.get(i));
		}
		@SuppressWarnings("unused")
		Polyline polylin = mMap.addPolyline(rectLine);

		latlngBuilder = new LatLngBounds.Builder();
		latlngBuilder.include(coordinate);
		latlngBuilder.include(point);
		bounds = latlngBuilder.build();

		cu = CameraUpdateFactory.newLatLngBounds(
				bounds, 400);
		mMap.animateCamera(cu);
		
		if (!markPhone.equals("Unknown")) {
			mMap.setOnInfoWindowClickListener(new OnInfoWindowClickListener() {
				@Override
				public void onInfoWindowClick(Marker arg0) {
					String number = "tel:" + markPhone.trim();
					Intent callIntent = new Intent(Intent.ACTION_CALL, Uri
							.parse(number));
					appStatus.setIsCall(true);
					startActivity(callIntent);
				}
			});
		}
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		try {
			SupportMapFragment fragment = (SupportMapFragment) getActivity()
					.getSupportFragmentManager().findFragmentById(R.id.map2);
			if (fragment != null)
				getFragmentManager().beginTransaction().remove(fragment)
						.commit();

		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}

	public void setStatus(int resID) {
		if (resID == R.drawable.marker_hostel)
			status = 4;
		else if (resID == R.drawable.marker_restaurant)
			status = 5;
		else if (resID == R.drawable.marker_gasoline)
			status = 6;
	}
}