package teeza.application.helpme;

import java.io.IOException;

import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.R;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.User;
import teeza.application.helpme.persistence.UserManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
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

public class Login_Activity extends Activity {
	public static CharSequence nameHost = "http://www.bumji.com/HelpMe/android/";
	private UserManager mManager;
	public String strMemberID, strStatusID, StatusID = "0";
	private ProgressDialog dialog;

	// private Context mContext;
	private EditText etUser, etPass;
	private TextView btnLogin;
	private String strError = "Unknow Status!";
	private ApplicationStatus appStatus;
	private Activity activity;
	private OKHttp okHttp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.loginlayout);
		mManager = new UserManager(this);
		btnLogin = (TextView) findViewById(R.id.textView3);
		etUser = (EditText) findViewById(R.id.txtUsername);
		etPass = (EditText) findViewById(R.id.txtPassword);
		appStatus = ApplicationStatus.getInstance();
		okHttp =  new OKHttp();
		activity = this;

		etPass.setOnEditorActionListener(new OnEditorActionListener() {
			public boolean onEditorAction(TextView v, int actionId,
					KeyEvent event) {
				if (actionId == EditorInfo.IME_ACTION_DONE) {
					onclickLogin();
				}
				return false;
			}
		});

		btnLogin.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				onclickLogin();
			}
		});
	}

	public void onclickLogin() {
		if (appStatus.isOnline(activity)) {
			if ((etPass.getText().toString()).equals("")
					|| (etUser.getText().toString()).equals("")) {
				new AlertDialog.Builder(Login_Activity.this)
						.setTitle("Error! ")
						.setIcon(android.R.drawable.btn_star_big_on)
						.setPositiveButton("Close", null)
						.setMessage("Please fill Username and Password").show();
			} else {
				dialog = ProgressDialog.show(Login_Activity.this, "",
						"Validating user...", true);
				new Thread(new Runnable() {
					public void run() {
						checkLogin();
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		} else {
			appStatus.setNetwork(activity);
		}
	}

	@Override
	protected void onResume() {
		if (appStatus.isOnline(this)) {
			try {
				login();
			} catch (Exception e) {
				e.printStackTrace();
			}
		} else {
			appStatus.setNetwork(this);
		}
		super.onResume();
	}

	public void login() {
		if (mManager.getStat().equals("1")) {
			if (mManager.getPin().equals("0")) {
				Intent intent = new Intent(getApplicationContext(),CreatePin_Activity.class);
				startActivity(intent);
			} else {
				Intent Helpme = new Intent(getApplicationContext(),
						Main_Activity.class);
				startActivity(Helpme);
				appStatus.setIsFirstLogin(false);
			}
		} else {
			appStatus.setIsFirstLogin(true);
		}
	}

	public void checkLogin() {
		StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
				.permitAll().build();
		StrictMode.setThreadPolicy(policy);
		try {
			RequestBody formBody = new FormEncodingBuilder()
					.add("strUser", etUser.getText().toString())
					.add("strPass", etPass.getText().toString()).build();
			String url = nameHost + "checkLogin.php";

			String resultServer = okHttp.POST(url, formBody);
			strStatusID = "0";
			JSONObject c = new JSONObject(resultServer);
			strStatusID = c.getString("StatusID");
			strMemberID = c.getString("MemberID");
			strError = c.getString("Error");

			// Prepare Login
			if (strStatusID.equals("0")) {
				// Dialog
				dialog.dismiss();
				showError();
			} else {
				savetoDB();
				Intent Helpme = new Intent(getApplicationContext(),
						CreatePin_Activity.class);
				startActivity(Helpme);
				mManager.setStat();
				mManager.resetPin();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void savetoDB() {
		RequestBody formBody = new FormEncodingBuilder().add("sMemberID",strMemberID).build();

		String strMemberID_new = "";
		String strUsername_new = "";
		String strPassword_new = "";
		String strName_new = "";
		String strPhone_new = "";
		String strAddress_new = "";
		String strPin_new = "";

		JSONObject c;
		try {
			c = new JSONObject(okHttp.POST(nameHost + "getByMemberID.php", formBody));
			strMemberID_new = c.getString("MemberID");
			strUsername_new = c.getString("Username");
			strPassword_new = c.getString("Password");
			strName_new = c.getString("Name") + "  " + c.getString("LName");
			strPhone_new = c.getString("Mobile");
			strAddress_new = c.getString("Address");

			if (!strMemberID_new.equals("")) {
				User user = new User(strMemberID_new, strUsername_new,
						strPassword_new, "1", "0", strName_new, strPhone_new,
						strAddress_new, strPin_new);
				mManager.registerUser(user);

			} else {
				dialog.dismiss();
				showError();
			}
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void onBackPressed() {
	}

	public void showError() {
		this.runOnUiThread(new Runnable() {
			@Override
			public void run() {
				new AlertDialog.Builder(Login_Activity.this)
						.setTitle("Error! ")
						.setIcon(android.R.drawable.btn_star_big_on)
						.setPositiveButton("Close",
								new DialogInterface.OnClickListener() {

									@Override
									public void onClick(DialogInterface dialog,
											int which) {
										etUser.setText("");
										etPass.setText("");
									}
								}).setMessage(strError).show();
			}
		});
	}
}
