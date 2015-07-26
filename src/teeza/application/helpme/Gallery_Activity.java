package teeza.application.helpme;

import java.util.ArrayList;

import teeza.application.helpme.R;
import teeza.application.helpme.adapter.ImageAdapter;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.model.ImageItem;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.GridView;
import android.widget.Toast;

public class Gallery_Activity extends Activity {
	private ImageAdapter imageAdapter;
	private ArrayList<ImageItem> images;
	private GridView imagegrid;
	private ApplicationStatus appStatus;
	private boolean isCancel;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	/** Called when the activity is first created. */
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.helpme);

		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		images = new ArrayList<ImageItem>();
		imageAdapter = new ImageAdapter(this, images);
		imageAdapter.initialize();
		imagegrid = (GridView) findViewById(R.id.PhoneImageGrid);
		imagegrid.setAdapter(imageAdapter);

		final Button back = (Button) findViewById(R.id.Button01);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				isCancel = true;
				finish();
			}
		});
		
		final Button selectBtn = (Button) findViewById(R.id.selectBtn);
		selectBtn.setOnClickListener(new OnClickListener() {

			public void onClick(View v) {
				final int len = images.size();
				int cnt = 0;
				String selectImages = "";
				for (int i = 0; i < len; i++) {
					if (images.get(i).isSelection()) {
						cnt++;
						selectImages = selectImages + images.get(i).getId()
								+ ",";
					}
				}
				if (cnt == 0) {
					Toast.makeText(getApplicationContext(),
							"Please select at least one image",
							Toast.LENGTH_LONG).show();

				} else {
					selectImages = selectImages.substring(0,
							selectImages.lastIndexOf(","));
					Intent returnIntent = new Intent();
					returnIntent.putExtra("Ids", selectImages);
					setResult(RESULT_OK, returnIntent);
					isCancel = true;
					finish();
				}

			}
		});

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
		appStatus.setOpenImage(false);

	}

	@Override
	protected void onPause() {
		super.onPause();
		if (!isCancel && !appStatus.isOpenImage())
			appStatus.onPause();
	}

	@Override
	public void onBackPressed() {
		isCancel = true;
		super.onBackPressed();
	}

}
