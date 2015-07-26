package teeza.application.helpme.view.fragment;

import org.json.JSONObject;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.Login_Activity;
import teeza.application.helpme.Main_Activity;
import teeza.application.helpme.R;
import teeza.application.helpme.http.OKHttp;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class EClaimDialog_Fragment extends DialogFragment {
	final String url = Login_Activity.nameHost + "check_policy.php";
	final String PREF_NAME = "IMAGE_ID";

	private Main_Activity main;
	private OKHttp okHttp;
	private int position;
	private SharedPreferences sp;
	private AlertDialog.Builder builder;
	private ProgressDialog dialog;
	private ProgressDialog ringProgressDialog;
	private EditText et_policy, et_car;
	private String st_policy, st_car, idcus;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	public EClaimDialog_Fragment(Context context, int position, String idcus) {
		main = (Main_Activity) context;
		this.position = position;
		this.idcus = idcus;
		okHttp = new OKHttp();
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
		et_car = (EditText) layout.findViewById(R.id.num_car);
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

						String num_policy, num_car = " ", active = " ";

						st_policy = et_policy.getText().toString().trim();
						st_car = et_car.getText().toString().trim();

						String url = Login_Activity.nameHost + "check_policy.php";
						RequestBody formBody = new FormEncodingBuilder()
							.add("num_policy", st_policy)
							.add("num_car", st_car)
							.add("customer_id", idcus)
							.build();
						
						StrictMode.setThreadPolicy(policy);
						try {
							JSONObject json_data = new JSONObject(okHttp.POST(url, formBody));
							num_policy = (json_data.getString("num_policy"));
							num_car = (json_data.getString("num_car"));
							active = (json_data.getString("active"));

							// check policy
							if (num_policy.equals("0")) {
								Toast.makeText(getActivity(),
										"ไม่มีเลขเลขกรมธรรม์ͺ",
										Toast.LENGTH_LONG).show();
							} else {

								sp = getActivity().getSharedPreferences(
										PREF_NAME, Context.MODE_PRIVATE);
								SharedPreferences.Editor editor = sp.edit();
								editor.putString("num_policy", num_policy);
								editor.putString("num_car", num_car);
								editor.putString("active", active);
								editor.commit();
								
								main.setOldposition(position);
								EClaim_Fragment claim = new EClaim_Fragment();
								claim.getFragmentManager();
								FragmentTransaction ft = getActivity()
										.getSupportFragmentManager().beginTransaction();
								ft.replace(R.id.frame_container, claim);
								ft.commit();

							}
							
						} catch (Exception e) {
							Toast.makeText(getActivity(),
									"Error",
									Toast.LENGTH_LONG).show();
							Log.e("Fail 3", e.toString());
						}
					}

				});
		return builder.create();
	}

	public void createRingProgress() {
		ringProgressDialog = ProgressDialog.show(getActivity(),
				"Please wait ...", "กรุญารอสักครู่ ...", true);
		ringProgressDialog.setCancelable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// Here you should write your
					// time consuming task...
					// Let the progress ring for 10
					// seconds...
					Thread.sleep(2000);
					dialog.cancel();
					ringProgressDialog.dismiss();
				} catch (Exception e) {

				}
			}
		}).start();
	}
}
