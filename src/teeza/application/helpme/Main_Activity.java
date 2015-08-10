package teeza.application.helpme;

import teeza.application.helpme.R;
import teeza.application.helpme.adapter.MenuListAdapter;
import teeza.application.helpme.model.ApplicationStatus;
import teeza.application.helpme.persistence.UserManager;
import teeza.application.helpme.view.fragment.Callcenter_Fragment;
import teeza.application.helpme.view.fragment.CheckInsurance_Fragment;
import teeza.application.helpme.view.fragment.EClaimCheckPolicy_Fragment;
import teeza.application.helpme.view.fragment.EClaimDialog_Fragment;
import teeza.application.helpme.view.fragment.EClaim_Fragment;
import teeza.application.helpme.view.fragment.GPS_Fragment;
import teeza.application.helpme.view.fragment.Main_Fragment;
import teeza.application.helpme.view.fragment.SearchPlace_Fragment;
import teeza.application.helpme.view.fragment.SettingMenu_Fragment;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuItem;
import com.google.android.gcm.GCMRegistrar;

public class Main_Activity extends SherlockFragmentActivity {

	Controller aController;

	// Asyntask
	AsyncTask<Void, Void, Void> mRegisterTask;
	public static String idcus;

	// Declare Variables
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private MenuListAdapter mMenuAdapter;
	private UserManager mManager;
	private String[] title, subtitle;
	int[] icon;
	// private CharSequence mDrawerTitle;
	private CharSequence mTitle;
	private int oldposition = 5;
	private TextView mytitle;
	private ImageView myicon;
	private ProgressDialog ringProgressDialog;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();
	final String PREF_NAME = "IMAGE_ID";
	private ApplicationStatus appStatus;
	private int selectedNum = 0;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mManager = new UserManager(this);
		idcus = mManager.getID().toString();
		appStatus = ApplicationStatus.getInstance();
		appStatus.setIsInApp(true);
		setContentView(R.layout.activity_menu);

		Intent intent = getIntent();
		selectedNum = intent.getIntExtra("selectItem", 0);
		appStatus.setIsInPage(intent.getBooleanExtra("isInPage", false));
		appStatus.checkLocation(this);

		// Log.d("Check onCreate MainActivity ",
		// "isFillPin: " + appStatus.isFillPin() + " isInApp: "
		// + appStatus.isInApp() + " isInPage: "
		// + appStatus.isInPage() + " isCall: "
		// + appStatus.isCall());

		title = new String[] { "Helpme", "เครมย้อนหลัง", "การเดินทาง การจราจร",
				"สถานที่เกี่ยวข้อง", "เช็คเบี้ยประกัน", "เบอร์โทรฉุกเฉิน",
				"ตั้งค่า", "ออกจากระบบ" };

