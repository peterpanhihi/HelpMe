package teeza.application.helpme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.json.JSONObject;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.R;
import teeza.application.helpme.adapter.QueueAdapter;
import teeza.application.helpme.custom_view.ExpandableHeightGridView;
import teeza.application.helpme.http.HttpFileUpload;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.QueueItem;
import teeza.application.helpme.persistence.UserManager;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.os.Vibrator;
import android.os.AsyncTask.Status;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.view.inputmethod.EditorInfo;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadQueue_Activity extends GMap_Activity {
	final String PREF_NAME = "IMAGE_ID";

	private static final int CANCELED = -4;
	private static final int OTHER_INTERNAL_ERROR = -3;
	private static final int SECURITY_ERROR = -2;
	private static final int SERVER_STATUS_UPLOADED = 1;

	private QueueAdapter queueAdapter;
	private ExpandableHeightGridView uploadList;

	private SharedPreferences sp;

	private String id;
	private String ids = "";

	private ProgressDialog progressDialog;
	private static final int PROGRESSDIALOG_ID = 0;

	private UserManager mManager;
	private UploadTask uploadTask;
	private int uploadCounter, dialogCouter;
	private boolean uploadFlag;
	private int year, month, day;
	private double lat, lng;
	private Context mContext;
	private Builder builder;
	private EditText detail, subject, num_active, num_policy, num_car,
			datepicker, name, location_action, address, phone, status_car,
			loss, location_check;
	private Button back;
	private File imageFile_new;

	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	private OKHttp okHttp;
	private ApplicationStatus appStatus;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		setContentView(R.layout.uploadqueue);
		StrictMode.setThreadPolicy(policy);
		mManager = new UserManager(this);
		id = mManager.getID().toString();
		queueAdapter = new QueueAdapter(this);

		okHttp = new OKHttp();
		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		detail = (EditText) findViewById(R.id.detail);
		subject = (EditText) findViewById(R.id.Subject);
		num_active = (EditText) findViewById(R.id.num_active);
		num_policy = (EditText) findViewById(R.id.num_policy);
		num_car = (EditText) findViewById(R.id.num_car);
		datepicker = (EditText) findViewById(R.id.datepiker);
		name = (EditText) findViewById(R.id.name);
		location_action = (EditText) findViewById(R.id.location_action);
		address = (EditText) findViewById(R.id.address);
		phone = (EditText) findViewById(R.id.phone);
		status_car = (EditText) findViewById(R.id.status_car);
		loss = (EditText) findViewById(R.id.loss);
		location_check = (EditText) findViewById(R.id.location_check);
		mContext = this;
		sp = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

		num_active.setText(sp.getString("active", " "));
		num_policy.setText(sp.getString("num_policy", " "));
		num_car.setText(sp.getString("num_car", " "));

		if (num_policy.getText().toString().equals("")) {
			subject.setNextFocusDownId(R.id.num_policy);
		} else {
			subject.setNextFocusDownId(R.id.name);
		}
		name.setNextFocusDownId(R.id.location_action);

		// Get current date by calender

		final Calendar c = Calendar.getInstance();
		year = c.get(Calendar.YEAR);
		month = c.get(Calendar.MONTH);
		day = c.get(Calendar.DAY_OF_MONTH);

		datepicker.setText(year + "-" + month + "-" + day);

		name.setText(mManager.getuser().getname());

		location_action.setOnFocusChangeListener(new OnFocusChangeListener() {
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus) {
					((EditText) v).setText(getAddress(lat, lng));
				}
			}
		});

		address.setText(mManager.getuser().getaddress());

		location_check.setOnFocusChangeListener(new OnFocusChangeListener() {

			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				((EditText) v).setText(getAddress(lat, lng));

			}
		});

		phone.setText(mManager.getuser().getphone());

		datepicker.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				// showDialog(DATE_DIALOG_ID);
				if (dialogCouter == 0) {
					builder = new AlertDialog.Builder(new ContextThemeWrapper(
							mContext, R.style.AppTheme));
					@SuppressWarnings("static-access")
					LayoutInflater inflater = (LayoutInflater) mContext
							.getSystemService(mContext.LAYOUT_INFLATER_SERVICE);
					View layout = inflater.inflate(R.layout.dialog_datetime,
							null);
					final DatePicker input = (DatePicker) layout
							.findViewById(R.id.datePicker1);
					builder.setView(layout);
					builder.setCancelable(true);
					builder.setPositiveButton("Cancel",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									dialog.cancel();
									dialogCouter = 0;
								}
							});
					builder.setNegativeButton("Set",
							new DialogInterface.OnClickListener() {
								public void onClick(
										final DialogInterface dialog,
										int whichButton) {
									datepicker.setText(input.getYear() + "-"
											+ input.getMonth() + "-"
											+ input.getDayOfMonth());
									dialogCouter = 0;
								}

							});
					builder.show();
					dialogCouter++;
				}

			}
		});

		uploadList = (ExpandableHeightGridView) findViewById(R.id.UploadList);
		uploadList.setAdapter(queueAdapter);
		uploadList.setExpanded(true);

		back = (Button) findViewById(R.id.Button01);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				backToMain();
			}
		});

		if (queueAdapter.getCount() == 0) {
			Intent select = new Intent(getApplicationContext(),
					EClaim_Activity.class);
			startActivityForResult(select, 1);
		}

		ImageView selectpic = (ImageView) findViewById(R.id.addmore);
		selectpic.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Intent select = new Intent(getApplicationContext(),
						EClaim_Activity.class);
				startActivityForResult(select, 1);
			}
		});

		final Button startUploadBtn = (Button) findViewById(R.id.StartUploadBtn);
		startUploadBtn.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			public void onClick(View v) {

				String policy1, car, value1, value2;

				policy1 = num_policy.getText().toString();
				car = num_car.getText().toString();

				// Log.e("test", car+" aa"+policy1);

				RequestBody formBody = new FormEncodingBuilder()
						.add("num_policy", policy1).add("num_car", car)
						.add("customer_id", id).build();
				String url = Login_Activity.nameHost + "check_policy.php";
				StrictMode.setThreadPolicy(policy);

				try {

					JSONObject json_data = new JSONObject(okHttp.POST(url,
							formBody));
					value1 = (json_data.getString("num_policy"));
					value2 = (json_data.getString("num_car"));

					// check policy
					if (value1.equals("0")) {
						Toast.makeText(getApplication(), "����բ����� ��سҵ�Ǩ�ͺ",
								Toast.LENGTH_LONG).show();
					} else {

						sp = getApplication().getSharedPreferences(PREF_NAME,
								Context.MODE_PRIVATE);
						SharedPreferences.Editor editor = sp.edit();
						editor.putString("num_policy", value1);
						editor.putString("num_car", value2);
						editor.commit();
						num_policy.setText(value1);
						num_car.setText(value2);

						if ((subject.getText().toString()).equals("")) {
							subject.setFocusable(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่ชื่อเหตุการณ์", Toast.LENGTH_LONG)
									.show();
						} else if ((num_policy.getText().toString()).equals("")) {
							num_policy.setFocusable(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่เลขที่กรมธรรม์", Toast.LENGTH_LONG)
									.show();
						} else if ((num_car.getText().toString()).equals("")) {
							num_car.setFocusable(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่ทะเบียนรถ", Toast.LENGTH_SHORT)
									.show();
						} else if ((name.getText().toString()).equals("")) {
							name.setFocusable(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่ชื่อผู้เอาประกัน",
									Toast.LENGTH_SHORT).show();
						} else if ((datepicker.getText().toString()).equals("")) {
							datepicker.setFocusable(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่วันที่เกิดเหตุ",
									Toast.LENGTH_SHORT).show();
						} else if ((location_action.getText().toString())
								.equals("")) {
							location_action.setFocusable(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่สถานที่เกิดเหตุ",
									Toast.LENGTH_SHORT).show();
						} else if ((detail.getText().toString()).equals("")) {
							detail.setFocusable(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่รายละเอียดการเกิดเหตุ",
									Toast.LENGTH_SHORT).show();
						} else if ((address.getText().toString()).equals("")) {
							address.setFocusable(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่ชื่อ ที่อยู่ ที่ติดต่อได้",
									Toast.LENGTH_SHORT).show();
						} else if ((phone.getText().toString()).equals("")) {
							phone.setFocusable(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่เบอร์โทรศัพท์ที่ติดต่อได้",
									Toast.LENGTH_SHORT).show();
						} else if ((location_check.getText().toString())
								.equals("")) {
							location_check.setFocusable(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่สถานที่ที่เราสามารถตรวจสอบได",
									Toast.LENGTH_SHORT).show();
						} else {
							send();
							showDialog(PROGRESSDIALOG_ID);
							setUpColorProgressDialog();
						}

					}

				} catch (Exception e) {
					Log.e("Fail 3", e.toString());
				}

			}
		});

		location_check
				.setOnEditorActionListener(new TextView.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							location_check.clearFocus();
							startUploadBtn.requestFocusFromTouch();
							return true;
						}
						return false;
					}
				});

		super.onCreate(savedInstanceState);
	}

	public void backToMain() {
		builder = new AlertDialog.Builder(mContext);
		builder.setTitle("HelpMe");
		builder.setMessage("ต้องการยกเลิกการทำงาน ? ");
		builder.setNegativeButton("ใช่", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(mContext, Main_Activity.class);
				intent.putExtra("isInPage", true);
				startActivity(intent);
			}
		});
		builder.setPositiveButton("ไม่ใช่", null);
		setColorDialog();
	}

	// get address
	private String getAddress(double latitude, double longitude) {
		appStatus.checkLocation(this);
		StringBuilder result = new StringBuilder();
		try {
			Geocoder geocoder = new Geocoder(this, Locale.getDefault());
			List<Address> addresses = geocoder.getFromLocation(latitude,
					longitude, 1);
			if (addresses.size() > 0) {
				Address address = addresses.get(0);
				result.append(address.getAddressLine(0) + " ");
				result.append(address.getLocality() + " ");
				result.append(address.getAdminArea() + " ");
				result.append(address.getCountryName() + " ");
				result.append(address.getCountryCode() + " ");
				result.append(address.getPostalCode() + " ");
			}
		} catch (IOException e) {
			Log.e("tag", e.getMessage());
		}
		return result.toString();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				appStatus.setIsInPage(true);
				ids = data.getStringExtra("Ids");
				queueAdapter.initialize(ids);
			}
			if (resultCode == RESULT_CANCELED) {
				appStatus.setIsInPage(true);
			}
		}

		if (requestCode == 2) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
			}

			if (resultCode == this.RESULT_CANCELED) {
			}
		}
	}

	@Override
	public void onResume() {
		if (appStatus.checkStatus()) {
			Intent intent = new Intent(getApplicationContext(),
					Pin_Activity.class);
			startActivityForResult(intent, 2);
		}
		appStatus.checkLocation(this);
		super.onResume();
	}

	@Override
	protected void onPause() {
		appStatus.onPause();
		super.onPause();
	}

	@Override
	public void updateUI() {
		lat = mCurrentLocation.getLatitude();
		lng = mCurrentLocation.getLongitude();
		super.updateUI();
	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESSDIALOG_ID:
			removeDialog(PROGRESSDIALOG_ID);
			progressDialog = new ProgressDialog(this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setMax(queueAdapter.getUploadCount());
			progressDialog.setTitle("Uploading");
			progressDialog.setCancelable(true);
			progressDialog.setMessage("Please wait...");
			progressDialog.setOnCancelListener(new OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					if (uploadTask != null
							&& uploadTask.getStatus() != AsyncTask.Status.FINISHED)
						uploadTask.cancel(true);
				}
			});
			break;
		default:
			progressDialog = null;
		}
		return progressDialog;
	}

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		switch (id) {
		case PROGRESSDIALOG_ID:
			if (uploadTask != null && uploadTask.getStatus() != Status.FINISHED)
				uploadTask.cancel(true);
			uploadTask = new UploadTask();
			uploadTask.execute();
			break;
		}
	}

	public int UploadFile(File f) {

		try {
			// Set your file here
			FileInputStream fstrm = new FileInputStream(f);

			// Set your server page url (and the file title/description)
			HttpFileUpload hfu = new HttpFileUpload(Login_Activity.nameHost
					+ "insertpictureeclaim.php", "my file title", detail
					.getText().toString());

			int success = hfu.Send_Now(fstrm, mManager.getID().toString());

			return success;
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		return 0;

	}

	class UploadTask extends AsyncTask<Void, Integer, String> {

		@Override
		protected String doInBackground(Void... unused) {
			uploadCounter = 0;
			for (QueueItem item : queueAdapter.queueItems) {

				if (item.getUploaded() != 1) {

					SimpleDateFormat sdf = new SimpleDateFormat(
							"yyyyMMdd_HHmmss");
					String fileName = "_" + id + "_" + sdf.format(new Date())
							+ ".jpg";
					File myDirectory = new File(
							Environment.getExternalStorageDirectory(),
							"DCIM/HelpMe");
					myDirectory.mkdirs();
					imageFile_new = new File(myDirectory, fileName);
					File imageFile = new File(item.getPath());

					Bitmap b = null;
					try {

						b = decodeFile(imageFile.getAbsolutePath());

						FileOutputStream fos = new FileOutputStream(
								imageFile_new);

						b.compress(Bitmap.CompressFormat.JPEG, 100, fos);
						item.setPath(imageFile_new.getPath());
						fos.close();
					} catch (IOException e) {

						Log.e("test upload", e.toString());
					}

					int status = UploadFile(imageFile_new);
					if (status == 1) {
						item.setUploaded(1);
						publishProgress(SERVER_STATUS_UPLOADED);
					} else {
						item.setUploaded(-1);
					}
					uploadCounter++;
					publishProgress(SERVER_STATUS_UPLOADED);

				}
			}
			return (null);
		}

		public Bitmap decodeFile(String filePath) {
			Bitmap bitmap;
			// Decode image size
			BitmapFactory.Options o = new BitmapFactory.Options();
			o.inJustDecodeBounds = true;
			BitmapFactory.decodeFile(filePath, o);

			// The new size we want to scale to
			final int REQUIRED_SIZE = 400;

			// Find the correct scale value. It should be the power of 2.
			int width_tmp = o.outWidth, height_tmp = o.outHeight;
			int scale = 1;
			while (true) {
				if (width_tmp < REQUIRED_SIZE && height_tmp < REQUIRED_SIZE)
					break;
				width_tmp /= 2;
				height_tmp /= 2;
				scale *= 2;
			}

			// Decode with inSampleSize
			BitmapFactory.Options o2 = new BitmapFactory.Options();
			o2.inSampleSize = scale;
			bitmap = BitmapFactory.decodeFile(filePath, o2);

			return bitmap;
		}

		@Override
		protected void onCancelled() {
			publishProgress(CANCELED);
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onProgressUpdate(Integer... statusCode) {
			switch (statusCode[0]) {
			case CANCELED:
				removeDialog(PROGRESSDIALOG_ID);
				Toast.makeText(getApplicationContext(), "CANCELED",
						Toast.LENGTH_SHORT).show();
				queueAdapter.notifyDataSetChanged();
				break;
			case OTHER_INTERNAL_ERROR:
				removeDialog(PROGRESSDIALOG_ID);
				Toast.makeText(getApplicationContext(), "OTHER_INTERNAL_ERROR",
						Toast.LENGTH_LONG).show();
				break;
			case SECURITY_ERROR:
				removeDialog(PROGRESSDIALOG_ID);
				Toast.makeText(getApplicationContext(), "SECURITY_ERROR",
						Toast.LENGTH_LONG).show();
				break;
			case SERVER_STATUS_UPLOADED:
				if (!uploadFlag)
					uploadFlag = true;
			default:
				progressDialog.setProgress(uploadCounter);
				queueAdapter.notifyDataSetChanged();
			}
		}

		@SuppressWarnings("deprecation")
		@Override
		protected void onPostExecute(String sResponse) {
			try {
				removeDialog(PROGRESSDIALOG_ID);

				AlertDialog.Builder builder = new AlertDialog.Builder(
						UploadQueue_Activity.this);

				// File imageFile_del = new
				// File(imageFile_new.getAbsolutePath());
				if (queueAdapter.getCount() > 0) {
					String message = "ข้อมูลถูกส่งเรียบร้อยแล้ว";

					builder.setMessage(message)
							.setCancelable(false)
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											Intent intent = new Intent();
											if (uploadFlag)
												setResult(RESULT_OK, intent);
											else
												setResult(RESULT_CANCELED,
														intent);
											UploadQueue_Activity.this.finish();
										}
									});
					final AlertDialog alert = builder.create();

					// Extra screen wake up notification
					WindowManager.LayoutParams winParams = alert.getWindow()
							.getAttributes();
					winParams.flags |= (WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
					alert.getWindow().setAttributes(winParams);

					alert.show();

					// Extra vibrate notification
					((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
							.vibrate(1000);

				} else {
					builder.setMessage("Please select photo to upload")
							.setCancelable(false)
							.setPositiveButton("Ok",
									new DialogInterface.OnClickListener() {
										public void onClick(
												DialogInterface dialog, int id) {
											dialog.cancel();
										}
									});
					final AlertDialog alert = builder.create();

					// Extra screen wake up notification
					WindowManager.LayoutParams winParams = alert.getWindow()
							.getAttributes();
					winParams.flags |= (WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
					alert.getWindow().setAttributes(winParams);

					alert.show();

					// Extra vibrate notification
					((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
							.vibrate(1000);
				}

			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	void send() {

		RequestBody formBody = new FormEncodingBuilder()
				.add("customer_id", id)
				.add("eclaim_subject", subject.getText().toString())
				.add("eclaim_detail", detail.getText().toString())
				.add("eclaim_notify", num_active.getText().toString())
				.add("eclaim_policy", num_policy.getText().toString())
				.add("car_id", num_car.getText().toString())
				.add("eclaim_date_event", datepicker.getText().toString())
				.add("eclaim_name", name.getText().toString())
				.add("eclaim_location_event",
						location_action.getText().toString())
				.add("eclaim_address", address.getText().toString())
				.add("eclaim_phone", phone.getText().toString())
				.add("eclaim_status_car", status_car.getText().toString())
				.add("eclaim_loss", loss.getText().toString())
				.add("eclaim_location_check",
						location_check.getText().toString()).build();
		StrictMode.setThreadPolicy(policy);

		try {
			JSONObject json_data = new JSONObject(okHttp.POST(
					Login_Activity.nameHost + "inserteclaim.php", formBody));
			String code1 = (json_data.getString("code"));
			if (code1.equals("1")) {
				Log.e("Insert", "Inserted Successfully");
			} else {
				Log.e("Insert", "Sorry Try Again");

			}
		} catch (Exception e) {
			Log.e("Fail 3", e.toString());
		}
	}

	// always verify the host - don't check for certificate
	final static HostnameVerifier DO_NOT_VERIFY = new HostnameVerifier() {
		public boolean verify(String hostname, SSLSession session) {
			return true;
		}
	};

	@SuppressWarnings("deprecation")
	@Override
	protected void onDestroy() {
		removeDialog(PROGRESSDIALOG_ID);
		if (uploadTask != null && uploadTask.getStatus() != Status.FINISHED)
			uploadTask.cancel(true);
		super.onDestroy();
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if ((keyCode == KeyEvent.KEYCODE_BACK)) {
			backToMain();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	public void setColorDialog() {
		Dialog d = builder.show();
		int dividerId = d.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = d.findViewById(dividerId);
		divider.setBackgroundColor(this.getResources().getColor(
				R.color.default_pink));

		int textViewId = d.getContext().getResources()
				.getIdentifier("android:id/alertTitle", null, null);
		TextView tv = (TextView) d.findViewById(textViewId);
		tv.setTextColor(this.getResources().getColor(R.color.default_pink));
	}

	public void setUpColorProgressDialog() {
		int dividerId = progressDialog.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = progressDialog.findViewById(dividerId);
		divider.setBackgroundColor(getResources()
				.getColor(R.color.default_pink));

		int textViewId = progressDialog.getContext().getResources()
				.getIdentifier("android:id/alertTitle", null, null);
		TextView tv = (TextView) progressDialog.findViewById(textViewId);
		tv.setTextColor(getResources().getColor(R.color.default_pink));
	}
}
