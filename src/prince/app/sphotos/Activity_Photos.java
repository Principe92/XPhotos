/*
 * Copyright (C) 2013 The Android Open Source Project 
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

import java.util.ArrayList;

import prince.app.sphotos.Request.GraphRequest;
import prince.app.sphotos.Request.GraphRequest.GraphError;
import prince.app.sphotos.bgtask.FetchData;
import prince.app.sphotos.bgtask.FetchData.WifiListener;
import prince.app.sphotos.database.AlbumsAccess;
import prince.app.sphotos.database.AlbumsAccess.AlbumDbListener;
import prince.app.sphotos.tools.ActivityImages;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Util;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Toast;
/**
 * An Activity that handles the loading of all the photos present in a particular Facebook Album of the current user
 * It receives an intent that indicates which album to load its photos. <br>
 * It inflates the following xml file:  <i> fb_main_activity </i>
 * @author Princewill Okorie
 * @since	Created		- 	April 12, 2014 <br>
 * 			Modified	-	May 15, 2014
 *
 */
/**
 * An Activity that handles the loading of all the photos present in a particular Facebook Album of the current user
 * It receives an intent that indicates which album to load its photos. <br>
 * It inflates the following xml file:  <i> fb_main_activity </i>
 * @author Princewill Okorie
 * @since	Created		- 	April 12, 2014 <br>
 * 			Modified	-	May 15, 2014
 *
 */
public class Activity_Photos extends ActivityImages implements Fragment_Favorite.FavoriteListener{
	
	private int mAlbum;
	
	private static final String TAG = Activity_Photos.class.getSimpleName();
	public static final int LAUNCH_FULL_IMAGE = 200;
	private static final int CONNECT = 100;
	
	public static final String IMAGE_DETAILS = "imageInfo";
	public static final String ALBUM_NAME = "nameOfAlbum";
	public static final String LAST_VIEWED_ALBUM = "album_position";
	public static final String GRID_VISIBLE = "visibility";
	
	private static final String IMAGE_FRAGMENT = "photos_fragment";
	private static final String FAV_FRAGMENT = "fav_fragment";
	public static final String FAV_SHARED_KEY = "fav_album";
	
	private static final String BAR_TITLE = "actionBar";
	private static final String INTERNET_STUB = "save_internet";
	private static final String ALBUM_STUB = "save_album";
	
	public static final String FROM_HOMEPAGE = "intent_origin";
	public static final String WHICH_ALBUM_NAME = "album_to_load";
	public static final String WHICH_ALBUM_POS = "album_pos_load";
	public static final String NO_FAVORITE = "No_Favorite";
	
	private String mIntentExtras;
	private String mActionTitle;

