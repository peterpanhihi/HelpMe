package teeza.application.helpme;

import teeza.application.helpme.R;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.persistence.UserManager;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ChangePin_Activity extends Activity {
	private UserManager mManager;

	private EditText etOldpin, etNewpin, etConfpin;
	private TextView txtError;
	private Button submit, cancel;

	private String idcus, oldpin, newpin, confpin;

	private StrictMode.ThreadPolicy policy;
	private ApplicationStatus appStatus;
	private boolean isCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changepin);
		policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		mManager = new UserManager(this);
		idcus = mManager.getID();

		etOldpin = (EditText) findViewById(R.id.oldpin);
		etNewpin = (EditText) findViewById(R.id.newpin);
		etConfpin = (EditText) findViewById(R.id.confnewpin);
		submit = (Button) findViewById(R.id.btn_submit);
		cancel = (Button) findViewById(R.id.btn_cancel);
		txtError = (TextView) findViewById(R.id.error);
		txtError.setVisibility(View.INVISIBLE);

		etConfpin.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
				submitPin();
			}
		});
		
		submit.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				submitPin();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View arg0) {
				isCancel = true;
				finish();
			}
		});
	}

	public void submitPin() {
		oldpin = etOldpin.getText().toString();
		newpin = etNewpin.getText().toString();
		confpin = etConfpin.getText().toString();

		if (checkPin()) {
			mManager.changepass(oldpin, newpin);
			mManager.setPin(newpin);
			txtError.setVisibility(View.INVISIBLE);
			etOldpin.setText("");
			etNewpin.setText("");
			etConfpin.setText("");
			Toast.makeText(getApplicationContext(),
					"เปลี่ยนรหัสผ่านเรียบร้อยแล้ว", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(getApplicationContext(),
					Main_Activity.class);
			intent.putExtra("selectItem", 6);
			intent.putExtra("isInPage", true);
			startActivity(intent);
		}
	}

	public boolean checkPin() {
		if (oldpin.equals("") || newpin.equals("") || confpin.equals("")) {
			txtError.setText("*โปรดใส่ข้อมูลให้ครบถ้วน");
			txtError.setVisibility(View.VISIBLE);
		} else if (!(oldpin.equals(mManager.getPin().toString()))) {
			txtError.setText("*รหัสผ่านไม่ถูกต้อง");
			txtError.setVisibility(View.VISIBLE);
		} else if (!(newpin.equals(confpin))) {
			txtError.setText("*รหัสผ่านใหม่ไม่ตรงกัน");
			txtError.setVisibility(View.VISIBLE);
		} else {
			return true;
		}
		return false;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
			}

			if (resultCode == this.RESULT_CANCELED) {
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
		isCancel = true;
		super.onBackPressed();
	}
}
