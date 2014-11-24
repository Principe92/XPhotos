package prince.app.sphotos;

import prince.app.sphotos.bgtask.FetchData;
import prince.app.sphotos.bgtask.FetchData.WifiListener;
import prince.app.sphotos.tools.ActivityImages;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Util;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Toast;

public class Activity_Tag extends ActivityImages{
	private static final String TAG = Activity_Tag.class.getSimpleName();
	private static final String TAG_FRAGMENT = "tag_upload";
	private static final String GRID_VISIBILITY = "gridViewVisibility";
	public static final String FB_PHOTOS_INTENT = "fb_photos";

	
	public static final int LAUNCH_FULL_IMAGE = 390;
	private static final int CLICKED_ALBUM_POSITION = 510;
	private static final int CONNECT = 100;
	
	private int mLaunchKey;
	
	@Override
	public void onCreate(Bundle oldState){
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		super.onCreate(oldState);
		setContentView(R.layout.fb_album_activity);
		
    	Intent intent = getIntent();
    	mLaunchKey = intent.getExtras().getInt("address");
    	Log.e(TAG, "Launch Key: " + mLaunchKey);
    	
    	// Name the Action Bar
    	getActionBar().setTitle((mLaunchKey == Util.UPLOADED_PHOTO_LAUNCH_CODE)? "Uploaded" : "Tagged");
		
    	if (oldState == null){
    		getSupportFragmentManager().beginTransaction()
    		.add(R.id.frame_fb_album_activity, Fragment_Tag.newInstance(mLaunchKey), TAG_FRAGMENT)
    		.commit();
    	}
        
        
        if (oldState != null){
    		boolean visible = oldState.getBoolean(GRID_VISIBILITY);
			
    		Log.i(TAG, "GridView was Visible before screen orientation: " + visible);
    		Fragment_Tag frag = (Fragment_Tag) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    		if (frag != null){
    			frag.setmGridVisible(visible);
    		}
        }
        
        initListener();
        
        // Set Top Left Back Icon
        getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	
	private void initListener(){
		// connection
	    FetchData.setListener(new WifiListener(){

			@Override
			public void onConnection() {
				Log.e(TAG, "Connection RESTORED");
				
				if (isStubVisible(R.id.stub_internet, R.id.viewstub_internet)) retryConnection(null);
				
			}});
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == android.R.id.home) {
			NavUtils.navigateUpFromSameTask(this);
			return true;
		}else {
			return super.onOptionsItemSelected(item);
		}
	}
	
	@Override
	public void onSaveInstanceState(Bundle state) {
		super.onSaveInstanceState(state);
		
		Fragment_Tag frag = (Fragment_Tag) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
		if (frag != null){
			state.putBoolean(GRID_VISIBILITY, frag.isGridViewVisible());
		}
	}
	
	 @Override
	 public void onActivityResult(int requestCode, int resultCode, Intent data) {
		 super.onActivityResult(requestCode, resultCode, data);
		 
		 if (requestCode == CLICKED_ALBUM_POSITION && resultCode == RESULT_OK) {
			 Fragment_Albums.onActivityResult(data.getExtras().getInt("album_position"));
		 }
	 }
	 
	 public void retryConnection(View view){
		 if (Global.getInstance().isConnection()){
				
			 getSupportFragmentManager().beginTransaction()
			 .replace(R.id.frame_fb_album_activity, Fragment_Tag.newInstance(mLaunchKey), TAG_FRAGMENT)
			 .commit();
		 }
	 }
		
	 public void checkConnection(View view){
		 startActivityForResult(new Intent(WifiManager.ACTION_PICK_WIFI_NETWORK), CONNECT);
	 }

	@Override
	public int getMenuLayout() {
		return R.menu.fb_main;
	}

	@Override
	protected void Switch(int position) {}

	@Override
	protected Bitmap getBigPhoto() {
		return null;
	}

	@Override
	protected Bitmap getSmallPhoto() {
		return null;
	}

	@Override
	protected int getBigPhotoID() {
		return 0;
	}

	@Override
	protected int getSmallPhotoID() {
		return 0;
	}

	@Override
	protected int getDrawerLayoutID() {
		return 0;
	}

	@Override
	protected int getDrawerListID() {
		return 0;
	}
	
	@Override
	protected String getActionBarTitle(){
		return getResources().getString(R.string.title_albums);
	}
	

	@Override
	protected void refreshGrid() {
		if (Global.getInstance().isConnection()){
			Fragment_Tag frag = (Fragment_Tag) getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
    		if (frag != null){
    			frag.refreshData();
    		}
		}
		else
			Toast.makeText(this, "No network connection", Toast.LENGTH_SHORT).show();
		
	}

	@Override
	public void onDbError() {
		Global.hideRemoveFrag(this, Global.findFrag(this, TAG_FRAGMENT), true);
		
		setStub(R.id.stub_internet, R.id.viewstub_internet);
		
	}

	@Override
	protected String getOnDrawerOpen() {
		return null;
	}

	@Override
	protected void hideShowMenu(Menu menu) {
		// hide details
		menu.findItem(R.id.action_details)
		.setVisible(false)
		.setEnabled(false);
		
	}

	@Override
	protected int getIndex() {
		return 0;
	}
		
		
}
