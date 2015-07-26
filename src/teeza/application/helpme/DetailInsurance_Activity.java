package teeza.application.helpme;

import java.io.IOException;
import java.text.DecimalFormat;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.R;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class DetailInsurance_Activity extends Activity {
	private String url = Login_Activity.nameHost + "insurance.php";
	private OKHttp okHttp;
	private TextView classinsu, toon, first, life, supsin, paguntua;
	private DecimalFormat myFormatter = new DecimalFormat("###,###,###");
	private Button cancel;
	private ApplicationStatus appStatus;
	private boolean isCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.detail_insurance);

		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		
		okHttp = new OKHttp();
		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		Intent intent = getIntent();
		String cartype = intent.getStringExtra("cartype_id").toString();
		String insutype = intent.getStringExtra("insu_id").toString();
		String insuclass = intent.getStringExtra("insu_name").toString();

		classinsu = (TextView) findViewById(R.id.classinsu);
		classinsu.setText(insuclass);
		toon = (TextView) findViewById(R.id.toon);
		first = (TextView) findViewById(R.id.first);
		life = (TextView) findViewById(R.id.life);
		supsin = (TextView) findViewById(R.id.supsin);
		paguntua = (TextView) findViewById(R.id.paguntua);
		cancel = (Button) findViewById(R.id.button1);
		SearchData(cartype, insutype);
		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				isCancel = true;
				finish();
			}
		});
	}

	public void SearchData(String cartype, String insutype) {
		try {
			RequestBody formBody = new FormEncodingBuilder()
			.add("cartype_id", cartype).add("insurancetype_id", insutype)
			.build();
			JSONArray data = new JSONArray(okHttp.POST(url, formBody));
			JSONObject c = data.getJSONObject(0);
			String toondeci = myFormatter.format(c.getInt("insurance_insured"));
			String firstdeci = myFormatter.format(c
					.getInt("insurance_deductible"));
			String lifedeci = myFormatter.format(c.getInt("insurance_life"));
			String supsindeci = myFormatter
					.format(c.getInt("insurance_assets"));
			String paguntuadeci = myFormatter
					.format(c.getInt("insurance_bail"));
			toon.setText(toondeci);
			first.setText(firstdeci);
			life.setText(lifedeci);
			supsin.setText(supsindeci);
			paguntua.setText(paguntuadeci);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (appStatus.checkStatus()) {
			Intent intent = new Intent(getApplicationContext(),
					Pin_Activity.class);
			startActivityForResult(intent, 1);
		}

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!isCancel) {
			appStatus.onPause();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		isCancel = true;
	}

}
