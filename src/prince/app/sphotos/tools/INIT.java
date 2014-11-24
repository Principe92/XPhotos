package prince.app.sphotos.tools;

import prince.app.sphotos.util.ImageCache.ImageCacheParams;
import android.content.Context;



/** A singleton class that handles generally used variables and functions for Gallery Activity */
public class INIT{
	private static INIT instance;
	public static String LOCAL_IMAGE_THUMB_DIR = "local_thumbs";
	public static String LOCAL_IMAGE_DIR = "local_image";
	
	public ImageCacheParams localphoto_cache;
	public ImageCacheParams localphotofull_cache;
	
	public void init_LocalPhotoCache(Context context, float percent){
		localphoto_cache = new ImageCacheParams(context, LOCAL_IMAGE_THUMB_DIR);
		localphoto_cache.setMemCacheSizePercent(percent); 
	}
	
	public void init_LocalPhotoFullCache(Context context, float percent){
		localphotofull_cache = new ImageCacheParams(context, LOCAL_IMAGE_DIR);
		localphotofull_cache.setMemCacheSizePercent(percent); 
	}
	
	
	
	
	
	
	
	
	
	public static void initInstance()
	  {
	    if (instance == null)
	    {
	      // Create the instance
	      instance = new INIT();
	    }
	  }
	
	public static INIT getInstance()
	  {
	    // Return the instance
	    return instance;
	  }
	
	private INIT()
	  {
	    // Constructor hidden because this is a singleton
	  }
}
