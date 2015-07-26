package teeza.application.helpme.view.fragment;


import teeza.application.helpme.R;
import teeza.application.helpme.model.ApplicationStatus;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

public class AboutApplication_Fragment extends Fragment {
	private View rootView;
	private Button cancel;
	private TextView aboutApp;
	private ApplicationStatus appStatus;
	
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		appStatus = ApplicationStatus.getInstance();
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.about_app, container, false);
		if (appStatus.isOnline(getActivity()))
			start();
		else appStatus.setNetwork(getActivity());
		return rootView;
	}

	private void start(){
		aboutApp = (TextView) rootView.findViewById(R.id.aboutApp);
		aboutApp.setText(getResources().getString(R.string.about_app));
		
		cancel = (Button) rootView.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				back();
			}
		});		
	}
	
	public void back() {
		SettingMenu_Fragment menu = new SettingMenu_Fragment();
		FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
		ft.replace(R.id.frame_container, menu);
		ft.commit();
	}

}