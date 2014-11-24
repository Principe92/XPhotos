package prince.app.sphotos.tools;

import java.util.Timer;
import java.util.TimerTask;

import prince.app.sphotos.Request.GraphRequest;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;

import com.facebook.Session;

/**
 * A Public function that handles requests that uses Timers
 * @author Princewill Okorie
 *
 */
public class TimerX {
	private static final String TAG = TimerX.class.getSimpleName();
	
	private static boolean sStartedGetCoverRequest;
	private static Timer sGetCoverURL;
	
	private static boolean sStartedUserPictureRequest;
	private static Timer sGetUserPicture;
	
	private static Timer sDownloadUserAlbum;
	private static boolean sStartedAlbumDownload;
	
	private static Timer sDiskUpdate;
	private static boolean sDiskUpdateDone;
	
	private static Timer sUserIdTimer;
	private static boolean sRequestStarted;
	
	
	/**
	 * A Timer that monitors network availability, availability of user's albums and starts {@link coverURLBatchRequest}
	 * @param context - The activity hosting the timer
	 * @param first   - true if we are downloading cover URL afresh
	 */
/*	public static void getCoverURL(final Activity context, final boolean first){
		sStartedGetCoverRequest = false;
		sGetCoverURL = new Timer();
		sGetCoverURL.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				context.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						synchronized(Global.getLock()){
							Session session = Session.getActiveSession();
							
							if (Global.getInstance().isConnection() && FBINIT.sAlbumsArray.size() > 0 && !sStartedGetCoverRequest && session != null && session.isOpened()){
								Log.d(TAG, "- started coverRequest -");
									
								sStartedGetCoverRequest = true;
								Bundle params = new Bundle();
								params.putString("fields", "source");
								GraphRequest.coverURLBatchRequest(session, params, first);
								sGetCoverURL.cancel();
							}
							
							else if (!FBINIT.ALBUMS_TASK_STARTED && !FBINIT.ALBUMS_TASK_DONE){
							//	getUserAlbums(context, true);
							}
						}
					}
				});
			}
		}, 0, Global.WAIT_TIME);
	} */
	
	
	/**
	 * A Timer that monitors network availability in order to request User Account Picture by calling {@link #UserPhotoRequest()}
	 * @param context	- The activity hosting the timer
	 */
/*	public static void getUserPicture(final Activity context){
		sStartedUserPictureRequest = false;
		sGetUserPicture = new Timer();
		sGetUserPicture.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				context.runOnUiThread(new Runnable(){
					@Override
					public void run(){
						synchronized(Global.getLock()){
							Session session = Session.getActiveSession();
							
							if (Global.getInstance().isConnection() && !sStartedUserPictureRequest && session != null && session.isOpened()){
								Log.d(TAG, "- started userPictureRequest -");
									
								sStartedUserPictureRequest = true;
								Bundle params = new Bundle();
								params.putString("fields", "picture");
								GraphRequest.UserPhotoRequest(session, "/me", params);
								sGetUserPicture.cancel();
							}
						}
					}
				});
			}
		}, 0, Global.WAIT_TIME);
	} */
	
	/**
	 * A Timer that waits for network availability in order to fetch user albums and their cover photos by calling {@link #albumRequest}
	 * @param context - The activity hosting the Timer
	 * @param first   - true if we are downloading afresh the user's albums
	 */
/*	public static void getUserAlbums(final Activity context, final boolean first){
		sStartedAlbumDownload = false;
		sDownloadUserAlbum = new Timer();
		sDownloadUserAlbum.scheduleAtFixedRate(new TimerTask(){
			public void run(){
				context.runOnUiThread(new Runnable(){
					@Override
					public void run() {
						synchronized(Global.getLock()){
							Session session = Session.getActiveSession();
							
							if (Global.getInstance().isConnection() && !sStartedAlbumDownload && session != null && session.isOpened()){
								Log.d(TAG, "- started albumRequest -");
									
								GraphRequest.albumRequest(session, "me/albums", first, null);
								sStartedAlbumDownload = true;
								sDownloadUserAlbum.cancel();
							}
						}
					}
				});
			};
		}, 0, Global.WAIT_TIME);
	}	*/
}
