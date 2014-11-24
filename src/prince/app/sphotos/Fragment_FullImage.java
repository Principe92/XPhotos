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
import prince.app.sphotos.util.ImageWorker;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Fragment_FullImage extends Fragment {
	private static final String IMAGE_DATA_EXTRA = "resId";
	private int mImageNum;
	private ImageView mImageView;


	public static Fragment_FullImage newInstance(int imageNum) {
		final Fragment_FullImage fragment = new Fragment_FullImage();
		
		final Bundle args = new Bundle();
		args.putInt(IMAGE_DATA_EXTRA, imageNum);
		fragment.setArguments(args);
		
		return fragment;
	}

	// Empty constructor, required as per Fragment docs
	public Fragment_FullImage() {}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setRetainInstance(true);
		
		mImageNum = getArguments() != null ? getArguments().getInt(IMAGE_DATA_EXTRA) : -1;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// image_detail_fragment.xml contains just an ImageView
		final View view = inflater.inflate(R.layout.gallery_full_main, container, false);
		mImageView = (ImageView) view.findViewById(R.id.imageView);
		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		if (Activity_FullImage.class.isInstance(getActivity())) {
			// Call out to ImageDetailActivity to load the bitmap in a background thread
			String address;
			synchronized (FBINIT.sImagesArray) {
				  address = FBINIT.sImagesArray.get(mImageNum).mImageURL;
			}
			((Activity_FullImage) getActivity()).getmImageFetcher().loadImage(address, mImageView, Util.FACEBOOK_FULL_IMAGE_LOAD_ID);
		}
		
		// Pass clicks on the ImageView to the parent activity to handle
        if (OnClickListener.class.isInstance(getActivity()) && Global.hasHoneycomb()) {
            mImageView.setOnClickListener((OnClickListener) getActivity());
        }
	}

	@Override
    public void onDestroy() {
        super.onDestroy();
        if (mImageView != null) {
            // Cancel any pending image work
            ImageWorker.cancelWork(mImageView);
            mImageView.setImageDrawable(null);
        }
    }

}

