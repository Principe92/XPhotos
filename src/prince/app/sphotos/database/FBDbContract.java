package prince.app.sphotos.database;

import android.provider.BaseColumns;

public class FBDbContract {
    public static final String TEXT_TYPE = " TEXT";
    public static final String INTEGER_TYPE = " INTEGER";
    public static final String COMMA_SEP = ",";
    
    public static final String FB_DB_NAME = "Facebook.db";
    
    public static final String FB_TAGGED_TABLE = "tagged_table";
    public static final String FB_UPLOADED_TABLE = "uploaded_table";
	
	public FBDbContract() {}
	
	public static abstract class AlbumHelper implements BaseColumns {
		 public static final String FB_ALBUM_TABLE = "facebook_albums";
		 
		 /** Unique ID of the Album */
		 public static final String ALBUM_ID = "album_id";  	
		
		 /** ID of Owner of the Album */
		 public static final String ALBUM_OWNER_ID = "owner_id";
		
		 /** Name of the Owner */
		 public static final String ALBUM_OWNER = "owner_name";		    
		
		 /** Album name */
		 public static final String ALBUM_NAME = "album_name";
		
		 /** Album cover photo ID */
		 public static final String ALBUM_COVER_PHOTO_ID = "cover_photo_id";	    
		
		 /** Number of Images in the Album */
		 public static final String ALBUM_SIZE = "album_size";			
		
		 /** Type of Album */
		 public static final String ALBUM_TYPE = "album_type";
		
		 /** Location of the album */
		 public static final String ALBUM_LOCATION = "album_location";
		
		 /** Time created */
		 public static final String ALBUM_CREATED_TIME = "created_time";					
		
		 /** Time Updated */
		 public static final String ALBUM_UPDATED_TIME = "uploaded_time";					
		
		 /** Privacy */
		 public static final String ALBUM_PRIVACY = "privacy";
		
		 /** Local album Path */
		 public static final String ALBUM_PATH = "album_path";
		
		 /** Album Cover URL */
		 public static final String ALBUM_COVER_URL = "cover_url";
		
		 /** Can we upload */
		 public static final String  ALBUM_UPLOADABLE = "can_upload";
		 
		 public static final String CREATE_ALBUM_TABLE =
			        "CREATE TABLE IF NOT EXISTS " + AlbumHelper.FB_ALBUM_TABLE 
			        				+ " (" 
			        				+ AlbumHelper._ID + " INTEGER PRIMARY KEY," 							//  entry id
			        				+ AlbumHelper.ALBUM_ID 				+ TEXT_TYPE + COMMA_SEP 		// album id
			        				+ AlbumHelper.ALBUM_OWNER_ID 		+ TEXT_TYPE + COMMA_SEP 		// album owner id
			        				+ AlbumHelper.ALBUM_OWNER 			+ TEXT_TYPE + COMMA_SEP			// album owner name
			        				+ AlbumHelper.ALBUM_NAME 			+ TEXT_TYPE + COMMA_SEP			// album name
			        				+ AlbumHelper.ALBUM_COVER_PHOTO_ID 	+ TEXT_TYPE + COMMA_SEP			// album cover photo id
			        				+ AlbumHelper.ALBUM_PRIVACY 			+ TEXT_TYPE + COMMA_SEP			// album privacy
			        				+ AlbumHelper.ALBUM_SIZE				+ INTEGER_TYPE + COMMA_SEP			// album size
			        				+ AlbumHelper.ALBUM_TYPE				+ TEXT_TYPE	+ COMMA_SEP			// album type
			        				+ AlbumHelper.ALBUM_LOCATION			+ TEXT_TYPE	+ COMMA_SEP			// album location
			        				+ AlbumHelper.ALBUM_CREATED_TIME		+ TEXT_TYPE	+ COMMA_SEP			// album created time
			        				+ AlbumHelper.ALBUM_UPDATED_TIME		+ TEXT_TYPE	+ COMMA_SEP			// album updated time
			        				+ AlbumHelper.ALBUM_COVER_URL		+ TEXT_TYPE	+ COMMA_SEP			// album cover url
			        				+ AlbumHelper.ALBUM_UPLOADABLE		+ TEXT_TYPE	+ COMMA_SEP			// album can upload to
			        				+ AlbumHelper.ALBUM_PATH												// album local path
			        				+ " )";
		 
		 public static final String SQL_DELETE_ENTRIES =
		            "DROP TABLE IF EXISTS " + AlbumHelper.FB_ALBUM_TABLE;

	}

	
	public static abstract class ImageHelper implements BaseColumns{
		public static final String BASE_TB_NAME = "album_";
		public static String IMAGES_TABLE_NAME;
		public static final String IMAGE_THUMB = "image_thumb";
		public static final String IMAGE_FULL = "image_full";
		
		public static String getCreate(String name){
			ImageHelper.IMAGES_TABLE_NAME  = name;
		       return "CREATE TABLE IF NOT EXISTS " + name
		        				+ " (" 
		        				+ ImageHelper._ID + " INTEGER PRIMARY KEY,"
		        				+ ImageHelper.IMAGE_THUMB	+ TEXT_TYPE + COMMA_SEP
		        				+ ImageHelper.IMAGE_FULL
		        				+ " )";
		}
	}
}
