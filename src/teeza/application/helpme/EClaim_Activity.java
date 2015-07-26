package teeza.application.helpme;

import java.util.ArrayList;

import teeza.application.helpme.R;
import teeza.application.helpme.adapter.ImageAdapter;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.Camera;
import teeza.application.helpme.model.ImageItem;
import teeza.application.helpme.persistence.UserManager;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class EClaim_Activity extends GMap_Activity {
	private final static int TAKE_IMAGE = 1;
	private final static int UPLOAD_IMAGES = 2;
	private final static int PIN_ACTIVITY = 4;
	
	private ImageAdapter imageAdapter;
	private ArrayList<ImageItem> images;
	private Uri imageUri;
	private MediaScannerConnection mScanner;
	private GridView imagegrid;
	private UserManager mManager;
	private String id;

	private ApplicationStatus appStatus;
	private Camera camera;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.eclaim);
		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		camera = new Camera(this);
		mManager = new UserManager(this);
		id = mManager.getID().toString();
		images = new ArrayList<ImageItem>();
		imageAdapter = new ImageAdapter(this, images);
		imageAdapter.initialize();
		imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
		imagegrid.setAdapter(imageAdapter);

		final Button selectBtn = (Button) findViewById(R.id.selectBtn);
		selectBtn.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				final int len = images.size();
				int cnt = 0;
				String selectImages = "";
				for (int i = 0; i < len; i++) {
					if (images.get(i).isSelection()) {
						cnt++;
						selectImages = selectImages
								+ images.get(i).getId() + ",";
					}
				}
				if (cnt == 0) {
					Toast.makeText(getApplicationContext(),"Please select at least one image",Toast.LENGTH_LONG).show();
				} else {
					selectImages = selectImages.substring(0,
							selectImages.lastIndexOf(","));
					Intent returnIntent = new Intent();
					returnIntent.putExtra("Ids", selectImages);
					returnIntent.putExtra("result", "true");
					setResult(RESULT_OK, returnIntent);
					finish();
				}
			}
		});

		final ImageView captureBtn = (ImageView) findViewById(R.id.captureBtn);
		captureBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				camera.setUp(id);
				imageUri = camera.getUri();

				Intent intent = new Intent(
						android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
				intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
				startActivityForResult(intent, TAKE_IMAGE);

			}
		});

		final Button cancel = (Button) findViewById(R.id.Button01);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.putExtra("result", "true");
				setResult(RESULT_CANCELED, intent);
				finish();
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		switch (requestCode) {
		case TAKE_IMAGE:
			appStatus.setIsInPage(true);
			try {
				if (resultCode == RESULT_OK) {
					camera.result(data, mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
					
					mScanner = new MediaScannerConnection(
							EClaim_Activity.this,
							new MediaScannerConnection.MediaScannerConnectionClient() {
								public void onMediaScannerConnected() {
									mScanner.scanFile(imageUri.getPath(), null /* mimeType */);
								}

								public void onScanCompleted(String path, Uri uri) {
									
									if (path.equals(imageUri.getPath())) {
										mScanner.disconnect();
										EClaim_Activity.this
												.runOnUiThread(new Runnable() {
													public void run() {
														updateUI();
													}
												});
									}
								}
							});
					mScanner.connect();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;
		case UPLOAD_IMAGES:
			if (resultCode == RESULT_OK) {
				String result = data.getStringExtra("result");
				appStatus.setIsInPage(Boolean.parseBoolean(result));
			}
			break;
		case PIN_ACTIVITY:
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
			}
		}
	}

	public void updateUI() {
		imageAdapter.checkForNewImages();
	}

	@Override
	public void onResume() {
		super.onResume();
		if (appStatus.checkStatus()) {
			Intent intent = new Intent(getApplicationContext(),
					Pin_Activity.class);
			startActivityForResult(intent, PIN_ACTIVITY);
		}
		appStatus.setOpenImage(false);
	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!appStatus.isOpenImage()) {
			appStatus.onPause();
		}
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		Intent intent = new Intent();
		setResult(RESULT_CANCELED, intent);
		finish();
	}
}
