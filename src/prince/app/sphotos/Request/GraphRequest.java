package prince.app.sphotos.Request;

import java.util.EventListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import prince.app.sphotos.database.AlbumsAccess;
import prince.app.sphotos.tools.Albums;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Images;
import prince.app.sphotos.tools.JSONBuild;
import prince.app.sphotos.tools.Util;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;

import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.RequestBatch;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.model.GraphUser;

/**
 * A Public class that handles Requests to the Facebook Open Graph Api
 * @author Princewill Okorie
 *
 */
public class GraphRequest {
	private static final String TAG = GraphRequest.class.getSimpleName();
	private static RequestListener mRequest;
	
	private static int mImageIndex = 0;
	private static int mCoverIndex = 0;
	
    private static boolean sPauseWork = false;
    private static boolean sCancelWork = false;
    private static final Object sPauseWorkLock = new Object();
    private static boolean sExitTasksEarly = false;
    
    public GraphRequest(){}
	
	public void initListener(RequestListener context){
		mRequest = context;
	}
	
	public enum GraphError {
		NO_IMAGE,
		NO_ALBUM, 
		NO_COVER
	}
	
	/**
	 * Method to update image cache and set download trackers once download finishes
	 * @param updateArray - Indicate whether to update the cache with new values
	 * @param setTrackers - Indicate whether to set the download trackers
	 * @param taskId	  - Unique task ID
	 */
	public static void imageAvailable(boolean updateArray, boolean setTrackers, String taskId){
		synchronized(FBINIT.sImagesArray_LOCK){
			
			if (updateArray){
				synchronized(FBINIT.sImagesArray){
					synchronized (FBINIT.sImagesArray_NEW){
						FBINIT.sImagesArray = FBINIT.sImagesArray_NEW.clone();
						FBINIT.sImagesArray_NEW.clear();
						FBINIT.IMAGES_UPDATED = true;
						Log.e(TAG, "!!! Image details cache update finished !!!");
					}
				}
			}
			
			if (setTrackers){
				FBINIT.IMAGES_READY = true;
				FBINIT.IMAGES_TASK_DONE = true;
				FBINIT.IMAGES_TASK_SUCCESS = true;
				Log.d(TAG, "!!! Image trackers set !!!");
			}
			
			FBINIT.sImagesArray_LOCK.notifyAll();
			mRequest.onGraphFinish(taskId);
			Log.d(TAG, "- imageRequest download done -");
		}
	}
	
	/**
	 * Method to update album cache and set download trackers once download finishes
	 * @param updateArray - Indicate whether to update the cache with new values
	 * @param setTrackers - Indicate whether to set the download trackers
	 * @param taskId	  - Unique task ID
	 */
	public static void albumAvailable(boolean updateArray, boolean setTrackers, String taskId){
		synchronized (FBINIT.sAlbumsArray_LOCK){
			
			if (updateArray){
				synchronized (FBINIT.sAlbumsArray) {
					synchronized (FBINIT.sAlbumsArray_NEW){
						FBINIT.sAlbumsArray = FBINIT.sAlbumsArray_NEW.clone();
						FBINIT.sAlbumsArray_NEW.clear();
						FBINIT.ALBUMS_UPDATED = true;
						Log.e(TAG, "!!! Album Cache Updated with new data !!!");
					}
				}
			} 
			
			if (setTrackers){
				FBINIT.ALBUMS_READY = true;
				FBINIT.ALBUMS_TASK_DONE = true;
				FBINIT.ALBUMS_TASK_SUCCESS = true;
				FBINIT.ALBUMS_TASK_STARTED = false;
				Log.e(TAG, "!!! Album trackers set !!!");
			}
			
			FBINIT.sAlbumsArray_LOCK.notifyAll();
			 
			mRequest.onGraphFinish(taskId);
			Log.e(TAG, "!!! Album download DONE !!!");
		 }
	}
	
	public static interface RequestListener extends EventListener{
		public void onGraphProgress(boolean refresh, String taskId);
		public void onGraphFinish(String taskId);
		public void onGraphStart(String taskId);
		public void coverReady(String taskId);
		public void onGraphError(GraphError error, String id);
	}
	
