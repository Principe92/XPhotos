package prince.app.sphotos.tools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import prince.app.sphotos.R;
import prince.app.sphotos.bgtask.FetchData;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

public abstract class ActivityBase extends FragmentActivity implements OnMenuItemClickListener{
	
	protected DrawerLayout mDrawerLayout;
    protected ListView mDrawerList;
    protected ActionBarDrawerToggle mDrawerToggle;
    
    protected FetchData mWifiListener = new FetchData();
    
    public static final int CAMERA_ID = 100;
	private String mImagePath;
	private File mImageFolder = Global.getInstance().createPath(Util.SOCIAL_PHOTOS_PATH);
	
    private File createImageFileName(){
		// Create an image file name
		SimpleDateFormat timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault());
		Date today = Calendar.getInstance().getTime();
	    String imageFileName = "IMG_XP_" + timeStamp.format(today);
	    File newImage = new File(mImageFolder.getPath() + File.separator + imageFileName + ".jpg");
	    

	    // Save a file: path for use with ACTION_VIEW intents
	    mImagePath = newImage.getPath();
	    return newImage;
	}
    
    private void TakePicture(){
		Intent mTakePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		Uri address = Uri.fromFile(createImageFileName());
		
		// Continue only if the File was successfully created
		if (address != null) {
			mTakePic.putExtra(MediaStore.EXTRA_OUTPUT, address);
			startActivityForResult(mTakePic, CAMERA_ID);
		}
	}
	
	private void galleryAddPic() {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
	    File file = new File(mImagePath);
	    Uri contentUri = Uri.fromFile(file);
	    mediaScanIntent.setData(contentUri);
	    sendBroadcast(mediaScanIntent);
	}
	
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected void initDrawer(){
		mDrawerLayout = (DrawerLayout) findViewById(getDrawerLayoutID());
        mDrawerList = (ListView) findViewById(getDrawerListID());
        mDrawerList.setFastScrollEnabled(true);
        
        // set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
      
        ImageView bigPhoto = (ImageView)findViewById(getBigPhotoID());
        ImageView smallPhoto = (ImageView)findViewById(getSmallPhotoID());
      /*  bigPhoto.setLayoutParams(new FrameLayout.LayoutParams(
        			getResources().getDimensionPixelSize(R.dimen.drawer_photo_width),
        			getResources().getDimensionPixelSize(R.dimen.drawer_photo_width))); */
        bigPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
    /*    smallPhoto.setLayoutParams(new FrameLayout.LayoutParams(
    			getResources().getDimensionPixelSize(R.dimen.user_drawer_photo_width),
    			getResources().getDimensionPixelSize(R.dimen.user_drawer_photo_width))); */
        smallPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        bigPhoto.setImageBitmap(getBigPhoto()); 
        smallPhoto.setImageBitmap(getSmallPhoto());
        
        // Set the drawer listView
        setDrawerList();
        
        // Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        // Enable ActionBar app icon to behave as action to toggle nav drawer

        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ){
        	 public void onDrawerOpened(View view){
         		getActionBar().setTitle(getOnDrawerOpen());
         	}
         	 
         	 public void onDrawerClosed(View view){
         		 getActionBar().setTitle(getActionBarTitle());
         	 }
        };
        
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		
		if (mDrawerToggle != null && mDrawerToggle.onOptionsItemSelected(item)) return true;
		
		int itemId = item.getItemId();
		if (itemId == R.id.action_main_camera) {
			TakePicture();
			return true;
		} else if (itemId == R.id.action_main_options) {
			openSettings(); 
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    
	    unregisterReceiver(mWifiListener);
	}
	
	@Override
	public void onResume(){
		super.onResume();
		 
		registerReceiver(mWifiListener, new IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION));
		 
	}
	
    @Override
    protected void onStop(){
		super.onStop();
		
		if (mDrawerLayout != null) mDrawerLayout.closeDrawers();
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    // Sync the toggle state after onRestoreInstanceState has occurred.
	  if (mDrawerToggle != null)  mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    // Pass any configuration change to the drawer toggle
	    if (mDrawerToggle != null) mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(getMenuLayout(), menu);
		return true;
	}
	
	/**
	 * ListView Listener
	 * @author Princewill
	 *
	 */
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        //change accounts.
	    	Switch(position);
	    	
	    	mDrawerLayout.closeDrawers();
	    }
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	       
	    if (requestCode == CAMERA_ID && resultCode == RESULT_OK) {
	    	galleryAddPic();
	    	Toast.makeText(getApplicationContext(), "Image saved to:\n" + mImagePath, Toast.LENGTH_SHORT).show();
	    }
	}
	
	/**
	 * Method to call when the List Items in the Navigation Drawer are pressed
	 * @param pos - position of the clicked list item
	 */
	protected abstract void Switch(int pos);
	
	/**
	 * Method to get the Layout of the Menu
	 * @return - the menu layout
	 */
	protected abstract int getMenuLayout();
	
	/**
	 * Method to get the image ID for the Navigation Drawer
	 * @return - Big Photo ID
	 */
    protected abstract int getBigPhotoID();
    
    /**
     * Method to get the image ID for the Navigation Drawer
     * @return - Small Photo ID
     */
    protected abstract int getSmallPhotoID();
    
    /**
     * Method to get a bitmap for the Navigation Drawer
     * @return - Big Photo bitmap
     */
    protected abstract Bitmap getBigPhoto();
    
    /**
     * Method to get a bitmap for the Navigation Drawer
     * @return - Small Photo bitmap
     */
    protected abstract Bitmap getSmallPhoto();
    
    /**
     * Method to get the ID for the DrawerLayout
     * @return - DrawerLayout ID in the xml layout file
     */
    protected abstract int getDrawerLayoutID();
    
    /**
     * Method to get the ID of the ListView in the Navigation Drawer
     * @return - ListView ID in the xml layout file
     */
    protected abstract int getDrawerListID();
    
    /**
     * Method to get the name on the actionBar when the Drawer is opened
     * @return - name of actionBar when the drawer opens
     */
    protected abstract String getOnDrawerOpen();
    
    /**
     * Method to get the title of the Action Bar
     * @return - title of the action bar
     */
    protected abstract String getActionBarTitle();
    
    /**
     * Method to set the drawerList
     */
    protected abstract void setDrawerList();
    
    /**
     * Method to inflate the overflow menu
     */
    protected abstract void openSettings();

}
