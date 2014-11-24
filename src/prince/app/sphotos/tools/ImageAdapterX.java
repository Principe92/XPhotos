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

import android.annotation.TargetApi;
import android.os.Build;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridLayout.LayoutParams;
import android.widget.GridView;

/**
 * A Public class that extends the BaseAdapter. It has a listener that triggers when images are to be populated to a grid
 * @author Princewill Okorie
 *
 */
public class ImageAdapterX extends BaseAdapter{
    private int mItemHeight = 0;
    private int mNumColumns = 0;
    private GridView.LayoutParams mImageViewLayoutParams;
    private LoadGridListener mLoadGrid;
	
	public void initListener(LoadGridListener context){
		mLoadGrid = context;
	}
	
	public interface LoadGridListener{
		public View getView(int position, View convertView, ViewGroup parent, GridView.LayoutParams params, int mItemHeight);
		public int getGridSize();
	}

    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public ImageAdapterX() {
        super();
        mImageViewLayoutParams = new GridView.LayoutParams(
                LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);
    }

	@Override
	public int getCount() {
        // If columns have yet to be determined, return no items
        if (getNumColumns() == 0) {
            return 0;
        }

        // Size + number of columns for top empty row
        return mLoadGrid.getGridSize();
	}
	
	@Override
    public Object getItem(int position) {
        return position < mNumColumns ?
                null : position - mNumColumns;
    }

    @Override
    public long getItemId(int position) {
        return position < mNumColumns ? 0 : position - mNumColumns;
    }

    @Override
    public int getViewTypeCount() {
        // Two types of views, the normal ImageView and the top row of empty views
        return 1;
    }

    @Override
    public int getItemViewType(int position) {
        return (position < mNumColumns) ? 1 : 0;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }
	
    public void setNumColumns(int numColumns) {
        mNumColumns = numColumns;
    }

    public int getNumColumns() {
        return mNumColumns;
    }
    
    /**
     * Sets the item height. Useful for when we know the column width so the height can be set
     * to match.
     *
     * @param height
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	public void setItemHeight(int height) {
        if (height == mItemHeight) {
            return;
        }
        mItemHeight = height;
        mImageViewLayoutParams =
                new GridView.LayoutParams(LayoutParams.MATCH_PARENT, mItemHeight);
        notifyDataSetChanged();
    }

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		return mLoadGrid.getView(position, convertView, parent, mImageViewLayoutParams, mItemHeight);
	}

}
