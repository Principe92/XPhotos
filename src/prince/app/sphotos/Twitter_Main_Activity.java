package prince.app.sphotos;

import prince.app.sphotos.tools.CameraX;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.ActivityX;
import prince.app.sphotos.tools.Util;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class Twitter_Main_Activity extends ActivityX {
	private static final String TAG = "Twitter";
	public static final String EXTRAS = "extras";
	private CameraX camera;
	
	
	@Override
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
		setContentView(getLayout());
		
		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(prince.app.sphotos.R.id.frame_fb_main_activity, new Twitter_Main_Fragment(), TAG);
			ft.commit();
		}
		
		// initialize the navigation drawer
		initDrawerView();
		
		// Initialize camera
		camera = new CameraX(this, Util.TWITTER_CAMERA_REQUEST, Global.getInstance().createPath(Util.SOCIAL_PHOTOS_PATH));
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == Util.TWITTER_CAMERA_REQUEST && resultCode == RESULT_OK) {
        	Bitmap imageBitmap = camera.getResult();
        	if (imageBitmap != null){
        		camera.galleryAddPic();
        		Toast.makeText(this,getResources().getString(R.string.text_image_saved), Toast.LENGTH_SHORT).show();
        		// TODO Refresh the grid's data
        	}
        }
	}

	@Override
	protected int getLayout() {
		return R.layout.fb_main_activity;
	}

	@Override
	protected int getDrawerLayoutId() {
		return R.id.drawer_fb_main_activity;
	}

	@Override
	protected int getDrawerListId() {
		return R.id.list_fb_main_activity;
	}

	@Override
	protected void SwitchAccount(int position) {
		if (position == 0){
			Intent launch = new Intent(this, Activity_Main.class);
			startActivity(launch);
		}
		
		if (position == 2){
			Intent launch = new Intent(this, Google_Main_Activity.class);
			startActivity(launch);
		}
		
		if (position == 3){
			Intent launch = new Intent(this, Gallery_Main_Activity.class);
			startActivity(launch);
		}
	}

	@Override
	protected int getMenuId() {
		return R.menu.fb_main;
	}

	@Override
	protected CameraX getCamera() {
		return camera;
	}

	@Override
	protected Bitmap getBigPhoto() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Bitmap getSmallPhoto() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected int getBigPhotoId() {
		return R.id.fb_main_drawer_big_photo;
	}

	@Override
	protected int getSmallPhotoId() {
		return R.id.fb_main_drawer_small_photo;
	}

	@Override
	protected String getBarTitle() {
		// TODO Auto-generated method stub
		return getResources().getString(R.string.title_twitter);
	}

	/* (non-Javadoc)
	 * @see prince.app.sphotos.tools.Navigation#SignOut()
	 */
	@Override
	protected void signOut() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void hideMenuItems(Menu menu) {
		// TODO Auto-generated method stub
		
	}

}
