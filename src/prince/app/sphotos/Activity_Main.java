/*
 * Copyright (C) 2013 The Android Open Source Project
 * Modified by Princewill Okorie 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package prince.app.sphotos;

import prince.app.sphotos.Request.GraphRequest;
import prince.app.sphotos.database.AlbumsAccess;
import prince.app.sphotos.database.DbService;
import prince.app.sphotos.database.FBDbContract.AlbumHelper;
import prince.app.sphotos.tools.ActivityAccounts;
import prince.app.sphotos.tools.CameraX;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Util;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Menu;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;

public class Activity_Main extends ActivityAccounts implements FragmentX.FragmentXListener{
	
	// debug variable
	private static final String TAG = Activity_Main.class.getSimpleName();
	
	public static final String MAIN_FRAG = "fb_introduction";
	
	public static final String TASK_ME = "me_request";
	public static final String TASK_ALBUM = "album_request";
	
    private static final int LOGIN = 0;
    private static final int MAIN = 1;
    private static final int LOGOUT = 2;
    private static final int FRAGMENT_COUNT = LOGOUT +1;

    private Fragment[] fragments = new Fragment[FRAGMENT_COUNT];
    private static final String SETTINGS_SHOWN = "logOut";
    private boolean mLogOutWasVisible;
	
	
	// local variables
	private UiLifecycleHelper mUiHelper;
	
    private boolean mResumed = false;
	
    private Session.StatusCallback mCallBack = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, 
	            SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	@Override
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
	    mUiHelper = new UiLifecycleHelper(this, mCallBack);
	    mUiHelper.onCreate(oldState);
	    setContentView(R.layout.main);
		
	//	setContentView(R.layout.fb_main_activity);
		
	//	if (BuildConfig.DEBUG) Global.getInstance().modPref(AlbumsAccess.LAST_UPDATED, "20140910");
	//	if (BuildConfig.DEBUG) Global.getInstance().modPref(UpdateFiles.LAST_GRID_IMAGE_UPDATE, "20140910");
		
        if (oldState != null) {
        	mLogOutWasVisible = oldState.getBoolean(SETTINGS_SHOWN);
        }

        
	    // setUp the fragments
        FragmentManager fm = getSupportFragmentManager();
        fragments[LOGIN] = fm.findFragmentById(R.id.splashFragment);
        fragments[MAIN] = fm.findFragmentById(R.id.selectionFragment);
        fragments[LOGOUT] = fm.findFragmentById(R.id.userSettingsFragment);

        FragmentTransaction transaction = fm.beginTransaction();
        for(int i = 0; i < fragments.length; i++) {
            transaction.hide(fragments[i]);
        }
        transaction.commit();
		
	    
	    // Initialize drawer
		initDrawer();
		
		Log.e(TAG, "ACTIVITY CREATED -*-*-*-*-");
	}
	
	
	
	@Override
	protected void onResumeFragments() {
		super.onResumeFragments();
		Session session = Session.getActiveSession();
	        
		// show logOut screen if we were about to logOut before we lost focus
		if (mLogOutWasVisible){
			showFragment(LOGOUT, true);
		}
	        
		else{
			if (session != null && session.isOpened()) {
				// if the session is already open, try to show the main fragment
				showFragment(MAIN, false);
			} else {
				// otherwise present the logIn screen and ask the user to login
				showFragment(LOGIN, false);
			}
		}
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	        if (mResumed) {
	            FragmentManager manager = getSupportFragmentManager();
	            int backStackSize = manager.getBackStackEntryCount();
	            for (int i = 0; i < backStackSize; i++) {
	                manager.popBackStack();
	            }
	            // check for the OPENED state instead of session.isOpened() since for the
	            // OPENED_TOKEN_UPDATED state, the selection fragment should already be showing.
	            if (state.equals(SessionState.OPENED)) {
	                showFragment(MAIN, false);
	                
	                // obtain album for the new user
	                fetchAlbums();
	                
	            } else if (state.isClosed()) {
	            	mLogOutWasVisible = false; // we no longer need to show the logOut screen
	                showFragment(LOGIN, false);
	            }
	        }
	    }

	    private void showFragment(int fragmentIndex, boolean addToBackStack) {
	        FragmentManager fm = getSupportFragmentManager();
	        FragmentTransaction transaction = fm.beginTransaction();
	        for (int i = 0; i < fragments.length; i++) {
	            if (i == fragmentIndex) {
	                transaction.show(fragments[i]);
	            } else {
	                transaction.hide(fragments[i]);
	            }
	        }
	        if (addToBackStack) {
	            transaction.addToBackStack(null);
	        }
	        transaction.commit();
	    }
	
	    private void fetchAlbums(){
	    	if (!Global.getInstance().isConnection() && FBINIT.isAlbumEmpty()){ 
	    		Toast.makeText(this, "No network connection!", Toast.LENGTH_SHORT).show();
	    		Toast.makeText(this, "Loading cached Albums", Toast.LENGTH_SHORT).show();
	    	
	    		// read album from db if we have no network connection
	    		Intent it = new Intent(this, AlbumsAccess.class);
	    		it.putExtra(DbService.DB_INTENT_KEY, DbService.READ_TB);
	    		it.putExtra(DbService.EXTRAS_STRING, AlbumHelper.FB_ALBUM_TABLE);
	    		startService(it);
	    	}
	    	
	    	if (!FBINIT.ALBUMS_TASK_STARTED){
	    		GraphRequest.albumRequest(	
	    								true, 						// We are calling this method for the first time
	    								true, 						// Update the cache with new values
	    								true, 						// Clear the download trackers
	    								null, 						// We currently have no requests for the next page of data
	    								TASK_ALBUM,					// The unique task ID
	    								FBINIT.sAlbumsArray_NEW,	// The cache to store downloaded data
	    								0);
		    
	    	}
	    	
	    	GraphRequest.makeMeRequest(Session.getActiveSession(), TASK_ME);
	    	
	    }

	@Override
	protected int getDrawerLayoutID() {
		return R.id.drawer_fb_main_activity;
	}

	@Override
	protected int getDrawerListID() {
		return R.id.list_fb_main_activity;
	}

	@Override
	protected void Switch(int position) {
		Intent launch = null;
		
		switch(position){
		case 1:
			launch = new Intent(this, Twitter_Main_Activity.class);
			break;
		case 2:
			launch = new Intent(this, Google_Main_Activity.class);
			break;
		case 3:
			launch = new Intent(this, Gallery_Main_Activity.class);
			break;
		default:
			break;
		}
		
		if (launch != null) startActivity(launch);
	}

	@Override
	protected int getMenuLayout() {
		return R.menu.fb_main;
	}

	@Override
	protected Bitmap getBigPhoto() {
		return Global.getInstance().getDrawerPhotos(Util.FB_DRAWER_BIG_IMAGE, 350, 350);
	}

	@Override
	protected Bitmap getSmallPhoto() {
		return Global.getInstance().getDrawerPhotos(Util.FB_DRAWER_SMALL_IMAGE, 350, 350);
	}

	@Override
	protected int getBigPhotoID() {
		return R.id.fb_main_drawer_big_photo;
	}

	@Override
	protected int getSmallPhotoID() {
		return R.id.fb_main_drawer_small_photo;
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    
	    mResumed = true;
	    mUiHelper.onResume();
	    
	    AppEventsLogger.activateApp(this);
	    Log.e(TAG, "RESUMED - - - - -");
	}
	
/*	@Override
	protected void onRestart() {
	    super.onRestart();
	    
	    if (FBINIT.isAlbumEmpty() && !FBINIT.ALBUMS_TASK_STARTED && !FBINIT.ALBUMS_TASK_DONE && !GraphRequest.inMap(TASK_ALBUM)){
	    	// obtain album for the new user
	    	GraphRequest.albumRequest(	
	    								true, 						// We are calling this method for the first time
	    								true, 						// Update the cache with new values
	    								true, 						// Clear the download trackers
	    								null, 						// We currently have no requests for the next page of data
	    								TASK_ALBUM,					// The unique task ID
	    								FBINIT.sAlbumsArray_NEW,	// The cache to store downloaded data
	    								0);
	    }
	    
	    Log.e(TAG, "RESTARTED - - - - -");
	} */

	@Override
	public void onPause() {
	    super.onPause();
	    
	    mUiHelper.onPause();
	    mResumed = false;
	    
	    AppEventsLogger.deactivateApp(this);
	    Log.e(TAG, "PAUSED - - - - -");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    mUiHelper.onActivityResult(requestCode, resultCode, data);
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	protected void onStop(){
		super.onStop();
    		
		Log.e(TAG, "STOPPED - - - - -");
	}
	
	@Override
	public void onDestroy() {
	    super.onDestroy();
	    
	    mUiHelper.onDestroy();
	    
	    Log.e(TAG, "DESTROYED - - - - -");
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    
	    mUiHelper.onSaveInstanceState(outState);
	    outState.putBoolean(SETTINGS_SHOWN, mLogOutWasVisible);
	    
	    Log.e(TAG, "INSTANCE SAVED - - - - -");
	}

	@Override
	protected String getActionBarTitle() {
		return getResources().getString(R.string.facebook);
	}
	
	@Override
	public void onBackPressed(){
		if (fragments[LOGOUT].isVisible()) {
			mLogOutWasVisible = false;
			Session session = Session.getActiveSession();
			if (session != null &&
					(session.isOpened() || session.isClosed()) ) {
				onSessionStateChange(session, session.getState(), null);
			}
		}
		else super.onBackPressed();
		
		Log.e(TAG, "BACK PRESSED - - - - -");
	}

	@Override
	protected void signOut() {
		
		mLogOutWasVisible = true;
		showFragment(LOGOUT, true);
	}


	@Override
	protected void hideMenuItems(Menu menu) {
		menu.removeItem(R.id.action_sync);
		
		if (fragments[LOGIN].isVisible() || fragments[LOGOUT].isVisible()){
			menu.findItem(R.id.action_logout).setVisible(false);
		}
		
	}

	@Override
	public void onDbError() {}


	@Override
	protected String getOnDrawerOpen() {
		return "Menu";
	}
	
	
	
		
}
