package teeza.application.helpme;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.R;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowDetailEClaim_Activity extends Activity {
	private ApplicationStatus appStatus;
	private GridView gridView;
	private ImageAdapter imageAdapter;
	private ArrayList<HashMap<String, Object>> MyArrList = new ArrayList<HashMap<String, Object>>();
	private String url = Login_Activity.nameHost + "selectpictureeclaim.php";
	private String url2 = Login_Activity.nameHost + "selecteclaims.php";
	private String Link = Login_Activity.nameHost + "test/test/pdf.php";
	private String ec_id, PDF;
	private TextView detail, date, price, status, subject, carcity, carid;
	private TextView num_policy, name, date_event, location_event, status_car,
			loss;
	private Button back;
	private LinearLayout download, money;
	private DecimalFormat myFormatter = new DecimalFormat("###,###,###");
	private ImageView imagestatus;
	private boolean isCancel;
	private OKHttp okHttp;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.showlist);

		// Permission StrictMode
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}
		okHttp = new OKHttp();
		Intent intent = getIntent();
		ec_id = intent.getStringExtra("eclaim_id");

		appStatus = ApplicationStatus.getInstance();
		appStatus.onCreate();

		num_policy = (TextView) findViewById(R.id.num_policy);
		name = (TextView) findViewById(R.id.name);
		date_event = (TextView) findViewById(R.id.date_event);
		location_event = (TextView) findViewById(R.id.location_event);
		status_car = (TextView) findViewById(R.id.status_car);
		loss = (TextView) findViewById(R.id.loss);

		subject = (TextView) findViewById(R.id.subjectshow);
		detail = (TextView) findViewById(R.id.detailshow);
		date = (TextView) findViewById(R.id.dateeclaim);
		carid = (TextView) findViewById(R.id.carid);
		carcity = (TextView) findViewById(R.id.carcity);
		price = (TextView) findViewById(R.id.price);
		status = (TextView) findViewById(R.id.status);
		download = (LinearLayout) findViewById(R.id.download);
		money = (LinearLayout) findViewById(R.id.money);
		imagestatus = (ImageView) findViewById(R.id.imagestatus);
		imageAdapter = new ImageAdapter(getApplicationContext());
		gridView = (GridView) findViewById(R.id.gridView1);
		back = (Button) findViewById(R.id.button1);
		back.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				isCancel = true;
				finish();
			}
		});
		Searchdata();
		Searchpic();
		gridView.setAdapter(imageAdapter);
		download.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Log.e("eclaim_id", ec_id);
				PDF = Link + "?eclaim_id=" + ec_id + "";
				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_VIEW);
				intent.addCategory(Intent.CATEGORY_BROWSABLE);
				intent.setData(Uri.parse("http://docs.google.com/viewer?url="
						+ PDF));
				startActivity(intent);
				appStatus.setIsInPage(true);
			}
		});
	}

	private void Searchdata() {
		RequestBody formBody = new FormEncodingBuilder()
				.add("eclaim_id", ec_id).build();
		try {
			JSONArray data = new JSONArray(okHttp.POST(url2, formBody));
			JSONObject c = data.getJSONObject(0);
			subject.setText(c.getString("eclaim_subject"));
			detail.setText(c.getString("eclaim_detail"));
			carid.setText(c.getString("car_id"));
			carcity.setText(c.getString("car_city"));

			num_policy.setText(c.getString("eclaim_policy"));
			name.setText(c.getString("eclaim_name"));
			date_event.setText(c.getString("eclaim_date_event"));
			location_event.setText(c.getString("eclaim_location_event"));
			status_car.setText(c.getString("eclaim_status_car"));
			loss.setText(c.getString("eclaim_loss"));

			String pricedeci = myFormatter.format(c.getInt("eclaim_price"));
			price.setText(pricedeci);
			date.setText(c.getString("eclaim_date") + "    Ref."
					+ c.getString("eclaim_notify"));
			status.setText(c.getString("eclaim_status"));

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		String check = status.getText().toString();
		if (check.equals("อนุมัติ")) {
			imagestatus.setImageResource(R.drawable.icon_ok_big);
		} else {
			download.setVisibility(LinearLayout.INVISIBLE);
			money.setVisibility(LinearLayout.INVISIBLE);
		}
	}

	public void Searchpic() {
		RequestBody formBody = new FormEncodingBuilder()
				.add("eclaim_id", ec_id).build();
		MyArrList.clear();
		try {
			JSONArray data = new JSONArray(okHttp.POST(url, formBody));
			HashMap<String, Object> map;

			for (int i = 0; i < data.length(); i++) {
				JSONObject c = data.getJSONObject(i);

				map = new HashMap<String, Object>();
				map.put("eclaim_picture_name",
						c.getString("eclaim_picture_name"));
				map.put("ImageThumBitmap",
						(Bitmap) loadBitmap(c.getString("eclaim_picture_name")));
				MyArrList.add(map);

			}

		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	class ImageAdapter extends BaseAdapter {

		private Context mContext;

		public ImageAdapter(Context context) {
			mContext = context;
		}

		public int getCount() {
			return MyArrList.size();
		}

		public Object getItem(int position) {
			return MyArrList.get(position);
		}

		public long getItemId(int position) {
			return position;
		}

		public View getView(final int position, View convertView,
				ViewGroup parent) {

			LayoutInflater inflater = (LayoutInflater) mContext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

			if (convertView == null) {
				convertView = inflater.inflate(R.layout.showlistitem, null);
			}

			// ColImage
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.picplace);
			imageView.setPadding(20, 20, 20, 0);
			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			try {
				imageView.setImageBitmap((Bitmap) MyArrList.get(position).get(
						"ImageThumBitmap"));
			} catch (Exception e) {
				// When Error
				imageView
						.setImageResource(android.R.drawable.ic_menu_report_image);
			}
			imageView.setOnClickListener(new OnClickListener() {

				@Override
				public void onClick(View arg0) {
					String namepic = (String) MyArrList.get(position).get(
							"eclaim_picture_name");
					Intent intent = new Intent(getApplicationContext(),
							Fullimage_Activity.class);
					intent.putExtra("eclaim_picture_name", namepic);
					startActivity(intent);
				}
			});
			return convertView;

		}

	}

	/***** Get Image Resource from URL (Start) *****/
	private static final String TAG = "Image";
	private static final int IO_BUFFER_SIZE = 1 * 1024;

	public static Bitmap loadBitmap(String url) {
		Bitmap bitmap = null;
		InputStream in = null;
		BufferedOutputStream out = null;
		// "http://103.253.75.75/~iufoorg/android/myfile/"+
		try {
			in = new BufferedInputStream(new URL(Login_Activity.nameHost
					+ "myfile/" + url).openStream(), IO_BUFFER_SIZE);

			final ByteArrayOutputStream dataStream = new ByteArrayOutputStream();
			out = new BufferedOutputStream(dataStream, IO_BUFFER_SIZE);
			copy(in, out);
			out.flush();

			final byte[] data = dataStream.toByteArray();
			BitmapFactory.Options options = new BitmapFactory.Options();
			// options.inSampleSize = 1;

			bitmap = BitmapFactory.decodeByteArray(data, 0, data.length,
					options);
		} catch (IOException e) {
			Log.e(TAG, "Could not load Bitmap from: " + url);
			e.printStackTrace();
		} finally {
			closeStream(in);
			closeStream(out);
		}

		return bitmap;
	}

	private static void closeStream(Closeable stream) {
		if (stream != null) {
			try {
				stream.close();
			} catch (IOException e) {
				android.util.Log.e(TAG, "Could not close stream", e);
			}
		}
	}

	private static void copy(InputStream in, OutputStream out)
			throws IOException {
		byte[] b = new byte[IO_BUFFER_SIZE];
		int read;
		while ((read = in.read(b)) != -1) {
			out.write(b, 0, read);
		}
	}

	/***** Get Image Resource from URL (End) *****/

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {

		if (requestCode == 1) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
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
		if (!isCancel)
			appStatus.onPause();
	}

	@Override
	public void onBackPressed() {
		super.onBackPressed();
		isCancel = true;
	}
}
