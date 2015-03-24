package prince.app.sphotos;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NavigationDrawerFragment extends Fragment{
	public static final String TAG = NavigationDrawerFragment.class.getSimpleName();
	
	private static final String PREF_USER_LEARNED_DRAWER = "navigation_drawer_learned";
	
	private boolean mUserLearnedDrawer;
	
	public static NavigationDrawerFragment newInstance(){
		final NavigationDrawerFragment fg = new NavigationDrawerFragment();
		return fg;
	}
	
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
		
		SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(getActivity());
		mUserLearnedDrawer = sp.getBoolean(PREF_USER_LEARNED_DRAWER, false);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle oldState){
		super.onCreateView(inflater, parent, oldState);
		
		View view = null; // = inflater.inflate(resource, parent, false);
		
		return view;
	}

}
