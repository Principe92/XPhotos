package prince.app.sphotos;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.http.HttpResponseCache;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.util.Log;

public class Activity_Welcome extends Activity {

	private SharedPreferences mPreference;
	private String mFirstShown;

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		HttpResponseCache cache = HttpResponseCache.getInstalled();
		if (cache != null){
			cache.flush();
		}
		
		Handler handler = new Handler();
		handler.postDelayed(new Runnable(){
		@Override
		public void run(){
			LaunchMain();
		}
	}, 2000);  
	}


private void LaunchMain(){
	mPreference = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
	mFirstShown = mPreference.getString(getResources().getString(R.string.pref_defaultAccount_key), "");  	// get preferred first screen
	Intent intent;
	
	switch(mFirstShown){
		case "facebook":
			intent = new Intent(this, FBMainActivity.class);
			break;
		case "twitter":
			intent = new Intent(this, Twitter_Main_Activity.class);
			break;
		case "google":
			intent = new Intent(this, Google_Main_Activity.class);
			break;
		case "gallery":
			intent = new Intent(this, Gallery_Main_Activity.class);
			break;
		default:
			intent = new Intent(this, FBMainActivity.class);
			break;
	}
	
	startActivity(intent);
}


}