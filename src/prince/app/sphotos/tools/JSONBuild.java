package prince.app.sphotos.tools;

import org.apache.http.ParseException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.facebook.Session;

import prince.app.sphotos.Request.GraphRequest;
import android.util.Log;
import android.util.SparseArray;

/**
 * A Public class that extracts data from JSONObjects and stores them in arrays
 * @author Princewill Okorie
 *
 */
public class JSONBuild {
	public static final String TAG = JSONBuild.class.getSimpleName();
	
	public static String TASK = "cover_";
	
	private static JSONBuildListener mBuild;
	
	public static void initBuildListener(JSONBuildListener context){
		mBuild = context;
	}
	
	public interface JSONBuildListener{
		public void onAlbumsReady(String taskId);
		public void onCoverReady(String taskId);
	}

	/**
	 * Places each album and its details into an array cell {@link FBINIT.ALL_FACEBOOK_ALBUM}
	 * @param graph	- A JSONObject representation of the user's albums
	 * @return		- The last stored position of the array
	 */
	public static int buildAlbums(JSONObject graph, int lastIndex, SparseArray<Albums> mCache, String taskId){
		
		try {
			JSONArray data = null;
			
			if (graph.has(FBINIT.DATA)){
				
				// obtain data array []
				data = graph.getJSONArray(FBINIT.DATA);
				
				// build each album object {}
				for (int i=0; i<data.length(); i++){
					Albums User = new Albums();
					JSONObject eachAlbum = data.getJSONObject(i);				
					User.mAlbumID = (eachAlbum.optString(FBINIT.ID));					// 	Set ID
					
					JSONObject from = eachAlbum.getJSONObject(FBINIT.FROM);		
					User.mAlbumOwnerID = (from.getString(FBINIT.ID));						// 	Set FROM ID
					User.mAlbumOwnerName = (from.getString(FBINIT.NAME));					//	Set FROM NAME
					
					User.mAlbumName = (eachAlbum.optString(FBINIT.NAME));					//	Set NAME
					User.mAlbumCoverPhotoID = (eachAlbum.optString(FBINIT.COVERPHOTO));		//	Set COVERPHOTO
					User.mAlbumPrivacy = (eachAlbum.optString(FBINIT.PRIVACY));				//	Set PRIVACY
					User.mAlbumSize = (eachAlbum.optInt(FBINIT.COUNT));					//	Set	COUNT
					User.mAlbumType = (eachAlbum.optString(FBINIT.TYPE));				//	Set TYPE
					User.mAlbumCT = (eachAlbum.optString(FBINIT.CTIME));			//	Set Created Time
					User.mAlbumUT = (eachAlbum.optString(FBINIT.UTIME)); 			//	Set Updated Time
					User.mAlbumUpload = (eachAlbum.optBoolean(FBINIT.CAN_UPLOAD));
			
					Log.i(TAG, "Album " + lastIndex + " - " + User.mAlbumName + " - SAVED !!!");
							 
					synchronized (mCache) {
						mCache.append(lastIndex, User);
						lastIndex += 1;
					}
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		
		return lastIndex;
	}
	
	/**
	 * Places the Http address of each album's cover photo into array cells {@link FBINIT.ALBUM_COVER_URL}
	 * @param result	- The JSONObject holding the URL
	 * @param list		- A list of indices with no cover photo id
	 * @param maxSize		- The maximum number of available cover photo image id
	 * @return			- True if each URL is successfully stored in the array
	 */
	public static int buildCoverURL(JSONObject result, int maxSize, SparseArray<Albums>mCache, String taskId, int index){
		if (result != null){
			if (result.has(FBINIT.SOURCE)){
				synchronized (mCache) {
						
					String address = result.optString(FBINIT.SOURCE);
					String imageId = result.optString(FBINIT.ID);
					String albumName = "";
						
					// add the cover url to the array
					if (!address.isEmpty() && !imageId.isEmpty()){
						for (int i=0; i<mCache.size(); i++){
							String mCoverId = mCache.get(i).mAlbumCoverPhotoID;
							if (mCoverId.equalsIgnoreCase(imageId)){
								mCache.get(i).mAlbumCoverURL = address;
								albumName = mCache.get(i).mAlbumName;
								Log.i(TAG, "Cover " + i + " URL for - " + albumName + " - saved ");
							}
						}
					}
				}
				
				return ++index;
			}
		}
		
		return index;
	}
	
	/**
	 * Places each photo of a particular album and its details into an array cell {@link FBINIT.IMAGE_DETAILS}
	 * @param result	- The JSONObject holding the photos of an album and its details
	 * @param maxSize		- The number of photos available in an album
	 * @return			- The http address of the next page of photos if available and within album size
	 */
	public static int buildImages(JSONObject result, int maxSize, int lastIndex, SparseArray<Images>mCache){
		
		try{
			if (result != null){
				if (result.has(FBINIT.DATA)){
					JSONArray aAlbum = result.getJSONArray(FBINIT.DATA);
					for (int i=0; i<aAlbum.length(); i++){
						Images User = new Images();
						
						JSONObject eachAlbum = aAlbum.getJSONObject(i);				
						User.mImageID = (eachAlbum.optString(FBINIT.ID));					// 	Set ID
						
						JSONObject from = eachAlbum.getJSONObject(FBINIT.FROM);		
						User.mImageOwnerID = (from.getString(FBINIT.ID));							// 	Set FROM ID
						User.mImageOwnerName = (from.getString(FBINIT.NAME));						//	Set FROM NAME
						
						User.mImageThumbURL =(eachAlbum.optString(FBINIT.PICTURE));				//	Set Thumbnail
						User.mImageHeight = (eachAlbum.optInt(FBINIT.HEIGHT));					//	Set HEIGHT
						User.mImageWidth = (eachAlbum.optInt(FBINIT.WIDTH));						//	Set WIDTH
						User.mImageCT = (eachAlbum.optString(FBINIT.CTIME));					//	Set	COUNT
						User.mImageUT = (eachAlbum.optString(FBINIT.UTIME));					//	Set TYPE
						
					/*	JSONArray image = eachAlbum.getJSONArray(FBINIT.IMAGE);
						JSONObject nImage = image.getJSONObject(0);
						User.mImageURL = (nImage.optString(FBINIT.SOURCE)); */
						
						User.mImageURL =(eachAlbum.optString(FBINIT.SOURCE));				
		
						synchronized (mCache) {
							mCache.append(lastIndex, User);
							lastIndex += 1;
						}
						
						Log.i(TAG, "Details of image " + (lastIndex-1) + " with id: " + eachAlbum.optString(FBINIT.ID) +"  downloaded");
					 }
				}
				else{
					Log.e(TAG, "Image details not available");
				}
			}
		}catch (JSONException e){
			// TODO: Deal with JSON build error
			FBINIT.IMAGES_TASK_SUCCESS = false;
			Log.e(TAG, "JSONException error");
			e.printStackTrace();
		}catch (ParseException e) {
			// TODO: Deal with JSON build error
			FBINIT.IMAGES_TASK_SUCCESS = false;
			Log.e(TAG, "ParseException error");
			e.printStackTrace();
		}
		
		return lastIndex;
	}
}