	private boolean fromHome;
	
	
	@Override
	public void onCreate(Bundle oldState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(oldState);
		setContentView(R.layout.fb_main_activity);
		
		initDrawer(); // initialize the Navigation Drawer
    	
    	fromHome = getIntent().getExtras().getBoolean(FROM_HOMEPAGE, false);
    	
    	initListeners();
        
        // were we launched from the homepage?
        if (fromHome){ // yes
        	mIntentExtras  =  getIntent().getExtras().getString(WHICH_ALBUM_NAME);
        	
        	if (FBINIT.isAlbumEmpty()){ // yes, album array is empty
        		
        		if (!Global.getInstance().isConnection()) setStub(R.id.stub_internet, R.id.viewstub_internet);
        		
        		else setStub(R.id.stub_albums, R.id.viewstub_albums);
        		
        		mActionTitle = mIntentExtras.equalsIgnoreCase(NO_FAVORITE) ? "Favorite Album" : mIntentExtras;	
        		getActionBar().setTitle(mActionTitle); // Set the title bar to the album name
        		
        	}
        	
        	else{ // no, we have album data
        		
        		// were we launched by favorite option and no favorite selected yet
        		if (mIntentExtras.equalsIgnoreCase(NO_FAVORITE)){
        			if (oldState == null){
            			getSupportFragmentManager().beginTransaction()
            			.replace(R.id.frame_fb_main_activity, Fragment_Favorite.newInstance(), FAV_FRAGMENT)
            			.commit();
            		}
        			
        			mActionTitle = "Favorite Album";
        			getActionBar().setTitle(mActionTitle); // Set the title bar to the album name
        		}
        		
        		else {
        			
        			mAlbum = FBINIT.getIndexByName(mIntentExtras);
        			
        			// If we can't find by name, search by ID
        			if (mAlbum == -1) mAlbum = FBINIT.getIndexByID(mIntentExtras);
        			
        			mActionTitle = FBINIT.sAlbumsArray.get(mAlbum).mAlbumName;
        			getActionBar().setTitle(mActionTitle); // Set the title bar to the album name
        			
        			// add the photos fragment
        			if (oldState == null){
        				getSupportFragmentManager().beginTransaction()
        				.replace(R.id.frame_fb_main_activity, Fragment_Photos.newInstance(mAlbum), IMAGE_FRAGMENT)
        				.commit();
        			}
        		}
        	}
        }
        
        // we were launched from the albums page
        else {
        	mAlbum = getIntent().getExtras().getInt(WHICH_ALBUM_POS);
        	mActionTitle = FBINIT.sAlbumsArray.get(mAlbum).mAlbumName;				// Get the name of the album
        	
        	if (oldState == null){
        		getSupportFragmentManager().beginTransaction()
        		.add(R.id.frame_fb_main_activity, Fragment_Photos.newInstance(mAlbum), IMAGE_FRAGMENT)
        		.commit();
        	}
        }
        
        // were we restored? 
        if (oldState != null){ // yes
    		boolean visible = oldState.getBoolean(GRID_VISIBLE);
			
    		Fragment_Photos frag = (Fragment_Photos) Global.findFrag(this, IMAGE_FRAGMENT);
    		if (frag != null){
    			frag.setmGridVisible(visible);
    		}
    		
    		mActionTitle = oldState.getString(BAR_TITLE);
    		getActionBar().setTitle(mActionTitle);
    		
    		if (oldState.getBoolean(INTERNET_STUB)) setStub(R.id.stub_internet, R.id.viewstub_internet);
    		else if (oldState.getBoolean(ALBUM_STUB)) setStub(R.id.stub_albums, R.id.viewstub_albums);
    	}
        
	}
	
