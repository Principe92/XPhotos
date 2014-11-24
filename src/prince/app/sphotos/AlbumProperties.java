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

import java.util.regex.Pattern;

import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

/**
 * Fragment class that shows the details of a particular facebook album
 * @author Princewill Okorie
 *
 */
public class AlbumProperties extends DialogFragment{
	private static final String TAG = AlbumProperties.class.getSimpleName();
	private static final String POSITION = "position";
	private static int sAlbumGridPos;
	
	private TextView mAlbumName;
	private TextView mAlbumOwner;
	private TextView mAlbumSize;
	private TextView mAlbumType;
	private TextView mAlbumPrivacy;
	private TextView mAlbumCreated;
	private TextView mAlbumUpdated;
	
	private static final String HYPHEN = "-";
	private static final String COLON = ":";
	private static final String PLUS = "+";
	private static final String TIME_DATE_SPLIT = "T";
	private static final String DIVIDER = "/";
	
	private Button mOkay;
	
	public static AlbumProperties newInstance(int position){
		Bundle args = new Bundle ();
		args.putInt(POSITION, position);
		final AlbumProperties mNewDetails = new AlbumProperties();
		mNewDetails.setArguments(args);
		mNewDetails.setStyle(DialogFragment.STYLE_NO_TITLE, android.R.style.Theme_Holo_Light_Dialog);
		return mNewDetails;
	}
	
	@Override
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
		
		sAlbumGridPos = getArguments().getInt(POSITION);
		
		setRetainInstance(true);
	}
	
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle oldState){
		super.onCreateView(inflater, parent, oldState);
		View view = inflater.inflate(R.layout.album_details_fragment, parent, false);
		
		// set the album name
		mAlbumName = (TextView) view.findViewById(R.id.album_name);
		mAlbumName.setText(FBINIT.sAlbumsArray.get(sAlbumGridPos).mAlbumName);
		
		// set the album owner
		mAlbumOwner = (TextView) view.findViewById(R.id.album_owner);
		mAlbumOwner.setText(FBINIT.sAlbumsArray.get(sAlbumGridPos).mAlbumOwnerName);
		
		// set the album size
		mAlbumSize = (TextView) view.findViewById(R.id.album_count);
		mAlbumSize.setText(""+FBINIT.sAlbumsArray.get(sAlbumGridPos).mAlbumSize + " Photos");
		
		// set the album type
		mAlbumType = (TextView) view.findViewById(R.id.album_type);
		mAlbumType.setText(Global.capitalize(FBINIT.sAlbumsArray.get(sAlbumGridPos).mAlbumType));
		
		// set the album privacy
		mAlbumPrivacy = (TextView) view.findViewById(R.id.album_privacy);
		mAlbumPrivacy.setText(Global.capitalize(FBINIT.sAlbumsArray.get(sAlbumGridPos).mAlbumPrivacy));
		
		// set the album created time
		mAlbumCreated = (TextView) view.findViewById(R.id.album_ctime);
		mAlbumCreated.setText(setDate(FBINIT.sAlbumsArray.get(sAlbumGridPos).mAlbumCT));
		
		// set the album size
		mAlbumUpdated = (TextView) view.findViewById(R.id.album_utime);
		mAlbumUpdated.setText(setDate(FBINIT.sAlbumsArray.get(sAlbumGridPos).mAlbumUT));
		
		mOkay = (Button) view.findViewById(R.id.btn_OKAY);
		mOkay.setOnClickListener(new OnClickListener(){
			@Override
			public void onClick(View v) {
				getDialog().dismiss();
			}});
		
		getDialog().setTitle(FBINIT.sAlbumsArray.get(sAlbumGridPos).mAlbumName);
		return view;
	}
	
	private String setDate(String data){
		String [] timeDateSplit = data.split(TIME_DATE_SPLIT);  // separate date from time
		String date = timeDateSplit[0];							// select date
		String time = timeDateSplit[1];							// select time
		String [] dateSplit = date.split(HYPHEN);				// split date into year, month, day
		String [] timeSplit = time.split(COLON);				// split time into hour, minute, seconds
		String [] secondSplit = timeSplit[2].split(Pattern.quote(PLUS));		// split seconds
		
		return dateSplit[2] + DIVIDER + dateSplit[1] + DIVIDER + dateSplit[0] + " at " + timeSplit[0] + COLON + timeSplit[1] + COLON + secondSplit[0];
	}
	
}