	 public static void setPauseWork(boolean pauseWork) {
	        synchronized (sPauseWorkLock) {
	            sPauseWork = pauseWork;
	            if (!sPauseWork) {
	                sPauseWorkLock.notifyAll();
	            }
	        }
	    }
	 
	 public static void cancelWork(boolean cancel){
		 sCancelWork = cancel;
	 }
	 
	 public static void setExitTasksEarly(boolean exitTasksEarly) {
	        sExitTasksEarly = exitTasksEarly;
	        setPauseWork(false);
	    }
	 
	 public static void requestCover(	Session session,
			 							String id,
			 							final SparseArray<Albums> cache,
			 							final String taskId,
			 							final String name){
		 
		 if (id != null && !id.isEmpty()){
			 
			 final Request.Callback requester = new Request.Callback(){
					@Override
					public void onCompleted(Response response) {
						GraphUser user = response.getGraphObjectAs(GraphUser.class);
						JSONObject result;
						
						removeRequest(taskId);
						
						if (user != null){
							result = user.getInnerJSONObject();
							
							JSONBuild.buildCoverURL(result, 0, cache, taskId, mCoverIndex);
						}
				 
						if (response.getError() != null){
							// TODO: Deal with network connection failure
							response.getRequest().setCallback(new Request.Callback() {
								@Override
								public void onCompleted(Response response) {
									GraphUser user = response.getGraphObjectAs(GraphUser.class);
									JSONObject result;
									
									if (user != null){
										result = user.getInnerJSONObject();
										
										JSONBuild.buildCoverURL(result, 0, cache, taskId, mCoverIndex);
									}
								}
							});
									
							response.getRequest().executeAsync();

							Log.e(TAG, "Error: " + response.getError());
							Log.e(TAG, "Http URL: " + response.getConnection());
						}
					}
			 
				};
				
				 // request for the cover url of all the albums
				Bundle params = new Bundle();
				params.putString("fields", "source");
				Request request = new Request(session, id , params, HttpMethod.GET, requester);
				
				if (Global.getInstance().isConnection()){
					request.executeAsync();
				}
				
				else{
					saveRequest(request, null, taskId);
				}
		 }
		 
		 else {
			 Log.e(TAG, "No Cover Photo for: " + name);
		 }
	 }
	
