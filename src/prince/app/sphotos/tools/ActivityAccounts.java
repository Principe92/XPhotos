package prince.app.sphotos.tools;

import java.util.ArrayList;
import java.util.List;

import prince.app.sphotos.R;
import prince.app.sphotos.SettingsActivity;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;
import android.widget.Toast;

public abstract class ActivityAccounts extends ActivityBase{
	private static final String TAG = ActivityAccounts.class.getSimpleName();
	
	// local variables
    private static final Integer[] socialIcons = { R.drawable.fb,
        R.drawable.ic_action_twitter_logo_blue, R.drawable.btn_gp, R.drawable.ic_launcher_gallery };
    
    private PopupMenu mOverflowMenu;
    
    private int  mExitCount = 2;
	
	@Override
	public void openSettings(){
		View view = findViewById(R.id.action_main_options);
		mOverflowMenu = new PopupMenu(this, view);
		mOverflowMenu.setOnMenuItemClickListener(this);
		MenuInflater inflater = mOverflowMenu.getMenuInflater();
		inflater.inflate(R.menu.fb_main_overflow, mOverflowMenu.getMenu());
		
		hideMenuItems(mOverflowMenu.getMenu());
		
		mOverflowMenu.show();
	}
		
	// Called when an item on the popUp MenuItem is pressed
	@Override
	public boolean onMenuItemClick(MenuItem item) {
	    int itemId = item.getItemId();
		if (itemId == R.id.action_logout){
			// Sign out from social account
			signOut();
	        return true;
		}
		if (itemId == R.id.action_settings) {
			// Open the settings menu
			Intent intent = new Intent();
	        intent.setClass(this, SettingsActivity.class);
	        startActivityForResult(intent, 0); 
	        Log.d(TAG, "settings called");
			return true;
		}
		else {
			return false;
		}
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onBackPressed(){
		mExitCount = mExitCount - 1;
		if (mExitCount == 1)
			Toast.makeText(this, "Press again to exit", Toast.LENGTH_LONG).show();;
		if (mExitCount == 0){
			mExitCount = 2;
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
	
	@Override
	protected void setDrawerList(){
		String[] social = getResources().getStringArray(R.array.social_accounts);
        
        //Set the adapter for the list view
		List<Account> mAccounts = new ArrayList<Account>();
        for (int i = 0; i < social.length; i++) {
            Account item = new Account(socialIcons[i], social[i]);
            mAccounts.add(item);
        }
        
        // set the ListViewAdapter
        mDrawerList.setAdapter(new ListViewListener(this, R.layout.social_media_accounts, mAccounts));
	}
	
    /**
     * Method to signOut from an account
     */
    protected abstract void signOut();
    
    /**
     * Method to hide some menu items before inflation
     * @param menu - The menu layout
     */
    protected abstract void hideMenuItems(Menu menu);
}
