package teeza.application.helpme.view.fragment;

import java.util.ArrayList;

import org.json.JSONObject;
import org.w3c.dom.Document;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.Login_Activity;
import teeza.application.helpme.R;
import teeza.application.helpme.UploadQueueHelpme_Activity;
import teeza.application.helpme.http.GMapV2Direction;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.persistence.UserManager;

public class Main_Fragment extends GMap_Fragment {
	private View rootView;
	private GoogleMap mMap;
	private Marker mMarker, mMarker2;
	private LatLng coordinate;
	private double lat, lng, lat2, lng2, ceilLat, ceilLng, floorLat, floorLng;
	private double claimer_id;
	private boolean zoom;
	private UserManager mManager;
	private String idcus;
	private String status = "rd";
	private boolean cancel[] = new boolean[2];
	private AlertDialog.Builder builder;
	private ApplicationStatus appStatus;
	private OKHttp okHttp;
	private StrictMode.ThreadPolicy policy;
	private boolean isClick, isClaimer, cancelClaimer;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManager = new UserManager(getActivity());
		idcus = mManager.getID().toString();
		okHttp = new OKHttp();
		appStatus = ApplicationStatus.getInstance();
		policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();

		ceilLat = Double.MIN_VALUE;
		ceilLng = Double.MIN_VALUE;
		floorLat = Double.MAX_VALUE;
		floorLng = Double.MAX_VALUE;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.mainlay, container, false);
		if (appStatus.isOnline(getActivity())) {
			if (mMap == null)
				mMap = ((SupportMapFragment) getFragmentManager()
						.findFragmentById(R.id.map)).getMap();
			mMap.getUiSettings().setMapToolbarEnabled(false);
			mMap.getUiSettings().setZoomControlsEnabled(false);
		} else {
			appStatus.setNetwork(getActivity());
		}
		return rootView;
	}

	/**
	 * Updates the latitude, the longitude, and the last location time in the
	 * UI.
	 */
	@Override
	public void updateUI() {
		super.updateUI();
		if (mCurrentLocation != null) {

			appStatus.checkLocation(getActivity());
			lat = mCurrentLocation.getLatitude();
			lng = mCurrentLocation.getLongitude();

			Log.d("Location", "lat: " + lat + " lng: " + lng);
			Log.d("Location Ceil", "ceilLat: " + ceilLat + " floorLat"
					+ floorLat);

			if(cancelClaimer) {
				mMap.clear();
				mMarker = mMap.addMarker(new MarkerOptions().position(
						new LatLng(lat, lng)).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.marker_user)));
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						coordinate, 16));
				cancelClaimer = false;
			} else if ((lat > ceilLat || lat < floorLat)
					|| (lng > ceilLng || lng < floorLng) || getClaim()) {
				coordinate = new LatLng(lat, lng);
				if (mMarker != null)
					mMarker.remove();

				if (mManager.getStatsend().equals("1")) {
					MarkClaimer();
					isClaimer = true;
				} else {
					mMap.clear();
					mMarker = mMap.addMarker(new MarkerOptions().position(
							new LatLng(lat, lng)).icon(
							BitmapDescriptorFactory
									.fromResource(R.drawable.marker_user)));
					isClaimer = false;
				}
				if (!zoom) {
					mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
							coordinate, 16));
					zoom = true;
				}
				mMap.setOnMarkerClickListener(new OnMarkerClickListener() {

					@Override
					public boolean onMarkerClick(Marker arg0) {
						if (!isClick) {
							isClick = true;
							if (mManager.getStatsend().equals("0"))
								Helpme();
							else
								Claimed();
						}
						return false;
					}
				});
				updateRange();
			}

		}
	}

	public void updateRange() {
		ceilLat = Math.ceil(lat * 10000) / 10000;
		ceilLng = Math.ceil(lng * 10000) / 10000;
		floorLat = Math.floor(lat * 10000) / 10000;
		floorLng = Math.floor(lng * 10000) / 10000;
	}

	public void Helpme() {
		builder = new AlertDialog.Builder(new ContextThemeWrapper(
				getActivity(), R.style.AppTheme));
		@SuppressWarnings("static-access")
		LayoutInflater inflater2 = (LayoutInflater) getActivity()
				.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
		View layout = inflater2.inflate(R.layout.dialog_layout1, null);
		builder.setCancelable(true);
		builder.setPositiveButton("ไม่ต้องการ",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						isClick = false;
						dialog.cancel();
					}
				});
		builder.setNegativeButton("ใช่", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// push statement
				Intent helpme = new Intent(getActivity(),
						UploadQueueHelpme_Activity.class);
				helpme.putExtra("lati", lat);
				helpme.putExtra("long", lng);
				helpme.putExtra("isInPage", "true");
				getActivity().startActivityForResult(helpme, 1);
				isClick = false;
				dialog.cancel();
			}
		});
		AlertDialog alert11 = builder.create();
		alert11.setView(layout);
		alert11.show();
	}

	public void Claimed() {
		builder = new AlertDialog.Builder(new ContextThemeWrapper(
				getActivity(), R.style.AppTheme));
		@SuppressWarnings("static-access")
		LayoutInflater inflater2 = (LayoutInflater) getActivity()
				.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
		View layout = inflater2.inflate(R.layout.dialog_layout2, null);
		builder.setCancelable(true);
		builder.setPositiveButton("ไม่ใช่",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						isClick = false;
						dialog.cancel();
					}
				});
		builder.setNegativeButton("ใช่", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int id) {
				// push statement
				send();
				mManager.setStatsend("0");
				status = "rd";
				mMap.clear();
				cancel[0] = false;
				cancel[1] = false;
				mMarker = mMap.addMarker(new MarkerOptions().position(
						new LatLng(lat, lng)).icon(
						BitmapDescriptorFactory
								.fromResource(R.drawable.marker_user)));
				updateUI();
				mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
						coordinate, 16));
				isClick = false;
				dialog.cancel();
			}
		});
		AlertDialog alert11 = builder.create();
		alert11.setView(layout);
		alert11.show();
	}

	public void MarkClaimer() {
		mMap.clear();
		if (cancel[0] == true && cancel[1] == false) {
			builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("");
			builder.setMessage("�ô�͡�ô��׹�ҹ �ѡ����");
			builder.setCancelable(true);
			builder.setNeutralButton("�Ѻ��Һ",
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int id) {
							dialog.cancel();
						}
					});
			AlertDialog alert11 = builder.create();
			alert11.show();
		}
		if (cancel[1]) {
			appStatus.checkLocation(getActivity());
			mMarker = mMap.addMarker(new MarkerOptions().position(
					new LatLng(lat, lng)).icon(
					BitmapDescriptorFactory
							.fromResource(R.drawable.marker_user)));
			mMarker2 = mMap.addMarker(new MarkerOptions().position(
					new LatLng(lat2, lng2)).icon(
					BitmapDescriptorFactory
							.fromResource(R.drawable.marker_claimer)));

			GMapV2Direction md = new GMapV2Direction();
			Document doc = md.getDocument((new LatLng(lat2, lng2)),
					(new LatLng(lat, lng)), GMapV2Direction.MODE_DRIVING);
			ArrayList<LatLng> directionPoint = md.getDirection(doc);
			PolylineOptions rectLine = new PolylineOptions().width(8).color(
					Color.BLUE);
			for (int i = 0; i < directionPoint.size(); i++) {
				rectLine.add(directionPoint.get(i));
			}

			@SuppressWarnings("unused")
			Polyline polylin = this.mMap.addPolyline(rectLine);

			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			builder.include(mMarker.getPosition());
			builder.include(mMarker2.getPosition());
			LatLngBounds bounds = builder.build();

			CameraUpdate camera = CameraUpdateFactory.newLatLngBounds(bounds,
					400);
			mMap.animateCamera(camera);
		} else {
			mMarker = mMap.addMarker(new MarkerOptions().position(
					new LatLng(lat, lng)).icon(
					BitmapDescriptorFactory
							.fromResource(R.drawable.marker_user)));
		}
	}

	protected boolean getClaim() {
		cancel[0] = cancel[1];

		RequestBody formBody = new FormEncodingBuilder().add("customer_id",
				idcus).build();

		StrictMode.setThreadPolicy(policy);

		try {
			String result = okHttp.POST(Login_Activity.nameHost + "routing.php", formBody);
			Log.i("RESPONSE", result);
			JSONObject json_data = new JSONObject(result);
			lat2 = (json_data.getDouble("lati"));
			lng2 = (json_data.getDouble("longi"));
			claimer_id = (json_data.getDouble("claimer_id"));

			if (lat2 != 0 && lng2 != 0) {
				Log.e("Claimer", "Get Loacation Success");
				Log.e("Claimer", "Get Loacation Latitude = " + lat2
						+ "Longtitude = " + lng2);
				mManager.setStatsend("1");
			} else {
				Log.e("Claimer", "Get Loacation Fail");
			}
		} catch (Exception e) {
			Log.i("GET CLAIM", "NO CLAIMER");
			if (isClaimer) {
				isClaimer = false;
				mManager.setStatsend("0");
				cancelClaimer = true;
				Log.i("JUST CANCEL", "1," + isClaimer);
				updateUI();
			} else {
				cancelClaimer = false;
			}
			cancel[1] = false;
			return false;
		}
		cancel[1] = true;
		return true;
	}

	void send() {

		String str_claimer_id = Double.toString(claimer_id);
		RequestBody formBody = new FormEncodingBuilder()
				.add("customer_id", idcus).add("Status", "fin")
				.add("claimer_id", str_claimer_id).build();
		StrictMode.setThreadPolicy(policy);

		try {
			JSONObject json_data = new JSONObject(okHttp.POST(
					Login_Activity.nameHost + "insert.php", formBody));
			String code1 = (json_data.getString("code"));
			if (code1.equals("1")) {
				Log.e("Insert", "Inserted Successfully");
			} else {
				Log.e("Insert", "Sorry Try Again");

			}
		} catch (Exception e) {
			Log.e("Fail 3", e.toString());
		}
	}

	public void onDestroyView() {
		super.onDestroyView();
		try {
			SupportMapFragment fragment = (SupportMapFragment) getActivity()
					.getSupportFragmentManager().findFragmentById(R.id.map);
			if (fragment != null)
				getFragmentManager().beginTransaction().remove(fragment)
						.commit();
		} catch (IllegalStateException e) {
			e.printStackTrace();
		}
	}
}
