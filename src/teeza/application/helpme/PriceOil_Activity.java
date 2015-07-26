package teeza.application.helpme;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;
import com.squareup.otto.Subscribe;

import teeza.application.helpme.R;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class PriceOil_Activity extends Activity {
	private OKHttp okHttp;
	private TextView oil95, oilso95, oilso91, oilsoe20, oilsoe85, oilde;
	private Button back;
	private ApplicationStatus appStatus;
	private boolean isCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.price_oil);

		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		oil95 = (TextView) findViewById(R.id.price95);
		oilso95 = (TextView) findViewById(R.id.priceso95);
		oilso91 = (TextView) findViewById(R.id.priceso91);
		oilsoe20 = (TextView) findViewById(R.id.pricee20);
		oilsoe85 = (TextView) findViewById(R.id.pricee85);
		oilde = (TextView) findViewById(R.id.pricede);
		back = (Button) findViewById(R.id.button1);
		back.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isCancel = true;
				finish();
			}
		});
		getoil();

	}

	private void getoil() {
		try {
			RequestBody formBody = new FormEncodingBuilder() 
			.add("pull", "1")
			.build();
			okHttp.POST("http://api.sixhead.com/oilprice/get-data.php", formBody);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
			}

			if (resultCode == this.RESULT_CANCELED) {
				// Write your code if there's no result
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
		if (!isCancel)
			appStatus.onPause();
	}

	@Override
	public void onBackPressed() {
		isCancel = true;
		super.onBackPressed();
	}
	
	@Subscribe
	public void dataRecived(String output) {
		JSONObject c;
		try {
			c = new JSONObject(output);
			oil95.setText(c.getString("oil1"));
			oilso95.setText(c.getString("oil2"));
			oilso91.setText(c.getString("oil3"));
			oilsoe20.setText(c.getString("oil4"));
			oilsoe85.setText(c.getString("oil5"));
			oilde.setText(c.getString("oil6"));

		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
}
