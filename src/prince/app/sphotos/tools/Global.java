package prince.app.sphotos.tools;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import prince.app.sphotos.R;
import prince.app.sphotos.bgtask.ScheduleTask;
import prince.app.sphotos.database.AlbumSQL;
import prince.app.sphotos.database.FBDbContract;
import prince.app.sphotos.util.ImageFetcher;
import prince.app.sphotos.util.ImageResizer;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.Display;
import android.widget.GridView;
import android.widget.Toast;

public class Global extends Application{

	// local variables
	private static Global generic;
	private static String TAG = "Global";
	private static Object Lock;
	public static Object netLock;
	
	public final static int WAIT_TIME = 10000;
	
	private AlbumSQL mdbFB; 
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public void onCreate(){
		super.onCreate();
		
		// Init singletons
		FBINIT.initInstance();
		INIT.initInstance();
		
		mdbFB = new AlbumSQL(this.getApplicationContext(), FBDbContract.FB_DB_NAME);
		
		// set preference to default values
		PreferenceManager.setDefaultValues(this, R.xml.app_preference, false);
		
		generic = this;
		
		Lock = new Object();
		netLock = new Object();
		
		Util.APPLICATION_PATH = createParentFolder();
		Util.FACEBOOK_PATH = createFolder(Util.APPLICATION_PATH, "Facebook");
		Util.GOOGLE_PATH = createFolder(Util.APPLICATION_PATH, "GooglePlus");
		Util.TWITTER_PATH = createFolder(Util.APPLICATION_PATH, "Twitter");
		Util.SOCIAL_PHOTOS_PATH = createFolder(Util.APPLICATION_PATH, "Gallery");
		
		
	/*	new ScheduleTask().setSchedule(getApplicationContext(),
				ScheduleTask.REFRESH_DATA, 0, 0, 10000); */
		
		new ScheduleTask().cancelSchedule(getApplicationContext(), 0, 0);
	}
	
	public static String capitalize(String origString){
		if (origString != null && !origString.isEmpty()){
			return origString.substring(0, 1).toUpperCase(Locale.getDefault()) + ((origString.length() > 1) ? origString.substring(1): "");
		}
		return "";
	}
	
	public AlbumSQL getFBDb(){ return mdbFB;}
	
	public void showToast(String msg){
		Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
	}
	
	public void showToast(String msg, int duration){
		Toast.makeText(this, msg, duration).show();
	}
	
	 public  boolean isConnection(){
		    ConnectivityManager connectivityManager 
		          = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		    NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		    return activeNetworkInfo != null && activeNetworkInfo.isConnected();
		}
	
	public static Object getLock(){
		return Lock;
	}
	
	public static Object newLock(){
		return new Object();
	}
	
	public static Global getInstance(){
		return generic;
	}
	
	public static Fragment findFrag(FragmentActivity view, String tag){
		return view.getSupportFragmentManager().findFragmentByTag(tag);
	}
	//****************************************************************************************************************/
	
	/**
	 * Creates the applications default folder in the external pictures directory
	 * <br><br><i>
	 * {@code private String createParentFolder()}
	 * </i>
	 * @return the file path of the folder
	 */
	private String createParentFolder(){
		if (isExternalStorageWritable()){
			File folder = new File(Util.SDCARD_PATH, "Social Photos");
			if (folder.mkdirs() || folder.exists()){
				return folder.getAbsolutePath();
			}
		}
		return null;
	}
	//****************************************************************************************************************/
	
	/**
	 * Creates a new file path or simply a new file
	 * <br><br><i><strong>
	 * {@code public File createPath(String paths)}
	 * </i></strong>
	 * @param paths - the name of the path to be created
	 * @return File - the new file (path) created
	 */
	public File createPath(String paths){
		File newFile = new File(paths);
		return newFile;
	}
	//****************************************************************************************************************/
	
	/**
	 * Creates a folder in a directory
	 * @return The default directory of the created folder
	 * @param  name - The name of the folder to be created
	 * @param	path - The path to create the folder			
	 * 
	 */
	public String createFolder(String path, String name){
		if (isExternalStorageWritable()){
			String newPath = path + File.separator + name;
			File folder = createPath(newPath);
			if (folder.mkdirs() || folder.exists()){
				return folder.getAbsolutePath();
			}
		}
		return null;
	}
	//****************************************************************************************************************/
	