	private void launchFragment(){
		// initialize album wait
		if (fromHome && FBINIT.isAlbumEmpty()) ((ViewStub) findViewById(R.id.stub_albums)).setVisibility(View.VISIBLE);
		
		else if (fromHome && !FBINIT.isAlbumEmpty()){
			// were we launched by favorite option and no favorite selected yet
    		if (mIntentExtras.equalsIgnoreCase(NO_FAVORITE)){
    			getSupportFragmentManager().beginTransaction()
    			.replace(R.id.frame_fb_main_activity, Fragment_Favorite.newInstance(), FAV_FRAGMENT)
    			.commit();
    		}
		}
					
		else {
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.frame_fb_main_activity, Fragment_Photos.newInstance(mAlbum), IMAGE_FRAGMENT)
			.commit();
		}
	}
	
	private void initListeners(){
		// Listen for when images in album is available
		// from web
	    new GraphRequest().initListener(new GraphRequest.RequestListener() {
	    	@Override
	    	public void onGraphProgress(boolean refresh, String taskId) {}
	    	
			@Override
			public void onGraphFinish(String taskId) {
				if (isTask(taskId)) init();
			}

			@Override
			public void onGraphStart(String taskId) {
				if (isTask(taskId) && isStubVisible(R.id.stub_internet, R.id.viewstub_internet)) setStub(R.id.stub_albums, R.id.viewstub_albums);
			}

			@Override
			public void coverReady(String id) {}

			@Override
			public void onGraphError(GraphError error, String id) {
				if (error == GraphError.NO_ALBUM && isTask(id)){
					onDbError();
				}
				
			}
	    });
	    
	    // from database
	    AlbumsAccess.setListener(new AlbumDbListener(){

			@Override
			public void onDbStart() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDbStop(boolean success) {
				if (success) init();
				
			}});
	    
	    
	    // connection
	    FetchData.setListener(new WifiListener(){

			@Override
			public void onConnection() {
				Log.e(TAG, "Connection RESTORED");
				
				if (isStubVisible(R.id.stub_internet, R.id.viewstub_internet)){
			//		launchFragment();
				}
				
			}});
	}
	
	private boolean isTask(String id){
		return id.equalsIgnoreCase(FBMainActivity.TASK_ALBUM);
	}
	
	/**
	 * Method to reinitialize necessary data once albums are available
	 */
	private void init(){
		//Set the adapter for the list view
		ArrayList<String> albumNames = new ArrayList<String>();
        int albumSize = FBINIT.sAlbumsArray.size();
        
        for (int i = 0; i < albumSize; i++) {
        	albumNames.add(FBINIT.sAlbumsArray.get(i).mAlbumName);
        }
        
        // Set the adapter for the list view
        mDrawerList.setAdapter(new ArrayAdapter<String>(Activity_Photos.this, R.layout.fb_drawer_album_list, albumNames));
        
        // were we launched by favorite option and no favorite selected yet
		if (mIntentExtras.equalsIgnoreCase(NO_FAVORITE)){
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.frame_fb_main_activity, Fragment_Favorite.newInstance(), FAV_FRAGMENT)
			.commit();
		}
		
		else {
			
			mAlbum = FBINIT.getIndexByName(mIntentExtras);
			
			// If we can't find by name, search by ID
			if (mAlbum == -1) mAlbum = FBINIT.getIndexByID(mIntentExtras);
			
			synchronized(FBINIT.sAlbumsArray){
				mActionTitle = FBINIT.sAlbumsArray.get(mAlbum).mAlbumName;
				getActionBar().setTitle(mActionTitle);
			}
		
			// add the photos fragment
			Fragment_Photos frag = (Fragment_Photos) Global.findFrag(this, IMAGE_FRAGMENT);
			if (frag == null || !frag.isVisible()){
		
				getSupportFragmentManager().beginTransaction()
				.replace(R.id.frame_fb_main_activity, Fragment_Photos.newInstance(mAlbum), IMAGE_FRAGMENT)
				.commit();
			}
		}
		
		Log.e(TAG, "!!! Albums wait Over; Inflating layout !!! ");
	}
	
	@Override
	public void onSaveInstanceState(Bundle state) {
	    // Save the user's current game state
		super.onSaveInstanceState(state);
		
		Fragment_Photos frag = (Fragment_Photos) Global.findFrag(this, IMAGE_FRAGMENT);
		if (frag != null){
			state.putBoolean(GRID_VISIBLE, frag.isGridViewVisible());
		}
		
		state.putString(BAR_TITLE, mActionTitle);
		state.putBoolean(INTERNET_STUB, isStubVisible(R.id.stub_internet, R.id.viewstub_internet));
		state.putBoolean(ALBUM_STUB, isStubVisible(R.id.stub_albums, R.id.viewstub_albums));
	} 
	
	@Override
    public void onBackPressed() {
    	Intent returnIntent = new Intent();
        returnIntent.putExtra(LAST_VIEWED_ALBUM, mAlbum);
        setResult(RESULT_OK, returnIntent);     
        finish();
	}


	@Override
	public int getMenuLayout() {
		return R.menu.fb_main;
	}


	@Override
	protected void Switch(int position) {
	
		if (FBINIT.sImagesArray_LASTUSED != position){
			mAlbum = position;
			
			getSupportFragmentManager().beginTransaction()
    		.replace(R.id.frame_fb_main_activity, Fragment_Photos.newInstance(mAlbum), IMAGE_FRAGMENT)
    		.commit();
    		
    		mActionTitle = FBINIT.sAlbumsArray.get(mAlbum).mAlbumName;			// Get the name of the album
		}
	}


	@Override
	protected Bitmap getBigPhoto() {
		return Global.getInstance().getDrawerPhotos(Util.FB_DRAWER_BIG_IMAGE, 350, 350);
	//	return null;
	}

	@Override
	protected Bitmap getSmallPhoto() {
		return Global.getInstance().getDrawerPhotos(Util.FB_DRAWER_SMALL_IMAGE, 350, 350);
	//	return null;
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
	protected int getDrawerLayoutID() {
		return R.id.drawer_fb_main_activity;
	}

	@Override
	protected int getDrawerListID() {
		return R.id.list_fb_main_activity;
	}
	
	@Override
	protected String getActionBarTitle(){
		return mActionTitle;
	}
	
	@Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		
		 if (requestCode == LAUNCH_FULL_IMAGE) {
			 if (resultCode == FragmentActivity.RESULT_OK) {
				 Fragment_Photos.onActivityResult(data.getExtras().getInt(Activity_FullImage.LAST_VIEWED_FULL));
			 }
		 }
	 }
	
	public void retryConnection(View view){
		// do we have connection?
		if (Global.getInstance().isConnection()){ // yes
			
			launchFragment();
			
			Toast.makeText(this, "Network ON", Toast.LENGTH_LONG).show();
		}
	}
	
	public void checkConnection(View view){
		startActivityForResult(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK), CONNECT);
	}
	


	@Override
	protected void refreshGrid() {
		if (!isStubVisible(R.id.stub_internet, R.id.viewstub_internet)){
			Fragment_Photos frag = (Fragment_Photos) Global.findFrag(this, IMAGE_FRAGMENT);
			if (frag != null && frag.isVisible()) frag.refreshData();
		}
	}
	
	public void onCheckboxClicked(View view) {
	    // Is the view now checked?
	    boolean checked = ((CheckBox) view).isChecked();
	    
	    // Check which checkbox was clicked
	    switch(view.getId()) {
	        case R.id.custom_checkbox:
	            if (checked){
	            	Toast.makeText(this, "launch options", Toast.LENGTH_SHORT).show();
	            }
	            break;
	    }
	}

	@Override
	public void onDbError() {
		Global.hideRemoveFrag(this, Global.findFrag(this, IMAGE_FRAGMENT), true);
		
		setStub(R.id.stub_internet, R.id.viewstub_internet);
	}

	@Override
	protected String getOnDrawerOpen() {
		return "Albums";
	}

	@Override
	protected void hideShowMenu(Menu menu) {
		// show details
		MenuItem item = menu.findItem(R.id.action_details);
		item.setEnabled(true);
		if (FBINIT.isAlbumEmpty()){
			item.setVisible(false);
		} else{
			item.setVisible(true);
		}
		
	}

	@Override
	public void onOkay(int position) {
		synchronized(FBINIT.sAlbumsArray){
			mActionTitle = FBINIT.sAlbumsArray.get(position).mAlbumName;
			
			SharedPreferences settings = PreferenceManager.getDefaultSharedPreferences(this);
			SharedPreferences.Editor edit = settings.edit();
			edit.putString(getResources().getString(R.string.pref_favFBAlbum_key), FBINIT.sAlbumsArray.get(position).mAlbumID);
			edit.commit();
		}
		
		
		
		mAlbum = position;
		getActionBar().setTitle(mActionTitle);
		getSupportFragmentManager().beginTransaction()
		.replace(R.id.frame_fb_main_activity, Fragment_Photos.newInstance(mAlbum), IMAGE_FRAGMENT)
		.commit();
		
	}

	@Override
	protected int getIndex() {
		return mAlbum;
	}

	@Override
	public Toolbar getToolBar() {
		// TODO Auto-generated method stub
		return null;
	}
}
