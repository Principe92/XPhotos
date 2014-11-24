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

import prince.app.sphotos.Request.GraphRequest;
import prince.app.sphotos.Request.GraphRequest.GraphError;
import prince.app.sphotos.bgtask.UpdateService;
import prince.app.sphotos.database.AlbumsAccess;
import prince.app.sphotos.database.AlbumsAccess.AlbumDbListener;
import prince.app.sphotos.database.DbService;
import prince.app.sphotos.database.FBDbContract.AlbumHelper;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.UpdateFiles;
import prince.app.sphotos.tools.Util;
import prince.app.sphotos.ui.RecyclingImageView;
import prince.app.sphotos.util.ImageCache.ImageCacheParams;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

public class Fragment_Main extends FragmentX{
	private final static String TAG = Fragment_Main.class.getSimpleName();
	
	// GridView variables
	private String[] mGridName;
	
	
	// local variables
	private Intent mLaunch;
	
	@Override
	protected void onCreateInit(){
		mGridName = getResources().getStringArray(R.array.main_grid);
		
	/*	if (!Global.getInstance().isConnection()){ 
	    	Toast.makeText(getActivity(), "No network connection!", Toast.LENGTH_SHORT).show();
	    	Toast.makeText(getActivity(), "Loading cached Albums", Toast.LENGTH_SHORT).show();
	    	
	    	// read album from db if we have no network connection
	    	Intent it = new Intent(getActivity(), AlbumsAccess.class);
	    	it.putExtra(DbService.DB_INTENT_KEY, DbService.READ_TB);
	    	it.putExtra(DbService.EXTRAS_STRING, AlbumHelper.FB_ALBUM_TABLE);
	    	getActivity().startService(it);
	    } */
		
    	AlbumsAccess.setListener(new AlbumDbListener(){
    		
			@Override
			public void onDbStart() {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onDbStop(boolean success) {
				if (success){
			
					FBINIT.ALBUMS_READY = true;
					
					synchronized(FBINIT.sAlbumsArray_LOCK){
						FBINIT.sAlbumsArray_LOCK.notifyAll();
					}
				}
				
			}});
		
		// Listens for when images in album is available
	    new GraphRequest().initListener(new GraphRequest.RequestListener() {
	    	@Override
	    	public void onGraphProgress(boolean refresh, String taskId) {}
	    	
			@Override
			public void onGraphFinish(String taskId) {
	    		if (taskId.equalsIgnoreCase(Activity_Main.TASK_ALBUM)){
	    			
	    			// Update grid counts
	    			UpdateFiles.updateCount(getActivity());
	    			
	    			updateGrid();
	    			
	    			// Update grid images
	    			Intent it = new Intent(getActivity(), UpdateService.class);
	    			it.putExtra(UpdateService.INTENT_KEY, UpdateService.UPDATE_GRID_PHOTOS);
	    			it.putExtra(UpdateService.EXTRAS, true);
	    			getActivity().startService(it);
				
					// Update the albums table in db
					UpdateFiles.updateDbAlbums(getActivity().getApplicationContext());
	    		}
			}

			@Override
			public void onGraphStart(String taskId) {}

			@Override
			public void coverReady(String id) {}

			@Override
			public void onGraphError(GraphError error, String id) {
				if (error == GraphError.NO_ALBUM && id.equalsIgnoreCase(Activity_Main.TASK_ALBUM)){
					
					//TODO: Do something about album Error like trying to fetch again
					Global.getInstance().showToast("Unable to fetch albums");
					
					// Read from cached albums
			    	Intent it = new Intent(getActivity(), AlbumsAccess.class);
			    	it.putExtra(DbService.DB_INTENT_KEY, DbService.READ_TB);
			    	it.putExtra(DbService.EXTRAS_STRING, AlbumHelper.FB_ALBUM_TABLE);
			    	getActivity().startService(it);
				}
			}
	    });
	    
	 // obtain album for the new user
   /* 	GraphRequest.albumRequest(	
    								true, 						// We are calling this method for the first time
    								true, 						// Update the cache with new values
    								true, 						// Clear the download trackers
    								null, 						// We currently have no requests for the next page of data
    								FB_Main_Activity.TASK_ALBUM,					// The unique task ID
    								FBINIT.sAlbumsArray_NEW,	// The cache to store downloaded data
    								0); */
	}
	
	private Bitmap getImage(int position){
    	
    	if (position == 0){
    		Bitmap bitmap = Global.getInstance().getImageFromInternal(Util.FB_ALBUM_GRID_IMAGE, 0, 0);
    		if (bitmap != null)
    			return bitmap;
    	}
    	if (position == 1){
    		Bitmap bitmap = Global.getInstance().getImageFromInternal(Util.FB_COVER_GRID_IMAGE, 0, 0);
    		if (bitmap != null)
    			return bitmap;
    	}
    	else if (position == 2){
    		Bitmap bitmap = Global.getInstance().getImageFromInternal(Util.FB_PROFILE_GRID_IMAGE, 0, 0);
    		if (bitmap != null)
    			return bitmap;
    	}
    	else if (position == 3){
    		Bitmap bitmap = Global.getInstance().getImageFromInternal(Util.FB_TAG_GRID_IMAGE, 0, 0);
    		if (bitmap != null)
    			return bitmap;
    	}
    	else if (position == 4){
    		Bitmap bitmap = Global.getInstance().getImageFromInternal(Util.FB_UPLOADED_GRID_IMAGE, 0, 0);
    		if (bitmap != null)
    			return bitmap;
    	} 
    	
    	return BitmapFactory.decodeResource(getResources(), R.drawable.empty_photo);
    }
    
    private int getCount(int position){

    	switch(position){
    		case 0:
    			return Global.getInstance().getIntPref(Util.FB_NUMBER_OF_ALBUMS);
    			
    		case 1:
    			return Global.getInstance().getIntPref(Util.FB_NUMBER_OF_COVER);
    			
    		case 2:
    			return Global.getInstance().getIntPref(Util.FB_NUMBER_OF_PROFILE);
    			
    		case 3:
    			return Global.getInstance().getIntPref(Util.FB_NUMBER_OF_TAGGED);
    			
    		case 4:
    			return Global.getInstance().getIntPref(Util.FB_NUMBER_OF_UPLOADED);
    			
    		case 5:
    			if (FBINIT.isAlbumEmpty()) return Global.getInstance().getIntPref(Util.FB_NUMBER_OF_FAVORITE);
    			else {
    				String mSpecial = PreferenceManager	.getDefaultSharedPreferences(getActivity().getApplicationContext())
    						.getString(getResources()
    								.getString(R.string.pref_favFBAlbum_key), "");  	// get preferred first screen
    			
    					int pos = FBINIT.getIndexByID(mSpecial);
    					// 	Album array will be empty initially, so return last saved favorite
    					return (pos != -1) ? FBINIT.sAlbumsArray.get(pos).mAlbumSize : Global.getInstance().getIntPref(Util.FB_NUMBER_OF_FAVORITE);
    			}
    			
    		default:
    			return 0;
    			
    	}
	}
    
    private static class ImageHolder{
		TextView txtTitle;
		RecyclingImageView imageItem;
		TextView count;
	}

	@Override
	protected void refreshData() {}

	@Override
	protected int getFragmentLayout() {
		return R.layout.layout_main;
	}

	@Override
	protected View getViewX(int position, View convertView, ViewGroup parent, LayoutParams params, int mItemHeight) {
		View row = convertView;
        ImageHolder holder = null;
        
        if (row == null) {  // if it's not recycled, initialize some attributes
        	LayoutInflater inflater = (LayoutInflater)getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        	row = inflater.inflate(R.layout.main_imageview, parent, false);
        	holder = new ImageHolder();
        	holder.txtTitle = (TextView) row.findViewById(R.id.item_text);
        	holder.imageItem = (RecyclingImageView) row.findViewById(R.id.item_image);
        	holder.count = (TextView) row.findViewById(R.id.item_count);
        	holder.imageItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
        	row.setLayoutParams(params);
        	row.setTag(holder);	
        }else{
        	holder = (ImageHolder) row.getTag();
        }
        
        if (row.getLayoutParams().height != mItemHeight) {
            row.setLayoutParams(params);
        }
        	
        	holder.imageItem.setImageBitmap(getImage(position));
            
            if (position == 5){
            	String initName = Global.getInstance().getStrPref(Util.FB_FAVORITE_NAME);
            	String finName = (!initName.isEmpty()) ? initName : mGridName[position];
            	if (FBINIT.isAlbumEmpty()){
            		holder.txtTitle.setText(finName);
            	} else{
            		
            		String mSpecial = PreferenceManager	.getDefaultSharedPreferences(getActivity().getApplicationContext())
            				.getString(getResources()
            						.getString(R.string.pref_favFBAlbum_key), "");  	// get preferred first screen
            	
            		int pos = FBINIT.getIndexByID(mSpecial);
            		holder.txtTitle.setText(pos != -1 ? FBINIT.sAlbumsArray.get(pos).mAlbumName : finName);
            	}
            }
            
            else{
            	holder.txtTitle.setText(mGridName[position]);
            }
            
            int count = getCount(position);
            
            if (count != 0){
            	holder.count.setText(Integer.toString(count));
            	holder.count.setVisibility(View.VISIBLE);
            }
            
            
        return row;
	}

	@Override
	protected int getGridSizeX() {
		return 6;
	}

	@Override
	protected int getImageSpacing() {
		return getResources().getDimensionPixelSize(R.dimen.imageSpacing_1dp);
	}

	@Override
	protected int getImageSize() {
		return getResources().getDimensionPixelSize(R.dimen.imageSize_150dp);
	}

	@Override
	protected String getClassName() {
		return Fragment_Main.class.getSimpleName();
	}

	@Override
	protected ImageCacheParams getImageCache() {
		return null;
	}

	@Override
	protected boolean isSwipeRefresheable() {
		return false;
	}

	@Override
	protected int getGridId() {
		return R.id.grid_fb_main_fragment;
	}

	@Override
	protected void onItemClickX(AdapterView<?> parent, View view, int position, long id) {
		switch(position){
		case 0:
			// go to albums
			mLaunch = new Intent(getActivity(), Activity_Albums.class);
			break;
			
		case 1:
			// go to cover
			mLaunch = new Intent(getActivity(), Activity_Photos.class);
			mLaunch.putExtra(Activity_Photos.WHICH_ALBUM_NAME, "Cover Photos");
			mLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			break;
			
		case 2:
			// go to profile
			mLaunch = new Intent(getActivity(), Activity_Photos.class);
			mLaunch.putExtra(Activity_Photos.WHICH_ALBUM_NAME, "Profile Pictures");
			mLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			break;
			
		case 3:
			// go to tags
			mLaunch = new Intent(getActivity(), Activity_Tag.class);
			mLaunch.putExtra("address", Util.TAG_PHOTO_LAUNCH_CODE);
			break;
			
		case 4:
			// go to uploaded
			mLaunch = new Intent(getActivity(), Activity_Tag.class);
			mLaunch.putExtra("address", Util.UPLOADED_PHOTO_LAUNCH_CODE);
			break;
			
		case 5:
			// go to shared
			mLaunch = new Intent(getActivity(), Activity_Photos.class);
			String mSpecial = PreferenceManager	.getDefaultSharedPreferences(getActivity().getApplicationContext())
												.getString(getResources()
												.getString(R.string.pref_favFBAlbum_key), "");  	// get preferred first screen
			
			mLaunch.putExtra(Activity_Photos.WHICH_ALBUM_NAME, (mSpecial.isEmpty() ? Activity_Photos.NO_FAVORITE : mSpecial));
			mLaunch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			break;
			
		default:
			break;
		}
		
		// start the new activity if not null
		if (mLaunch != null){
			mLaunch.putExtra(Activity_Photos.FROM_HOMEPAGE, true);
			startActivity(mLaunch);
		}
	}

	@Override
	protected int swipeLayout() {
		return 0;
	}

	@Override
	protected int progressBar() {
		return 0;
	}

	@Override
	protected int viewToSwipe() {
		return 0;
	}

	@Override
	protected void onDestroyX() {
		
	}

	@Override
	protected void onPauseX() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void personalGridInit() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void onResumeX() {
		updateGrid();
		
	}

	@Override
	protected int[] swipeColor() {
		// TODO Auto-generated method stub
		return null;
	}
}
