package prince.app.sphotos.tools;

import java.util.HashMap;
import java.util.List;
import java.util.Stack;

import prince.app.sphotos.Request.RequestX;
import prince.app.sphotos.util.ImageCache.ImageCacheParams;
import android.content.Context;
import android.util.SparseArray;

import com.facebook.Session;


public class FBINIT {
	private static FBINIT instance;
	
	public static String FACEBOOK_USER_ID;
	public static boolean REQUESTDONE;
	public static String FB_ACCESS_TOKEN;
	public static boolean LOGGED;
	
	
	public static final String ID = "id";
	public static final String FROM = "from";
	public static final String NAME = "name";
	public static final String COVERPHOTO = "cover_photo";
	public static final String COUNT = "count";
	public static final String TYPE = "type";
	public static final String CTIME = "created_time";
	public static final String UTIME = "updated_time";
	public static final String PRIVACY = "privacy";
	public static final String DATA = "data";
	public static final String ALBUMS = "albums";
	public static final String PICTURE = "picture";
	public static final String NEXT = "next";
	public static final String WIDTH = "width";
	public static final String HEIGHT = "height";
	public static final String IMAGE = "images";
	public static final String SOURCE = "source";
	public static final String GET = "https://graph.facebook.com";
	public static final String PAGING = "paging";
	public static final String CURSORS = "cursors";
	public static final String AFTER = "after";
	public static final String PHOTOS = "photos";
	public static final String TAGS = "tags";
	public static final String PREVIOUS = "previous";
	public static final String URL = "url";
	public static final String CAN_UPLOAD = "can_upload";
	
	private static final String IMAGE_ALBUMCOVER_DIR = "cover_thumbs";
	private static final String IMAGE_THUMBNAIL_DIR = "thumbs";
	private static final String IMAGE_FULL_DIR = "image";
	
	public static final HashMap<String, RequestX> sRequests = new HashMap<String, RequestX>();
	public static final Stack<String> sReqStack = new Stack<String>();
	public static final Object sReqLock = new Object();
	
	private List<String> PERMISSIONS;
	public static boolean updatedAlbumGridPhoto = false;
	
	/**
	 * Stores all the Facebook Albums and their respective details (meta-data)
	 */
	public static SparseArray<Albums> sAlbumsArray = new SparseArray<Albums>();
	public static SparseArray<Albums> sAlbumsArray_NEW = new SparseArray<Albums>();
	
	/**
	 * Stores all the image details of a particular Album
	 */
	public static SparseArray<Images> sImagesArray = new SparseArray<Images>();
	public static SparseArray<Images> sImagesArray_NEW = new SparseArray<Images>();
	
	
	
	public ImageCacheParams coverphoto;
	public ImageCacheParams imageCache;
	public ImageCacheParams fullImageCache;
	
	public static boolean IMAGES_READY = false;
	public static final Object sImagesArray_LOCK = new Object();
	public static boolean IMAGES_TASK_DONE = false;
	public static boolean IMAGES_TASK_SUCCESS = false;
	public static int sImagesArray_LASTUSED = -1;
	public static boolean IMAGES_UPDATED = false;
	
	public static boolean ALBUMS_READY = false;
	public static final Object sAlbumsArray_LOCK = new Object();
	public static boolean ALBUMS_TASK_DONE = false;
	public static boolean ALBUMS_TASK_SUCCESS = false;
	public static boolean ALBUMS_TASK_STARTED = false;
	public static boolean ALBUMS_UPDATED = false;
	
	public static String DOWNLOADING_ALBUM_IMAGES_ADDRESS = "";

	public static boolean COVER_TASK_DONE = false;
	public static boolean COVER_TASK_SUCCESS = false;
	public static int COVER_COUNT = 0;
	
	
	private int lastposition = -1;
	public Session lastSession;
	
	
	/**
	 * @return the lastSession
	 */
	public Session getLastSession() {
		return lastSession;
	}

	/**
	 * @param lastSession the lastSession to set
	 */
	public void setLastSession(Session lastSession) {
		this.lastSession = lastSession;
	}

