package prince.app.sphotos.tools;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

import prince.app.sphotos.R;
import prince.app.sphotos.Request.GraphRequest;
import prince.app.sphotos.bgtask.UpdateService;
import prince.app.sphotos.database.AlbumsAccess;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.facebook.Session;

public class UpdateFiles {
	private static final String TAG = UpdateFiles.class.getSimpleName();
	
	public static final String TASK_ALBUM = "task_album";
	public static final String TASK_COVER = "task_cover";
	public static final String TASK_PROFILE = "task_profile";
	public static final String TASK_TAG = "task_tag";
	public static final String TASK_UPLOAD = "task_upload";
	public static final String TASK_DRAWER_BIG = "task_drawer_big_pic";
	public static final String TASK_DRAWER_SMALL = "task_drawer_small_pic";
	
	public static final String LAST_GRID_IMAGE_UPDATE = "grid_image_key";
	
	/**
	 * Updates the image for the album grid on the principal menu
	 */
	public static void uAlbumsGridImage(){
		Random newRandom = new Random();
		int num = Global.getInstance().getIntPref(Util.FB_NUMBER_OF_ALBUMS);
		final int ran = num > 0 ? newRandom.nextInt(num - 2) + 2 : 3;
		String id = FBINIT.sAlbumsArray.get(ran).mAlbumCoverPhotoID;
		
		if (id != null && !id.isEmpty()){
			GraphRequest.userCoverURLRequest(Session.getActiveSession(), id, Util.FB_ALBUM_GRID_IMAGE, TASK_ALBUM);
			Log.d(TAG, "- album grid image updated -");
		}
	}
	
	public static void uCoverGridImage(){
		String mImageID = FBINIT.getCoverIDByName("Cover Photos");
		if (mImageID != null && !mImageID.isEmpty()){
			GraphRequest.userCoverURLRequest(Session.getActiveSession(), mImageID, Util.FB_COVER_GRID_IMAGE, TASK_COVER);
			Log.d(TAG, "- cover grid image updated -");
		}
	}
	
	public static void uProfileGridImage(){
		String mImageID = FBINIT.getCoverIDByName("Profile Pictures");
		if (mImageID != null && !mImageID.isEmpty()){
			GraphRequest.userCoverURLRequest(Session.getActiveSession(), mImageID, Util.FB_PROFILE_GRID_IMAGE, TASK_PROFILE);
			Log.d(TAG, "- profile grid image updated -");
		}
	}
	
	public static void uDrawerBigPhoto() {
		String id = FBINIT.getAlbumID("Cover Photos");
		if (id != null && !id.isEmpty()){
			String graphPath = id + "/photos";
			Bundle params = new Bundle();
			params.putString("fields", "source");
			params.putLong("limit", 1);
			GraphRequest.albumFirstImageRequest(Session.getActiveSession(), Util.FB_DRAWER_BIG_IMAGE, graphPath, params, TASK_DRAWER_BIG);
			Log.d(TAG, "- drawer big image updated -");
		}
		
	}
	
	public static void uDrawerSmallPhoto(){
		// obtain user account picture
    	Bundle params = new Bundle();
		params.putString("fields", "picture");
		GraphRequest.UserPhotoRequest(Session.getActiveSession(), "/me", params, TASK_DRAWER_SMALL);
	}
	
	public static void uTagUploaded(boolean tagged){
		String type;
		String graphPath = "me/photos";
		Bundle params = new Bundle();
		params.putString("fields", "source");
		params.putLong("limit", 1);
		
		if (tagged){
			type = "tagged";
			params.putString("type", type);
		
			GraphRequest.albumFirstImageRequest(	Session.getActiveSession(), 
													Util.FB_TAG_GRID_IMAGE, 
													graphPath, 
													params, 
													TASK_TAG);
			Log.d(TAG, "- tagged grid updated -");
		}
		
		else{
			type = "uploaded";
			params.putString("type", type);
			
			GraphRequest.albumFirstImageRequest(	Session.getActiveSession(), 
													Util.FB_UPLOADED_GRID_IMAGE, 
													graphPath, 
													params, 
													TASK_UPLOAD);
		
			Log.d(TAG, "- uploaded grid updated -");
		}
	}
	
	public static void uAllGrid(){
		
		if (!canUpdate(Global.getInstance().getStrPref(LAST_GRID_IMAGE_UPDATE))){
			Log.e(TAG, "!!! Grid Images up to date !!!");
			return;
		}
		
		// update album grid photo
		uAlbumsGridImage();
		
		// update cover grid photo
		uCoverGridImage();
		
		// update drawer big photo
		uDrawerBigPhoto();
		
		// update drawer small photo
		uDrawerSmallPhoto();
		
		// update profile grid photo
		uProfileGridImage();
		
		// update tagged grid photo
		uTagUploaded(true);
		
		// update uploaded grid photo
		uTagUploaded(false);
		
		Global.getInstance().modPref(LAST_GRID_IMAGE_UPDATE, AlbumsAccess.getTime());
	}
	
	public static void updateCount(Context ct){ 
		Global.getInstance().modPref(Util.FB_NUMBER_OF_COVER, FBINIT.getAlbumSizeByName("Cover Photos", FBINIT.sAlbumsArray));
		Global.getInstance().modPref(Util.FB_NUMBER_OF_PROFILE, FBINIT.getAlbumSizeByName("Profile Pictures", FBINIT.sAlbumsArray));
		Global.getInstance().modPref(Util.FB_NUMBER_OF_ALBUMS, FBINIT.sAlbumsArray.size());
		
		String mSpecial = PreferenceManager	.getDefaultSharedPreferences(ct.getApplicationContext())
				.getString(ct.getResources()
				.getString(R.string.pref_favFBAlbum_key), "");  	// get preferred first screen
    	
    		int pos = FBINIT.getIndexByID(mSpecial);
    		if (pos != -1 && !FBINIT.isAlbumEmpty()){
    			//store favorite album name and count in sharedPreference
    			Global.getInstance().modPref(Util.FB_NUMBER_OF_FAVORITE, FBINIT.sAlbumsArray.get(pos).mAlbumSize);
    			Global.getInstance().modPref(Util.FB_FAVORITE_NAME, FBINIT.sAlbumsArray.get(pos).mAlbumName);
    		}
	} 
	
	public static void updateDbAlbums(Context ct){
		if (!canUpdate(Global.getInstance().getStrPref(AlbumsAccess.LAST_UPDATED))){
			Log.e(TAG, "!!! Albums up to date !!!");
			return;
		}
		
		// update the albums table in db
		Intent it = new Intent(ct, UpdateService.class);
		it.putExtra(UpdateService.INTENT_KEY, UpdateService.UPDATE_ALBUMS);
		it.putExtra(UpdateService.EXTRAS, true);
		ct.startService(it);
		Log.i(TAG, "Fetching albums to update db !!!");
	}
	
	public static boolean canUpdate(String strLast){
		String strToday = AlbumsAccess.getTime();
		Log.i(TAG, "Last album update: " + strLast);
		SimpleDateFormat timeStamp = new SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());
		
		if (!strLast.isEmpty()){
			try {
				Date dtToday = timeStamp.parse(strToday);
				Date dtLast = timeStamp.parse(strLast);
				
				
				if (!dtToday.after(dtLast)){
					return false;
				}
				
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		
		return true;
	}
	

}
