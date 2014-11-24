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

package prince.app.sphotos;

import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Util;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.util.Log;
import android.util.SparseArray;
import android.view.ActionMode;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.AbsListView.MultiChoiceModeListener;

public class MultipleChoice implements MultiChoiceModeListener {
	private final static String TAG = MultipleChoice.class.getSimpleName();
	private Activity mContext;
	private SparseArray<String> mImageList;
	
	public MultipleChoice(Activity activity) {
		this.mContext = activity;
		this.mImageList = new SparseArray<String>();
	}

	@TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	@Override
	public boolean onCreateActionMode(ActionMode mode, Menu menu) {
		// Inflate the menu for the CAB - Contextual Action Bar
		mode.setTitle("0");
        MenuInflater inflater = mode.getMenuInflater();
        inflater.inflate(R.menu.gallery_popmenu, menu);
        
		return true;
	}

	@Override
	public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
		int itemId = item.getItemId();
		if (itemId == R.id.action_share) {
			return true;
		}else if (itemId == R.id.action_details) {
			return true;
		}else if (itemId == R.id.action_select_all) {
			
			for (int i = 0; i<FBINIT.sImagesArray.size(); i++){
			/*	if (!mImageList.containsKey(i) || mImageList.get(i) == null){
					mImageList.put(i, FBINIT.sImagesArray.get(i).getImageID());
				} */
			}
			
			mode.setTitle("" + mImageList.size());
			Log.d(TAG, "imageList size: " + mImageList.size());
			return true;
		}else if (itemId == R.id.action_delete) {
			return true;
		}else if (itemId == R.id.action_save) {
			// download all selected images to external storage
			Log.d(TAG, "save image");
			for (int i=0; i<mImageList.size(); i++){
				Log.d(TAG, "http: " + mImageList.get(i));
				if (mImageList.get(i) != null){
				    Global.getInstance().downloadSaveToExternal(mImageList.get(i), Util.FACEBOOK_PATH, null, true);
				    Log.d(TAG, mImageList.get(i));
				    Log.d(TAG, "image " + i + " downloaded");
				}
			}
			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void onDestroyActionMode(ActionMode mode) {
		// TODO Auto-generated method stub

	}

	@Override
	public void onItemCheckedStateChanged(ActionMode mode, int position,
			long id, boolean checked) {

		if (checked){
				mImageList.put(position, FBINIT.sImagesArray.get(position).mImageURL);
				Log.d(TAG, "image at position: " + position + " added to array");
			}
		
		else{
		    mImageList.remove(position);
			Log.d(TAG, "image at position: " + position + " removed from array");
		}
		
		mode.setTitle("" + mImageList.size());
	}
	

}
