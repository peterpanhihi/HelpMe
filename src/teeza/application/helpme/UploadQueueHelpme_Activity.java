package teeza.application.helpme;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;
import com.squareup.otto.Subscribe;

import teeza.application.helpme.R;
import teeza.application.helpme.adapter.QueueAdapter;
import teeza.application.helpme.http.HttpFileUpload;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.Camera;
import teeza.application.helpme.model.ImageItem;
import teeza.application.helpme.model.QueueItem;
import teeza.application.helpme.persistence.UserManager;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.os.Vibrator;
import android.os.AsyncTask.Status;
import android.provider.MediaStore;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class UploadQueueHelpme_Activity extends Activity {
	private QueueAdapter queueAdapter;
	private GridView UploadList;
	private String id;
	private ProgressDialog progressDialog;

	private static final int PROGRESSDIALOG_ID = 0;
	private static final int CANCELED = -4;
	private static final int OTHER_INTERNAL_ERROR = -3;
	private static final int SECURITY_ERROR = -2;
	private static final int SERVER_STATUS_UPLOADED = 1;

	private UserManager mManager;
	private UploadTask uploadTask;
	private int uploadCounter;
	private boolean uploadFlag;
	private EditText detail;
	private double lng, lat;
	private String ids = "";
	private Uri imageUri;
	private StrictMode.ThreadPolicy policy;
	public ArrayList<ImageItem> images = new ArrayList<ImageItem>();
	private ApplicationStatus appStatus;
	private AlertDialog.Builder builder;
	private LayoutInflater inflater;
	private View layout;
	private boolean isCancel;
	private Camera camera;
	private MediaScannerConnection mScanner;
	private OKHttp okHttp;
	private Context mContext;

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.uploadqueuehelpme);
		policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
		StrictMode.setThreadPolicy(policy);
		mManager = new UserManager(this);
		queueAdapter = new QueueAdapter(this);
		okHttp = new OKHttp();
		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();
		mContext = this;

		Intent intent = getIntent();
		id = mManager.getID().toString();
		lat = intent.getDoubleExtra("lati", lat);
		lng = intent.getDoubleExtra("long", lng);

		detail = (EditText) findViewById(R.id.autoCompleteTextView1);
		UploadList = (GridView) findViewById(R.id.UploadList);
		UploadList.setAdapter(queueAdapter);

		camera = new Camera(this);

		capture();