		icon = new int[] { R.drawable.menu_1_helpme, R.drawable.menu_2_claim,
				R.drawable.menu_3_traffic, R.drawable.menu_4_places,
				R.drawable.menu_5_insurance, R.drawable.menu_6_call,
				R.drawable.menu_7_setting, R.drawable.menu_8_logout };

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);
		mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
				GravityCompat.START);
		mMenuAdapter = new MenuListAdapter(Main_Activity.this, title, subtitle,
				icon);
		mDrawerList.setAdapter(mMenuAdapter);

		mDrawerList.setOnItemClickListener(new DrawerItemClickListener());

		this.getActionBar().setDisplayShowCustomEnabled(true);
		this.getActionBar().setDisplayShowTitleEnabled(false);

		LayoutInflater inflator = LayoutInflater.from(this);
		View v = inflator.inflate(R.layout.titleview, null);
		mytitle = (TextView) v.findViewById(R.id.title);
		myicon = (ImageView) v.findViewById(R.id.iconmenu);

		this.getActionBar().setCustomView(v);

		getSupportActionBar().setHomeButtonEnabled(true);

		if (savedInstanceState == null) {
			selectItem(selectedNum);
			if(selectedNum == 8) {
				selectedNum = 1;
			}
			mytitle.setText(title[selectedNum]);
			myicon.setImageResource(icon[selectedNum]);
			getSupportActionBar().setIcon(R.drawable.icon_mainmenu);
		}

		aController = (Controller) getApplicationContext();

		// Check if Internet Connection present
		if (!aController.isConnectingToInternet()) {

			// Internet Connection is not present
			aController.showAlertDialog(Main_Activity.this,
					"Internet Connection Error",
					"Please connect to working Internet connection", false);

			// stop executing code by return
			return;
		}

		// Check if GCM configuration is set
		if (Config.YOUR_SERVER_URL == null || Config.GOOGLE_SENDER_ID == null
				|| Config.YOUR_SERVER_URL.length() == 0
				|| Config.GOOGLE_SENDER_ID.length() == 0) {

			// GCM sernder id / server url is missing
			aController.showAlertDialog(Main_Activity.this,
					"Configuration Error!",
					"Please set your Server URL and GCM Sender ID", false);

			// stop executing code by return
			return;
		}

		// Check if Internet present
		if (!aController.isConnectingToInternet()) {

			// Internet Connection is not present
			aController.showAlertDialog(Main_Activity.this,
					"Internet Connection Error",
					"Please connect to Internet connection", false);
			// stop executing code by return
			return;
		}

		// Make sure the device has the proper dependencies.
		GCMRegistrar.checkDevice(this);

		// Make sure the manifest permissions was properly set
		GCMRegistrar.checkManifest(this);

		// Register custom Broadcast receiver to show messages on activity
		registerReceiver(mHandleMessageReceiver, new IntentFilter(
				Config.DISPLAY_MESSAGE_ACTION));

		// Get GCM registration id
		final String regId = GCMRegistrar.getRegistrationId(this);

		// Check if regid already presents
		if (regId.equals("")) {

			// Register with GCM
			GCMRegistrar.register(this, Config.GOOGLE_SENDER_ID);

		} else {

			// Device is already registered on GCM Server
			if (GCMRegistrar.isRegisteredOnServer(this)) {

				// Skips registration.
				Toast.makeText(getApplicationContext(),
						"Already registered with GCM Server", Toast.LENGTH_LONG)
						.show();

			} else {

				// Try to register again, but not in the UI thread.
				// It's also necessary to cancel the thread onDestroy(),
				// hence the use of AsyncTask instead of a raw thread.

				final Context context = this;
				mRegisterTask = new AsyncTask<Void, Void, Void>() {

					@Override
					protected Void doInBackground(Void... params) {

						// Register on our server
						// On server creates a new user
						aController.register(context, idcus, regId);

						return null;
					}

					@Override
					protected void onPostExecute(Void result) {
						mRegisterTask = null;
					}

				};

				// execute AsyncTask
				mRegisterTask.execute(null, null, null);
			}
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getSupportMenuInflater().inflate(R.menu.activity_itemlist, menu);
		MenuItem item_menu = (MenuItem) menu.findItem(R.id.add_item);
		MenuItem item_menu_oil = (MenuItem) menu.findItem(R.id.oil_item);
		if (oldposition != 1)
			item_menu.setVisible(false);
		if (oldposition != 3)
			item_menu_oil.setVisible(false);
		return true;
	}

	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {

		MenuItem item_menu = (MenuItem) menu.findItem(R.id.add_item);
		MenuItem item_menu_oil = (MenuItem) menu.findItem(R.id.oil_item);

		if (oldposition == 1) {
			item_menu.setEnabled(true);
		} else if (oldposition == 3) {
			item_menu_oil.setEnabled(true);
		} else {
			item_menu.setEnabled(false);
			item_menu_oil.setEnabled(false);
		}

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		if (item.getItemId() == android.R.id.home) {

			if (mDrawerLayout.isDrawerOpen(mDrawerList)) {
				mDrawerLayout.closeDrawer(mDrawerList);
			} else {
				mDrawerLayout.openDrawer(mDrawerList);
			}
		} else if (item.getItemId() == R.id.add_item) {
			EClaimCheckPolicy_Fragment edf = new EClaimCheckPolicy_Fragment(
					idcus);
			edf.show(getSupportFragmentManager(), "Dialog");
			appStatus.setIsInPage(true);
		} else if (item.getItemId() == R.id.oil_item) {
			Intent Priceoil = new Intent(getApplicationContext(),
					PriceOil_Activity.class);
			startActivityForResult(Priceoil, 1);
		}

		return super.onOptionsItemSelected(item);
	}

	// ListView click listener in the navigation drawer
	private class DrawerItemClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			mytitle.setText(title[position]);
			myicon.setImageResource(icon[position]);
			getSupportActionBar().setIcon(R.drawable.icon_mainmenu);
			selectItem(position);

			if (position != 6) {
				ringProgressDialog = ProgressDialog.show(Main_Activity.this,
						"Please wait ...", "กรุณารอสักครู่ ...", true);
				ringProgressDialog.setCancelable(true);
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Thread.sleep(1000);
							ringProgressDialog.dismiss();
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}).start();
			}
		}
	}

	private void selectItem(final int position) {
		// Locate Position
		switch (position) {
		case 0:
			if (position != oldposition) {
				Fragment mapfragment = new Main_Fragment();
				mapfragment.getFragmentManager();
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.frame_container, mapfragment);
				ft.commit();

			}
			setOldposition(position);
			appStatus.setIsInApp(true);
			break;
		case 1:
			EClaimDialog_Fragment edf = new EClaimDialog_Fragment(this,
					position, idcus);
			edf.show(getSupportFragmentManager(), "Dialog");

			appStatus.setIsInApp(true);
			break;
		case 2:
			if (position != oldposition) {
				Fragment mapfragment = new GPS_Fragment();
				mapfragment.getFragmentManager();
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.frame_container, mapfragment);
				ft.addToBackStack("tag").commit();
			}
			setOldposition(position);
			appStatus.setIsInApp(true);
			break;
		case 3:
			if (position != oldposition) {
				Fragment mapfragment = new SearchPlace_Fragment();
				mapfragment.getFragmentManager();
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.frame_container, mapfragment);
				ft.commit();
			}
			setOldposition(position);
			appStatus.setIsInApp(true);
			break;
		case 4:
			if (position != oldposition) {
				CheckInsurance_Fragment check = new CheckInsurance_Fragment();
				check.getFragmentManager();
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.frame_container, check);
				ft.commit();
			}
			setOldposition(position);
			appStatus.setIsInApp(true);
			break;
		case 5:
			if (position != oldposition) {
				Callcenter_Fragment call = new Callcenter_Fragment();
				call.getFragmentManager();
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.frame_container, call);
				ft.commit();
			}
			setOldposition(position);
			appStatus.setIsInApp(true);
			break;
		case 6:
			if (position != oldposition) {
				SettingMenu_Fragment setting = new SettingMenu_Fragment();
				setting.getFragmentManager();
				FragmentTransaction ft = getSupportFragmentManager()
						.beginTransaction();
				ft.replace(R.id.frame_container, setting);
				ft.commit();
			}
			setOldposition(position);
			appStatus.setIsInApp(true);
			break;
		case 7:
			if (position != oldposition) {
				mManager.resetStat();
				mManager.resetPin();
				appStatus.reset();
				Intent logout = new Intent(getApplicationContext(),
						Login_Activity.class);
				startActivity(logout);
			}
			setOldposition(position);
			break;
		case 8:	
			EClaim_Fragment claim = new EClaim_Fragment();
			claim.getFragmentManager();
			FragmentTransaction ft = getSupportFragmentManager()
					.beginTransaction();
			ft.replace(R.id.frame_container, claim);
			ft.commit();
			setOldposition(1);
			break;
		}

		mDrawerList.setItemChecked(position, true);
		mDrawerLayout.closeDrawer(mDrawerList);
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getSupportActionBar().setTitle(mTitle);
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1) {
			if (resultCode == this.RESULT_OK) {
				appStatus.onResume();
			}

			if (resultCode == this.RESULT_CANCELED) {
				appStatus.setIsInPage(true);
			}
		}
	}

	@Override
	protected void onResume() {
		super.onResume();
		appStatus.checkLocation(this);
		Log.d("Check onResume MainActivity ",
				"isFillPin: " + appStatus.isFillPin() + " isInApp: "
						+ appStatus.isInApp() + " isInPage: "
						+ appStatus.isInPage() + " isCall: "
						+ appStatus.isCall());
		if (appStatus.checkStatus() && !appStatus.isCall()) {
			Intent intent = new Intent(getApplicationContext(),
					Pin_Activity.class);
			startActivityForResult(intent, 1);
		}
		appStatus.setIsCall(false);

	}

	@Override
	protected void onPause() {
		super.onPause();
		appStatus.onPause();

		Log.d("Check onPause MainActivity ",
				"isFillPin: " + appStatus.isFillPin() + " isInApp: "
						+ appStatus.isInApp() + " isInPage: "
						+ appStatus.isInPage());
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		appStatus.onDestroy();
		Log.d("Check onDestroy MainActivity ",
				"isFillPin: " + appStatus.isFillPin() + " isInApp: "
						+ appStatus.isInApp() + " isInPage: "
						+ appStatus.isInPage());
		// Cancel AsyncTask
		if (mRegisterTask != null) {
			mRegisterTask.cancel(true);
		}
		try {
			// Unregister Broadcast Receiver
			unregisterReceiver(mHandleMessageReceiver);

			// Clear internal resources.
			GCMRegistrar.onDestroy(this);

		} catch (Exception e) {
			Log.e("UnRegister Receiver Error", "> " + e.getMessage());
		}
	}

	public int getOldposition() {
		return oldposition;
	}

	public void setOldposition(int oldposition) {
		this.oldposition = oldposition;
	}

	@Override
	public void onBackPressed() {
	}

	// Create a broadcast receiver to get message and show on screen
	private final BroadcastReceiver mHandleMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context context, Intent intent) {
			String newMessage = "";
			try {
				newMessage = intent.getExtras().getString(Config.EXTRA_MESSAGE);
			} catch (RuntimeException e) {
				e.printStackTrace();
			}

			// Waking up mobile if it is sleeping
			aController.acquireWakeLock(getApplicationContext());

			if (newMessage != null)
				Toast.makeText(getApplicationContext(),
						"Got Message: " + newMessage, Toast.LENGTH_LONG).show();

			// Releasing wake lock
			aController.releaseWakeLock();
		}
	};
}