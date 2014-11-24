package prince.app.sphotos.tools;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;

/** A class that manages network connection dialogs */
public class NoWifi extends DialogFragment {
	private static int sPositive;
	private static int sNegative;
	private static int sHeader;
	
	public static NoWifi newInstance(int header, int positive, int negative){
		sHeader = header;
		sPositive = positive;
		sNegative = negative;
		final NoWifi dialog = new NoWifi();
		return dialog;
	}
	
	@Override
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
		
		setRetainInstance(true);
	}
	
    @Override
    public Dialog onCreateDialog(Bundle oldState) {
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(sHeader)
        	   .setMessage(sHeader)
               .setPositiveButton(sPositive, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // Check network connection
                	   startActivity(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK));
                   }
               })
               .setNegativeButton(sNegative, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                	   dialog.dismiss();
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
}