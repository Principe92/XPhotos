package prince.app.sphotos;

import prince.app.sphotos.tools.CameraX;
import prince.app.sphotos.tools.DepthPageTransformer;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Util;
import prince.app.sphotos.util.ImageFetcher;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NavUtils;
import android.support.v4.view.ViewPager;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupMenu;
import android.widget.PopupMenu.OnMenuItemClickListener;
import android.widget.Toast;

public class Activity_FullImage extends FragmentActivity implements OnClickListener, OnMenuItemClickListener{
    public static final String EXTRA_IMAGE = "extra_image";
    public static final String LAST_VIEWED_FULL = "imageFull";
    private static final String TAG = Activity_FullImage.class.getSimpleName();
    
    private ImagePagerAdapter mAdapter;
    private ViewPager mPager;
    private static int position;
    private static int length;
    private  static int LAST_VIEWED_FULL_IMAGE;
    
    private ImageFetcher mImageFetcher;


    
    private CameraX camera;

    static final int REQUEST_IMAGE_CAPTURE = 1;
	String mCurrentPhotoPath; 
	Cursor nCursor;
	
    

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.gallery_viewpager); // Contains just a ViewPager
        setProgressBarIndeterminate(true);
        
        getExtras();
        initPager();
        
        // calculate height and width of screen for adequate image resizing
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;
        final int longest = (height > width ? height : width) / 2;
        
        Log.e(TAG, "Size: " + longest);
        Log.e(TAG, "Height: " + height);
        Log.e(TAG, "Width: " + width);
        
        FBINIT.getInstance().init_FullImageCache(getApplicationContext(), 0.25f);
        setmImageFetcher(new ImageFetcher(this, longest));
        getmImageFetcher().addImageCache(this, FBINIT.getInstance().fullImageCache);
        getmImageFetcher().setImageFadeIn(false);
        
        camera = new CameraX(this, Util.FACEBOOK_CAMERA_REQUEST, Global.getInstance().createPath(Util.SOCIAL_PHOTOS_PATH));
    }
    
    private void getExtras(){
    	Intent i = getIntent();
        int[] arg = i.getExtras().getIntArray(Activity_Photos.IMAGE_DETAILS);
        String name = i.getExtras().getString(Activity_Photos.ALBUM_NAME);
        getActionBar().setTitle(name);			// Set the title bar to the album name
		position = arg[0];
		length = arg[1];
    }
    
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void initPager(){
    	 mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), length);
		 mPager = (ViewPager) findViewById(R.id.pager);
		 mPager.setAdapter(mAdapter);
		 mPager.setPageTransformer(true, new DepthPageTransformer());
		 mPager.setPageMargin((int) getResources().getDimension(R.dimen.image_detail_pager_margin));
		 mPager.setCurrentItem(position); //set page to the image pressed
		 mPager.setOffscreenPageLimit(2);
		 
		 // Set up activity to go full screen
	        getWindow().addFlags(LayoutParams.FLAG_FULLSCREEN);

	        // Enable some additional newer visibility and ActionBar features to create a more
	        // immersive photo viewing experience
	        if (Global.hasHoneycomb()) {
	            final ActionBar actionBar = getActionBar();

	            // Hide title text and set home as up
	            actionBar.setDisplayHomeAsUpEnabled(true);

	            // Hide and show the ActionBar as the visibility changes
	            mPager.setOnSystemUiVisibilityChangeListener(
	                    new View.OnSystemUiVisibilityChangeListener() {
	                        @Override
	                        public void onSystemUiVisibilityChange(int vis) {
	                            if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
	                                actionBar.hide();
	                            } else {
	                                actionBar.show();
	                            }
	                        }
	                    });

	            // Start low profile mode and hide ActionBar
	            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
	            actionBar.hide();
	            Toast.makeText(this, "Tap to reveal or hide bar", Toast.LENGTH_SHORT).show();
	        }
    }
    
    @Override
    public void onBackPressed() {
    	Intent returnIntent = new Intent();
        returnIntent.putExtra(LAST_VIEWED_FULL, LAST_VIEWED_FULL_IMAGE);
        setResult(RESULT_OK,returnIntent);     
        finish();
    }
 	
    public static class ImagePagerAdapter extends FragmentStatePagerAdapter {
        private final int mSize;

        public ImagePagerAdapter(FragmentManager fm, int size) {
            super(fm);
            mSize = size;
        }

        @Override
        public int getCount() {
            return mSize;
        }

        @Override
        public Fragment getItem(int pos) {
        	LAST_VIEWED_FULL_IMAGE = pos;
            return Fragment_FullImage.newInstance(pos);
        }
    }

	@Override
	public boolean onMenuItemClick(MenuItem item) {
	    int itemId = item.getItemId();
		if (itemId == R.id.action_set) {
			//Set current image as:
			return true;
		} else if (itemId == R.id.action_slideshow) {
			// Perform a slideshow
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.fb_main, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		} else if (itemId == R.id.action_main_camera) {
			camera.TakePicture();
			return true;
		} else if (itemId == R.id.action_main_options) {
			openSettings();
			return true;
		}else {
			return super.onOptionsItemSelected(item);
		}
} 
	

	public void openSettings(){
		View view = findViewById(R.id.action_main_options);
	    PopupMenu popup = new PopupMenu(this, view);
	    popup.setOnMenuItemClickListener(this);
	    MenuInflater inflater = popup.getMenuInflater();
	    inflater.inflate(R.menu.full_image, popup.getMenu());
	    popup.show(); 
}

	@Override
	public void onResume(){
		super.onResume();
		mImageFetcher.setExitTasksEarly(false);
	}
	
	@Override
    protected void onPause() {
        super.onPause();
        mImageFetcher.setExitTasksEarly(true);
        mImageFetcher.flushCache();
    }
	
	@Override
    protected void onDestroy() {
        super.onDestroy();
        mImageFetcher.closeCache();
    }

	/**
	 * @return the mImageFetcher
	 */
	public ImageFetcher getmImageFetcher() {
		return mImageFetcher;
	}

	/**
	 * @param mImageFetcher the mImageFetcher to set
	 */
	public void setmImageFetcher(ImageFetcher mImageFetcher) {
		this.mImageFetcher = mImageFetcher;
	}
	
	/**
     * Set on the ImageView in the ViewPager children fragments, to enable/disable low profile mode
     * when the ImageView is touched.
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    @Override
    public void onClick(View v) {
        final int vis = mPager.getSystemUiVisibility();
        if ((vis & View.SYSTEM_UI_FLAG_LOW_PROFILE) != 0) {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_VISIBLE);
        } else {
            mPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE);
        }
    }
    
}
    