	/**
	 * Makes an Async Batch Request using the Facebook Graph Api to retrieve the URL's of the cover photos of the user's albums
	 * @param session	- An active facebook session
	 * @param params	- Extra parameters to bundle to request
	 * @param first 	- Indicates whether to clear the download trackers
	 * @params cache    - The cache to store downloaded values
	 */
	public static void coverURLBatchRequest(	 
												Bundle params, 
												boolean first, 
												final String taskId, 
												final SparseArray<Albums> cache){
		
		final RequestBatch batch = new RequestBatch();
		
		for (int i = 0; i<cache.size(); i++){
			String id = cache.get(i).mAlbumCoverPhotoID;
			
			if (id != ""){
				final Request.Callback requester = new Request.Callback(){
					@Override
					public void onCompleted(Response response) {
						GraphUser user = response.getGraphObjectAs(GraphUser.class);
						JSONObject result;
						
						removeRequest(taskId);
						
						if (user != null){
							result = user.getInnerJSONObject();
							
							mCoverIndex = JSONBuild.buildCoverURL(result, batch.size(), cache, taskId, mCoverIndex);
							
							if (mCoverIndex == (batch.size())){
								mRequest.coverReady(taskId);
								mCoverIndex = 0;
								Log.e(TAG, " !!! Cover URL Ready !!!");
							}
						}
						
						// If Errors were encountered
						if (response.getError() != null){
							// TODO: Deal with network connection failure
						/*	response.getRequest().setCallback(new Request.Callback() {
								@Override
								public void onCompleted(Response response) {
									GraphUser user = response.getGraphObjectAs(GraphUser.class);
									JSONObject result;
									if (user != null){
										result = user.getInnerJSONObject();
										
										mCoverIndex = JSONBuild.buildCoverURL(result, batch.size(), cache, taskId, mCoverIndex);
										
										if (mCoverIndex == (batch.size())){
											mRequest.coverReady(taskId);
											Log.e(TAG, " !!! Cover URL Ready !!!");
										}
									}
								}
							});
									
							response.getRequest().executeAsync();

							Log.e(TAG, "Error: " + response.getError());
							Log.e(TAG, "Http URL: " + response.getConnection());
							FBINIT.COVER_TASK_DONE = true;
							FBINIT.COVER_TASK_SUCCESS = false; */
							
							if (mRequest != null) mRequest.onGraphError(GraphError.NO_COVER, taskId);
						}
					}
			 
				};
				
				Request request = new Request(Session.getActiveSession(), id , params, HttpMethod.GET, requester);
				batch.add(request);
			}
		    
			else{
				Log.e(TAG, "Cover " + i + " URL - " + cache.get(i).mAlbumName + " - not available ");
			}
		}
		
		if (!batch.isEmpty()){
			if (Global.getInstance().isConnection()){
				if (first){
					FBINIT.COVER_TASK_DONE = false;
					FBINIT.COVER_TASK_SUCCESS = false;
					FBINIT.COVER_COUNT = 0;
					
					Log.d(TAG, " !!! Cover Request Started !!! ");
				}
				
				batch.executeAsync();
			}
			
			else {
				saveRequest(null, batch, taskId);
			}
		}
		else {
			mRequest.onGraphFinish(taskId);
		}
		
		Log.e(TAG, "!!! Requesting # " + batch.size() + " Cover URLs !!!");
	}
	
