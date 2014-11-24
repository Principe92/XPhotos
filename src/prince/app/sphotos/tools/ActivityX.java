/*
 * Copyright (C) 2013 The Android Open Source Project 
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at 
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */

package prince.app.sphotos.tools;

import java.util.ArrayList;
import java.util.List;

import prince.app.sphotos.R;
import prince.app.sphotos.SettingsActivity;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

/**
 * A Generic FragmentActivity extended by other classes that wants to implement a Drawer Navigation Type View Hierarchy
 * @author Princewill Okorie
 *
 */
public abstract class ActivityX extends FragmentActivity implements OnMenuItemClickListener{
	
	// drawer variables
	private DrawerLayout mDrawerLayout;
    private ListView mDrawerList;
    private List<Account> mAccounts;
    private ActionBarDrawerToggle mDrawerToggle;
    
    // local variables
    private static final Integer[] socialIcons = { R.drawable.fb,
        R.drawable.ic_action_twitter_logo_blue, R.drawable.btn_gp, R.drawable.ic_launcher_gallery };
    
    private static final String TAG = ActivityX.class.getSimpleName();
    
    private PopupMenu mPopup;
    
    private int  mFinish = 2;
	
    /**
     * Retrieves the layout of the Activity extending this class
     * @return <i>R.layout.layout_name</i>
     */
    protected abstract int getLayout();
    
    /** 
     * Retrieves the id of the Drawer Layout
     * @return <i>R.id.drawerlayout_id</i>
     */
    protected abstract int getDrawerLayoutId();
    
    /** 
     * Retrieves the id of the ListView of the Drawer Layout
     * @return <i>R.id.listview_id</i>
     */
    protected abstract int getDrawerListId();
    
    /**
     *  This function is overridden by classes extending this class. <br>
     *  This function is called once an item is selected from the drawer list view
     */
    protected abstract void SwitchAccount(int position);
    
    /**
     * Retrieves the layout of the Action Bar Menu
     * @return <i>R.menu.example</i>
     */
    protected abstract int getMenuId();
    
    /** Retrieves the camera object */
    protected abstract CameraX getCamera();
    
    /** get the drawer big image */
    protected abstract Bitmap getBigPhoto();
    
    /** get the drawer small image */
    protected abstract Bitmap getSmallPhoto();
    
    /** get the imageView id of drawer big photo */
    protected abstract int getBigPhotoId();
    
    /** get the imageView id of drawer small photo */
    protected abstract int getSmallPhotoId();
    
    /** get the title on the action bar */
    protected abstract String getBarTitle();
    
    /** Sign Out from the account */
    protected abstract void signOut();
    
    protected abstract void hideMenuItems(Menu menu);
    
    
	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	protected void initDrawerView(){
        mDrawerLayout = (DrawerLayout) findViewById(getDrawerLayoutId());
        mDrawerList = (ListView) findViewById(getDrawerListId());
        mDrawerList.setFastScrollEnabled(true);
        
        //set a custom shadow that overlays the main content when the drawer opens
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);
      
        ImageView bigPhoto = (ImageView)findViewById(getBigPhotoId());
        ImageView smallPhoto = (ImageView)findViewById(getSmallPhotoId());
    /*    bigPhoto.setLayoutParams(new RelativeLayout.LayoutParams(
        			getResources().getDimensionPixelSize(R.dimen.drawer_photo_width),
        			getResources().getDimensionPixelSize(R.dimen.drawer_photo_width))); */
        bigPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
      /*  smallPhoto.setLayoutParams(new RelativeLayout.LayoutParams(
    			getResources().getDimensionPixelSize(R.dimen.user_drawer_photo_width),
    			getResources().getDimensionPixelSize(R.dimen.user_drawer_photo_width)));*/
        smallPhoto.setScaleType(ImageView.ScaleType.CENTER_CROP);
        
        bigPhoto.setImageBitmap(getBigPhoto()); 
        smallPhoto.setImageBitmap(getSmallPhoto());
        
        String[] social = getResources().getStringArray(R.array.social_accounts);
        
