/**
 * 
 */
package prince.app.sphotos;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * @author Princewill
 *
 */
public class SettingsActivity extends PreferenceActivity{
	
	@Override
	protected void onCreate(Bundle savedState) {
		// TODO Auto-generated method stub
		super.onCreate(savedState);
	  
	 getFragmentManager().beginTransaction().replace(android.R.id.content,
	                new SettingsFragment()).commit();
	 }
	
}
