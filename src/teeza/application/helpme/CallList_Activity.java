package teeza.application.helpme;

import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Document;

import teeza.application.helpme.R;
import teeza.application.helpme.adapter.CallCenter_Adapter;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.PhoneNumber;
import com.google.android.gms.maps.model.LatLng;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemClickListener;
import app.akexorcist.gdaplibrary.GooglePlaceSearch;
import app.akexorcist.gdaplibrary.PlaceType;
import app.akexorcist.gdaplibrary.GooglePlaceSearch.OnPlaceResponseListener;

@SuppressLint("NewApi")
public class CallList_Activity extends GMap_Activity {
	final String API_KEY = "AIzaSyBoC4w1t1mr8HE5Y_8IvsJg1ZOK8_k0cdY";

	private List<PhoneNumber> numbers;

	private int radius = 20000;
	private LatLng coordinate;
	private double lat, lng;
	private String type, key, phoneTitle, phoneNum;
	private GooglePlaceSearch gp;
	private String language = "th";
	private LocationListener listener;
	private Button back;
	private CallCenter_Adapter adapter;
	private ListView listViewData;
	private TextView title;
	private RelativeLayout progressBar;
	private ApplicationStatus appStatus;
	private boolean isCancel;
	private ArrayList<ContentValues> datas;

	

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.list_layout);
		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();
		
		gp = new GooglePlaceSearch(API_KEY);
		
		key = getIntent().getStringExtra("Key");
		
		numbers = new ArrayList<PhoneNumber>();
		adapter = new CallCenter_Adapter(this, numbers);
		listViewData = (ListView) findViewById(R.id.listView1);
		listViewData.setAdapter(adapter);
		listViewData
				.setOnItemClickListener(new OnItemClickListener() {
					public void onItemClick(AdapterView<?> parent,
							View view, int position, long id) {
						final String callNumber = numbers.get(
								position).getNumber();
						String number = "tel:" + callNumber.trim();
						Intent callIntent = new Intent(
								Intent.ACTION_CALL, Uri
										.parse(number));
						if (!appStatus.isCall()) {
							startActivityForResult(callIntent, 2);
							appStatus.setIsCall(true);
						}
					}
				});
		back = (Button) findViewById(R.id.button1);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isCancel = true;
				finish();
			}
		});
		title = (TextView) findViewById(R.id.title);
		progressBar = (RelativeLayout) findViewById(R.id.relative_progressBar);
		
		super.onCreate(savedInstanceState);
	}

	public void Location() {
		appStatus.checkLocation(this);
		numbers.clear();

		switch (key) {
		case "1":
			type = PlaceType.POLICE;
			title.setText("แจ้งเหตุร้าย");
			break;
		case "2":
			type = PlaceType.HOSPITAL;
			title.setText("โรงพยาบาล");
			break;
		case "3":
			type = PlaceType.DOCTOR;
			title.setText("เจ็บป่วยฉุกเฉิน");
			break;
		case "4":
			type = PlaceType.FIRE_STATION;
			title.setText("สถานีดับเพลิง");
			break;
		case "5":
			type = PlaceType.TAXI_STAND;
			title.setText("บริการแท็กซี่");
			break;
		default:
			break;
		}

		gp.setLogging(true);
		gp.setOnPlaceResponseListener(new OnPlaceResponseListener() {
			public void onResponse(String status,
					ArrayList<ContentValues> arr_data, Document doc) {

				Toast.makeText(getApplicationContext(), status,
						Toast.LENGTH_LONG).show();

				if (status.equals(GooglePlaceSearch.STATUS_OK)) {
					datas = arr_data;
				}
			}
		});
		gp.getNearby(lat, lng, radius, type, language);

	}

	

	@Override
	public void updateUI() {
		if(mCurrentLocation != null) {
			lat = mCurrentLocation.getLatitude();
			lng = mCurrentLocation.getLongitude();
			coordinate = new LatLng(lat, lng);
			Location();
			new Task().execute();
		}
		super.updateUI();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
				Location();
				new Task().execute();
			}
		} else if (requestCode == 2) {
			appStatus.setIsCall(false);
			appStatus.setIsInPage(true);
		}
	}

	

	@Override
	public void onResume() {
		if (appStatus.checkStatus() && !appStatus.isCall()) {
			Intent intent = new Intent(getApplicationContext(),
					Pin_Activity.class);
			startActivityForResult(intent, 1);
		}
		appStatus.checkLocation(this);
		super.onResume();
	}

	

	@Override
	protected void onPause() {
		if (!isCancel) {
			appStatus.onPause();
		}
		super.onPause();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		isCancel = true;
	}

	class Task extends AsyncTask<String, Integer, Boolean> {

		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
			listViewData.setVisibility(View.GONE);
			super.onPreExecute();
		}

		@Override
		protected void onPostExecute(Boolean result) {
			progressBar.setVisibility(View.GONE);
			listViewData.setVisibility(View.VISIBLE);
			if(datas != null) {
				for (int i = 0; i < datas.size(); i++) {
					phoneTitle = datas.get(i).getAsString(
							GooglePlaceSearch.PLACE_NAME);
					phoneNum = datas.get(i).getAsString(
							GooglePlaceSearch.PLACE_PHONENUMBER);

					if (phoneTitle != null && phoneNum != null
							&& !phoneNum.toLowerCase().equals("unknown"))
						numbers.add(new PhoneNumber(phoneTitle, phoneNum));
				}
			}
			adapter.notifyDataSetChanged();
			super.onPostExecute(result);
		}

		@Override
		protected Boolean doInBackground(String... params) {
			try {
				Thread.sleep(3000);
			} catch (Exception e) {
				e.printStackTrace();
			}
			return null;
		}

	}
}
