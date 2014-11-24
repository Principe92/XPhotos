package prince.app.sphotos.tools;

import java.util.EventListener;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

public class AlertDialogX extends DialogFragment{
	public static final String TITLE = "dialog_title";
	public static final String MESSAGE = "message";
	public static final String POS_ID = "positive id";
	public static final String NEG_ID = "negative_id";
	public static final String TYPE = "dialog_type";
	
	private AlertXListener listen;
	
	public static AlertDialogX newInstance(String title, String message, int pos_id, int neg_id, String type){
		AlertDialogX aX = new AlertDialogX();
		Bundle args = new Bundle();
		args.putString(TITLE, title);
		args.putString(MESSAGE, message);
		args.putInt(POS_ID, pos_id);
		args.putInt(NEG_ID, neg_id);
		args.putString(TYPE, type);
		aX.setArguments(args);
		return aX;
	}
	
	public interface AlertXListener extends EventListener {
		public void onPosClick(String type);
		}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listen = (AlertXListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement Listener");
        }
    }

	
	 @Override
	 public Dialog onCreateDialog(Bundle savedInstanceState) {
		 String title = getArguments().getString(TITLE);
		 String msg = getArguments().getString(MESSAGE);
		 int posID = getArguments().getInt(POS_ID);
		 int negID = getArguments().getInt(NEG_ID);
		 final String type = getArguments().getString(TYPE);

		 return new AlertDialog.Builder(getActivity())
		 								.setTitle(title)
		 								.setMessage(msg)
		 								.setPositiveButton(posID,
		 										new DialogInterface.OnClickListener() {
		 									public void onClick(DialogInterface dialog, int whichButton) {
		 										listen.onPosClick(type);
		 									}
		 								}
		 										)
		 								.setNegativeButton(negID,
		 										new DialogInterface.OnClickListener() {
		 									public void onClick(DialogInterface dialog, int whichButton) {
		 										getDialog().dismiss();
		 									}
		 								}
		 									)
		 							.create();
	 }
}
