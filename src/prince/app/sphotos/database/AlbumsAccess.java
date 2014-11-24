package prince.app.sphotos.database;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.EventListener;

import prince.app.sphotos.Request.GraphRequest;
import prince.app.sphotos.Request.GraphRequest.GraphError;
import prince.app.sphotos.Request.GraphRequest.RequestListener;
import prince.app.sphotos.database.FBDbContract.AlbumHelper;
import prince.app.sphotos.tools.Albums;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;
import android.util.SparseArray;

public class AlbumsAccess extends DbService{
	private static final String TAG = AlbumsAccess.class.getSimpleName();
	private final static SQLiteDatabase dbWr = Global.getInstance().getFBDb().getWritableDatabase();
	private final SQLiteDatabase dbRd = Global.getInstance().getFBDb().getReadableDatabase();
	
	public static final String FETCH_ALBUM = "album_db_update";
	public static final String LAST_UPDATED = "db_time_updated";
	
	public static SparseArray<Albums> mCache = new SparseArray<Albums>();
	private static AlbumDbListener DbListener;

/*	public AlbumsAccess() {
		super(TAG);
	} */
	
	public interface AlbumDbListener extends EventListener{
		void onDbStart();
		void onDbStop(boolean success);
	}
	
	public static void setListener(AlbumDbListener ar){
		DbListener = ar;
	}

