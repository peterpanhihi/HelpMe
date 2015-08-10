package teeza.application.helpme.view.fragment;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONObject;

import com.squareup.okhttp.FormEncodingBuilder;
import com.squareup.okhttp.RequestBody;

import teeza.application.helpme.Login_Activity;
import teeza.application.helpme.R;
import teeza.application.helpme.ShowDetailEClaim_Activity;
import teeza.application.helpme.http.OKHttp;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.persistence.UserManager;
import android.support.v4.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

public class EClaim_Fragment extends Fragment {
	final String url = Login_Activity.nameHost + "selecteclaim.php";

	private UserManager mManager;
	private String idcus;
	private RelativeLayout progressBar;
	private ListView listView1;
	private ImageAdapter imageAdapter;
	private ArrayList<HashMap<String, Object>> MyArrList = new ArrayList<HashMap<String, Object>>();
	private View rootView;
	private ProgressDialog ringProgressDialog;
	private OKHttp okHttp;
	private ApplicationStatus appStatus;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		mManager = new UserManager(getActivity());
		idcus = mManager.getID().toString();
		setHasOptionsMenu(true);
		appStatus = ApplicationStatus.getInstance();
		okHttp = new OKHttp();

		// Permission StrictMode
		if (android.os.Build.VERSION.SDK_INT > 9) {
			StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
					.permitAll().build();
			StrictMode.setThreadPolicy(policy);
		}

	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.eclaimlist, container, false);
		progressBar = (RelativeLayout) rootView
				.findViewById(R.id.relative_progressBar);
		imageAdapter = new ImageAdapter(getActivity());
		listView1 = (ListView) rootView.findViewById(R.id.listView1);
		listView1.setAdapter(imageAdapter);
		return rootView;
	}

	public void SearchData() {
		try {
			RequestBody formBody = new FormEncodingBuilder()
				.add("customer_id", idcus)
				.build();
			
			MyArrList.clear();

			JSONArray data = new JSONArray(okHttp.POST(url, formBody));
			HashMap<String, Object> map;

			for (int i = 0; i < data.length(); i++) {
				JSONObject c = data.getJSONObject(i);
				map = new HashMap<String, Object>();
				
				map.put("eclaim_id", c.getString("eclaim_id"));
				map.put("eclaim_subdetail", c.getString("eclaim_subject"));
				map.put("eclaim_price", c.getString("eclaim_price"));
				map.put("eclaim_date", c.getString("eclaim_date"));
				map.put("eclaim_status", c.getString("eclaim_status"));
				map.put("ImageThumBitmap",
						(Bitmap) loadBitmap(c.getString("eclaim_picture_name")));
				MyArrList.add(map);
			}

		} catch (Exception e) {
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
				convertView = inflater.inflate(R.layout.activity_column, null);
			}

			// ColImage
			ImageView imageView = (ImageView) convertView
					.findViewById(R.id.piceclaim);
			imageView.setPadding(0, 10, 0, 0);
			imageView.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dialogLoad();
					Intent show = new Intent(getActivity(),
							ShowDetailEClaim_Activity.class);
					show.putExtra("eclaim_id",
							MyArrList.get(position).get("eclaim_id").toString());
					show.putExtra("isInPage", "true");
					startActivity(show);
				}
			});

			imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
			try {
				imageView.setImageBitmap((Bitmap) MyArrList.get(position).get(
						"ImageThumBitmap"));
			} catch (Exception e) {
				// When Error
				imageView
						.setImageResource(android.R.drawable.ic_menu_report_image);
			}
			// ColImgID
			TextView detail = (TextView) convertView
					.findViewById(R.id.detaileclaim);
			detail.setPadding(10, 0, 0, 0);
			detail.setText(MyArrList.get(position).get("eclaim_subdetail")
					.toString());
			detail.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dialogLoad();
					Intent show = new Intent(getActivity(),
							ShowDetailEClaim_Activity.class);
					show.putExtra("eclaim_id",
							MyArrList.get(position).get("eclaim_id").toString());
					show.putExtra("isInPage", "true");
					startActivity(show);
				}
			});

			// ColImgName
			TextView txtdate = (TextView) convertView
					.findViewById(R.id.dateeclaim);
			txtdate.setPadding(10, 0, 0, 0);
			txtdate.setText(MyArrList.get(position).get("eclaim_date")
					.toString());
			txtdate.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View arg0) {
					dialogLoad();
					Intent show = new Intent(getActivity(),
							ShowDetailEClaim_Activity.class);
					show.putExtra("eclaim_id",
							MyArrList.get(position).get("eclaim_id").toString());
					show.putExtra("isInPage", "true");
					startActivity(show);
				}
			});

			TextView txtstatus = (TextView) convertView
					.findViewById(R.id.statuseclaim);
			txtstatus.setText(MyArrList.get(position).get("eclaim_status")
					.toString());
			ImageView imagestatus = (ImageView) convertView
					.findViewById(R.id.imagestatus);

			if (txtstatus.getText().toString().equals("รออนุมัติ")) {
				imagestatus.setImageResource(R.drawable.icon_wait_small);
			} else {
				imagestatus.setImageResource(R.drawable.icon_ok_small);
			}
			return convertView;
		}

	}

	public void dialogLoad() {

		ringProgressDialog = ProgressDialog.show(getActivity(),
				"Please wait ...", "กรุณารอสักครู่ ...", true);
		ringProgressDialog.setCancelable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					// Here you should write your time consuming task...
					// Let the progress ring for 10 seconds...
					Thread.sleep(1000);
					ringProgressDialog.dismiss();
				} catch (Exception e) {

				}
			}
		}).start();
	}

	/***** Get Image Resource from URL (Start) *****/
	private static final String TAG = "Image";
	private static final int IO_BUFFER_SIZE = 100;

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

	@Override
	public void onStart() {
		super.onStart();
		if (appStatus.isOnline(getActivity()))
			new Task().execute();
		else
			appStatus.setNetwork(getActivity());
	}

	@Override
	public void onResume() {
		super.onResume();
		if (appStatus.isOnline(getActivity()))
			new Task().execute();
		else
			appStatus.setNetwork(getActivity());
	}

	class Task extends AsyncTask<String, Integer, Boolean> {
		@Override
		protected void onPreExecute() {
			progressBar.setVisibility(View.VISIBLE);
			listView1.setVisibility(View.GONE);
			super.onPreExecute();
		}

		@Override
		protected Boolean doInBackground(String... st) {
			SearchData();
			return null;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			imageAdapter.notifyDataSetChanged();
			progressBar.setVisibility(View.GONE);
			listView1.setVisibility(View.VISIBLE);
			super.onPostExecute(result);
		}
	}
}