package prince.app.sphotos.database;

import prince.app.sphotos.database.FBDbContract.AlbumHelper;
import prince.app.sphotos.database.FBDbContract.ImageHelper;

public class CursorProj {
	
	private CursorProj(){}
	
	public static final String[] ALBUM_DB_PROJECTION = {
		AlbumHelper._ID,
		AlbumHelper.ALBUM_ID,
		AlbumHelper.ALBUM_OWNER_ID,
		AlbumHelper.ALBUM_OWNER,
		AlbumHelper.ALBUM_NAME,
		AlbumHelper.ALBUM_COVER_PHOTO_ID,
		AlbumHelper.ALBUM_SIZE,
		AlbumHelper.ALBUM_TYPE,
		AlbumHelper.ALBUM_LOCATION,
		AlbumHelper.ALBUM_CREATED_TIME,
		AlbumHelper.ALBUM_UPDATED_TIME,
		AlbumHelper.ALBUM_PRIVACY,
		AlbumHelper.ALBUM_PATH,
		AlbumHelper.ALBUM_COVER_URL,
		AlbumHelper.ALBUM_UPLOADABLE
	};
	
	public static final String [] IMAGE_DB_PROJECTION = {
		ImageHelper._ID,
		ImageHelper.IMAGE_THUMB,
		ImageHelper.IMAGE_FULL
	};
}
