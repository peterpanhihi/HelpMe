package teeza.application.helpme.view.fragment;

import java.util.Arrays;

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
import android.widget.TextView;

public class TermAndCondition_Fragment extends Fragment {

	private View rootView;
	private Button cancel;
	private TextView content;
	private String[] strContent;
	private ApplicationStatus appStatus;
	
	public void onCreate(Bundle savedInstanceState) {
		setRetainInstance(true);
		super.onCreate(savedInstanceState);
		appStatus = ApplicationStatus.getInstance();
		strContent =  getResources().getStringArray(R.array.term_and_condition); 
	}

	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		rootView = inflater.inflate(R.layout.term_and_condition, container, false);
		if (appStatus.isOnline(getActivity())) start();
		else appStatus.setNetwork(getActivity());
		return rootView;
	}

	private void start(){
		cancel = (Button) rootView.findViewById(R.id.cancel);
		content = (TextView) rootView.findViewById(R.id.term_and_condition_textview);
	
		String temContent;
		temContent = (Arrays.toString(strContent).replaceAll("\\[|\\]", ""));
		content.setText(temContent.replaceAll("\\,", "\n"));
		
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