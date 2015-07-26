package teeza.application.helpme.model;

import teeza.application.helpme.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.provider.Settings;
import android.util.Log;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Pan on 6/8/2015 AD.
 */
public class ApplicationStatus {
	private boolean isFillPin;
	private boolean isFirstLogin;
	private boolean isInApp;
	private boolean isInPage;
	private boolean isCall;
	private boolean isOpenImage;
	private boolean showLocation;
	private boolean isSetLocation;
	private AlertDialog.Builder builder;

	private static ApplicationStatus instance = null;

	protected ApplicationStatus() {
		isFillPin = false;
		isInApp = false;
		isInPage = false;
		isCall = false;
		isOpenImage = false;
	}

	public static ApplicationStatus getInstance() {
		if (instance == null) {
			instance = new ApplicationStatus();
		}
		return instance;
	}

	public boolean isFillPin() {
		return isFillPin;
	}

	public void setIsFillPin(boolean isFillPin) {
		this.isFillPin = isFillPin;
	}

	public boolean isFirstLogin() {
		return isFirstLogin;
	}

	public void setIsFirstLogin(boolean isFirstLogin) {
		this.isFirstLogin = isFirstLogin;

		if (this.isFirstLogin()) {
			setIsFillPin(true);
		}
	}

	public boolean isInApp() {
		return isInApp;
	}

	public void setIsInApp(boolean isInApp) {
		this.isInApp = isInApp;
	}

	public boolean isInPage() {
		return isInPage;
	}

	public void setIsInPage(boolean isInPage) {
		this.isInPage = isInPage;
	}

	public boolean isCall() {
		return isCall;
	}

	public void setIsCall(boolean isCall) {
		this.isCall = isCall;
	}

	public boolean isOpenImage() {
		return isOpenImage;
	}

	public void setOpenImage(boolean isOpenImage) {
		this.isOpenImage = isOpenImage;
	}

	public void reset() {
		this.setIsFirstLogin(false);
		this.setIsFillPin(false);
		this.setIsInApp(false);
		this.setIsInPage(false);
	}

	public void onCreate() {
		this.setIsInPage(true);
		this.setIsInApp(true);
	}

	public void onResume() {
		this.setIsFillPin(true);
		this.setIsInPage(true);
		this.setIsInApp(true);
	}

	public void onPause() {
		if (!isSetLocation) {
			this.setIsInPage(false);
			this.setIsFirstLogin(false);
			this.setIsFillPin(false);
		}
		isSetLocation = false;
	}

	public void onDestroy() {
		this.setIsInApp(false);
	}

	public boolean checkStatus() {
		Log.d("CheckStatus ApplicationStatus ", "isFillPin: " + isFillPin()
				+ " isInApp: " + isInApp() + " isInPage: " + isInPage()
				+ " isSetLocation" + isSetLocation);
		return !this.isFillPin() && !this.isFirstLogin() && this.isInApp()
				&& !this.isInPage() && !isSetLocation;
	}

	public boolean isOpenGPS(Activity activity) {
		LocationManager manager = (LocationManager) activity
				.getSystemService(Context.LOCATION_SERVICE);
		return manager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	/**
	 * 
	 */
	public void checkLocation(final Activity activity) {
		if (activity != null)
			if (!isOpenGPS(activity))
				if (!showLocation) {
					showLocation = true;
					builder = new AlertDialog.Builder(activity);
					builder.setTitle("HelpMe");
					builder.setMessage("Please open GPS");
					builder.setNegativeButton("Setting",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									showLocation = false;
									Intent i = new Intent(
											Settings.ACTION_LOCATION_SOURCE_SETTINGS);
									activity.startActivity(i);
									isSetLocation = true;
								}
							});
					builder.setPositiveButton("Close",
							new DialogInterface.OnClickListener() {

								@Override
								public void onClick(DialogInterface dialog,
										int which) {
									showLocation = false;
									isSetLocation = false;
								}
							});
					setColorDialog(activity);
				}
	}

	public boolean isOnline(Activity activity) {
		ConnectivityManager cm = (ConnectivityManager) activity
				.getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		return netInfo != null && netInfo.isConnectedOrConnecting();
	}

	public void setNetwork(final Activity activity) {
		builder = new AlertDialog.Builder(new ContextThemeWrapper(activity,
				R.style.AppTheme));
		@SuppressWarnings("static-access")
		LayoutInflater inflater2 = (LayoutInflater) activity
				.getSystemService(activity.LAYOUT_INFLATER_SERVICE);
		View layout = inflater2.inflate(R.layout.dialog_wifi, null);
		builder.setCancelable(true);
		builder.setPositiveButton("Close",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						dialog.cancel();
					}
				});
		builder.setNegativeButton("Setting",
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// push statement
						activity.startActivity(new Intent(
								Settings.ACTION_WIFI_SETTINGS));
						dialog.cancel();
					}
				});
		AlertDialog alert11 = builder.create();
		alert11.setView(layout);
		alert11.show();
	}

	public void setColorDialog(Activity activity) {
		Dialog d = builder.show();
		int dividerId = d.getContext().getResources()
				.getIdentifier("android:id/titleDivider", null, null);
		View divider = d.findViewById(dividerId);
		divider.setBackgroundColor(activity.getResources().getColor(
				R.color.default_pink));

		int textViewId = d.getContext().getResources()
				.getIdentifier("android:id/alertTitle", null, null);
		TextView tv = (TextView) d.findViewById(textViewId);
		tv.setTextColor(activity.getResources().getColor(R.color.default_pink));
	}
}