	/**
	 * Makes an Async Request using the Facebook Graph Api to retrieve the details of all images in a given facebook album
	 * @param session		- An active facebook session
	 * @param graphPath		- Api path to retrieve the image details
	 * @param params		- Extra parameters to bundle to request
	 * @param size			- The number of images in a given album
	 * @param first			- Indicates whether we are calling this method for the first time
	 * @param cacheUpdate   - Indicates whether to update the image cache with new values
	 * @param tClear 		- Indicates whether to clear the download trackers
	 * @param newRequest 	- A Facebook Request to grab the next page of results
	 * @param taskId    	- Unique task id supplied by the calling class
	 * @param cache    		- The cache to store downloaded values
	 */
	public static void imageRequest( 
										final String graphPath, 
										final Bundle params, 
										final int size, 
										final boolean first, 
										final boolean cacheUpdate, 
										final boolean tClear, 
										Request newRequest, 
										final String taskId,
										final SparseArray<Images> cache){
		
		Log.e(TAG, "first: " + first + ", cacheUpdate: " + cacheUpdate + ", tClear: " + tClear);
		
		if (first) mImageIndex = 0;
		
		if (cacheUpdate && first) FBINIT.IMAGES_READY = false;
		
		final Request.Callback requester = new Request.Callback(){
			 @Override
			 public void onCompleted(Response response) {
				 GraphUser user = response.getGraphObjectAs(GraphUser.class);
				 JSONObject result;
				 
				 removeRequest(taskId);
				 
				 if (user != null){
					 result = user.getInnerJSONObject();
					 mImageIndex = JSONBuild.buildImages(result, size, mImageIndex, cache);
					 
					 // update main cache (only occurs when downloading for the first time)
					 if (cacheUpdate){
						 synchronized(FBINIT.sImagesArray){
							 synchronized (FBINIT.sImagesArray_NEW){
								 FBINIT.sImagesArray = FBINIT.sImagesArray_NEW.clone();
								 Log.e(TAG, "!!! Image details cache updated !!!");
							 }
						 }
					 }
					 
					 // TODO: Decide whether to wake lock on available or task finished
					 synchronized(FBINIT.sImagesArray_LOCK){
						 FBINIT.sImagesArray_LOCK.notifyAll();
						 FBINIT.IMAGES_READY = true;
					 }
					 
					 mRequest.onGraphProgress(cacheUpdate, taskId);
					 
					 Request nextResult = response.getRequestForPagedResults(Response.PagingDirection.NEXT);
					 if (nextResult != null){
						 
						 if (size != -1){
							 if (mImageIndex < size){
								 Log.d(TAG, "!!! Fetching next page of images !!!");
								 imageRequest(	null, 
										 		null, 
										 		size, 
										 		false, 			// We are not calling the function for the first time (first)
										 		cacheUpdate, 	// Pass on the decision to update cache to the next iteration
										 		tClear, 		// Pass on the decision to clear trackers to the next iteration
										 		nextResult, 
										 		taskId, 
										 		cache); 
							 }
						 }
						 else{
							 Log.d(TAG, "!!! Fetching next page of images !!!");
							 imageRequest(	null, 
									 		null, 
									 		size, 
									 		false, 				// We are not calling the function for the first time (first)
									 		cacheUpdate, 		// Pass on the decision to update cache to the next iteration
									 		tClear, 			// Pass on the decision to clear trackers to the next iteration
									 		nextResult, 
									 		taskId, 
									 		cache);
						 }
					 } 
					 
					 else{
						 Log.e(TAG, "WE ARE DONE");
						 imageAvailable(true, tClear, taskId);
						
						 // Update total number of tagged photos or uploaded photos
						 if (FBINIT.sImagesArray_LASTUSED == -2){
							 Global.getInstance().modPref(Util.FB_NUMBER_OF_TAGGED, mImageIndex);
						 }
						 
						 else if (FBINIT.sImagesArray_LASTUSED == -3){
							 Global.getInstance().modPref(Util.FB_NUMBER_OF_UPLOADED, mImageIndex);
						 }
					 }
				 }
				 
				 if (response.getError() != null){
					 
					 if (mRequest != null) mRequest.onGraphError(GraphError.NO_IMAGE, taskId);
				 }
			 }
			 
		 };
		    
		    Request request = (newRequest != null) ? newRequest : new Request(Session.getActiveSession(), graphPath, params, HttpMethod.GET, requester);
		    if (newRequest != null) request.setCallback(requester);
		    
		    if (Global.getInstance().isConnection()){
		    	
		    	if (tClear && first){
					FBINIT.IMAGES_TASK_DONE = false;
					FBINIT.IMAGES_TASK_SUCCESS = false;
					
					Log.e(TAG, "!!! Image task trackers Cleared !!!");
					Log.d(TAG, "- !!! Image Request Started !!!");
		    	}
		    	
		    	mRequest.onGraphStart(taskId);
		    	request.executeAsync();
		    }
		    else
		    	saveRequest(request, null, taskId);
	}	
	
