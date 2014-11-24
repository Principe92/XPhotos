
package prince.app.sphotos.tools;

import java.io.File;

import android.os.Environment;


/**
 * Public Class containing all static utility constants.
 */
public class Util {

	private Util() {};
	
/*
**********************************************************************
****** Static Variables holding the request id for the device camera *
**********************************************************************/
	
	/**Unique Intent ID to request device camera
    * @return 10
    */
   public static int GALLERY_CAMERA_REQUEST = 101;
   
   /**Unique Intent ID to request device camera
    * @return 30
    */
   public static int FACEBOOK_CAMERA_REQUEST = 102;
   
   /**Unique Intent ID to request device camera
    * @return 50
    */
   public static final int GOOGLE_CAMERA_REQUEST = 103;
   
   /**Unique Intent ID to request device camera
    * @return 60
    */
   public static final int TWITTER_CAMERA_REQUEST = 104;
   
//************************************************************************************************************************************************//
    
    
    /**The Unique ID for calling loadImage in ImageWorker to load full size Images from SDCard
     * @return 100
     */
    public static String LOCALPHOTO_FULLIMAGE_LOAD_ID = "local_full";
    
    /**Unique ID when starting Local_FullImageActivity
     * @return 200
     */
    public static int LOCALPHOTO_FULLIMAGE_REQUEST_CODE = 200;
    
    /**Unique ID for intent's extra data when starting LocalPhoto_FullImageActivity
     * @return "localphoto_result"
     */
    public static String LOCALPHOTO_RESULT = "localphoto_result";
    
    /**The Unique ID for calling loadImage in ImageWorker to load thumb nails Images from SDCard
     * @return 300
     */
    public static String LOCALPHOTO_THUMBNAIL_LOAD_ID = "local_thumb";
    
    
    /**
     * Unique ID to use the loadImage of ImageWorker to display facebook images in full
     * @return "facebook_full_image"
     */
    public static String FACEBOOK_FULL_IMAGE_LOAD_ID = "facebook_full_image";
    
    /**
     * Unique ID to use the loadImage of ImageWorker to load the cover photos of albums
     * @return "album_cover"
     */
    public static final String ALBUM_COVERPHOTO_LOAD_ID = "album_cover";
    
    /**
     * Unique ID to use the loadImage of ImageWorker to load images of an Album
     * @return "album_images"
     */
    public static final String ALBUM_IMAGES_LOAD_ID = "album_images";
    //**********************************************************************************************************************************************/

    /*
     * ************************************************************
     * Static Variables holding the launch codes of each Activity *
     * *************************************************************/
    
    /**
     * Launch Code for Uploaded Photos Activity
     * @return -3
     */
	public static final int UPLOADED_PHOTO_LAUNCH_CODE = -3;
	
	/**
	 * Launch Code for Tagged Photos Activity
     * @return -2
     */
	public static final int TAG_PHOTO_LAUNCH_CODE = -2;
	
	/**
	 * Launch Code for Test Activity
	 * @return 300
	 */
    public static final int FB_ALBUMS_LAUNCH_CODE = 300;
    
    /**
     * Launch Code for Activity_Facebook_EachAlbum
     * @return 400
     */
    public static final int FB_EACH_ALBUM_LAUNCH_CODE = 400;
    
//*************************************************************************************************************************************************//
    
    /**
     * Parent folder of the application
     */
    public static String APPLICATION_PATH;
    
    /**
     * Default External Media Path
     */
	public static File SDCARD_PATH = Environment.getExternalStoragePublicDirectory(
            Environment.DIRECTORY_PICTURES);
	
	/**
	 * Holds the default path for downloaded facebook images
	 */
	public static String FACEBOOK_PATH;
	
	/**
	 * Holds the default path for downloaded google images
	 */
	public static String GOOGLE_PATH;
	
	/**
	 * Holds the default path for downloaded twitter images
	 */
	public static String TWITTER_PATH;
	
	/**
	 * Holds the default path for images taken using the app
	 */
	public static String SOCIAL_PHOTOS_PATH;
	
	/**
	 * A flag to indicate that a file should be saved to an external storage
	 * @return "sdCard"
	 */
	public final static String SAVE_IMAGE_TO_EXTERNAL = "sdCard";
	
	/**
	 * A flag to indicate the last launched activity
	 */
	public final static String LAST_LAUNCHED_ACTIVITY = "last_launched_code";
	
	/**
	 * A flag to indicate that a file should be saved to an internal storage
	 * @return "memory"
	 */
	public final static String SAVE_IMAGE_TO_INTERNAL = "memory";
    
    
    /**
     * Static variable holding the key to the name of the user in Preferences
     * @return "user_id"
     */
    public final static String FB_USER_ID = "user_id";
    //**********************************************************************************************************************************************/
    
    
    /*
     * ****************************************************************
     * Static Variables holding the sizes of things in preferences    *
     * ****************************************************************/
    
    /**
     * number of user albums
     * @return "albums_count"
     */
    public final static String FB_NUMBER_OF_ALBUMS = "albums_count";
    
    /**
     * number of uploaded photos
     * @return "uploaded_count"
     */
    public final static String FB_NUMBER_OF_UPLOADED = "uploaded_count";
    
    /**
     * number of tagged photos
     * @return "tagged_count"
     */
    public final static String FB_NUMBER_OF_TAGGED = "tagged_count";
    
    /**
     * number of cover photos
     * @return "cover_count"
     */
    public final static String FB_NUMBER_OF_COVER = "cover_count";
    
    /**
     * number of profile photos
     * @return "profile_count"
     */
    public final static String FB_NUMBER_OF_PROFILE = "profile_count";
    
    /**
     * number of favorite album
     * @return "fav_count"
     */
    public final static String FB_NUMBER_OF_FAVORITE = "fav_count";
    
    /**
     * name of favorite album
     * @return "fav_album"
     */
    public final static String FB_FAVORITE_NAME = "fav_album";
    
//*************************************************************************************************************************************************//
    
    
    /*
     * ***********************************************************************
     * Static Variables holding the key of photos stored in internal storage *
     * ***********************************************************************/
    
    /**
     * Image file name for grid: Tagged Photos
     */
    public final static String FB_TAG_GRID_IMAGE = "tagged_grid_image";
    
    /**
     * Image file name for grid: Uploaded Photos
     */
    public final static String FB_UPLOADED_GRID_IMAGE = "uploaded_grid_image";
    
    /**
	 * Image file name for drawer big photo
	 */
	public final static String FB_DRAWER_BIG_IMAGE = "drawer_big_image";
	
	/**
	 * Image file name for drawer small photo
	 */
    public final static String FB_DRAWER_SMALL_IMAGE = "drawer_small_image";
    
    /**
     * Image file name for grid: Albums
     */
    public final static String FB_ALBUM_GRID_IMAGE = "albums_grid_image";
    
    /**
     * Image file name for grid: Profile Photos
     */
    public final static String FB_PROFILE_GRID_IMAGE = "profiles_grid_image";
    
    /**
	 * Image file name for grid: Cover Photos
	 */
    public final static String FB_COVER_GRID_IMAGE = "cover_grid_image";
    
//*************************************************************************************************************************************************//
}