	/** Checks if external storage is available for read and write 
	 * @return true if external storage is available for read and write*/
	public boolean isExternalStorageWritable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state)) {
	        return true;
	    }
	    return false;
	}
	//****************************************************************************************************************/
	
	/** remove fragment */
	public static void hideRemoveFrag(FragmentActivity view, Fragment fragment, boolean remove){
		if (fragment != null){
				FragmentManager fm = view.getSupportFragmentManager();
			    FragmentTransaction transaction = fm.beginTransaction();
			    transaction.hide(fragment);
			    if (remove && fragment.isAdded()){
			    	transaction.remove(fragment);
			    	Log.d(TAG, fragment.getClass().getName() + " fragment removed");
			    }
			    transaction.commit();
			    Log.d(TAG, fragment.getClass().getName() + " fragment hidden");
		    }
	}
	//****************************************************************************************************************/
	
	/** add a fragment and indicate if you want to show immediately*/
	public static void addShowFrag(FragmentActivity view, Fragment fragment, int id, boolean show){
		if (fragment != null){
			FragmentManager fm = view.getSupportFragmentManager();
		    FragmentTransaction transaction = fm.beginTransaction();
		    if (!fragment.isAdded()){
		    	transaction.add(id,  fragment);
		    	Log.d(TAG, fragment.getClass().getName() + " fragment added");
		    }
		    
		    if (show){
		    	transaction.show(fragment);
		    	Log.d(TAG, fragment.getClass().getName() + " fragment shown");
		    }
		    else
		    	transaction.hide(fragment);
		    
		    transaction.commit();
		}
	}
	//****************************************************************************************************************/
	
	/** show fragment */
	public static void showFrag(FragmentActivity view, Fragment fragment){
		if (fragment != null && !fragment.isVisible()){
			FragmentManager fm = view.getSupportFragmentManager();
		    FragmentTransaction transaction = fm.beginTransaction();
		    transaction.show(fragment);
		    transaction.commit();
		    Log.d(TAG, fragment.getClass().getName() + " fragment shown");
		}
	}
	//****************************************************************************************************************/
	
	@TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
	/**
	 * Calculates the number of columns to draw the screen grid
	 * @param frame - activity
	 * @param gridview - the gridView
	 * @param x - image Size
	 * @param y - image Spacing
	 * @return
	 */
	public static int numberOfColumns(Activity frame, GridView gridview, int x, int y){
		Display display = frame.getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		int width = size.x;
		int grid = (int) Math.floor(
	                width / (x + y));
		Log.e(TAG, "# of columns: " + grid + "; imageSize: " + x + "; imageSpacing: " + y);
		return grid;
	}
	//****************************************************************************************************************/
	
	/**
	 * Checks if a file exists in a given external storage directory
	 * <br><br><i><strong>
	 * {@code public boolean isInDisk(String path, String filename)}
	 * </i></strong>
	 * @param path
	 * @param filename
	 * @return
	 */
	public boolean isInDisk(String path, String filename, boolean internal){
		File directory, check;
		try {
			if (internal || path == null)
				directory = getFilesDir();
			else
				directory = new File(path);
			
			check = File.createTempFile(filename, ".jpg", directory);
			if (check.exists()){
				return true;
			}
			else
				return false;
		} catch (IOException e) {
			return false;
		}
	}
	//****************************************************************************************************************/
	
	/**
	 * gets the photo for the navigation drawer given the filename
	 * @param filename
	 * @return the photo if found or a default photo
	 */
	public Bitmap getDrawerPhotos(String filename, int reqWidth, int reqHeight){
		Bitmap bitmap = getImageFromInternal(filename, reqWidth, reqHeight);
		if (bitmap != null)
			return bitmap;
		return BitmapFactory.decodeResource(getResources(),R.drawable.index); 
	}
	//*****************************************************************************************************************/
	
	public Bitmap getImageFromInternal(String filename, int reqWidth, int reqHeight){
		try {
			FileInputStream image = openFileInput(filename);
			Bitmap bitmap;
			if (reqWidth == 0 || reqHeight == 0){
				bitmap = BitmapFactory.decodeStream(image);
			} else{
				bitmap = ImageResizer.decodeSampledBitmapFromDescriptor(image.getFD(), reqWidth, reqHeight, null);
			}
			
			image.close();
			return bitmap;
		} catch (NullPointerException e){
			return null;
		}catch (FileNotFoundException e) {
			Log.d(TAG, "file do not exist");
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	//*****************************************************************************************************************/
	
	public Bitmap getImageFromExternal(String filename){
		try {
			BufferedInputStream image = new BufferedInputStream(new FileInputStream(filename));
			Bitmap bitmap = BitmapFactory.decodeStream(image);
			image.close();
			return bitmap;
		} catch (NullPointerException e){
			return null;
		}catch (FileNotFoundException e) {
			Log.d(TAG, "file do not exist");
			return null;
		} catch (IOException e) {
			return null;
		}
	}
	//*****************************************************************************************************************/
	
	/** Checks if external storage is available to at least read */
	public boolean isExternalStorageReadable() {
	    String state = Environment.getExternalStorageState();
	    if (Environment.MEDIA_MOUNTED.equals(state) ||
	        Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
	        return true;
	    }
	    return false;
	}
	//*****************************************************************************************************************/
	
	/**
	 * 
	 * Public function to download an image and save it to the available external storage
	 * with the given filePath and filename
	 * 
	 * <br><br><i>
	 * {@code public void saveImageToExternalStorage(String URL, String filePath, String filename)}
	 * </i>
	 * @param URL - a web address (http) to download the image
	 * @param filePath - A directory to save the image
	 * @param filename - The name of the file
	 * @param notify   - True to notify the user that an image have been saved to sd card
	 */
	
	public void downloadSaveToExternal(String URL, String filePath, String filename, boolean notify){
		if (isExternalStorageWritable() && URL != null){
			String imageFileName;
			if (filename !=null){
				imageFileName = filename;
			}else{
				SimpleDateFormat timeStamp = new SimpleDateFormat("yyyyMMdd_hhmmss", java.util.Locale.getDefault());
				Date today = Calendar.getInstance().getTime();
				imageFileName = timeStamp.format(today) + "_IMG_FB.";
			}
			
			File directory = new File(filePath);
			try {
				File image = File.createTempFile(
						imageFileName,  /* prefix */
						".jpg",         /* suffix */
						directory      /* directory */
						);
			
				if (image.exists()) image.delete();
				
				FileOutputStream save = new FileOutputStream(image);
				downloadImageFromURL task = new downloadImageFromURL(save);
				task.execute(URL);
				Log.d(TAG, "!!! Image: " + image.getName() + " saved to External Storage  !!!");
				
				if (notify){
				    Toast.makeText(this, "Image saved", Toast.LENGTH_SHORT).show();
				}
			
			} catch (IOException e) {
				e.printStackTrace();
			}
			
		}
	}
	//*****************************************************************************************************************/
	
	
	/**
	 * Public function to download an image and save it to the available internal storage
	 * with the given filename
	 * <br><br><i>
	 * {@code public void saveImageToInternalStorage(String URL, String filename)}
	 * </i>
	 * @param URL - a web address (http) to download the image
	 * @param filename - the name of the file
	 */
	public void downloadSaveToInternal(String URL, String filename){
		FileOutputStream save;
		try { 
			if (URL != null){
				save = openFileOutput(filename, Context.MODE_PRIVATE);
				downloadImageFromURL task = new downloadImageFromURL(save);
				task.execute(URL);
				Log.d(TAG, "!!! Image: " + filename + " saved to Internal !!!");
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	//*****************************************************************************************************************/
	
	/**
	 * downloads image from the web and saves it to the specified internal/external folder
	 * @author Principe
	 *
	 */
	private class downloadImageFromURL extends AsyncTask<String, Void, Void>{
		private FileOutputStream save;
		
		
		public downloadImageFromURL(FileOutputStream save2) {
			this.save = save2;
		}


		@Override
		protected Void doInBackground(String... params) {
			boolean result = ImageFetcher.downloadUrlToStream(params[0], save);
			Log.d(TAG, "downloadFromURL result: " + result);
			return null;
		}
		
	}
	//*****************************************************************************************************************/
	
	/*
	 *****************************************************************
	 *    FUNCTIONS TO PLAY WITH SHARED PREFERENCES                  *
	 *****************************************************************     
	 */
	
	/**
	 * modifies the integer value associated with the given key
	 * @param key - unique identifier
	 * @param value - new value
	 */
	public void modPref(String key, int value){
		SharedPreferences sharedpreferences;
		sharedpreferences = getSharedPreferences(key, Context.MODE_PRIVATE);
		Editor editor = sharedpreferences.edit();
		editor.putInt(key, value);
		editor.commit();
	}
	
	/**
	 * modifies the String value associated with the given key
	 * @param key - unique identifier
	 * @param value - new value
	 */
	public void modPref(String key, String value){
		SharedPreferences sharedpreferences;
		sharedpreferences = getSharedPreferences(key, Context.MODE_PRIVATE);
		Editor editor = sharedpreferences.edit();
		editor.putString(key, value);
		editor.commit();
	}
	
	/**
	 * Returns the String value associated with the given key
	 * @param key - the unique identifier
	 * @return the String value associated with the key or empty string if not found
	 */
	public String getStrPref(String key){
		SharedPreferences sharedpreferences;
		sharedpreferences = getSharedPreferences(key, Context.MODE_PRIVATE);
		return sharedpreferences.getString(key, "");
	}
	
	/**
	 * Returns the integer value associated with the given key
	 * @param key - th unique identifier
	 * @return the int value associated with the key or 0 if not found
	 */
	public Integer getIntPref(String key){
		SharedPreferences sharedpreferences;
		sharedpreferences = getSharedPreferences(key, Context.MODE_PRIVATE);
		return sharedpreferences.getInt(key, 0);
	}
	
	/**
	 * Checks if a preference is stored
	 * @param key - the unique identifier
	 * @return true - if the preference exists
	 */
	public boolean prefExist(String key){
		SharedPreferences sharedpreferences;
		sharedpreferences = getSharedPreferences(key, Context.MODE_PRIVATE);
		return sharedpreferences.contains(key);
	}
	
	
	/*
	 *****************************************************************
	 *    FUNCTIONS TO CHECK THE TYPE OF ANDROID VERSIONS            *
	 *****************************************************************     
	 */
	
	public static boolean hasFroyo() {
        // Can use static final constants like FROYO, declared in later versions
        // of the OS since they are inclined at compile time. This is guaranteed behavior.
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.FROYO;
    }

    public static boolean hasGingerbread() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD;
    }

    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    public static boolean hasJellyBean() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN;
    }

    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    public static String time(){
    	return DateFormat.getTimeInstance().format(new Date());
    }
    
    //*******************************************************************************************************/
}

