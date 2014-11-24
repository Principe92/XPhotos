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

import java.util.Timer;
import java.util.TimerTask;

import prince.app.sphotos.Request.GraphRequest;
import prince.app.sphotos.Request.GraphRequest.GraphError;
import prince.app.sphotos.database.DbService;
import prince.app.sphotos.database.FBDbContract.ImageHelper;
import prince.app.sphotos.database.ImagesAccess;
import prince.app.sphotos.database.ImagesAccess.ImageDbListener;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Tasks;
import prince.app.sphotos.ui.RecyclingImageView;
import prince.app.sphotos.util.ImageCache.ImageCacheParams;
import prince.app.sphotos.util.ImageFetcher;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.CheckBox;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

/**
 * A fragment class that shows the image thumbnails of a particular facebook album in grids
 * @author Princewill Okorie
 *
 */
public class Fragment_Photos extends FragmentX{
	private static final String TAG = Fragment_Photos.class.getSimpleName();
	public static final String TASK_IMAGE = "task_";
	public static final String GRID_POSITION = "position";
	
	// Local variables
	private int mImageCount;
	private int mGridPosition;
	private String mAlbumName;
	private String mAlbumID;
	
	private static ImageFetcher sImageFetcher;
	
	private String mGraphPath;
	
	private boolean mLoadedCache = false;
	
	/**
	 * A Function that creates a new instance of the fragment
	 * @param album - The grid position of the facebook album to load its photos
	 * @return
	 */
	public static Fragment_Photos newInstance(int album) {
		final Fragment_Photos fragment = new Fragment_Photos();
		final Bundle args = new Bundle();
		args.putInt(GRID_POSITION, album);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	protected void onCreateInit(){
		// initialize the image fetcher class
		sImageFetcher = new ImageFetcher(getActivity(), getImageSize());
		sImageFetcher.setLoadingImage(R.drawable.empty_photo);
		sImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), getImageCache());
		
		
		// retrieve the intent position
		mGridPosition = getArguments() != null ? getArguments().getInt(GRID_POSITION) : 0;
		
		// SetUp Listener for image availability
		new GraphRequest().initListener(new GraphRequest.RequestListener() {
			@Override
			public void onGraphProgress(boolean refresh, String taskId) {
				if (taskId.equalsIgnoreCase(TASK_IMAGE + mAlbumID)){
					// Start the refreshing indicator
					getmSwipeRefreshLayout().setRefreshing(true);
				
					getmGridView().setVisibility(View.VISIBLE);
					getmProgress().setVisibility(View.INVISIBLE);
				}
			}

			@Override
			public void onGraphFinish(String taskId) {
				if (taskId.equalsIgnoreCase(TASK_IMAGE + mAlbumID)){
					if (mLoadedCache) {
						mLoadedCache = false;
						updateGrid();
					}
				
					getmGridView().setVisibility(View.VISIBLE);
					getmProgress().setVisibility(View.INVISIBLE);
				
					// Stop the refreshing indicator
					getmSwipeRefreshLayout().setRefreshing(false);
					
					// Save to db
					if (FBINIT.IMAGES_UPDATED){
						Log.d(TAG, "INTENT: Save image data to DB");
						Intent it = new Intent(getActivity(), ImagesAccess.class);
						it.putExtra(DbService.DB_INTENT_KEY, DbService.UPDATE_TB);
						it.putExtra(DbService.EXTRAS_STRING, ImageHelper.BASE_TB_NAME + mAlbumID);
						getActivity().startService(it);
					}
				}
			}

			@Override
			public void onGraphStart(String taskId) {
				if (taskId.equalsIgnoreCase(TASK_IMAGE + mAlbumID)){
					// Start the refreshing indicator
					if (isGridViewVisible() && getmSwipeRefreshLayout() != null){
						getmSwipeRefreshLayout().setRefreshing(true);
					}
				}
				
			}

			@Override
			public void coverReady(String taskId) {}

			@Override
			public void onGraphError(GraphError error, String id) {}
		}); 
		
		// Fetch new images or NOT depending on intent value
		if (FBINIT.sImagesArray_LASTUSED != mGridPosition || FBINIT.isImageEmpty()){
			FBINIT.sImagesArray_LASTUSED = mGridPosition;
			
			fetchImageData();
	
		}
		
		else {
			FBINIT.IMAGES_READY = true;
			FBINIT.IMAGES_TASK_DONE = true;
			FBINIT.IMAGES_TASK_SUCCESS = true;
			
			synchronized(FBINIT.sImagesArray_LOCK){
				FBINIT.sImagesArray_LOCK.notifyAll();
			}
			
            setmGridVisible(true);
		}
		