	/**
	 * Makes an Async Request using the Facebook Graph Api to retrieve a user's available facebook albums
	 * @param session	- An active facebook session
	 * @param first - Indicates we are calling this method for the first time
	 * @param cacheUpdate - Indicates we are allowed to update the cache with new values
	 * @param tClear 	- Indicates whether to clear the download trackers
	 * @param newRequest - A Facebook Request to retrieve the next page of results
	 * @param taskId - A unique task ID supplied by the calling class
	 * @param index - The starting index position for storing data in cache
	 */
	public static void albumRequest( 
										final boolean first, 
										final boolean cacheUpdate, 
										final boolean tClear, 
										Request newRequest, 
										final String taskId, 
										final SparseArray<Albums> cache, 
										final int index){
		
		final Request.Callback wrapper = new Request.Callback(){
			
			 @Override
			 public void onCompleted(Response response) {
				 GraphUser user = response.getGraphObjectAs(GraphUser.class);
				 
				 // remove request from map if present
				 removeRequest(taskId);
				 
				 // save user id
				// if (user.getId() != null) Global.getInstance().modPref(Util.FB_USER_ID, user.getId());
				 
				 if (user != null){
					 JSONObject result = user.getInnerJSONObject();
					 int lastIndex = JSONBuild.buildAlbums(result, index, cache, taskId);
					 
					 // TODO: Decide whether to wake lock on available or task finished
					 if (cacheUpdate && cache.equals(FBINIT.sAlbumsArray_NEW)){
						 synchronized(FBINIT.sAlbumsArray_LOCK){
							 FBINIT.sAlbumsArray_LOCK.notifyAll();
							 FBINIT.ALBUMS_READY = true;
						 }
					 
						 // update main cache (only occurs when downloading for the first time)
						 synchronized(FBINIT.sAlbumsArray){
							 synchronized (FBINIT.sAlbumsArray_NEW){
								 FBINIT.sAlbumsArray = FBINIT.sAlbumsArray_NEW.clone();
								 Log.e(TAG, "!!! Albums details cache updated !!!");
							 }
						 }
					 } 
					 
					 mRequest.onGraphProgress(cacheUpdate, taskId);
					 
					 Request nextResult = response.getRequestForPagedResults(Response.PagingDirection.NEXT);
					 if (nextResult != null){
						 Log.e(TAG, "!!! Grab Next Page of Albums !!!");
						 
						 switch(taskId){
						 	case AlbumsAccess.FETCH_ALBUM:
						 		albumRequest(false, cacheUpdate, tClear, nextResult, taskId, AlbumsAccess.mCache, lastIndex);
						 		break;
						 	
						 	default:
						 		albumRequest(false, cacheUpdate, tClear, nextResult, taskId, FBINIT.sAlbumsArray_NEW, lastIndex);
						 		break;
						 }
						 
					 }
					 
					 else{
						 FBINIT.ALBUMS_UPDATED = true;
						 
						 albumAvailable(cacheUpdate, tClear, taskId);
						 
						 // request for the cover url of all the albums
						 Bundle params = new Bundle();
						 params.putString("fields", "source");
						 
						 switch(taskId){
						 	case AlbumsAccess.FETCH_ALBUM:
						 		coverURLBatchRequest(params, true, taskId, AlbumsAccess.mCache);
						 		break;
						 	default:
						 		coverURLBatchRequest(params, true, taskId, FBINIT.sAlbumsArray);
						 		break;
						 }
						 
					 }
						 
				 }
				 
				 if (response.getError() != null){
					// TODO: Deal with network connection failure
				/*	 Log.e(TAG, "albumRequest Error: " + response.getError());
					 Log.e(TAG, "albumRequest Http URL: " + response.getConnection());
					 
					 FBINIT.ALBUMS_READY = false;
					 FBINIT.ALBUMS_TASK_DONE = true;
					 FBINIT.ALBUMS_TASK_SUCCESS = false;
					 FBINIT.ALBUMS_TASK_STARTED = false; */
					 
					 if (mRequest != null) mRequest.onGraphError(GraphError.NO_ALBUM, taskId);
				 }
			 }
			 
		 }; 
		    
		    Request request = (newRequest != null) ? newRequest : new Request(Session.getActiveSession(), "me/albums", null, HttpMethod.GET, wrapper);
		    if (newRequest != null) request.setCallback(wrapper);
		    
		    if (Global.getInstance().isConnection()){
		    	
		    	if (tClear && first){
		    		FBINIT.ALBUMS_READY = false;
		    		FBINIT.ALBUMS_TASK_SUCCESS = false;
		    		FBINIT.ALBUMS_TASK_DONE = false;
		    		FBINIT.ALBUMS_TASK_STARTED = true;
		    		FBINIT.ALBUMS_UPDATED = false;
		    		
		    		Log.e(TAG, "!!! Album task trackers Cleared !!!");
		    		Log.e(TAG, "!!! Albums task Started !!!");
		    	}
		    	
		    	mRequest.onGraphStart(taskId);
		    	request.executeAsync();
		    }
		    
		    else {
		    	saveRequest(request, null, taskId);
		    }
	}
	