        //Set the adapter for the list view
        mAccounts = new ArrayList<Account>();
        for (int i = 0; i < social.length; i++) {
            Account item = new Account(socialIcons[i], social[i]);
            mAccounts.add(item);
        }
        
        // set the ListViewAdapter
        mDrawerList.setAdapter(new ListViewListener(this,R.layout.social_media_accounts, mAccounts));
        
        //Set the list's click listener
        mDrawerList.setOnItemClickListener(new DrawerItemClickListener());
        
        //enable ActionBar app icon to behave as action to toggle nav drawer
        mDrawerToggle = new ActionBarDrawerToggle(
                this,                  /* host Activity */
                mDrawerLayout,         /* DrawerLayout object */
                R.drawable.ic_drawer,  /* nav drawer image to replace 'Up' caret */
                R.string.drawer_open,  /* "open drawer" description for accessibility */
                R.string.drawer_close  /* "close drawer" description for accessibility */
                ){
        	
        	 public void onDrawerOpened(View view){
        		getActionBar().setTitle("Accounts");
        	}
        	 
        	 public void onDrawerClosed(View view){
        		 getActionBar().setTitle(getBarTitle());
        	 }
        };
        
        mDrawerLayout.setDrawerListener(mDrawerToggle);
        
        getActionBar().setDisplayHomeAsUpEnabled(true);
        getActionBar().setHomeButtonEnabled(true);
	}
	
	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
	    super.onPostCreate(savedInstanceState);
	    // Sync the toggle state after onRestoreInstanceState has occurred.
	    mDrawerToggle.syncState();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
	    super.onConfigurationChanged(newConfig);
	    // Pass any configuration change to the drawer toggle
	    mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(getMenuId(), menu);
		return true;
	}
	
	private class DrawerItemClickListener implements ListView.OnItemClickListener {
	    @Override
	    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
	        //change accounts.
	    	SwitchAccount(position);
	    	
	    	mDrawerLayout.closeDrawers();
	    }
	}
	
	// Called when an item on the actionBar is clicked
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		if (mDrawerToggle.onOptionsItemSelected(item)) {
            return true;
        }
		
		int itemId = item.getItemId();
		if (itemId == R.id.action_main_camera) {
			getCamera().TakePicture();
			return true;
		} else if (itemId == R.id.action_main_options) {
			openSettings(); 
			return true;
		} else {
			return super.onOptionsItemSelected(item);
		}
	} 

	public void openSettings(){
		View view = findViewById(R.id.action_main_options);
		mPopup = new PopupMenu(this, view);
		mPopup.setOnMenuItemClickListener(this);
		MenuInflater inflater = mPopup.getMenuInflater();
		inflater.inflate(R.menu.fb_main_overflow, mPopup.getMenu());
		hideMenuItems(mPopup.getMenu());
		mPopup.show();
	}
	
	// Called when an item on the popUp MenuItem is pressed
	@Override
	public boolean onMenuItemClick(MenuItem item) {
	    int itemId = item.getItemId();
		if (itemId == R.id.action_logout){
			// Sign out from social account
			signOut();
	        return true;
		}
		if (itemId == R.id.action_settings) {
			// Open the settings menu
			Intent intent = new Intent();
	        intent.setClass(this, SettingsActivity.class);
	        startActivityForResult(intent, 0); 
	        Log.d(TAG, "settings called");
			return true;
		}
		else {
			return false;
		}
	}
	
	public  boolean isWifi(){
		ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
		return activeNetworkInfo != null && activeNetworkInfo.isConnected();
	}

	@TargetApi(Build.VERSION_CODES.JELLY_BEAN)
	@Override
	public void onBackPressed(){
		mFinish = mFinish - 1;
		if (mFinish == 1)
			Toast.makeText(this, "Press again to exit", Toast.LENGTH_LONG).show();;
		if (mFinish == 0){
			Intent intent = new Intent(Intent.ACTION_MAIN);
			intent.addCategory(Intent.CATEGORY_HOME);
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
	
	@Override
	public void onResume(){
		super.onResume();
		ImageView bigPhoto = (ImageView)findViewById(getBigPhotoId());
		bigPhoto.setImageBitmap(getBigPhoto());
	}
}
