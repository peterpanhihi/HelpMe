package teeza.application.helpme.view.fragment;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.w3c.dom.Document;

import teeza.application.helpme.CallList_Activity;
import teeza.application.helpme.Login_Activity;
import teeza.application.helpme.R;
import teeza.application.helpme.adapter.CallCenter_Adapter;
import teeza.application.helpme.custom_view.ExpandableHeightListView;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.PhoneNumber;
import teeza.application.helpme.model.User;
import teeza.application.helpme.persistence.UserManager;
import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import app.akexorcist.gdaplibrary.GooglePlaceSearch;
import app.akexorcist.gdaplibrary.GooglePlaceSearch.OnPlaceResponseListener;
import app.akexorcist.gdaplibrary.PlaceType;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

@SuppressLint("NewApi")
public class Callcenter_Fragment extends GMap_Fragment {
	final String API_KEY = "AIzaSyBoC4w1t1mr8HE5Y_8IvsJg1ZOK8_k0cdY";
	final String[] others = new String[] { "แจ้งเหตุร้าย", "โรงพยาบาล",
			"เจ็บป่วยฉุกเฉิน", "สถานีดับเพลิง", "บริการแท็กซี่" };

	private LinearLayout liL, liR;
	private int radius = 1000;
	private String title, snippet, idcus;
	private LatLng coordinate;
	private double lat, lng, ceilLat, ceilLng, floorLat, floorLng;
	private GooglePlaceSearch gp;
	private View rootView;
	private String language = "en";
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
	private UserManager mManager;
	private User user = new User();

	private OKHttp okHttp;
	private ApplicationStatus appStatus;

	private RelativeLayout progressBar;
	private ExpandableHeightListView listView_callNear;
	private ExpandableHeightListView listView_others;
	private ArrayList<PhoneNumber> numbers;
	private CallCenter_Adapter cnAdapter;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		mManager = new UserManager(getActivity());
		okHttp = new OKHttp();
		idcus = mManager.getID();
		appStatus = ApplicationStatus.getInstance();
		appStatus.checkLocation(getActivity());
		
