/*
 * Copyright (C) 2013 Princewill Chibututu Okorie 
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

import java.util.Timer;
import java.util.TimerTask;

import prince.app.sphotos.Request.GraphRequest;
import prince.app.sphotos.Request.GraphRequest.GraphError;
import prince.app.sphotos.database.AlbumsAccess;
import prince.app.sphotos.database.AlbumsAccess.AlbumDbListener;
import prince.app.sphotos.tools.AlertDialogX;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Tasks;
import prince.app.sphotos.ui.RecyclingImageView;
import prince.app.sphotos.util.ImageCache.ImageCacheParams;
import prince.app.sphotos.util.ImageFetcher;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

/**
 * Fragment class that shows a grid list of all the user's facebook albums
 * @author Princewill Okorie
 *
 */
public class Fragment_Albums extends FragmentX {
	private static String TAG = Fragment_Albums.class.getSimpleName();
	public static final int CLICKED_ALBUM_POSITION = 510;
	
	public static AlbumProperties mAlbumDetails;
	private static ImageFetcher sImageFetcher;
	
	public static final String DEL_ALBUM = "delete_album";
	public static final String PROPERTIES = "properties";
	public static final String GET_ALBUM = "download_album";
	
	@Override
	protected void personalGridInit() {
		// register for contextual menu
		registerForContextMenu(getmGridView());
	}
	
	
	@Override
	protected void onCreateInit(){
		sImageFetcher = new ImageFetcher(getActivity(), getImageSize());
		sImageFetcher.setLoadingImage(R.drawable.empty_photo);
		sImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), getImageCache());
		
		if (!FBINIT.isAlbumEmpty()) setmGridVisible(true);
		
		if (!Global.getInstance().isConnection()){
		
			AlbumsAccess.setListener(new AlbumDbListener(){

				@Override
				public void onDbStart() {
					// TODO Auto-generated method stub
				
				}

				@Override
				public void onDbStop(boolean success) {
					Log.e(TAG, "DB STOP - - - - -");
					
					if (success){
						// Stop the refreshing indicator
						getmSwipeRefreshLayout().setRefreshing(false);
			
						FBINIT.ALBUMS_READY = true;
					
						synchronized(FBINIT.sAlbumsArray_LOCK){
							FBINIT.sAlbumsArray_LOCK.notifyAll();
						}
			
						getmGridView().setVisibility(View.VISIBLE);
						getmProgress().setVisibility(View.INVISIBLE);
					}
					
					else {
						// no internet connection and no cached albums
						getActivity().runOnUiThread(new Runnable(){

							@Override
							public void run() {
								mCallback.onDbError();
								
							}
							
						});
					}
				
				}});
		}
		
		new GraphRequest().initListener(new GraphRequest.RequestListener() {
	    	@Override
	    	public void onGraphProgress(boolean refresh, String id) {
	    		
	    		if (id.equalsIgnoreCase(TAG) || id.equalsIgnoreCase(FBMainActivity.TASK_ALBUM)){
	    			// Start the refreshing indicator
	    			getmSwipeRefreshLayout().setRefreshing(true);
	    			
					getmGridView().setVisibility(View.VISIBLE);
					getmProgress().setVisibility(View.INVISIBLE);
	    		}
	    	}
	    	
			@Override
			public void onGraphFinish(String id) {}

			@Override
			public void onGraphStart(String taskId) {}

			@Override
			public void coverReady(String id) {
			
				if (id.equalsIgnoreCase(TAG) || id.equalsIgnoreCase(FBMainActivity.TASK_ALBUM)){
					// Stop the refreshing indicator
					getmSwipeRefreshLayout().setRefreshing(false);
						
					// update grid info
					updateGrid();
				}
				
			}

			@Override
			public void onGraphError(GraphError error, String id) {
				
				if (error == GraphError.NO_COVER && id.equalsIgnoreCase(FBMainActivity.TASK_ALBUM)){
					Global.getInstance().showToast("Unable to load images");
					if (!Global.getInstance().isConnection()) Global.getInstance().showToast("Check network connection", Toast.LENGTH_LONG);
				}
				
				else if (error == GraphError.NO_ALBUM && id.equalsIgnoreCase(FBMainActivity.TASK_ALBUM)){
					mCallback.onDbError();
				}
				
				// may occur if we refresh
				else if (error == GraphError.NO_COVER && id.equalsIgnoreCase(TAG)){
					Global.getInstance().showToast("Unable to load new images");
					if (!Global.getInstance().isConnection()) Global.getInstance().showToast("Check network connection", Toast.LENGTH_LONG);
					
					// Stop the refreshing indicator
					getmSwipeRefreshLayout().setRefreshing(false);
				}
				
				// may occur if we refresh
				else if (error == GraphError.NO_ALBUM && id.equalsIgnoreCase(TAG)){
					Global.getInstance().showToast("Unable to update Albums");
					if (!Global.getInstance().isConnection()) Global.getInstance().showToast("Check network connection", Toast.LENGTH_LONG);
					
					// Stop the refreshing indicator
					getmSwipeRefreshLayout().setRefreshing(false);
				}
				
			}
	    });
		
		
	/*	if (FBINIT.isAlbumEmpty() && !FBINIT.ALBUMS_TASK_STARTED && !FBINIT.ALBUMS_TASK_DONE && !GraphRequest.inMap(FB_Main_Activity.TASK_ALBUM)){
	    	// obtain album for the new user
	    	GraphRequest.albumRequest(	
	    								true, 							// We are calling the method for the first time
	    								true, 							// Update the cache with new values
	    								true, 							// Clear the download trackers
	    								null, 							// We currently have no request for the next page of results
	    								FB_Main_Activity.TASK_ALBUM,	// Unique task ID
	    								FBINIT.sAlbumsArray_NEW,		// The cache to use to store downloaded values
	    								0);
		} */
		
		setTimer(); // set timer to wait for data
	}
	
	private void setTimer(){
		
		new Timer().schedule(new TimerTask(){

			@Override
			public void run() {
				if (getActivity() != null){
					getActivity().runOnUiThread(new Runnable(){

						@Override
						public void run() {
							// no internet connection
							if (FBINIT.isAlbumEmpty()) mCallback.onDbError();
						
						}
					
					});
				
				}
			}
			
			}, Global.WAIT_TIME);
	}
	
	
	public static void clearCache(){
		sImageFetcher.clearCache();
	}
	
	@Override
	protected int getFragmentLayout() {
		return R.layout.layout_albums;
	}
	
	@Override
	protected int getGridSizeX() {
		return (FBINIT.isAlbumEmpty() ? 1 : FBINIT.sAlbumsArray.size());
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
		return Fragment_Albums.class.getSimpleName();
	}


	@Override
	protected ImageCacheParams getImageCache() {
		// initialize the cache
	    FBINIT.getInstance().init_CoverPhoto(getActivity(), 0.25f);
	    
		return FBINIT.getInstance().coverphoto;
	}


	@Override
	protected boolean isSwipeRefresheable() {
		return true;
	}


	@Override
	protected int getGridId() {
		return R.id.grid_fb_main_fragment;
	}


	@Override
	protected void onItemClickX(AdapterView<?> parent, View view, int position, long id) {
		if (!FBINIT.isAlbumEmpty()){
			Intent intent = new Intent(getActivity(), Activity_Photos.class);
			intent.putExtra(Activity_Photos.WHICH_ALBUM_POS, 	position);
			intent.putExtra(Activity_Photos.FROM_HOMEPAGE, 		false);
			intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivityForResult(intent, CLICKED_ALBUM_POSITION);	
		}
		
		else{
			Toast.makeText(getActivity(), "Still fetching albums", Toast.LENGTH_SHORT).show();
		}
		
	}


	@Override
	protected int swipeLayout() {
		return R.id.main_swipe;
	}


	@Override
	protected int progressBar() {
		return R.id.main_progress;
	}


	@Override
	protected int viewToSwipe() {
		return R.id.grid_fb_main_fragment;
	}

	@Override
	protected void refreshData() {
		if (Global.getInstance().isConnection()){
			// Start the refreshing indicator
			getmSwipeRefreshLayout().setRefreshing(true);

    		// download album and cover url again 
    		GraphRequest.albumRequest(	
    									true, 						// We are calling the method for the first time
    									true, 						// Update the cache with new values
    									true, 						// Clear the download trackers
    									null, 						// We currently have no next page request
    									TAG,						// The unique task ID
    									FBINIT.sAlbumsArray_NEW,   // The cache to use to store downloaded values
    									0);
		

    	} 
		
		else{
    		Toast.makeText(getActivity(), "No network connection", Toast.LENGTH_SHORT).show();
			// Stop the refreshing indicator
			getmSwipeRefreshLayout().setRefreshing(false);
    	}
		
	}
	
	@Override
	public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
	    super.onCreateContextMenu(menu, v, menuInfo);
	    AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)menuInfo;
	    
	    // set the contextual menu header
	    menu.setHeaderTitle(FBINIT.sAlbumsArray.get(info.position).mAlbumName);
	    
	    MenuInflater inflater = getActivity().getMenuInflater();
	    inflater.inflate(R.menu.album_menu, menu);
	    
	    // hide add photos if we cannot upload to this album
	    if (!FBINIT.sAlbumsArray.get(info.position).mAlbumUpload){
	    	menu.removeItem(R.id.album_add);
	    }
	}
	
	// Called when an item on the contextual menu is pressed
	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo)item.getMenuInfo();
		
	    switch (item.getItemId()) {
	        case R.id.album_open:
	            // Launch the album
	        	onItemClickX(null, info.targetView, info.position,info.id);
	            return true;
	        case R.id.album_details:
	            //TODO: Show details of the album
	        	//Inflate the details dialog
				FragmentTransaction ft = getFragmentManager().beginTransaction();
				Fragment aX = getFragmentManager().findFragmentByTag(PROPERTIES);
				if (aX != null) ft.remove(aX);
				ft.addToBackStack(null);
				
				DialogFragment details = AlbumProperties.newInstance(info.position);
				details.show(ft, PROPERTIES);
	        	
	            return true;
	        case R.id.album_add:
	            //TODO: Launch intent to add photos to album
	        	Toast.makeText(getActivity(), "Add photos", Toast.LENGTH_SHORT).show();
	            return true;
	        case R.id.album_delete:
	            //TODO: Delete album from facebook
	        	DialogFragment aT = AlertDialogX.newInstance(	"delete " + FBINIT.sAlbumsArray.get(info.position).mAlbumName + " ?", 
						getActivity().getResources().getString(R.string.warning_delete_album), 
						R.string.go_on, 
						R.string.cancel,
						DEL_ALBUM);
	        	
	        	aT.show(getFragmentManager(), DEL_ALBUM);
	            return true;
	        case R.id.album_download:
	            //TODO: Launch intent to add photos to album
	        	//TODO: Delete album from facebook
	        	DialogFragment aY = AlertDialogX.newInstance(	"download " + FBINIT.sAlbumsArray.get(info.position).mAlbumName + " ?", 
						getActivity().getResources().getString(R.string.warning_download_albums), 
						R.string.go_on, 
						R.string.cancel,
						GET_ALBUM);
	        	
	        	aY.show(getFragmentManager(), DEL_ALBUM);
	            return true;
	        default:
	            return super.onContextItemSelected(item);
	    }
	}
	
	private class ImageHolder{
			TextView albumNameView;
			RecyclingImageView imageView;
			TextView albumSizeView;
		}

	@Override
	protected View getViewX(int position, View convertView, ViewGroup parent, LayoutParams params, int mItemHeight) {
		View mView = convertView;
		ImageHolder imageHolder = null;
		if (mView == null) {  // if it's not recycled, initialize some attributes
	           
			LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
			mView = inflater.inflate(R.layout.albums_imageview, parent, false);
			imageHolder = new ImageHolder();
			imageHolder.albumNameView = (TextView) mView.findViewById(R.id.item_text);
			imageHolder.imageView = (RecyclingImageView) mView.findViewById(R.id.item_image);
	        imageHolder.imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
	        imageHolder.albumSizeView = (TextView) mView.findViewById(R.id.item_count);
	        mView.setLayoutParams(params);
	        mView.setTag(imageHolder);
	        } else {
	        	imageHolder = (ImageHolder) mView.getTag();
	        }
	        
		 // Check the height matches our calculated column width
		 if (mView.getLayoutParams().height != mItemHeight) {
			 mView.setLayoutParams(params);
			 Log.e(TAG, "imageView height different from calculated height");
		 }
	        
		 new Tasks(imageHolder.imageView, sImageFetcher, Tasks.ALBUMS_WAIT).execute(position);
	        
		 synchronized (FBINIT.sAlbumsArray){
			 if (FBINIT.sAlbumsArray != null && FBINIT.sAlbumsArray.size() > position ){
				 String name = FBINIT.sAlbumsArray.get(position).mAlbumName;
				 imageHolder.albumNameView.setText(name);
	        	
				 int count = FBINIT.sAlbumsArray.get(position).mAlbumSize;
				 imageHolder.albumSizeView.setText(Integer.toString(count));
				 imageHolder.albumSizeView.setVisibility(View.VISIBLE);
				 
				 getActivity().setProgressBarIndeterminateVisibility(false);
			 }
			 else{
					
				 getActivity().setProgressBarIndeterminateVisibility(true);
				 
			 }
		 }
	        
		 return mView;
	}

	@Override
	protected void onDestroyX() {
		sImageFetcher.closeCache();
		
	}

	@Override
	protected void onPauseX() {
		sImageFetcher.setPauseWork(false);
		sImageFetcher.setExitTasksEarly(true);
		sImageFetcher.flushCache();
	}
	
	@Override
	protected void onResumeX() {
        sImageFetcher.setExitTasksEarly(false);
	}


	@Override
	protected int[] swipeColor() {
		int color [] = {	R.color.swipe_color_2,
				R.color.swipe_color_1,
				R.color.swipe_color_4,
				R.color.swipe_color_3};

		return color;
	}
	
	public void launchOptions(View view){
		getActivity().openContextMenu(view);
	}
	
}
