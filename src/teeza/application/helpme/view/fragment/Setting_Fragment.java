package teeza.application.helpme.view.fragment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.ChangePass_Activity;
import teeza.application.helpme.ChangePin_Activity;
import teeza.application.helpme.Login_Activity;
import teeza.application.helpme.R;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.Car;
import teeza.application.helpme.model.User;
import teeza.application.helpme.persistence.UserManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class Setting_Fragment extends Fragment {

	final String PREF_NAME = "IMAGE_ID";
	private SharedPreferences sp;

	private UserManager mManager;
	private User user;
	private TextView name, phone, address, cartype, carbrand, carseries, carcity, carpolicy;
	private Spinner carid, image_size_id;
	private Button submit, cancel;
	private int itemselect, image_item_select;
	private View rootView;
	private String idcus;
	private LinearLayout changepass, changepin;
	private ArrayList < String > StrName1;
	private ArrayAdapter < String > StrNameAdap1;
	private StrictMode.ThreadPolicy policy;
	private ArrayList < Car > cars;
	private ApplicationStatus appStatus;
	private OKHttp okHttp;

	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		appStatus = ApplicationStatus.getInstance();
		okHttp = new OKHttp();
		mManager = new UserManager(getActivity());
		user = new User();
		idcus = mManager.getID();
		cars = new ArrayList < Car > ();
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.setting_fragment, container, false);
		changepass = (LinearLayout) rootView.findViewById(R.id.changepass);
		changepin = (LinearLayout) rootView.findViewById(R.id.changepin);
		if (appStatus.isOnline(getActivity())) Start();
		else appStatus.setNetwork(getActivity());
		
		rootView.setFocusableInTouchMode(true);
		rootView.requestFocus();
		rootView.setOnKeyListener(new View.OnKeyListener() {
		        @Override
		        public boolean onKey(View v, int keyCode, KeyEvent event) {
		            if( keyCode == KeyEvent.KEYCODE_BACK ) {
		               back();
		                return true;
		            } else {
		                return false;
		            }
		        }
		    });
		return rootView;
	}

	private void Start() {
		image_size_id = (Spinner) rootView.findViewById(R.id.imageid);

		name = (TextView) rootView.findViewById(R.id.name);
		phone = (TextView) rootView.findViewById(R.id.phone);
		address = (TextView) rootView.findViewById(R.id.address);
		carpolicy = (TextView) rootView.findViewById(R.id.carpolicy);
		cartype = (TextView) rootView.findViewById(R.id.cartype);
		carbrand = (TextView) rootView.findViewById(R.id.carbrand);
		carseries = (TextView) rootView.findViewById(R.id.carseries);
		carcity = (TextView) rootView.findViewById(R.id.carcity);
		carid = (Spinner) rootView.findViewById(R.id.carid);
		submit = (Button) rootView.findViewById(R.id.submit);
		cancel = (Button) rootView.findViewById(R.id.cancel);
		user = mManager.getuser();
		getcar();
		name.setText(user.getname().toString());
		phone.setText(user.getphone().toString());
		address.setText(user.getaddress().toString());
		changepass.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent changepass = new Intent(getActivity(), ChangePass_Activity.class);
				startActivity(changepass);
			}
		});
		
		changepin.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent changepin = new Intent(getActivity(), ChangePin_Activity.class);
				startActivity(changepin);
			}
		});

		sp = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
		int image_id = sp.getInt("image_id", 0);
		image_size_id.setSelection(image_id);

		image_size_id.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

			public void onItemSelected(AdapterView <? > parent, View view, int position, long id) {
				image_item_select = position;
			}
			@Override
			public void onNothingSelected(AdapterView <? > parent) {

			}
		});

		carid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {@Override
			public void onItemSelected(AdapterView <? > parent, View view,
			int position, long id) {
				carpolicy.setText(cars.get(position).getCarpolicy().toString());
				cartype.setText(cars.get(position).getCartype().toString());
				carbrand.setText(cars.get(position).getcarband().toString());
				carseries.setText(cars.get(position).getcarseries().toString());
				carcity.setText(cars.get(position).getcarcity().toString());
				itemselect = position;
			}
		@Override
			public void onNothingSelected(AdapterView <? > parent) { }

		});

		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				send(cars.get(itemselect).getCarid().toString());

				sp = getActivity().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
				SharedPreferences.Editor editor = sp.edit();
				editor.putInt("image_id", image_item_select);
				editor.commit();

				back();

			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				back();
				getcar();
				carpolicy.setText(cars.get(0).getCarpolicy().toString());
				cartype.setText(cars.get(0).getCartype().toString());
				carbrand.setText(cars.get(0).getcarband().toString());
				carseries.setText(cars.get(0).getcarseries().toString());
				carcity.setText(cars.get(0).getcarcity().toString());
			}
		});
	}

	public void back() {
		SettingMenu_Fragment menu = new SettingMenu_Fragment();
		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.frame_container, menu);
		ft.commit();
	}

	private void getcar() {
		cars.clear();
		String url2 = Login_Activity.nameHost + "selectcar.php";
		
		RequestBody formBody = new FormEncodingBuilder()
			.add("sMemberID", idcus)
			.build();
		
		try {
			JSONArray data = new JSONArray(okHttp.POST(url2, formBody));
			for (int i = 0; i < data.length(); i++) {
				JSONObject c2 = data.getJSONObject(i);
				Car car = new Car();
				car.setCarid(c2.getString("car_id")); Log.i("car_id", c2.getString("car_id"));
				car.setCarpolicy(c2.getString("car_policy"));
				car.setCartype(c2.getString("cartype_name"));
				car.setcarband(c2.getString("car_brand"));
				car.setcarseries(c2.getString("car_name"));
				car.setcarcity(c2.getString("car_city"));
				car.setcarstatus(c2.getString("car_status"));
				cars.add(car);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		StrName1 = new ArrayList < String > ();
		for (int i = 0; i < cars.size(); i++) {
			StrName1.add(cars.get(i).getCarid().toString());
		}
		StrNameAdap1 = new ArrayAdapter < String > (getActivity(), R.layout.spiner_layout, R.id.textspin, StrName1);
		carid.setAdapter(StrNameAdap1);
	}

	protected void send(String carid) {
		RequestBody formBody = new FormEncodingBuilder()
			.add("customer_id", idcus)
			.add("CarID", carid)
			.build();
		StrictMode.setThreadPolicy(policy);

		try {
			JSONObject json_data = new JSONObject(okHttp.POST(Login_Activity.nameHost + "carselect.php", formBody));
			String code1 = "";
			code1 = json_data.getString("car_status");

			if (code1.equals("1")) {
				Log.e("Insert", "Inserted Successfully");
				Toast.makeText(getActivity(), "เปลี่ยนรถที่ใช้ให้เรียบร้อยแล้ว", Toast.LENGTH_LONG).show();
			} else {
				Log.e("Insert", "Sorry Try Again");
				Toast.makeText(getActivity(), "กรุณาลองอีกครั้ง", Toast.LENGTH_LONG).show();
			}
		} catch (Exception e) {
			Log.e("Fail 3", e.toString());
		}
	}
}