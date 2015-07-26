package teeza.application.helpme.view.fragment;

import teeza.application.helpme.R;
import teeza.application.helpme.model.ApplicationStatus;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;

public class ContactUs_Fragment extends Fragment {

	private ApplicationStatus appStatus;
	private View rootView;
	private Button cancel;
	
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		appStatus = ApplicationStatus.getInstance();
		super.onCreate(savedInstanceState);
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.contact_us, container, false);
		if (appStatus.isOnline(getActivity())) start();
		else appStatus.setNetwork(getActivity());
		return rootView;
	}

	private void start(){
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