//		ImageView selectpic = (ImageView) findViewById(R.id.gallery);
//		selectpic.setOnClickListener(new OnClickListener() {
//			@Override
//			public void onClick(View v) {
//				Intent select = new Intent(getApplicationContext(),
//						Gallery_Activity.class);
//				appStatus.setIsInPage(true);
//				startActivityForResult(select, 1);
//			}
//		});

		ImageView capturepic = (ImageView) findViewById(R.id.capturepic);
		capturepic.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				capture();
			}
		});

		final Button startUploadBtn = (Button) findViewById(R.id.StartUploadBtn);
		startUploadBtn.setOnClickListener(new OnClickListener() {

			@SuppressWarnings("deprecation")
			public void onClick(View v) {
				startUploadBtn.setEnabled(false);
				if (queueAdapter.getCount() == 0) {
					Toast.makeText(getApplicationContext(),
							"กรถณาเลือกภาพเพื่อดำเนินการ", Toast.LENGTH_LONG)
							.show();
					startUploadBtn.setEnabled(true);
				} else if(detail.getText().toString().equals("")) {
					Toast.makeText(getApplicationContext(),
							"กรุณาใส่รายละเอียดความเสียหายที่เกิดขึ้น", Toast.LENGTH_LONG)
							.show();
					startUploadBtn.setEnabled(true);
				} else {
					send();
					showDialog(PROGRESSDIALOG_ID);

					int dividerId = progressDialog
							.getContext()
							.getResources()
							.getIdentifier("android:id/titleDivider", null,
									null);
					View divider = progressDialog.findViewById(dividerId);
					divider.setBackgroundColor(getResources().getColor(
							R.color.default_pink));

					int textViewId = progressDialog.getContext().getResources()
							.getIdentifier("android:id/alertTitle", null, null);
					TextView tv = (TextView) progressDialog
							.findViewById(textViewId);
					tv.setTextColor(getResources().getColor(
							R.color.default_pink));
				}
			}
		});

		Button cancel = (Button) findViewById(R.id.Button01);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				cancelProcess();
			}
		});

	}

	public void cancelProcess() {
		builder = new AlertDialog.Builder(mContext);
		builder.setTitle("HelpMe");
		builder.setMessage("ต้องการยกเลิกการทำงาน ? ");
		builder.setNegativeButton("ใช่", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				isCancel = true;
				finish();
			}
		});
		builder.setPositiveButton("ไม่ใช่", null);
		setColorDialog();
	}

	public void capture() {
		camera.setUp(id);
		imageUri = camera.getUri();
		Intent capture = new Intent(
				android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
		capture.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
		appStatus.setIsInPage(true);
		startActivityForResult(capture, 2);
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == RESULT_OK) {
				ids = data.getStringExtra("Ids");
				queueAdapter.initialize(ids);
			}
		} else if (requestCode == 2) {
			if (resultCode == RESULT_OK) {
				camera.result(data, lat, lng);
				mScanner = new MediaScannerConnection(
						UploadQueueHelpme_Activity.this,
						new MediaScannerConnection.MediaScannerConnectionClient() {
							public void onMediaScannerConnected() {
								mScanner.scanFile(imageUri.getPath(), null /* mimeType */);
							}

							public void onScanCompleted(String path, Uri uri) {
								if (path.equals(imageUri.getPath())) {
									mScanner.disconnect();
								}
							}
						});
				mScanner.connect();
				queueAdapter.addcapture(camera.getFile());
				queueAdapter.notifyDataSetChanged();
			}
		} else if (requestCode == 3) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
			}
		}
		appStatus.setIsInPage(true);

	}

	@SuppressWarnings("deprecation")
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case PROGRESSDIALOG_ID:
			removeDialog(PROGRESSDIALOG_ID);
			progressDialog = new ProgressDialog(this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
			progressDialog.setProgressDrawable(getResources().getDrawable(
					R.drawable.custom_progressbar));
			progressDialog.setMax(queueAdapter.getUploadCount());
			progressDialog.setTitle("Uploading");
			progressDialog.setCancelable(false);
			progressDialog.setCanceledOnTouchOutside(false);
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

	class UploadTask extends AsyncTask<Void, Integer, String> {
		@Override
		protected String doInBackground(Void... unused) {
			uploadCounter = 0;
			for (QueueItem item : queueAdapter.queueItems) {
				if (item.getUploaded() != 1) {
					File imageFile = new File(item.getPath());

					try {
						Bitmap original = BitmapFactory.decodeFile(imageFile
								.getAbsolutePath());
						FileOutputStream fos = new FileOutputStream(
								imageFile.getAbsolutePath());
						original.compress(Bitmap.CompressFormat.JPEG, 50, fos);

						item.setPath(imageFile.getPath());
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					}
					int status = UploadFile(imageFile);
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
				Toast.makeText(getApplicationContext(),
						getString(R.string.CanceledMessage), Toast.LENGTH_SHORT)
						.show();
				queueAdapter.notifyDataSetChanged();
				break;
			case OTHER_INTERNAL_ERROR:
				removeDialog(PROGRESSDIALOG_ID);
				Toast.makeText(getApplicationContext(),
						getString(R.string.internal_exception_message),
						Toast.LENGTH_LONG).show();
				break;
			case SECURITY_ERROR:
				removeDialog(PROGRESSDIALOG_ID);
				Toast.makeText(getApplicationContext(),
						getString(R.string.security_exception_message),
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
				builder = new AlertDialog.Builder(new ContextThemeWrapper(
						UploadQueueHelpme_Activity.this, R.style.AppTheme));
				inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
				if (queueAdapter.getUploadCount() == 0) {
					if (uploadCounter >= 1) {
						mManager.setStatsend("1");
					}

					layout = inflater.inflate(R.layout.dialog_layout_upload,
							null);
					builder.setCancelable(false).setPositiveButton("ตกลง",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									Intent intent = new Intent();
									if (uploadFlag) {
										intent.putExtra("result", "true");
										setResult(RESULT_OK, intent);
									} else
										setResult(RESULT_CANCELED, intent);
									UploadQueueHelpme_Activity.this.finish();
								}
							});
				} else {
					mManager.setStatsend("0");
					layout = inflater.inflate(R.layout.dialog_layout_error,
							null);
					builder.setCancelable(false).setPositiveButton("Ok",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int id) {
									dialog.cancel();
								}
							});
				}

				final AlertDialog alert = builder.create();

				// Extra screen wake up notification
				WindowManager.LayoutParams winParams = alert.getWindow()
						.getAttributes();
				winParams.flags |= (WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
				alert.getWindow().setAttributes(winParams);
				alert.setView(layout);
				alert.show();

				// Extra vibrate notification
				((Vibrator) getSystemService(Context.VIBRATOR_SERVICE))
						.vibrate(1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public int UploadFile(File f) {

		try {
			// Set your file here
			FileInputStream fstrm = new FileInputStream(f);

			// Set your server page url (and the file title/description)
			HttpFileUpload hfu = new HttpFileUpload(Login_Activity.nameHost
					+ "insertpicture.php", "my file title", detail.getText()
					.toString());

			int success = hfu.Send_Now(fstrm, mManager.getID().toString());

			return success;
		} catch (FileNotFoundException e) {
		}
		return 0;

	}

	public void send() {
		String lati = String.valueOf(lat);
		String longti = String.valueOf(lng);
		StrictMode.setThreadPolicy(policy);

		try {
			RequestBody formBody = new FormEncodingBuilder()
					.add("customer_id", id).add("lati", lati)
					.add("longti", longti)
					.add("symptom", detail.getText().toString())
					.add("Status", "rd").build();
			okHttp.POST(Login_Activity.nameHost + "insert.php", formBody);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

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
			cancelProcess();
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	protected void onResume() {
		super.onResume();
		if (appStatus.checkStatus()) {
			Intent intent = new Intent(getApplicationContext(),
					Pin_Activity.class);
			startActivityForResult(intent, 3);
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
		cancelProcess();
		super.onBackPressed();
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

}
