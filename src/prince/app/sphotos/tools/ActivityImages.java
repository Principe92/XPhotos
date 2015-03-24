package prince.app.sphotos.tools;

import java.util.ArrayList;

import prince.app.sphotos.AlbumProperties;
import prince.app.sphotos.FragmentX;
import prince.app.sphotos.R;
import prince.app.sphotos.SettingsActivity;
import android.content.Intent;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.ArrayAdapter;
import android.widget.PopupMenu;

public abstract class ActivityImages extends ActivityBase implements FragmentX.FragmentXListener{
	private static final String TAG = ActivityImages.class.getSimpleName();
	private static final String PROPERTIES = "album_details";
	
	private PopupMenu mOverflowMenu;
	
	protected void setStub(int origId, int finId){
		ViewStub myStub = ((ViewStub) findViewById(origId));
		
		if (myStub != null) myStub.setVisibility(View.VISIBLE);
		else ((View) findViewById(finId)).setVisibility(View.VISIBLE);
	}
	
	
	protected boolean isStubVisible(int origId, int finId){
		ViewStub myStub = ((ViewStub) findViewById(origId));
		
		if (myStub != null) return myStub.getVisibility() == View.VISIBLE;
		else return ((View) findViewById(finId)).getVisibility() == View.VISIBLE;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    int itemId = item.getItemId();
	    
		if (itemId == R.id.action_settings) {
			// Open the settings menu
			Intent intent = new Intent();
	        intent.setClass(this, SettingsActivity.class);
	        startActivityForResult(intent, 0); 
			return true;
		} else if (itemId == R.id.action_sync) {
			// Refresh the grid data
			refreshGrid();
			return true;
		} else if (itemId == R.id.action_details) {
			//Inflate the details dialog
			FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
			Fragment aX = getSupportFragmentManager().findFragmentByTag(PROPERTIES);
			if (aX != null) ft.remove(aX);
			ft.addToBackStack(null);
			
			DialogFragment aY = AlbumProperties.newInstance(getIndex());
			aY.show(ft, PROPERTIES);
			return true;
		} else {
			return false;
		}
	}
	
	@Override
	public void openSettings(){
		/*	View view = findViewById(R.id.action_main_options);
			mOverflowMenu = new PopupMenu(this, view);
			mOverflowMenu.setOnMenuItemClickListener(this);
			MenuInflater inflater = mOverflowMenu.getMenuInflater();
			inflater.inflate(R.menu.fb_main_overflow, mOverflowMenu.getMenu());
			
			// hide logOut
			mOverflowMenu.getMenu().findItem(R.id.action_logout)
			.setVisible(false)
			.setEnabled(false);
			
			hideShowMenu(mOverflowMenu.getMenu());
			
			mOverflowMenu.show(); */
		}
	
	@Override
	protected void setDrawerList(){
		if (!FBINIT.isAlbumEmpty()){
	        
        	//Set the adapter for the list view
        	ArrayList<String> albumNames = new ArrayList<String>();
        	int albumSize = FBINIT.sAlbumsArray.size();
        	for (int i = 0; i < albumSize; i++) {
        		albumNames.add(FBINIT.sAlbumsArray.get(i).mAlbumName);
        	}
        
        	// Set the adapter for the list view
        	mDrawerList.setAdapter(new ArrayAdapter<String>(this, R.layout.fb_drawer_album_list, albumNames)); 
        }
	}
	
    /**
     * Method called when we want to refresh the data set of a View
     */
    protected abstract void refreshGrid();
    
    /**
     * Method to hide any unnecessary menu item
     * @param menu - Menu Item to hide
     */
    protected abstract void hideShowMenu(Menu menu);
    
    /**
     * Method to get the array index of the currently displayed album
     * @return - The index position of the array
     */
    protected abstract int getIndex();

}
