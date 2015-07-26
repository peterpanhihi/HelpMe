package teeza.application.helpme.view.fragment;

import teeza.application.helpme.R;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.ListFragment;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Toast;

public class SettingMenu_Fragment extends ListFragment {
	final String[] menus = new String[] { "Profile Setting",
			"Notification Setting (On/Off)", "About Software (App Version)",
			"Term and Condition", "Contact Us" };
	private ProgressDialog ringProgressDialog;
	private int oldposition;

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		oldposition = Integer.MAX_VALUE;
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(),
				android.R.layout.simple_list_item_1, menus);
		setListAdapter(adapter);
	}

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		switch (position) {
		case 0:
			if (position != oldposition) {
				Setting_Fragment set = new Setting_Fragment();
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, set);
				ft.commit();
				startRingProgress();
				oldposition = position;
			}
		case 1:
			if (position != oldposition) {
				Notification_Fragment set = new Notification_Fragment();
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, set);
				ft.commit();
				startRingProgress();
				oldposition = position;
			}
			break;
		case 2:
			if (position != oldposition) {
				AboutApplication_Fragment set = new AboutApplication_Fragment();
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, set);
				ft.commit();
				startRingProgress();
				oldposition = position;
			}
			break;
		case 3:
			if (position != oldposition) {
				TermAndCondition_Fragment set = new TermAndCondition_Fragment();
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, set);
				ft.commit();
				startRingProgress();
				oldposition = position;
			}
			break;
		case 4:
			if (position != oldposition) {
				ContactUs_Fragment set = new ContactUs_Fragment();
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, set);
				ft.commit();
				startRingProgress();
				oldposition = position;
			}
			break;
		}
	}

	public void startRingProgress() {
		ringProgressDialog = ProgressDialog.show(getActivity(),
				"Please wait ...", "กรุณารอสักครู่ ...", true);
		ringProgressDialog.setCancelable(true);
		new Thread(new Runnable() {
			@Override
			public void run() {
				try {
					Thread.sleep(1000);
					ringProgressDialog.dismiss();
				} catch (Exception e) {

				}
			}
		}).start();
	}
}
