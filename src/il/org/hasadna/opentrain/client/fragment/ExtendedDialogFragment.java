package il.org.hasadna.opentrain.client.fragment;

import il.org.hasadna.opentrain.R;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/**
 * Created by Noam.m on 3/4/14.
 */
public class ExtendedDialogFragment extends DialogFragment {

	private DialogInterface.OnClickListener dialogClicksListener;

	public void setDialogClicksListener(
			DialogInterface.OnClickListener dialogClicksListener) {
		this.dialogClicksListener = dialogClicksListener;
	}

	public static ExtendedDialogFragment newInstance() {
		ExtendedDialogFragment frag = new ExtendedDialogFragment();
		Bundle args = new Bundle();
		args.putInt("title", R.string.app_name);
		args.putInt("message", R.string.alert_dialog_confirm_exit);
		frag.setArguments(args);
		return frag;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		int title = getArguments().getInt("title");
		int message = getArguments().getInt("message");

		return new AlertDialog.Builder(getActivity())
				.setIcon(R.drawable.ic_launcher)
				.setTitle(title)
				.setMessage(message)
				.setPositiveButton(R.string.alert_dialog_ok,
						dialogClicksListener)
				.setNegativeButton(R.string.alert_dialog_cancel,
						dialogClicksListener).create();
	}
}