	/**
	 * Makes an Async Request using the Facebook Open Graph Api to retrieve the user's profile picture
	 * @param session 	- An active facebook session
	 * @param graphPath	- Api path to retrieve user's profile picture
	 * @param params	- Extra parameters to bundle to request
	 */
	public static void UserPhotoRequest(	final Session session, 
											final String graphPath, 
											Bundle params, 
											final String taskId){
		 
		Request.GraphUserCallback myCallback = new Request.GraphUserCallback(){
			 @Override
			 public void onCompleted(GraphUser user, Response response) {
				 removeRequest(taskId);
				 
				 if (user != null){
						 try {
							 JSONObject result = user.getInnerJSONObject();
							 if (result.has(FBINIT.PICTURE)){
								 JSONObject picture = result.getJSONObject(FBINIT.PICTURE);
								 if (picture.has(FBINIT.DATA)){
									 JSONObject data = picture.getJSONObject(FBINIT.DATA);
									 String URL = data.optString(FBINIT.URL);
									 if (!URL.isEmpty()){
										 Global.getInstance().downloadSaveToInternal(URL, Util.FB_DRAWER_SMALL_IMAGE);
										 Log.d(TAG, "- user account image updated -");
									 }
								 }
							 }
						} catch (JSONException e) {
							e.printStackTrace();
						}
				 }
				 
				 if (response.getError() != null){
					 // TODO: Deal with network connection failure
					 Log.e(TAG, "Error: " + response.getError());
					 Log.e(TAG, "Http URL: " + response.getConnection());
				 }
			 }
			 
		 };
		 
		    final Request.GraphUserCallback finalCallback = myCallback;
		    Request.Callback wrapper = new Request.Callback() {
		        @Override
		        public void onCompleted(Response response) {
		            finalCallback.onCompleted(response.getGraphObjectAs(GraphUser.class), response);
		        }
		    };
		    
		    Request request = new Request(session, graphPath, params, HttpMethod.GET, wrapper);
		    
		    if (Global.getInstance().isConnection())
		    	request.executeAsync();
		    else
		    	saveRequest(request, null, taskId);
	} 
	
	/**
	 * Get the first image in an album and save it using the provided filename
	 * @param session - An active facebook session
	 * @param fileName - name to store image with
	 * @param graphPath - FB APi Graph request
	 * @param params - additional parameters
	 * @param taskId - worker task id
	 */
	public static void albumFirstImageRequest(	Session session, 
												final String fileName, 
												final String graphPath, 
												final Bundle params, 
												final String taskId){
		
		final Request.Callback wrapper = new Request.Callback(){
			 @Override
			 public void onCompleted(Response response) {
				 GraphUser user = response.getGraphObjectAs(GraphUser.class);
				 if (user != null){
						 try {
							 JSONObject result = user.getInnerJSONObject();
							 if (result.has(FBINIT.DATA)){								
								 JSONArray data = result.getJSONArray(FBINIT.DATA);							// get "data": [
								 JSONObject mObj_1 = data.getJSONObject(0);									// get {}
								 if (mObj_1.has(FBINIT.SOURCE)){
									 String URL = mObj_1.optString(FBINIT.SOURCE);							// get "source":
									 if (!URL.isEmpty()){
										 Global.getInstance().downloadSaveToInternal(URL, fileName);
										 Log.d(TAG, "- image updated in internal -");
									 }
								 }
							 }
						} catch (JSONException e) {
							e.printStackTrace();
						}
				 }
				 
				 if (response.getError() != null){
					 // TODO: Deal with network connection failure
					 Log.e(TAG, "Error: " + response.getError());
					 Log.e(TAG, "Http URL: " + response.getConnection());
				 }
			 }
			 
		 };
		    
		 Request request = new Request(session, graphPath, params, HttpMethod.GET, wrapper);
		 
		 if (Global.getInstance().isConnection())
			 request.executeAsync();
		 else
			 saveRequest(request, null, taskId);
	}
	
