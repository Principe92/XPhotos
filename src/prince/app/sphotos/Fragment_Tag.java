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
import prince.app.sphotos.database.DbService;
import prince.app.sphotos.database.FBDbContract;
import prince.app.sphotos.database.ImagesAccess;
import prince.app.sphotos.database.ImagesAccess.ImageDbListener;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Tasks;
import prince.app.sphotos.tools.Util;
import prince.app.sphotos.ui.RecyclingImageView;
import prince.app.sphotos.util.ImageCache.ImageCacheParams;
import prince.app.sphotos.util.ImageFetcher;
import android.annotation.TargetApi;
import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AbsListView.LayoutParams;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.Toast;

public class Fragment_Tag extends FragmentX {
	private static final String TAG = Fragment_Tag.class.getSimpleName();
	public static final String LAUNCHCODE = "launch_code";
	public static final String TASK = "task_tag";
	
	// Local variables
	private int mLaunchKey;
	private String mType;
	private String mAlbumName;
	
	private String mGraphPath;
	private Bundle mBundle;
	
	
	private static ImageFetcher sImageFetcher;
	private boolean mLoadedCache = false;
	
    
    /**
     * Returns a new instance of the FB_Tag_Fragment
     * @param launchCode - indicates whether we want to load tagged photos or uploaded photos
     * @return
     */
    public static Fragment_Tag newInstance(int launchCode) {
		final Fragment_Tag fragment = new Fragment_Tag();
		final Bundle args = new Bundle();
		args.putInt(LAUNCHCODE, launchCode);
		fragment.setArguments(args);
		return fragment;
	}
    
