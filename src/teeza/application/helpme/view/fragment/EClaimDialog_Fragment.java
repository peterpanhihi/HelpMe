package teeza.application.helpme.view.fragment;

import teeza.application.helpme.Login_Activity;
import teeza.application.helpme.Main_Activity;
import teeza.application.helpme.R;
import teeza.application.helpme.model.ApplicationStatus;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class EClaimDialog_Fragment extends DialogFragment {
	final String url = Login_Activity.nameHost + "check_policy.php";
	final String PREF_NAME = "IMAGE_ID";

	private Main_Activity main;
	private int position;
	private String idcus;
	private AlertDialog.Builder builder;
	private Button etClaim, etHistory;
	private EClaimDialog_Fragment dialog;
	private ApplicationStatus appStatus;
	private StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
			.permitAll().build();

	public EClaimDialog_Fragment(Context context, int position, String idcus) {
		main = (Main_Activity) context;
		dialog = this;
		appStatus = ApplicationStatus.getInstance();
		this.position = position;
		this.idcus = idcus;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		builder = new AlertDialog.Builder(new ContextThemeWrapper(
				getActivity(), R.style.AppTheme));
		@SuppressWarnings("static-access")
		LayoutInflater inflater = (LayoutInflater) getActivity()
				.getSystemService(getActivity().LAYOUT_INFLATER_SERVICE);
		View layout = inflater.inflate(R.layout.dialog_claimed, null);
		etClaim = (Button) layout.findViewById(R.id.btnclaim);
		etHistory = (Button) layout.findViewById(R.id.btnclaimhistory);

		etClaim.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				main.setOldposition(position);
				EClaimCheckPolicy_Fragment edf = new EClaimCheckPolicy_Fragment(idcus);
				edf.show(getActivity().getSupportFragmentManager(), "Dialog");
				dialog.dismiss();
			}
		});

		etHistory.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				main.setOldposition(position);
				EClaim_Fragment claim = new EClaim_Fragment();
				claim.getFragmentManager();
				FragmentTransaction ft = getActivity()
						.getSupportFragmentManager().beginTransaction();
				ft.replace(R.id.frame_container, claim);
				ft.commit();
				dialog.dismiss();
			}
		});
		builder.setView(layout);
		builder.setCancelable(true);
		return builder.create();
	}
}