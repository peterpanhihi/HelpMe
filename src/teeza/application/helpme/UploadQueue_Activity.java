package teeza.application.helpme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.SSLSession;

import org.json.JSONException;
import org.json.JSONObject;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.ResultCallback;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.PlaceBuffer;
import com.google.android.gms.location.places.Places;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.R;
import teeza.application.helpme.adapter.PlaceAutocompleteAdapter;
import teeza.application.helpme.adapter.QueueAdapter;
import teeza.application.helpme.custom_view.ExpandableHeightGridView;
import teeza.application.helpme.date_time_picker.DateTime;
import teeza.application.helpme.date_time_picker.DateTimePicker;
import teeza.application.helpme.date_time_picker.SimpleDateTimePicker;
import teeza.application.helpme.http.HttpFileUpload;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.QueueItem;
import teeza.application.helpme.persistence.UserManager;
import teeza.application.helpme.view.fragment.GPS_Fragment;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class UploadQueue_Activity extends GMap_Activity implements
		DateTimePicker.OnDateTimeSetListener {
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
	private int uploadCounter;
	private boolean uploadFlag;
	private Activity activity;
	private Builder builder;
	private EditText detail, subject, num_active, num_policy, num_car,
			datepicker, name, phone, loss, date_accident;
	protected GoogleApiClient googleApiClient;
	private AutoCompleteTextView location_action;
	private PlaceAutocompleteAdapter mAdapter;
	private Spinner status_car;
	private Button back;
	private File imageFile_new;
	private ImageView btnCal;

	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	private OKHttp okHttp;
	private ApplicationStatus appStatus;
	private SimpleDateTimePicker simpleDateTimePicker;

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
		Log.i("OnCreate UploadQueue","isInPage " + appStatus.isInPage() + " isInApp " +appStatus.isInApp());

		googleApiClient = new GoogleApiClient.Builder(this)
				.addApi(Places.PLACE_DETECTION_API)
				.addOnConnectionFailedListener(this)
				.addApi(Places.GEO_DATA_API).build();

		mAdapter = new PlaceAutocompleteAdapter(this,
				R.layout.autocomplete_list, googleApiClient,
				GPS_Fragment.BOUNDS_GREATER_SYDNEY, null);

		detail = (EditText) findViewById(R.id.detail);
		subject = (EditText) findViewById(R.id.Subject);
		num_active = (EditText) findViewById(R.id.num_active);
		num_policy = (EditText) findViewById(R.id.num_policy);
		num_car = (EditText) findViewById(R.id.num_car);
		datepicker = (EditText) findViewById(R.id.datepiker);
		name = (EditText) findViewById(R.id.name);
		location_action = (AutoCompleteTextView) findViewById(R.id.location_action);
		location_action.setOnItemClickListener(autoOnClickPlaceSearch);
		location_action.setAdapter(mAdapter);
		phone = (EditText) findViewById(R.id.phone);
		status_car = (Spinner) findViewById(R.id.status_car);
		loss = (EditText) findViewById(R.id.loss);
		date_accident = (EditText) findViewById(R.id.date_accident);
		btnCal = (ImageView) findViewById(R.id.calendar);
		activity = this;
		sp = this.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);

		String[] status = { "ใช้งานได้", "ใช้งานไม่ได้" };
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				R.layout.spiner_layout, R.id.textspin, status);
		status_car.setAdapter(adapter);

		Intent intent = getIntent();
		num_policy.setText(intent.getStringExtra("car_policy"));
		num_car.setText(intent.getStringExtra("car_id"));

		String url = Login_Activity.nameHost + "check_policy.php";
		RequestBody formBody = new FormEncodingBuilder()
				.add("num_policy", num_policy.getText().toString())
				.add("num_car", num_car.getText().toString())
				.add("customer_id", id).build();

		try {
			JSONObject json_data = new JSONObject(okHttp.POST(url, formBody));
			num_active.setText(json_data.getString("active"));

		} catch (JSONException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		subject.setNextFocusDownId(R.id.name);

		// Get current date by calender

		DateFormat df = new SimpleDateFormat("EEE, d MMM yyyy, HH:mm a");
		String date = df.format(Calendar.getInstance().getTime());
		datepicker.setText(date);

		name.setText(mManager.getuser().getname());
		name.setNextFocusDownId(R.id.date_accident);

		simpleDateTimePicker = SimpleDateTimePicker.make(
				"Set Date & Time Title", new Date(), this,
				getSupportFragmentManager());

		btnCal.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				simpleDateTimePicker.show();
			}
		});

		phone.setText(mManager.getuser().getphone());

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
			appStatus.setIsInPage(true);
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

				startUploadBtn.setEnabled(false);
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
							startUploadBtn.setEnabled(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่ชื่อเหตุการณ์", Toast.LENGTH_LONG)
									.show();
						} else if ((num_policy.getText().toString()).equals("")) {
							num_policy.setFocusable(true);
							startUploadBtn.setEnabled(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่เลขที่กรมธรรม์", Toast.LENGTH_LONG)
									.show();
						} else if ((num_car.getText().toString()).equals("")) {
							num_car.setFocusable(true);
							startUploadBtn.setEnabled(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่ทะเบียนรถ", Toast.LENGTH_SHORT)
									.show();
						} else if ((name.getText().toString()).equals("")) {
							name.setFocusable(true);
							startUploadBtn.setEnabled(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่ชื่อผู้เอาประกัน",
									Toast.LENGTH_SHORT).show();
						} else if ((datepicker.getText().toString()).equals("")) {
							datepicker.setFocusable(true);
							startUploadBtn.setEnabled(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่วันที่เกิดเหตุ",
									Toast.LENGTH_SHORT).show();
						} else if(date_accident.getText().toString().equals("")){
							date_accident.setFocusable(true);
							startUploadBtn.setEnabled(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่วันและเวลาที่เกิดเหตุ",
									Toast.LENGTH_SHORT).show();
						} else if ((location_action.getText().toString())
								.equals("")) {
							location_action.setFocusable(true);
							startUploadBtn.setEnabled(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่สถานที่เกิดเหตุ",
									Toast.LENGTH_SHORT).show();
						} else if ((detail.getText().toString()).equals("")) {
							detail.setFocusable(true);
							startUploadBtn.setEnabled(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่รายละเอียดการเกิดเหตุ",
									Toast.LENGTH_SHORT).show();
						} else if ((phone.getText().toString()).equals("")) {
							phone.setFocusable(true);
							startUploadBtn.setEnabled(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่เบอร์โทรศัพท์ที่ติดต่อได้",
									Toast.LENGTH_SHORT).show();
						} else if(loss.getText().toString().equals("")) {
							loss.setEnabled(true);
							startUploadBtn.setEnabled(true);
							Toast.makeText(getApplication(),
									"กรุณาใส่ระบุความเสียหาย",
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

		super.onCreate(savedInstanceState);
	}

	protected AdapterView.OnItemClickListener autoOnClickPlaceSearch = new AdapterView.OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			appStatus.checkLocation(activity);
			final PlaceAutocompleteAdapter.PlaceAutocomplete item = mAdapter
					.getItem(position);
			final String placeId = String.valueOf(item.placeId);
			PendingResult<PlaceBuffer> placeResult = Places.GeoDataApi
					.getPlaceById(googleApiClient, placeId);
			placeResult.setResultCallback(mUpdatePlaceDetailsCallback);

		}
	};

	private ResultCallback<PlaceBuffer> mUpdatePlaceDetailsCallback = new ResultCallback<PlaceBuffer>() {
		@Override
		public void onResult(PlaceBuffer places) {
			if (!places.getStatus().isSuccess()) {
				// Request did not complete successfully
				Log.e("GSP", "Place query did not complete. Error: "
						+ places.getStatus().toString());
				places.release();
				return;
			}
			// Get the Place object from the buffer.
			final Place place = places.get(0);

			// Display the third party attributions if set.
			final CharSequence thirdPartyAttribution = places.getAttributions();

			places.release();
		}
	};

	public void backToMain() {
		builder = new AlertDialog.Builder(activity);
		builder.setTitle("เคลมย้อนหลัง");
		builder.setMessage("ต้องการยกเลิกการทำงาน ? ");
		builder.setNegativeButton("ใช่", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(activity, Main_Activity.class);
				intent.putExtra("isInPage", true);
				startActivity(intent);
			}
		});
		builder.setPositiveButton("ไม่ใช่", null);
		setColorDialog();
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
	public void onResume() {Log.i("onResume UploadQueue","isInPage " + appStatus.isInPage() + " isInApp " +appStatus.isInApp());
		if (appStatus.checkStatus()) {
			Intent intent = new Intent(getApplicationContext(), Pin_Activity.class);
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
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
			progressDialog.setMessage("Please wait...");
			progressDialog.setOnCancelListener(new OnCancelListener() {

				public void onCancel(DialogInterface dialog) {
					if (uploadTask != null && uploadTask.getStatus() != AsyncTask.Status.FINISHED)
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

				builder = new AlertDialog.Builder(activity);

				// File imageFile_del = new
				// File(imageFile_new.getAbsolutePath());
				if (queueAdapter.getCount() > 0) {
					String message = "ข้อมูลถูกส่งเรียบร้อยแล้ว";
					builder.setTitle("เคลมย้อนหลัง");
					builder.setMessage(message);
					builder.setNegativeButton("Ok",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									Intent intent = new Intent(activity,
											Main_Activity.class);
									intent.putExtra("isInPage", true);
									startActivity(intent);
								}
							});
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
				}

				setColorDialog();
				// Extra vibrate notification
				((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
						.vibrate(1000);

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
				.add("eclaim_phone", phone.getText().toString())
				.add("eclaim_status_car",
						status_car.getSelectedItem().toString())
				.add("eclaim_loss", loss.getText().toString()).build();

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
	public void onBackPressed() {
		backToMain();
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

	@Override
	public void DateTimeSet(Date date) {
		DateTime mDateTime = new DateTime(date);
		date_accident.setText(mDateTime.getDateString());
	}

	@Override
	public void onStart() {
		super.onStart();
		googleApiClient.connect();
	}

	@Override
	public void onStop() {
		googleApiClient.disconnect();
		super.onStop();
	}
}