	@Override
	protected void readDb(String table) {
		Cursor cr = getCursor(dbRd, AlbumHelper.FB_ALBUM_TABLE, CursorProj.ALBUM_DB_PROJECTION, null, null, null, null, null);
				
		if (cr != null && cr.moveToFirst()){
			int index;
			do {
				Albums album = new Albums();
				index = cr.getInt(cr.getColumnIndexOrThrow(AlbumHelper._ID));
				album.mAlbumID 				= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_ID));
				album.mAlbumOwnerID 		= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_OWNER_ID));
				album.mAlbumOwnerName 		= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_OWNER));
				album.mAlbumName 			= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_NAME));
				album.mAlbumCoverPhotoID	= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_COVER_PHOTO_ID));
				album.mAlbumPrivacy 		= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_PRIVACY));
				album.mAlbumSize			= cr.getInt(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_SIZE));
				album.mAlbumType			= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_TYPE));
				album.mAlbumCT				= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_CREATED_TIME));
				album.mAlbumUT				= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_UPDATED_TIME));
				album.mAlbumPath			= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_PATH));
				album.mAlbumCoverURL		= cr.getString(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_COVER_URL));
				album.mAlbumUpload			= cr.getInt(cr.getColumnIndexOrThrow(AlbumHelper.ALBUM_UPLOADABLE)) == 0 ? true : false;
						
				FBINIT.sAlbumsArray.append(index, album);
				Log.e(TAG, "album " + index + ": " + FBINIT.sAlbumsArray.get(index).mAlbumName + " 				added to ARRAY");
			} 
			while (cr.moveToNext());
					
			cr.close();
			
			// Announce the availability of albums
			if (DbListener != null)  DbListener.onDbStop(true);
		}
		

		else { 
			Log.e(TAG, "!!! albums not available in database !!!");
			if (cr != null) cr.close();
			if (DbListener != null)  DbListener.onDbStop(false);
		}
		
	}

	@Override
	protected void updateDb(String table, boolean mCache) {
		if (mCache) mCacheUpdateDb();
		
		else mAlbumsUpdateDb(null);
	}
		
	private void mAlbumsUpdateDb(String table){
		// table created?
		if (!isTableCreated(dbWr, AlbumHelper.FB_ALBUM_TABLE)){ // no
			// create table
			createTable(dbWr, AlbumHelper.CREATE_ALBUM_TABLE);
			Log.i(TAG, "!!! creating table: " + AlbumHelper.FB_ALBUM_TABLE + " for first time !!!");
			
			// insert new values
			write(FBINIT.sAlbumsArray);
		}
		
		else { // yes
			if (FBINIT.sAlbumsArray.size() > 0){
				int i;
				for (i = 0; i< FBINIT.sAlbumsArray.size(); i++){
					
					String selection = AlbumHelper._ID + " LIKE ?";
					String[] selectionArgs = { String.valueOf(i) };
					long newRowId = dbWr.update(	AlbumHelper.FB_ALBUM_TABLE, 
													cvAlbum(i, true, FBINIT.sAlbumsArray.get(i)), 
													selection, 
													selectionArgs
													);
					
					if (newRowId == 0) dbWr.insert(	AlbumHelper.FB_ALBUM_TABLE, 
													null, 
													cvAlbum(i, true, FBINIT.sAlbumsArray.get(i))
													);
				}
				
				
				String time = getTime();
				Log.i(TAG, "#" + i + " rows updated in " + AlbumHelper.FB_ALBUM_TABLE + " at " + time);
				Global.getInstance().modPref(LAST_UPDATED, time);
				FBINIT.ALBUMS_UPDATED = false;
			}
		}
	}
	
	private static void write(SparseArray<Albums> mCache){
		
		if (mCache.size() > 0){
			for (int i=0; i< mCache.size(); i++){
				long newRowId = dbWr.insert(	AlbumHelper.FB_ALBUM_TABLE, 
												null, 
												cvAlbum(i, false, mCache.get(i))
												);
				
				Log.i(TAG, "db row " + i + " with id: " + newRowId + " created");
			}
		}
	}
	
	private static ContentValues cvAlbum(int index, boolean update, Albums album){
		ContentValues values = new ContentValues();
		
		if (!update) values.put(AlbumHelper._ID, index);
		values.put(AlbumHelper.ALBUM_ID, 			album.mAlbumID);
		values.put(AlbumHelper.ALBUM_OWNER_ID, 		album.mAlbumOwnerID);
		values.put(AlbumHelper.ALBUM_OWNER, 		album.mAlbumOwnerName);
		values.put(AlbumHelper.ALBUM_NAME, 			album.mAlbumName);
		values.put(AlbumHelper.ALBUM_COVER_PHOTO_ID,album.mAlbumCoverPhotoID);
		values.put(AlbumHelper.ALBUM_SIZE, 			album.mAlbumSize);
		values.put(AlbumHelper.ALBUM_TYPE, 			album.mAlbumType);
		values.put(AlbumHelper.ALBUM_CREATED_TIME, 	album.mAlbumCT);
		values.put(AlbumHelper.ALBUM_UPDATED_TIME, 	album.mAlbumUT);
		values.put(AlbumHelper.ALBUM_PRIVACY,	 	album.mAlbumPrivacy);
		values.put(AlbumHelper.ALBUM_PATH,			album.mAlbumPath);
		values.put(AlbumHelper.ALBUM_COVER_URL,	 	album.mAlbumCoverURL);
		values.put(AlbumHelper.ALBUM_UPLOADABLE, 	(album.mAlbumUpload ? 0:1)); // add 0 if true, else 1
		
		return values;
	}
	
	public static void mCacheUpdateDb(){
		
		new GraphRequest().initListener(new RequestListener(){

			@Override
			public void coverReady(String taskId) {
				
				if (taskId.equalsIgnoreCase(FETCH_ALBUM)){
				
					// table created?
					if (!isTableCreated(dbWr, AlbumHelper.FB_ALBUM_TABLE)){ // no
						// create table
						createTable(dbWr, AlbumHelper.CREATE_ALBUM_TABLE);
						Log.i(TAG, "!!! creating table: " + AlbumHelper.FB_ALBUM_TABLE + " for first time !!!");
						
						// insert new values
						write(mCache);
					}
					
					else { // yes
						if (mCache.size() > 0){
							int i;
							for (i = 0; i< mCache.size(); i++){
								
								String selection = AlbumHelper._ID + " LIKE ?";
								String[] selectionArgs = { String.valueOf(i) };
								long newRowId = dbWr.update(	AlbumHelper.FB_ALBUM_TABLE, 
																cvAlbum(i, true, mCache.get(i)), 
																selection, 
																selectionArgs
																);
								
								if (newRowId == 0) dbWr.insert(	AlbumHelper.FB_ALBUM_TABLE, 
																null, 
																cvAlbum(i, true, mCache.get(i))
																);
							}
							
							String time = getTime();
							Log.i(TAG, "#" + i + " rows updated in " + AlbumHelper.FB_ALBUM_TABLE + " at " + time);
							Global.getInstance().modPref(LAST_UPDATED, time);
							mCache.clear();
						}
					}
				}
				
			}

			@Override
			public void onGraphProgress(boolean refresh, String taskId) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGraphFinish(String taskId) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGraphStart(String taskId) {
				// TODO Auto-generated method stub
				
			}

			@Override
			public void onGraphError(GraphError error, String id) {
				if (error == GraphError.NO_COVER && id.equalsIgnoreCase(FETCH_ALBUM)){
					coverReady(id);
				}
				
			}});
		
		GraphRequest.albumRequest(	
									true, 						// We are calling this method for the first time	 
									false, 						// Do not update the cache with new values
									false, 						// Do not clear the download trackers
									null, 						// We currently have no request for the next page of data
									FETCH_ALBUM, 				// Unique task ID
									mCache,						// Cache to store the downloaded values
									0);
		
	}
	
	public static String getTime(){
		SimpleDateFormat timeStamp = new SimpleDateFormat("yyyyMMdd", java.util.Locale.getDefault());
		Date today = Calendar.getInstance().getTime();
		return timeStamp.format(today);
	}
}
