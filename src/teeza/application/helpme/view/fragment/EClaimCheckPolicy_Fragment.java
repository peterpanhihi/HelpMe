package teeza.application.helpme.view.fragment;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.Login_Activity;
import teeza.application.helpme.R;
import teeza.application.helpme.UploadQueue_Activity;
import teeza.application.helpme.http.OKHttp;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;

public class EClaimCheckPolicy_Fragment extends DialogFragment {

	private AlertDialog.Builder builder;
	private EditText et_policy;
	private Spinner carid;
	private ArrayList<String> carsID;
	private ArrayList<String> carPolicy;
	private ArrayAdapter<String> adapter;
	private OKHttp okHttp;
	private String idcus;
	private StrictMode.ThreadPolicy policy;

	public EClaimCheckPolicy_Fragment(String idcus) {
		okHttp = new OKHttp();
		this.idcus = idcus;
		policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		builder = new AlertDialog.Builder(new ContextThemeWrapper(
				getActivity(), R.style.AppTheme));
		@SuppressWarnings("static-access")
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_check_policy, null);
		et_policy = (EditText) layout.findViewById(R.id.num_policy);
		et_policy.setEnabled(false);
		
		carid = (Spinner) layout.findViewById(R.id.car_id);
		carsID = new ArrayList<String>();
		carPolicy = new ArrayList<String>();
		
		StrictMode.setThreadPolicy(policy);
		getcar();
		
		Log.i("CAR LENGTH", carsID.size() + "");
		adapter = new ArrayAdapter<String>(getActivity(),
				R.layout.spiner_layout, R.id.textspin, carsID);
		carid.setAdapter(adapter);
		
		carid.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view,
					int position, long id) {
				et_policy.setText(carPolicy.get(position));
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {
			}

		});
		
		builder.setView(layout);
		builder.setCancelable(true);
		builder.setPositiveButton("ยกเลิก",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						dialog.cancel();
					}
				});
		builder.setNegativeButton("ตกลง",
				new DialogInterface.OnClickListener() {
					public void onClick(final DialogInterface dialog,
							int whichButton) {
						Intent upload = new Intent(getActivity(),
								UploadQueue_Activity.class);
						upload.putExtra("car_policy", et_policy.getText().toString());
						upload.putExtra("car_id", carid.getSelectedItem().toString());
						startActivity(upload);
						dialog.dismiss();
					}

				});
		return builder.create();
	}

	public void getcar() {
		carsID.clear(); 
		String url2 = Login_Activity.nameHost + "selectcar.php";

		RequestBody formBody = new FormEncodingBuilder()
				.add("sMemberID", idcus).build();

		try {
			JSONArray data = new JSONArray(okHttp.POST(url2, formBody));
			for (int i = 0; i < data.length(); i++) {
				JSONObject c2 = data.getJSONObject(i);
				carsID.add(c2.getString("car_id"));
				carPolicy.add(c2.getString("car_policy"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}	
	}
}