		setTimer();
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
							if (FBINIT.sImagesArray.size() == 0) mCallback.onDbError();
							
						}
						
					});
					
				}
			}
		}, Global.WAIT_TIME);
	}
	
	private void fetchImageData(){
		
		// init necessary variables
		synchronized (FBINIT.sAlbumsArray){
        	mAlbumID = FBINIT.sAlbumsArray.get(mGridPosition).mAlbumID;			// ID of the album
        	mImageCount = FBINIT.sAlbumsArray.get(mGridPosition).mAlbumSize;			// Number of photos in the album
        	mAlbumName = FBINIT.sAlbumsArray.get(mGridPosition).mAlbumName;			// Get the name of the album
			getActivity().getActionBar().setTitle(mAlbumName);								// Set the title bar to the album name
        }
		
		mGraphPath = "/" + mAlbumID + "/photos";
		
		// Clear arrays
		synchronized (FBINIT.sImagesArray){
			FBINIT.sImagesArray.clear();
			FBINIT.sImagesArray_NEW.clear();
		}
		
		// No network connection
		if (!Global.getInstance().isConnection()){
			Toast.makeText(getActivity(), "Loading cached images", Toast.LENGTH_SHORT).show();
			
			// Init Listener for Db Access
			ImagesAccess.setListener(new ImageDbListener(){

				@Override
				public void onDbStart() {
					// TODO Auto-generated method stub
					
				}

				@Override
				public void onDbStop(boolean success) {
					if (success){
						FBINIT.IMAGES_READY = true;
						FBINIT.IMAGES_TASK_DONE = true;
						FBINIT.IMAGES_TASK_SUCCESS = true;
						
						synchronized(FBINIT.sImagesArray_LOCK){
							FBINIT.sImagesArray_LOCK.notifyAll();
						}
						

						getActivity().runOnUiThread(new Runnable(){
							@Override
							public void run() {
								getmGridView().setVisibility(View.VISIBLE);
								getmProgress().setVisibility(View.INVISIBLE);
							}});
						
						mLoadedCache = true;
					}
					
					else{
						getActivity().runOnUiThread(new Runnable(){
							@Override
							public void run() {
								Toast.makeText(getActivity(), "No cached images !!!", Toast.LENGTH_SHORT).show();
								mCallback.onDbError();
							}});
					}
					
				}}
					);
			
			// Initialize intent to load images from db
			Intent it = new Intent(getActivity(), ImagesAccess.class);
			it.putExtra(DbService.DB_INTENT_KEY, DbService.READ_TB);
			it.putExtra(DbService.EXTRAS_STRING, ImageHelper.BASE_TB_NAME + mAlbumID);
			getActivity().startService(it);
			
			// Only update cache with new data after download is done
			GraphRequest.imageRequest(	 
										mGraphPath, 
										null, 
										mImageCount, 
										true, 
										false, 
										true, 
										null, 
										TASK_IMAGE + mAlbumID,
										FBINIT.sImagesArray_NEW);
			
		}
		
		// We have internet connection. fetch data from server
		else{
			// update cache with new data on the go
			GraphRequest.imageRequest(	 
										mGraphPath, 
										null, 
										mImageCount, 
										true, 
										true, 
										true, 
										null, 
										TASK_IMAGE + mAlbumID,
										FBINIT.sImagesArray_NEW);
		}
	}

	public static int getGridCount(){
		synchronized(FBINIT.sImagesArray){
			if (FBINIT.sImagesArray.size() > 0){
				return FBINIT.sImagesArray.size();
			}
		}
		return 1;
	}
	
	private class ImageHolder{
		public RecyclingImageView imageItem;
		public CheckBox selected;
	}
	
	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	private class GridListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,long id) {
			
			Intent intent = new Intent(getActivity(), Activity_FullImage.class);
	    	int argument[] = {position, mImageCount};
	        intent.putExtra(Activity_Photos.IMAGE_DETAILS, argument);
	        intent.putExtra(Activity_Photos.ALBUM_NAME, mAlbumName);
	        
	        if (Global.hasJellyBean()) {
	            ActivityOptions options =
	                    ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
	            getActivity().startActivity(intent, options.toBundle());
	        } else {
	            startActivityForResult(intent, Activity_Photos.LAUNCH_FULL_IMAGE);
	        }
	        
	        startActivityForResult(intent,Activity_Photos.LAUNCH_FULL_IMAGE);		
		}
	}
	
	public static void onCheckboxClicked() {
	}

	public static void clearCache() {
		sImageFetcher.clearCache();
	}
	
	@Override
	protected void refreshData(){
		if (Global.getInstance().isConnection()){
			
			// Start the refreshing indicator
			getmSwipeRefreshLayout().setRefreshing(true);
			
			mGridPosition = getArguments() != null ? getArguments().getInt(GRID_POSITION) : 0;
			mAlbumID = FBINIT.sAlbumsArray.get(mGridPosition).mAlbumID;
			mGraphPath = "/" + mAlbumID + "/photos";
			GraphRequest.imageRequest(	
										mGraphPath, 
										null, 
										mImageCount, 
										true, 
										false, 
										true, 
										null, 
										TASK_IMAGE + mAlbumID,
										FBINIT.sImagesArray_NEW);
		} else {
			
			// Stop the refreshing indicator
			getmSwipeRefreshLayout().setRefreshing(false);
			Toast.makeText(getActivity(), "No network connection", Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected int getFragmentLayout() {
		return R.layout.photos_fragment;
	}

	@Override
	protected View getViewX(int position, View convertView, ViewGroup parent, LayoutParams params, int mItemHeight) {
		// Now handle the main ImageView thumbnails
    	ImageHolder image = null;
    	View row = convertView;
        if (row == null) { // if it's not recycled, instantiate and initialize
        	LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        	row = inflater.inflate(R.layout.custom_image, parent, false);
        	
        	image = new ImageHolder();
            image.imageItem = (RecyclingImageView) row.findViewById(R.id.custom_image);
            image.selected = (CheckBox) row.findViewById(R.id.custom_checkbox);
            image.selected.setVisibility(View.INVISIBLE);
            
            image.imageItem.setScaleType(ImageView.ScaleType.CENTER_CROP);
            row.setLayoutParams(params);
            
            row.setTag(image);
        } 
        else { // Otherwise re-use the converted view
            image = (ImageHolder) row.getTag();
        }

        // Check the height matches our calculated column width
        if (row.getLayoutParams().height != mItemHeight) {
            row.setLayoutParams(params);
            Log.i(TAG, "imageView height different from calculated height");
        }
        
        // Finally load the image asynchronously into the ImageView, this also takes care of
        // setting a placeholder image while the background thread runs
        new Tasks(image.imageItem, sImageFetcher, Tasks.IMAGES_WAIT).execute(position);
        return row;
	}

	@Override
	protected int getGridSizeX() {
		return getGridCount();
	}

	@Override
	protected int getImageSpacing() {
		return getResources().getDimensionPixelSize(R.dimen.imageSpacing_1dp);
	}

	@Override
	protected int getImageSize() {
		return getResources().getDimensionPixelSize(R.dimen.imageSize_100dp);
	}

	@Override
	protected String getClassName() {
		return Fragment_Photos.class.getSimpleName();
	}

	@Override
	protected ImageCacheParams getImageCache() {
		// initialize the cache
	    FBINIT.getInstance().init_Thumbnail(getActivity(), 0.25f);
	    
		return FBINIT.getInstance().imageCache;
	}

	@Override
	protected void onDestroyX() {
		
		GraphRequest.removeRequest(TASK_IMAGE + mAlbumID);
		
		FBINIT.IMAGES_READY = false;
		FBINIT.IMAGES_TASK_DONE = false;
		FBINIT.IMAGES_TASK_SUCCESS = false;
		FBINIT.IMAGES_UPDATED = false;
		sImageFetcher.closeCache();
		
		Log.e(TAG, "DESTROYED - - - - -");
	}

	@Override
	protected void onPauseX() {
		
        sImageFetcher.setPauseWork(false);
		sImageFetcher.setExitTasksEarly(true);
		sImageFetcher.flushCache();
		
		Log.e(TAG, "PAUSED - - - - -");
	}
	
	@Override
	public void onStop(){
		super.onStop();
		
		Log.e(TAG, "STOPPED - - - - -");
	}
	
	@Override
	protected void onResumeX() {
		
        sImageFetcher.setExitTasksEarly(false);
	}

	@Override
	protected boolean isSwipeRefresheable() {
		return true;
	}

	@Override
	protected int getGridId() {
		return R.id.grid;
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	protected void onItemClickX(AdapterView<?> parent, View view, int position,long id) {
		Intent intent = new Intent(getActivity(), Activity_FullImage.class);
    	int argument[] = {position, mImageCount};
        intent.putExtra(Activity_Photos.IMAGE_DETAILS, argument);
        intent.putExtra(Activity_Photos.ALBUM_NAME, mAlbumName);
        
        if (Global.hasJellyBean()) {
            ActivityOptions options =
                    ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
            getActivity().startActivity(intent, options.toBundle());
        } else {
            startActivityForResult(intent, Activity_Photos.LAUNCH_FULL_IMAGE);
        }
        
        startActivityForResult(intent,Activity_Photos.LAUNCH_FULL_IMAGE);
		
	}
	
	
	@Override
	protected int swipeLayout() {
		return R.id.photos_swipe;
	}

	@Override
	protected int progressBar() {
		return R.id.photos_progress;
	}

	@Override
	protected int viewToSwipe() {
		return R.id.grid;
	}

	@Override
	protected void personalGridInit() {
		getmGridView().setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        getmGridView().setMultiChoiceModeListener(new MultipleChoice(getActivity()));
        
		getmGridView().setOnScrollListener(new AbsListView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(AbsListView absListView, int scrollState) {
                // Pause fetcher to ensure smoother scrolling when flinging
                if (scrollState == AbsListView.OnScrollListener.SCROLL_STATE_FLING) {
                    // Before Honeycomb pause image loading on scroll to help with performance
                    if (!Global.hasHoneycomb()) {
                        sImageFetcher.setPauseWork(true);
                    }
                } else { 
                    sImageFetcher.setPauseWork(false);
                }
            }

            @Override
            public void onScroll(AbsListView absListView, int firstVisibleItem,
                    int visibleItemCount, int totalItemCount) {
            }
        });
		
	}

	@Override
	protected int[] swipeColor() {
		int color [] = {	R.color.swipe_color_1,
							R.color.swipe_color_4,
							R.color.swipe_color_3,
							R.color.swipe_color_2};

		return color;
	}
	
}
