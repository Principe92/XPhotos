package prince.app.sphotos;

import prince.app.sphotos.tools.CameraX;
import prince.app.sphotos.tools.DepthPageTransformer;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.INIT;
import prince.app.sphotos.tools.Util;
import prince.app.sphotos.util.ImageFetcherX;
import android.annotation.TargetApi;
import android.app.ActionBar;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
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
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager.LayoutParams;
import android.widget.PopupMenu;
import android.widget.Toast;

public class Gallery_Full_Activity extends FragmentActivity implements OnClickListener, 
OnMenuItemClickListener, android.widget.PopupMenu.OnMenuItemClickListener{
    public static final String EXTRA_IMAGE = "extra_image";
    private static final String TAG = Gallery_Full_Activity.class.getSimpleName();

    private ImagePagerAdapter mAdapter;
    private ViewPager mPager;
    private static int position;
    private static int length;
    private static int LAST_VIEWED_FULL_IMAGE;
    
    private ImageFetcherX mImageFetcher;
    private CameraX camera;



    static final int REQUEST_IMAGE_CAPTURE = 1;
	String mCurrentPhotoPath; 
	Cursor nCursor;
    

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_viewpager); // Contains just a ViewPager
        
        getExtras();
        initPager();
        
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        final int height = displayMetrics.heightPixels;
        final int width = displayMetrics.widthPixels;
        final int longest = (height > width ? height : width) / 2;
        
        INIT.getInstance().init_LocalPhotoFullCache(getApplicationContext(), 0.50f);
        setmImageFetcher(new ImageFetcherX(this, longest));
        getmImageFetcher().setLoadingImage(R.drawable.empty_photo);
        getmImageFetcher().addImageCache(this, INIT.getInstance().localphotofull_cache);
        getmImageFetcher().setImageFadeIn(false);

        
        camera = new CameraX(this, Util.GALLERY_CAMERA_REQUEST, Global.getInstance().createPath(Util.SOCIAL_PHOTOS_PATH));
    }
    
    public void getExtras(){
    	Intent i = getIntent();
        int[] arg = i.getExtras().getIntArray(Gallery_Main_Fragment.EXTRAS);
		position = arg[0];
		length = arg[1];
		Log.d(TAG, "Image Position: " + position);
		Log.d(TAG, "Image Count: " + length);
    }
    
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void initPager(){
    	 mAdapter = new ImagePagerAdapter(getSupportFragmentManager(), length);
		 mPager = (ViewPager) findViewById(R.id.pager);
		 mPager.setAdapter(mAdapter);
		 mPager.setPageTransformer(true, new DepthPageTransformer());
		 mPager.setCurrentItem(position); //set page to the image pressed
		 mPager.setOffscreenPageLimit(2);
		 mPager.setPageMargin((int) getResources().getDimension(R.dimen.image_detail_pager_margin));
		 
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
	            Toast.makeText(this, "Tap to reveal/hide menu", Toast.LENGTH_LONG).show();
	        }
    }
    
    @Override
    public void onBackPressed() {
    	Intent returnIntent = new Intent();
        returnIntent.putExtra(Util.LOCALPHOTO_RESULT,LAST_VIEWED_FULL_IMAGE);
        setResult(RESULT_OK,returnIntent);     
        finish();
    }
    
    @Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Util.GALLERY_CAMERA_REQUEST && resultCode == RESULT_OK) {
        	Bitmap imageBitmap = camera.getResult();
        	if (imageBitmap != null){
        		camera.galleryAddPic();
        		Toast.makeText(this,getResources().getString(R.string.text_image_saved), Toast.LENGTH_SHORT).show();
        	}
        }
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
            return Gallery_Full_Fragment.newInstance(pos);
        }
    }

	@Override
	public boolean onMenuItemClick(MenuItem item) {
	    int itemId = item.getItemId();
		if (itemId == R.id.action_set) {
			// TODO Set current image as:
			return true;
		} else if (itemId == R.id.action_slideshow) {
			// TODO Perform a slide show
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
			openOverflow();
			return true;
		}else {
			return super.onOptionsItemSelected(item);
		}
} 
	

	public void openOverflow(){
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
	}
	
	 @Override
	    protected void onDestroy() {
	        super.onDestroy();
	        mImageFetcher.closeCache();
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
    
    
	/**
	 * @return the mImageFetcher
	 */
	public ImageFetcherX getmImageFetcher() {
		return mImageFetcher;
	}

	/**
	 * @param mImageFetcher the mImageFetcher to set
	 */
	public void setmImageFetcher(ImageFetcherX mImageFetcher) {
		this.mImageFetcher = mImageFetcher;
	}
    
}
    




