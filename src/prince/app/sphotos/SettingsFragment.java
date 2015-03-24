/**
 * 
 */
package prince.app.sphotos;

import java.util.ArrayList;

import prince.app.sphotos.Request.GraphRequest;
import prince.app.sphotos.Request.GraphRequest.GraphError;
import prince.app.sphotos.Request.GraphRequest.RequestListener;
import prince.app.sphotos.bgtask.FetchData;
import prince.app.sphotos.database.AlbumsAccess;
import prince.app.sphotos.database.AlbumsAccess.AlbumDbListener;
import prince.app.sphotos.tools.FBINIT;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.preference.SwitchPreference;
import android.util.Log;

/**
 * @author Principe
 *
 */
public class SettingsFragment extends PreferenceFragment implements OnSharedPreferenceChangeListener{
	public static final String ENABLED = "Enabled";
	public static final String DISABLED = "Disabled";
	
	private ListPreference mPrefFav;
	private static final String TAG = SettingsFragment.class.getSimpleName();
	
	protected FetchData mWifiListener = new FetchData();
	
	
	@Override
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
		addPreferencesFromResource(R.xml.app_preference);
		
		
		attachListener(findPreference(getResources().getString(R.string.pref_cacheClearMode_key)));
		attachListener(findPreference(getResources().getString(R.string.pref_cache_key)));
		attachListener(findPreference(getResources().getString(R.string.pref_defaultAccount_key)));
		
		mPrefFav = (ListPreference) findPreference(getResources().getString(R.string.pref_favFBAlbum_key));
		
		initListener();
		
		setUpFavorite();
	}
	
	private void initListener(){
		
		new GraphRequest().initListener(new RequestListener(){

			@Override
			public void onGraphProgress(boolean refresh, String taskId) {}

			@Override
			public void onGraphFinish(String taskId) {
				if (getActivity() != null && taskId.equalsIgnoreCase(FBMainActivity.TASK_ALBUM)){
					initEntries(mPrefFav);
					attachListener(mPrefFav);
				}
			}

			@Override
			public void onGraphStart(String taskId) {}

			@Override
			public void coverReady(String taskId) {}

			@Override
			public void onGraphError(GraphError error, String id) {
				if (getActivity() != null && id.equalsIgnoreCase(FBMainActivity.TASK_ALBUM)){
					if (error == GraphError.NO_ALBUM){
						mPrefFav.setSummary("Error loading albums");
					}
				}
				
			}
			
		});
		
		AlbumsAccess.setListener(new AlbumDbListener(){

			@Override
			public void onDbStart() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDbStop(boolean success) {
				if (getActivity() != null && success){
					initEntries(mPrefFav);
					attachListener(mPrefFav);
				}
				
			}
			
		});
	}
	
	private void initEntries(Preference myPref){
		if (((ListPreference) myPref).getEntries() == null || ((ListPreference) myPref).getEntries().length == 0){
			
			// setUp the Entries
			ArrayList<String> mAlbumNames = new ArrayList<String>();
			int albumSize = FBINIT.sAlbumsArray.size();
        
			for (int i = 2; i < albumSize; i++) {
				mAlbumNames.add(FBINIT.sAlbumsArray.get(i).mAlbumName);
			}
			CharSequence[] mEntries = mAlbumNames.toArray(new CharSequence[mAlbumNames.size()]);
        
			// setUp the Entries values
			ArrayList<String> mAlbumID = new ArrayList<String>();
        
			for (int i = 2; i < albumSize; i++) {
				mAlbumID.add(FBINIT.sAlbumsArray.get(i).mAlbumID);
			}
			CharSequence[] mValues = mAlbumID.toArray(new CharSequence[mAlbumID.size()]);
        
			((ListPreference) myPref).setEntries(mEntries);
			((ListPreference) myPref).setEntryValues(mValues);
			((ListPreference) myPref).setDefaultValue(mValues[0]);
			
			myPref.setEnabled(true);
		}
	}
	
	private void setUpFavorite(){
		if (FBINIT.isAlbumEmpty()){
			mPrefFav.setSummary("Still loading albums");
			mPrefFav.setShouldDisableView(true);
			mPrefFav.setEnabled(false);
		}
		
		else {
			
			initEntries(mPrefFav);
	        
	        attachListener(mPrefFav);
		}
	}
	
	private Preference.OnPreferenceChangeListener sPreferenceListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			String stringValue = newValue.toString();
			// TODO Auto-generated method stub
			if (preference instanceof ListPreference){
				
				ListPreference listPreference = (ListPreference) preference;
				int index = listPreference.findIndexOfValue(stringValue);

				// Set the summary to reflect the new value.
				preference
						.setSummary(index >= 0 ? listPreference.getEntries()[index]
								: null);
			}
			
			else if (preference instanceof SwitchPreference){
				preference.setSummary(((Boolean) newValue)? ENABLED : DISABLED);
			}
			return true;
		}
	};
	
	private void attachListener(Preference preference) {
		// Set the listener to watch for value changes.
		preference
				.setOnPreferenceChangeListener(sPreferenceListener);

		// Trigger the listener immediately with the preference's
		// current value.
		if (preference instanceof SwitchPreference){
			sPreferenceListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getBoolean(preference.getKey(),
							false));
		}
		else if (preference instanceof ListPreference){
			sPreferenceListener.onPreferenceChange(
					preference,
					PreferenceManager.getDefaultSharedPreferences(
							preference.getContext()).getString(preference.getKey(),
							""));
		}
		
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    getPreferenceScreen().getSharedPreferences()
	            .registerOnSharedPreferenceChangeListener(this);
	    
	    getActivity().registerReceiver(mWifiListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
	}

	@Override
	public void onPause() {
	    super.onPause();
	    getPreferenceScreen().getSharedPreferences()
	            .unregisterOnSharedPreferenceChangeListener(this);
	    
	    getActivity().unregisterReceiver(mWifiListener);
	}


	@Override
	public void onSharedPreferenceChanged(SharedPreferences sharedPreferences,
			String key) {
		// TODO Auto-generated method stub
		
	}
}
