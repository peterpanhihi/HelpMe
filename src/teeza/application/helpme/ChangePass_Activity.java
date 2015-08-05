package teeza.application.helpme;

import java.io.IOException;
import java.io.InputStream;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.R;
import teeza.application.helpme.http.OKHttp;
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

public class ChangePass_Activity extends Activity {
	private EditText oldpass, newpass, confnewpass;
	private TextView error;
	private Button submit, cancel;
	private UserManager mManager;
	private String idcus, oldpassword, newpassword, confnewpassword;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
	private OKHttp okHttp;
	private ApplicationStatus appStatus;
	private boolean isCancel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.changpassword);

		okHttp = new OKHttp();
		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		mManager = new UserManager(this);
		idcus = mManager.getID();
		oldpass = (EditText) findViewById(R.id.oldpass);
		newpass = (EditText) findViewById(R.id.newpass);
		confnewpass = (EditText) findViewById(R.id.confnewpass);
		submit = (Button) findViewById(R.id.submit);
		cancel = (Button) findViewById(R.id.Button01);
		error = (TextView) findViewById(R.id.error);
		error.setVisibility(View.INVISIBLE);

		confnewpass.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View arg0) {
				submitPass();
			}
		});

		submit.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				submitPass();
			}
		});

		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				isCancel = true;
				finish();
			}
		});
	}

	void send() {
		try {
			RequestBody formBody = new FormEncodingBuilder()
					.add("customer_id", idcus).add("newpassword", newpassword)
					.build();
			okHttp.POST(Login_Activity.nameHost + "cpassword.php", formBody);
		} catch (IOException e) {
			e.printStackTrace();
		}
		StrictMode.setThreadPolicy(policy);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
			}
		}
	}

	public void submitPass() {
		oldpassword = oldpass.getText().toString();
		newpassword = newpass.getText().toString();
		confnewpassword = confnewpass.getText().toString();
		if (oldpassword.equals("") || newpassword.equals("")
				|| confnewpassword.equals("")) {
			error.setText("*โปรดใส่ข้อมูลให้ครบถ้วน");
			error.setVisibility(View.VISIBLE);
		} else if (!(oldpassword.equals(mManager.getPassword().toString()))) {
			error.setText("*รหัสผ่านไม่ถูกต้อง");
			error.setVisibility(View.VISIBLE);
		} else if (!(newpassword.equals(confnewpassword))) {
			error.setText("*รหัสผ่านใหม่ไม่ตรงกัน");
			error.setVisibility(View.VISIBLE);
		} else {
			mManager.changepass(oldpassword, newpassword);
			send();
			error.setVisibility(View.INVISIBLE);
			oldpass.setText("");
			newpass.setText("");
			confnewpass.setText("");
			Toast.makeText(getApplicationContext(),
					"เปลี่ยนรหัสผ่านเรียบร้อยแล้ว", Toast.LENGTH_LONG).show();
			Intent intent = new Intent(getApplicationContext(),
					Main_Activity.class);
			intent.putExtra("selectItem", 6);
			intent.putExtra("isInPage", true);
			startActivity(intent);
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

}
