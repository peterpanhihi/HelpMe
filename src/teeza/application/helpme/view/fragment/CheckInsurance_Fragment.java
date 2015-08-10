package teeza.application.helpme.view.fragment;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.DetailInsurance_Activity;
import teeza.application.helpme.Login_Activity;
import teeza.application.helpme.R;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.Car;
import teeza.application.helpme.persistence.UserManager;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

public class CheckInsurance_Fragment extends Fragment{	
	
	private String urlInsurance = Login_Activity.nameHost+"insurancetype.php";
	private String urlSelectCar = Login_Activity.nameHost+"selectcar.php";
	private UserManager mManager;
	private ImageAdapter imageAdapter;
	private Spinner carid;
	private View rootView;
	private ListView list1;
	private ArrayList<HashMap<String, Object>> MyArrList = new ArrayList<HashMap<String, Object>>();
	private ArrayAdapter<String> StrNameAdap1;
	private DecimalFormat myFormatter = new DecimalFormat("###,###,###");
	private ArrayList<Car> cars = new ArrayList<Car>();
	private ArrayList<String> StrName1;
	private AlertDialog.Builder builder;
	private ApplicationStatus appStatus;
	private OKHttp okHttp;
	
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true); 
	    super.onCreate(savedInstanceState);
	    mManager = new UserManager(getActivity());
	    appStatus = ApplicationStatus.getInstance();
	    okHttp = new OKHttp();
	    // Permission StrictMode
        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	     rootView = inflater.inflate(R.layout.check, container, false);
	     if(appStatus.isOnline(getActivity()))
	    	 Start();
	     else
	    	appStatus.setNetwork(getActivity());
	     return rootView;
	}
	
	public void Start() {	
		carid = (Spinner) rootView.findViewById(R.id.spinner1);		
		getcar();
		
		carid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {	
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				SearchData(cars.get(position).getCarid().toString());
			}
			
			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}
		});
	}
	
	private void getcar() {
		cars.clear();
		
		RequestBody formBody = new FormEncodingBuilder()
			.add("sMemberID", mManager.getID().toString())
			.build();
		
		try {	
			JSONArray data = new JSONArray(okHttp.POST(urlSelectCar, formBody));				
			for(int i = 0; i < data.length(); i++) {
				JSONObject c2 = data.getJSONObject(i);
		        Car car = new Car();
		        car.setCarid(c2.getString("car_id"));
		        Log.i("car_id", c2.getString("car_id"));
		        car.setCartype(c2.getString("cartype_id"));
		        car.setcarband(c2.getString("car_brand"));
		        car.setcarseries(c2.getString("car_name"));
		        car.setcarcity(c2.getString("car_city"));
		        car.setcarstatus(c2.getString("car_status"));
		        cars.add(car);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		StrName1 = new ArrayList<String>();
		for(int i = 0; i < cars.size();i++)
		{
			StrName1.add(cars.get(i).getCarid().toString());
		}
		StrNameAdap1 = new ArrayAdapter<String>(getActivity(), R.layout.spinner_layout2,R.id.textspin,StrName1);
		carid.setAdapter(StrNameAdap1);
	}
	
	public void SearchData(String carid) {	
		
		RequestBody formBody = new FormEncodingBuilder()
			.add("car_id",carid)
			.build();
		
        MyArrList.clear();
		try {
			JSONArray data = new JSONArray(okHttp.POST(urlInsurance, formBody));			
			HashMap<String, Object> map;
			
			for(int i = 0; i < data.length(); i++){
                JSONObject c = data.getJSONObject(i);
    			map = new HashMap<String, Object>();
    			
    			map.put("insu_name", c.getString("insurancetype_name"));
    			String pricedeci = myFormatter.format(c.getInt("insurancetype_financial"));
    			map.put("insu_price",pricedeci);
    			map.put("insu_type", c.getString("cartype_name"));
    			map.put("insu_id", c.getString("insurancetype_id"));
    			map.put("cartype_id", c.getString("cartype_id"));
    			
    			MyArrList.add(map); 			
			} 
		} catch (Exception e) {
			e.printStackTrace();
		}
		imageAdapter = new ImageAdapter(getActivity());  
		list1 = (ListView) rootView.findViewById(R.id.listViewcheck);
	    list1.setAdapter(imageAdapter);
    }
	
	class ImageAdapter extends BaseAdapter {
        private Context mContext; 
        public ImageAdapter(Context context) { 
            mContext = context; 
        } 
        public int getCount() { 
            return MyArrList.size();    
        } 
        public Object getItem(int position) { 
            return MyArrList.get(position); 
        } 
        public long getItemId(int position) { 
            return position; 
        } 
		public View getView(final int position, View convertView, ViewGroup parent) {
			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);	 
			if (convertView == null) {
				convertView = inflater.inflate(R.layout.check_layout, null); 
			}
			// classinsurance
			TextView classinsu = (TextView) convertView.findViewById(R.id.textView2);
			classinsu.setText(MyArrList.get(position).get("insu_name").toString());
			// priceinsurance
			TextView priceinsu = (TextView) convertView.findViewById(R.id.priceinsu1);
			priceinsu.setText(MyArrList.get(position).get("insu_price").toString());
			//typeinsu
			TextView txtstatus = (TextView) convertView.findViewById(R.id.textView3);
			txtstatus.setText(MyArrList.get(position).get("insu_type").toString());
			//layoutonclick
			LinearLayout li1 = (LinearLayout) convertView.findViewById(R.id.insu1);
			li1.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent detail = new Intent(getActivity(),DetailInsurance_Activity.class);
					detail.putExtra("insu_id",MyArrList.get(position).get("insu_id").toString());
					detail.putExtra("cartype_id",MyArrList.get(position).get("cartype_id").toString());
					detail.putExtra("insu_name",MyArrList.get(position).get("insu_name").toString());
					detail.putExtra("isInPage", "true");
					startActivity(detail);
				}
			});
			return convertView;	
		}
        
    }
}