	public void init_CoverPhoto(Context context, float percent){
		coverphoto = new ImageCacheParams(context, IMAGE_ALBUMCOVER_DIR);
		coverphoto.setMemCacheSizePercent(percent); 
	}
	
	public void init_Thumbnail(Context context, float percent){
		imageCache = new ImageCacheParams(context, IMAGE_THUMBNAIL_DIR);
		imageCache.setMemCacheSizePercent(percent);
	}
	
	public void init_FullImageCache(Context context, float percent){
		fullImageCache = new ImageCacheParams(context, IMAGE_FULL_DIR);
		fullImageCache.setMemCacheSizePercent(percent);
	}

	public int getLastposition() {
		return lastposition;
	}

	public void setLastposition(int lastposition) {
		this.lastposition = lastposition;
	}

	public List<String> getPERMISSIONS() {
		return PERMISSIONS;
	}

	public void setPERMISSIONS(List<String> list) {
		PERMISSIONS = list;
	}
	
	public static String getAlbumID(String albumName){
		if (albumName != null && !albumName.isEmpty() && !isAlbumEmpty()){
			int albumSize = FBINIT.sAlbumsArray.size();
			for (int i = 0; i < albumSize; i++){
				String name = FBINIT.sAlbumsArray.get(i).mAlbumName;
				if (name.equalsIgnoreCase(albumName)){
					return FBINIT.sAlbumsArray.get(i).mAlbumID;
				}
			}
		}
		return null;
	}
	
	public static String getCoverIDByName(String albumName){
		if (albumName != null && !albumName.isEmpty() && !isAlbumEmpty()){
			int albumSize = FBINIT.sAlbumsArray.size();
			for (int i = 0; i < albumSize; i++){
				String name = FBINIT.sAlbumsArray.get(i).mAlbumName;
				if (name.equalsIgnoreCase(albumName)){
					return FBINIT.sAlbumsArray.get(i).mAlbumCoverPhotoID;
				}
			}
		}
		return null;
	}
	
	public static int getAlbumSizeByName(String albumName, SparseArray<Albums> mCache){
		if (albumName != null && !albumName.isEmpty() && mCache != null){
			int albumSize = mCache.size();
			for (int i = 0; i < albumSize; i++){
				String name = mCache.get(i).mAlbumName;
				if (name.equalsIgnoreCase(albumName)){
					return mCache.get(i).mAlbumSize;
				}
			}
		}
		return 0;
	}
	
	public static int getIndexByName(String albumName){
		if (albumName != null && !albumName.isEmpty() && !isAlbumEmpty()){
			int albumSize = FBINIT.sAlbumsArray.size();
			for (int i = 0; i < albumSize; i++){
				String name = FBINIT.sAlbumsArray.get(i).mAlbumName;
				if (name.equalsIgnoreCase(albumName)){
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Method to get the index position of an album in an Array
	 * @param ID - The ID of the album
	 * @return The index position or -1 if unsuccessful
	 */
	public static int getIndexByID(String ID){
		if (ID != null && !ID.isEmpty() && !isAlbumEmpty()){
			int albumSize = FBINIT.sAlbumsArray.size();
			for (int i = 0; i < albumSize; i++){
				String id = FBINIT.sAlbumsArray.get(i).mAlbumID;
				if (id.equalsIgnoreCase(ID)){
					return i;
				}
			}
		}
		return -1;
	}
	
	/**
	 * Method to check if Album Array is  empty
	 * @return true if empty or else, false
	 */
	public static boolean isAlbumEmpty(){
		synchronized(sAlbumsArray){
			return sAlbumsArray.size() == 0;
		}
	}
	

	public static void initInstance()
	  {
	    if (instance == null)
	    {
	      // Create the instance
	      instance = new FBINIT();
	    }
	  }
	
	public static FBINIT getInstance()
	  {
	    // Return the instance
	    return instance;
	  }
	
	private FBINIT()
	  {
	    // Constructor hidden because this is a singleton
	  }

	public static boolean isImageEmpty() {
		synchronized(sImagesArray){
			return sImagesArray.size() == 0;
		}
	}

}