		ceilLat = Double.MIN_VALUE;
		ceilLng = Double.MIN_VALUE;
		floorLat = Double.MAX_VALUE;
		floorLng = Double.MAX_VALUE;
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.activity_call, container, false);
		if (appStatus.isOnline(getActivity())) {
			gp = new GooglePlaceSearch(API_KEY);
			declareVariable();
			new Task().execute();
		} else
			appStatus.setNetwork(getActivity());

		return rootView;
	}

	public void declareVariable() {
		liL = (LinearLayout) rootView.findViewById(R.id.btncallL);
		liL.setOnClickListener(OnClickCallLeft);

		liR = (LinearLayout) rootView.findViewById(R.id.btncallR);
		liR.setOnClickListener(OnClickCallRight);

		progressBar = (RelativeLayout) rootView
				.findViewById(R.id.relative_progressBar);
		listView_callNear = (ExpandableHeightListView) rootView
				.findViewById(R.id.list_callnear);

		numbers = new ArrayList<PhoneNumber>();
		cnAdapter = new CallCenter_Adapter(getActivity(), numbers);
		listView_callNear.setAdapter(cnAdapter);

		listView_callNear.setClickable(true);
		listView_callNear
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						String phoneNumber = numbers.get(position).getNumber();
						String number = "tel:" + phoneNumber.trim();
						Intent callIntent = new Intent(Intent.ACTION_CALL, Uri.parse(number));
						if (!appStatus.isCall()) {
							startActivity(callIntent);
							appStatus.setIsCall(true);
						}
					}
				});

		listView_others = (ExpandableHeightListView) rootView
				.findViewById(R.id.list_others);
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.callcenterlist_other, R.id.title, others);
		listView_others.setAdapter(adapter);
		listView_others
				.setOnItemClickListener(new AdapterView.OnItemClickListener() {

					@Override
					public void onItemClick(AdapterView<?> arg0, View arg1,
							int position, long arg3) {
						Intent intent = new Intent(getActivity(),
								CallList_Activity.class);
						intent.putExtra("Key", position + 1 + "");
						startActivity(intent);
					}

				});
		listView_others.setExpanded(true);
	}

	private class MyPhoneListener extends PhoneStateListener {

		private boolean onCall = false;

		@Override
		public void onCallStateChanged(int state, String incomingNumber) {

			switch (state) {
			case TelephonyManager.CALL_STATE_RINGING:
				// phone ringing...
				// Toast.makeText(getActivity(), incomingNumber + " calls you",
				// Toast.LENGTH_LONG).show();
				break;

			case TelephonyManager.CALL_STATE_OFFHOOK:
				// one call exists that is dialing, active, or on hold
				// Toast.makeText(getActivity(), "on call...",
				// Toast.LENGTH_LONG).show();
				// because user answers the incoming call

				// Gps Save data
				lat = mCurrentLocation.getLatitude();
				lng = mCurrentLocation.getLongitude();
				sendToHost("call");

				Log.e("addess", getAddress(lat, lng) + "^^" + lat + "^^" + lng);
				sendToHost("call");

				onCall = true;
				break;

			case TelephonyManager.CALL_STATE_IDLE:
				// in initialization of the class and at the end of phone call

				// detect flag from CALL_STATE_OFFHOOK
				if (onCall == true) {
					// Toast.makeText(getActivity(), "restart app after call",
					// Toast.LENGTH_LONG).show();

					if (appStatus.isOnline(getActivity())) {
						sendToHost("stop");
					}
					onCall = false;
				}
				break;
			default:
				break;
			}

		}
	}

	public void sendToHost(String status) {
		user = mManager.getuser();
		String lati = String.valueOf(lat);
		String longti = String.valueOf(lng);

		try {
			RequestBody formBody = new FormEncodingBuilder()
					.add("customer_id", idcus)
					.add("name", user.getname().toString())
					.add("address", getAddress(lat, lng))
					.add("phone", user.getphone().toString())
					.add("status", status).add("lati", lati)
					.add("longti", longti).build();
			okHttp.POST(Login_Activity.nameHost + "insert_call.php", formBody);
		} catch (IOException e) {
			e.printStackTrace();
		}
		StrictMode.setThreadPolicy(policy);
	}

	
	@Override
	public void updateUI() {
		if (mCurrentLocation != null) {
			if((lat > ceilLat || lat < floorLat) || (lng > ceilLng || lng < floorLng)) {
				numbers.clear();
				appStatus.checkLocation(getActivity());
				lat = mCurrentLocation.getLatitude();
				lng = mCurrentLocation.getLongitude();
				coordinate = new LatLng(lat,lng);

				searchPlace(PlaceType.HOSPITAL);
				searchPlace(PlaceType.POLICE);
				updateRange();
			}
		}
		super.updateUI();
	}
	
	public void updateRange(){
		ceilLat = Math.ceil(lat*10000)/10000;
		ceilLng = Math.ceil(lng*10000)/10000;
		floorLat = Math.floor(lat*10000)/10000;
		floorLng = Math.floor(lng*10000)/10000;
	}

	public void searchPlace(String type) {
		gp.setLogging(true);
		gp.getNearby(lat, lng, radius, type, language);
		gp.setOnPlaceResponseListener(new OnPlaceResponseListener() {
			public void onResponse(String status,
					ArrayList<ContentValues> arr_data, Document doc) {
				if (status.equals(GooglePlaceSearch.STATUS_OK)) {
					int size = 0, i = 0;
					while (size < 3 && i < arr_data.size()) {
						title = arr_data.get(i).getAsString(
								GooglePlaceSearch.PLACE_NAME);
						snippet = arr_data.get(i).getAsString(
								GooglePlaceSearch.PLACE_PHONENUMBER);
						if (title != null && snippet != null) {
							if (snippet.toLowerCase().equals("unknown")) {
								i++;
							} else {
								numbers.add(new PhoneNumber(title, snippet));
								size++;
								i++;
							}
						}
					}
					progressBar.setVisibility(View.GONE);
					listView_callNear.setVisibility(View.VISIBLE);
					listView_callNear.setExpanded(true);
					cnAdapter.notifyDataSetChanged();
				}
			}
		});
	}

	// get address
	private String getAddress(double latitude, double longitude) {
		StringBuilder result = new StringBuilder();
		try {
			Geocoder geocoder = new Geocoder(getActivity(), Locale.getDefault());
			List<Address> addresses = geocoder.getFromLocation(latitude,
					longitude, 1);
			if (addresses.size() > 0) {
				Address address = addresses.get(0);
				result.append(address.getAddressLine(0)).append("\n");
				result.append(address.getLocality()).append("\n");
				result.append(address.getAdminArea()).append("\n");
				result.append(address.getCountryName()).append("\n");
				result.append(address.getCountryCode()).append("\n");
				result.append(address.getPostalCode()).append("\n");
			}
		} catch (IOException e) {
			Log.e("tag", e.getMessage());
		}

		return result.toString();
	}

	private OnClickListener OnClickCallLeft = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final String callNumber = "0851526447";// "1736";
			String number = "tel:" + callNumber.trim();
			Intent callIntent = new Intent(Intent.ACTION_CALL,
					Uri.parse(number));
			if (!appStatus.isCall()) {
				startActivity(callIntent);
				appStatus.setIsCall(true);
			}

			// add PhoneStateListener for monitoring
			MyPhoneListener phoneListener = new MyPhoneListener();
			TelephonyManager telephonyManager = (TelephonyManager) getActivity()
					.getSystemService(Context.TELEPHONY_SERVICE);
			// receive notifications of telephony state changes
			telephonyManager.listen(phoneListener,
					PhoneStateListener.LISTEN_CALL_STATE);
			if (appStatus.isOnline(getActivity())) {
				sendToHost("wait");
			}
		}
	};

	private OnClickListener OnClickCallRight = new OnClickListener() {

		@Override
		public void onClick(View v) {
			final String callNumber = "191";
			String number = "tel:" + callNumber.trim();
			Intent callIntent = new Intent(Intent.ACTION_CALL,
					Uri.parse(number));
			if (!appStatus.isCall()) {
				startActivity(callIntent);
				appStatus.setIsCall(true);
			}
		}
	};

	class Task extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			progressBar.setVisibility(View.VISIBLE);
			listView_callNear.setVisibility(View.GONE);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				appStatus.checkLocation(getActivity());
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

	}

}