	/**
	 * Makes an async request for the cover photo of a given album
	 * @param session	- An active facebook session
	 * @param id		- The unique id of the album
	 * @param fileName	- The name of the image file to be stored
	 */
	public static void userCoverURLRequest(Session session, String id, final String fileName, final String taskId){
		Bundle params = new Bundle();
		params.putString("fields", "source");
		
		Request.GraphUserCallback myCallback = new Request.GraphUserCallback(){
			 @Override
			 public void onCompleted(GraphUser user, Response response) {
				 if (user != null){
					 // remove the request if saved
					 removeRequest(taskId);
					 
					 JSONObject result = user.getInnerJSONObject();
					 
					 if (result.has(FBINIT.SOURCE)){
						 String URL = result.optString(FBINIT.SOURCE);
						 if (!URL.isEmpty()){
							 Global.getInstance().downloadSaveToInternal(URL, fileName);
							 //	 Global.getInstance().downloadSaveToExternal(URL, Util.FACEBOOK_PATH, fileName+".jpg");
						 }
					 }
				 }
				 
				 if (response.getError() != null){
					 // TODO: Deal with network connection failure
					 Log.e(TAG, "userCoverURLRequest Error: " + response.getError());
					 Log.e(TAG, "userCoverURLRequest Http URL: " + response.getConnection());
					 
					 saveRequest(response.getRequest(), null, taskId); // save the request to be repeated later on
				 }
			 }
			 
		 };
		 
		    final Request.GraphUserCallback finalCallback = myCallback;
		    Request.Callback wrapper = new Request.Callback() {
		        @Override
		        public void onCompleted(Response response) {
		            finalCallback.onCompleted(response.getGraphObjectAs(GraphUser.class), response);
		        }
		    };
		    
		    Request request = new Request(session, id, params, HttpMethod.GET, wrapper);
		    
		    if (Global.getInstance().isConnection())
		    	request.executeAsync();
		    else
		    	saveRequest(request, null, taskId);
	}
	
	
	/** makes an Async Request to retrieve the facebook username */
	public static void makeMeRequest(final Session session, final String taskId) {
		
	    Request request = Request.newMeRequest(	Session.getActiveSession(), 
	    		new Request.GraphUserCallback() {
	    	
	        @Override
	        public void onCompleted(GraphUser user, Response response) {
	        	// remove request from map if present
	            removeRequest(taskId);
	            
	            // If the response is successful
	            if (session == Session.getActiveSession()) {
	                if (user != null) {
	                	
	                	Global.getInstance().modPref(Util.FB_USER_ID, user.getId());
	                    
	                    // set the access permissions
	                    FBINIT.getInstance().setPERMISSIONS(session.getPermissions());
	        	    	Log.e("Permissions", "Permission: " + session.getPermissions());
	        	    	
	                }
	            }
	            
	            if (response.getError() != null) {
	            	saveRequest(response.getRequest(), null, taskId);
	            }
	            
	        }
	    });
	    
	    if (Global.getInstance().isConnection())
	    	request.executeAsync();
	    
	    else {
	    	saveRequest(request, null, taskId);
	    }
	} 
	
	/**
	 * Add request to be launched once connection available to stack
	 * @param req - facebook request
	 * @param bReq - batch request
	 * @param id   - unique task id
	 */
	public static void saveRequest(Request req, RequestBatch bReq, String id){
		RequestX rX = new RequestX();
		rX.mRequest = req;
		rX.mBatchRequest = bReq;
		rX.mTag = id;
		
		synchronized(FBINIT.sReqLock){
			if (!FBINIT.sRequests.containsKey(id)){
				FBINIT.sRequests.put(id, rX);
				FBINIT.sReqStack.push(id);
				Log.d(TAG, "Request: " + id + " added to map");
			}
		}
	}
	
	
	/**
	 * Remove a request from the map
	 * @param id - unique request id
	 */
	public static void removeRequest(String id){
		synchronized(FBINIT.sReqLock){
			if (FBINIT.sRequests.containsKey(id)){
				FBINIT.sRequests.remove(id);
				Log.d(TAG, "Request: " + id + " removed from map");
			}
		}
	}
	
	/**
	 * Check if a request is in map
	 * @param id - unique request id
	 * @return 
	 */
	public static boolean inMap(String id){
		return FBINIT.sRequests.containsKey(id);
	}
}
