package prince.app.sphotos;

import prince.app.sphotos.tools.ActivityGeneric;
import prince.app.sphotos.tools.Util;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.view.View;
import android.widget.AdapterView;

public class Offline_Image_Activity extends ActivityGeneric{
	private static final String TAG = Offline_Image_Activity.class.getSimpleName();
	
	@Override
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
		
		if (getSupportFragmentManager().findFragmentByTag(TAG) == null) {
			final FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			ft.add(R.id.frame_fb_album_activity, new Offline_Image_Fragment(), TAG);
			ft.commit();
		}
	}

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,long id) {}

	@Override
	public int getActivityLayout() {
		return R.layout.fb_album_activity;
	}

	@Override
	public int getMenuLayout() {
		return R.menu.fb_main;
	}

	@Override
	public boolean initDrawer() {
		return false;
	}

	@Override
	protected void SwitchAccount(int position) {}

	@Override
	protected String getBarTitle() {
		return getResources().getString(R.string.offline_image_activity);
	}

	@Override
	protected int getCamID() {
		return Util.FACEBOOK_CAMERA_REQUEST;
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
	protected Bitmap getBigPhoto() {
		return null;
	}

	@Override
	protected Bitmap getSmallPhoto() {
		return null;
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
	protected int getFrameID() {
		return R.id.frame_fb_album_activity;
	}

	@Override
	protected void refreshGrid() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void clearCache() {
		// TODO Auto-generated method stub
		
	}

}
