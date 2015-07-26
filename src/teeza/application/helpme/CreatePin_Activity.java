package teeza.application.helpme;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import teeza.application.helpme.R;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.persistence.UserManager;

/**
 * Create PIN code page Created by PAN on 6/4/2015 AD.
 */
public class CreatePin_Activity extends Activity {

	private UserManager mManager;
	private ApplicationStatus appStatus;
	private EditText etPin;
	private EditText etCPin;
	private TextView btnOK;
	private String pin, confirmPin;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.createpin_layout);

		mManager = new UserManager(this);
		appStatus = ApplicationStatus.getInstance();

		etPin = (EditText) findViewById(R.id.txtPin);
		etCPin = (EditText) findViewById(R.id.txtConfirmPin);
		etCPin.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					createPin();
				}
				return false;
			}
		});
		btnOK = (TextView) findViewById(R.id.btnOK);
		btnOK.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				createPin();
			}
		});
	}

	public void createPin() {
		if (appStatus.isOnline(this)) {
			pin = etPin.getText().toString();
			confirmPin = etCPin.getText().toString();
			if (pin.equals("") || confirmPin.equals("") || pin.length() != 4
					|| confirmPin.length() != 4 || !pin.equals(confirmPin)) {
				new AlertDialog.Builder(CreatePin_Activity.this)
						.setTitle("Error! ")
						.setIcon(android.R.drawable.btn_star_big_on)
						.setPositiveButton("Close", null)
						.setMessage("Incorrect Username and Password").show();
				etPin.setText("");
				etCPin.setText("");
			} else if (pin.equals(confirmPin)) {
				mManager.setPin(pin);
				Intent helpMe = new Intent(getApplicationContext(),
						Main_Activity.class);
				startActivity(helpMe);
				appStatus.setIsFirstLogin(false);
				appStatus.setIsFillPin(true);
				mManager.setStat();
			}
		} else {
			appStatus.setNetwork(this);
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (Integer.valueOf(android.os.Build.VERSION.SDK) < 7
				&& keyCode == KeyEvent.KEYCODE_BACK
				&& event.getRepeatCount() == 0) {
			onBackPressed();
		}

		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onBackPressed() {
		// This will be called either automatically for you on 2.0
		// or later, or by the code above on earlier versions of the
		// platform.
		return;
	}
}
