/*
 * Copyright (C) 2014 Princewill Chibututu Okorie
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
import prince.app.sphotos.Request.GraphRequest.GraphError;
import prince.app.sphotos.tools.ActivityImages;
import prince.app.sphotos.tools.AlertDialogX;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Util;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;

public class Activity_Albums extends ActivityImages implements AlertDialogX.AlertXListener{
	private static final String TAG = Activity_Albums.class.getSimpleName();
	private static final int CONNECT = 100;
	private static final String GRID_VISIBLE = "gridViewVisible";
	
	@Override
	public void onCreate(Bundle oldState){
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(oldState);
		setContentView(R.layout.fb_album_activity);
		
		if (oldState == null) {
			getSupportFragmentManager().beginTransaction()
			.add(R.id.frame_fb_album_activity, new Fragment_Albums(), TAG)
			.commit();
		}
		
		if (oldState != null){
			Boolean visible = oldState.getBoolean(GRID_VISIBLE);
			
			Fragment_Albums frag = (Fragment_Albums) Global.findFrag(this, TAG);
			if (frag != null) frag.setmGridVisible(visible);
			
		}
		
		
		// Enable Top Left Back Icon
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	private void initListeners(){
		// Listen for when images in album is available
		// from web
	    new GraphRequest().initListener(new GraphRequest.RequestListener() {
	    	@Override
	    	public void onGraphProgress(boolean refresh, String taskId) {}
	    	
			@Override
			public void onGraphFinish(String taskId) {
				Log.e(TAG, "GRAPH FINISH - - - - -");
				
				if (isTask(taskId)){
					if (isStubVisible(R.id.stub_albums, R.id.viewstub_albums)){
					
						getSupportFragmentManager().beginTransaction()
						.replace(R.id.frame_fb_album_activity, new Fragment_Albums(), TAG)
						.commit();
					}
				}
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
	}
	
	private boolean isTask(String id){
		return id.equalsIgnoreCase(Activity_Main.TASK_ALBUM);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		
		Fragment_Albums frag = (Fragment_Albums) Global.findFrag(this, TAG);
		if (frag != null){
			state.putBoolean(GRID_VISIBLE, frag.isGridViewVisible());
		}
	} 
	
	 @Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 super.onActivityResult(requestCode, resultCode, data);
		 
		 Log.e(TAG, "RequestCode: " + requestCode);
		 Log.e(TAG, "Result: " + resultCode);
		 
		 if (requestCode == Fragment_Albums.CLICKED_ALBUM_POSITION && resultCode == RESULT_OK) {
			 Fragment_Albums.onActivityResult(data.getExtras().getInt(Activity_Photos.LAST_VIEWED_ALBUM));
		 }
	 }


	@Override
	public int getMenuLayout() {
		return R.menu.fb_main;
	}


	@Override
	protected void Switch(int position) {}


	@Override
	protected Bitmap getBigPhoto() {
		return Global.getInstance().getDrawerPhotos(Util.FB_COVER_GRID_IMAGE, 350, 350);
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
	protected int getDrawerLayoutID() {
		return R.id.drawer_fb_main_activity;
	}

	@Override
	protected int getDrawerListID() {
		return R.id.list_fb_main_activity;
	}
	
	public void retryConnection(View view){
		if (Global.getInstance().isConnection()){
			
			getSupportFragmentManager().beginTransaction()
			.replace(R.id.frame_fb_album_activity, new Fragment_Albums(), TAG)
			.commit();
		}
	}
	
	public void checkConnection(View view){
		startActivityForResult(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK), CONNECT);
	}
	
	@Override
	protected String getActionBarTitle(){
		return getResources().getString(R.string.title_albums);
	}


	@Override
	protected void refreshGrid() {
		Fragment_Albums frag = (Fragment_Albums) Global.findFrag(this, TAG);
		if (frag != null){
			frag.refreshData();
		}
	}

	@Override
	public void onDbError() {
		Global.hideRemoveFrag(this, Global.findFrag(this, TAG), true);
		
		setStub(R.id.stub_internet, R.id.viewstub_internet);
		
		initListeners();
		
		setProgressBarIndeterminateVisibility(false);
		
		if (FBINIT.isAlbumEmpty() && !FBINIT.ALBUMS_TASK_STARTED && !FBINIT.ALBUMS_TASK_DONE && !GraphRequest.inMap(Activity_Main.TASK_ALBUM)){
	    	// obtain album for the new user
	    	GraphRequest.albumRequest(	
	    								true, 						// We are calling this method for the first time
	    								true, 						// Update the cache with new values
	    								true, 						// Clear the download trackers
	    								null, 						// We currently have no requests for the next page of data
	    								Activity_Main.TASK_ALBUM,// The unique task ID
	    								FBINIT.sAlbumsArray_NEW,	// The cache to store downloaded data
	    								0);
	    }
		
	}

	@Override
	protected String getOnDrawerOpen() {
		return null;
	}

	@Override
	protected void hideShowMenu(Menu menu) {
		// hide details
		menu.findItem(R.id.action_details)
		.setVisible(false)
		.setEnabled(false);
		
	}

	@Override
	public void onPosClick(String type) {
		if (type.equalsIgnoreCase(Fragment_Albums.DEL_ALBUM)) Global.getInstance().showToast("Deleting Album");
		
		else if (type.equalsIgnoreCase(Fragment_Albums.GET_ALBUM)) Global.getInstance().showToast("Downloading Album");
		
	}

	@Override
	protected int getIndex() {
		return 0;
	}
	
	public void launchOptions(View view){
		Fragment_Albums aX = (Fragment_Albums) getSupportFragmentManager().findFragmentByTag(TAG);
		if (aX != null) aX.launchOptions(view); 
	}
	

} //TODO End of class