    @Override
	protected void onCreateInit(){
    	sImageFetcher = new ImageFetcher(getActivity(), getImageSize());
        sImageFetcher.setLoadingImage(R.drawable.empty_photo);
        sImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), getImageCache());
        
        // Obtain the unique launch identifier
        mLaunchKey = getArguments() != null ? getArguments().getInt(LAUNCHCODE) : 0;
     		
        // Set Graph Api parameter
        mType = (mLaunchKey == Util.UPLOADED_PHOTO_LAUNCH_CODE) ? "uploaded" : "tagged";
     		
        // set the db table name
        final String dbName = (mLaunchKey == Util.UPLOADED_PHOTO_LAUNCH_CODE) ? FBDbContract.FB_UPLOADED_TABLE : FBDbContract.FB_TAGGED_TABLE ;
     		
        // Assign the adequate name
        mAlbumName = (mLaunchKey == Util.UPLOADED_PHOTO_LAUNCH_CODE)? "Uploaded Photos" : "Tagged Photos";
     		
        // Set right name on the ActionBar
        getActivity().getActionBar().setTitle(mAlbumName);	
        
        // initialize listener
        new GraphRequest().initListener(new GraphRequest.RequestListener(){
        	@Override
        	public void onGraphProgress(boolean refresh, String taskId) {
        		
        		if (taskId.equalsIgnoreCase(TASK) && isOnline()){
        			// Start the refreshing indicator
        			getmSwipeRefreshLayout().setRefreshing(true);
        			
        			// make Grid Visible
        			getmGridView().setVisibility(View.VISIBLE);
        			
        			// make Progress Bar invisible
        			getmProgress().setVisibility(View.INVISIBLE);
        		}
        	}
     				
        	@Override
        	public void onGraphFinish(String taskId) {
        		
        		if (taskId.equalsIgnoreCase(TASK) && isOnline()){
        			// Stop the refreshing indicator
        			getmSwipeRefreshLayout().setRefreshing(false);
        			
        			if (mLoadedCache){
        				mLoadedCache = false;
        				updateGrid();
        			}
        			
        			// Save to db
					if (FBINIT.IMAGES_UPDATED){
						Log.d(TAG, "INTENT: Save image data to DB");
						Intent it = new Intent(getActivity(), ImagesAccess.class);
						it.putExtra(DbService.DB_INTENT_KEY, DbService.UPDATE_TB);
						it.putExtra(DbService.EXTRAS_STRING, dbName);
						getActivity().startService(it);
					}
        		}
        	}

        	@Override
        	public void onGraphStart(String taskId) {}

        	@Override
        	public void coverReady(String taskId) {}

        	@Override
        	public void onGraphError(GraphError error, String id) {
        		if(error == GraphError.NO_IMAGE && id.equalsIgnoreCase(TASK) && isOnline()){
        			// Check if we have some images loaded
        			synchronized(FBINIT.sImagesArray_NEW){
        				if (FBINIT.sImagesArray_NEW.size() == 0 && FBINIT.sImagesArray.size() == 0){
        					// initialize db read
        					// Initialize intent to load images from db
        					Intent it = new Intent(getActivity(), ImagesAccess.class);
        					it.putExtra(DbService.DB_INTENT_KEY, DbService.READ_TB);
        					it.putExtra(DbService.EXTRAS_STRING, dbName);
        					getActivity().startService(it);
        				}
     							
        				else{
        					if (FBINIT.sImagesArray_NEW.size() > FBINIT.sImagesArray.size()){
        						onGraphProgress(true, TASK);
        						GraphRequest.imageAvailable(true, true, TASK);
        					}
     								
        					else{
        						onGraphProgress(true, TASK);
        						GraphRequest.imageAvailable(false, true, TASK);
        					}
     								
        					Global.getInstance().showToast("Unable to fetch all photos");
        					
        					if (!Global.getInstance().isConnection()) Global.getInstance().showToast("Check network connection", Toast.LENGTH_LONG);
        				}
        			}
        		}
     					
        	}
        }); 
     				
        // Init Listener for Db Access
        ImagesAccess.setListener(new ImageDbListener(){

        	@Override
        	public void onDbStart() {}

        	@Override
        	public void onDbStop(boolean success) {
        		if (isOnline()){
        			
	        		if (success){
	        			FBINIT.IMAGES_READY = true;
	        			FBINIT.IMAGES_TASK_DONE = true;
	        			FBINIT.IMAGES_TASK_SUCCESS = true;
	        			FBINIT.IMAGES_UPDATED = false;
	     						
	        			synchronized(FBINIT.sImagesArray_LOCK){
	        				FBINIT.sImagesArray_LOCK.notifyAll();
	        			}
	     								
	     						
	        			getActivity().runOnUiThread(new Runnable(){
	        				@Override
	        				public void run() {
	        					getmGridView().setVisibility(View.VISIBLE);
	        					getmProgress().setVisibility(View.INVISIBLE);
	        				}
	        			});	
	        			
	        			mLoadedCache = true;
	        		}	
	     							
	        		else{
	        			getActivity().runOnUiThread(new Runnable(){
	        				@Override
	        				public void run() {
	        					Toast.makeText(getActivity(), "No cached images !!!", Toast.LENGTH_SHORT).show();
	        					mCallback.onDbError();
	        				}
	        			});
	        		}
        		}
        	}
     						
        });
        
		
		// fetch new image data if current activity/fragment didn't use cache last
		if (FBINIT.sImagesArray_LASTUSED != mLaunchKey || FBINIT.isImageEmpty()){
			FBINIT.sImagesArray_LASTUSED = mLaunchKey;
			
			// Clear arrays
			synchronized (FBINIT.sImagesArray){
				FBINIT.sImagesArray.clear();
				FBINIT.sImagesArray_NEW.clear();
			}
				
			mGraphPath = "me/photos";
			mBundle= new Bundle();
			mBundle.putString("type", mType);
				
			// No network connection
			if (!Global.getInstance().isConnection()){
				Toast.makeText(getActivity(), "Loading cached images", Toast.LENGTH_SHORT).show();
				
				// Initialize intent to load images from db
				Intent it = new Intent(getActivity(), ImagesAccess.class);
				it.putExtra(DbService.DB_INTENT_KEY, DbService.READ_TB);
				it.putExtra(DbService.EXTRAS_STRING, dbName);
				getActivity().startService(it);
				
				// Only update cache with new data after download is done
				GraphRequest.imageRequest(	 
											mGraphPath, 
											mBundle, 
											-1, 
											true, 
											false, 
											true, 
											null, 
											TASK,
											FBINIT.sImagesArray_NEW);
					}
				
			else{
			
				GraphRequest.imageRequest(	
										mGraphPath, 					// The FB Graph API request
										mBundle, 						// Extra Parameters
										-1, 							// Maximum number of data to retrieve
										true, 							// We are calling this method for the first time    (first)
										true,							// Update the cache with new data while downloading (cacheUpdate)
										true,							// Clear the download trackers						(tClear)
										null, 							// We currently have no request for the next page of data
										TASK,							// The unique ID task
										FBINIT.sImagesArray_NEW);		// The cache to store values with
			}
		}
		
		else{
			FBINIT.IMAGES_READY = true;
			FBINIT.IMAGES_TASK_DONE = true;
			FBINIT.IMAGES_TASK_SUCCESS = true;
			
			synchronized(FBINIT.sImagesArray_LOCK){
				FBINIT.sImagesArray_LOCK.notifyAll();
			}
			
			setmGridVisible(true);
		}
		
    }
	
	/**
	 * Get the current number of elements in the IMAGE_DETAILS array
	 * @return - The current size or 25 if currently empty
	 */
	private int getGridCount(){
		synchronized(FBINIT.sImagesArray){
			if (FBINIT.sImagesArray.size() > 0){
				return FBINIT.sImagesArray.size();
			}
		}
		return 25;
	}

	public static void clearCache() {
		sImageFetcher.clearCache();
		
	}
	
	public void refreshData(){
		if (Global.getInstance().isConnection()){
	        // Start the refreshing indicator
			getmSwipeRefreshLayout().setRefreshing(true);
        
	        // refresh download parameters
	        mGraphPath =  "me/photos";
	        mBundle= new Bundle();
	        mBundle.putString("type", mType); 
	        GraphRequest.imageRequest(	
	        							mGraphPath, 					// FB Graph API request
	        							mBundle, 						// Extra parameter
	        							-1, 							// Maximum number of data to fetch
	        							true, 							// We are calling this method for the first time
	        							false,							// Do not update the cache while downloading
	        							true,							// Clear the download trackers
	        							null, 							// We currently have no requests for the next page of results
	        							TASK,							// The unique task ID
	        							FBINIT.sImagesArray_NEW);		// The cache to store downloaded values
		} else {
	        // Stop the refreshing indicator
	        getmSwipeRefreshLayout().setRefreshing(false);
	        Toast.makeText(getActivity(), "No network connection", Toast.LENGTH_SHORT).show();
		}
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
	protected int getFragmentLayout() {
		return R.layout.photos_fragment;
	}


	@Override
	protected View getViewX(int position, View convertView, ViewGroup parent, LayoutParams params, int mItemHeight) {
		// Now handle the main ImageView thumbnails
        ImageView imageView;
        if (convertView == null) { // if it's not recycled, instantiate and initialize
            imageView = new RecyclingImageView(getActivity());
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setLayoutParams(params);
        } else { // Otherwise re-use the converted view
            imageView = (ImageView) convertView;
        }

        // Check the height matches our calculated column width
        if (imageView.getLayoutParams().height != mItemHeight) {
            imageView.setLayoutParams(params);
            Log.d(TAG, "imageView height different from calculated height");
        }

        // Finally load the image asynchronously into the ImageView, this also takes care of
        // setting a placeholder image while the background thread runs
        new Tasks(imageView, sImageFetcher, Tasks.IMAGES_WAIT).execute(position);
        return imageView;
	}


	@Override
	protected int getGridSizeX() {
		return getGridCount();
	}


	@Override
	protected int getImageSpacing() {
		return getResources().getDimensionPixelSize(R.dimen.image_thumbnail_spacing);
	}


	@Override
	protected int getImageSize() {
		return getResources().getDimensionPixelSize(R.dimen.image_thumbnail_size);
	}


	@Override
	protected String getClassName() {
		return Fragment_Tag.class.getSimpleName();
	}


	@Override
	protected ImageCacheParams getImageCache() {
		// init the cache
	    FBINIT.getInstance().init_Thumbnail(getActivity(), 0.50f);
	    
	    return FBINIT.getInstance().imageCache;
	}


	@Override
	protected void onDestroyX() {
		GraphRequest.setExitTasksEarly(true);
		
		GraphRequest.removeRequest(TASK);
		
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
	protected void onResumeX() {
		
        sImageFetcher.setExitTasksEarly(false);
        
        Log.e(TAG, "RESUMED - - - - -");
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
	protected void onItemClickX(AdapterView<?> parent, View view, int position, long id) {
		Intent intent = new Intent(getActivity(), Activity_FullImage.class);
    	int argument[] = {position, getGridCount()};
        intent.putExtra(Activity_Photos.IMAGE_DETAILS, argument);
        intent.putExtra(Activity_Photos.ALBUM_NAME, mAlbumName);
        
        if (Global.hasJellyBean()) {
            ActivityOptions options =
                    ActivityOptions.makeScaleUpAnimation(view, 0, 0, view.getWidth(), view.getHeight());
            getActivity().startActivity(intent, options.toBundle());
        } else {
            startActivityForResult(intent, Activity_Tag.LAUNCH_FULL_IMAGE);
        }
        
        startActivityForResult(intent,Activity_Tag.LAUNCH_FULL_IMAGE);
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
	protected int[] swipeColor() {
		int color [] = {	R.color.swipe_color_1,
							R.color.swipe_color_2,
							R.color.swipe_color_3,
							R.color.swipe_color_4};
		
		return color;
	}
}
