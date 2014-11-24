package prince.app.sphotos;

import prince.app.sphotos.Gallery.CacheService;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.INIT;
import prince.app.sphotos.tools.ImageAdapterX;
import prince.app.sphotos.tools.ImageAdapterX.LoadGridListener;
import prince.app.sphotos.tools.Util;
import prince.app.sphotos.ui.RecyclingImageView;
import prince.app.sphotos.util.ImageFetcherX;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.ActionMode;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

public class Gallery_Main_Fragment extends Fragment {
	private static final String TAG = Gallery_Main_Fragment.class.getSimpleName();
	public static final String EXTRAS = "extras";
	
	// Grid variables
	private static GridView grid;
	private static ImageAdapterX imageAdapter;
	private int imageSize, imageSpacing;
	
	// local variables
	private ImageFetcherX mImageFetcher; 
	public static int mImageCount = 0;
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retain this fragment across configuration changes.
		setRetainInstance(true);
		
		imageAdapter = new ImageAdapterX();
		imageAdapter.initListener(new LoadGridListener(){

			@Override
			public View getView(int position, View convertView, ViewGroup parent, GridView.LayoutParams params, int mItemHeight) {
				// Now handle the main ImageView thumbnails
		        ImageView imageView;
		        if (convertView == null) { // if it's not recycled, instantiate and initialize
		            imageView = new RecyclingImageView(getActivity());
		            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
		            imageView.setLayoutParams(params);
		        } else { // Otherwise re-use the converted view
		            imageView = (ImageView) convertView;
		        }
		
		        // Check the height matches our calculated column width
		        if (imageView.getLayoutParams().height != mItemHeight) {
		            imageView.setLayoutParams(params);
		            Log.d(TAG, "imageView height different from calculated height");
		        }
		
		        // Finally load the image asynchronously into the ImageView, this also takes care of
		        // setting a placeholder image while the background thread runs
		        mImageFetcher.loadImage(position, imageView, Util.LOCALPHOTO_THUMBNAIL_LOAD_ID, true);
		        return imageView;
			}

			@Override
			public int getGridSize() {
				Log.i(TAG, "Gallery Size: " + CacheService.getImageList(getActivity()).size());
				return CacheService.getImageList(getActivity()).size();
			}});
		
		INIT.getInstance().init_LocalPhotoCache(getActivity(), 0.25f);
		imageSize = getResources().getDimensionPixelSize(R.dimen.imageSize_100dp); 
		imageSpacing = getResources().getDimensionPixelSize(R.dimen.imageSpacing_1dp);
        mImageFetcher = new ImageFetcherX(getActivity(), imageSize);
        mImageFetcher.setLoadingImage(R.drawable.empty_photo);
        mImageFetcher.addImageCache(getActivity().getSupportFragmentManager(), INIT.getInstance().localphoto_cache);
      //  mImageFetcher.setContext(getActivity());
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.grid, container, false);
	    initGrid(view);
		
		return view;
	}
	
	private void initGrid(View view){
		grid = (GridView) view.findViewById(R.id.grid);
		grid.setClipToPadding(false);
		grid.setOnItemClickListener(new GridListener()); 
        grid.setAdapter(imageAdapter);
        grid.setVerticalScrollBarEnabled(true);
        grid.setFastScrollEnabled(true);
        grid.setVisibility(View.VISIBLE);
        
        grid.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
					@Override
                    public void onGlobalLayout() {
                        if (imageAdapter.getNumColumns() == 0) {

                        	final int numColumns = (int) Math.floor(
                        			grid.getWidth() / (imageSize + imageSpacing));
                        	if (numColumns > 0) {
                                final int columnWidth =
                                        (grid.getWidth() / numColumns) - imageSpacing;
                                imageAdapter.setNumColumns(numColumns);
                                imageAdapter.setItemHeight(columnWidth);

                                if (Global.hasJellyBean()) {
                                	grid.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                } else {
                                	grid.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                }
                            }
                        }
                    }
                });
     
        grid.setNumColumns(Global.numberOfColumns(getActivity(),grid, imageSize,imageSpacing));
        grid.setAdapter(imageAdapter);
	

        grid.setChoiceMode(GridView.CHOICE_MODE_MULTIPLE_MODAL);
        grid.setMultiChoiceModeListener(new MultiChoiceModeListener() {
        	@Override
            public void onItemCheckedStateChanged(ActionMode mode, int position,
                                                  long id, boolean checked) {
        	}

            @Override
            public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
                int itemId = item.getItemId();
				if (itemId == R.id.action_delete) {
					return true;
				} else if (itemId == R.id.action_share) {
					return true;
				}else if (itemId == R.id.action_details) {
					return true;
				}else if (itemId == R.id.action_select_all) {
					return true;
				} else {
					return false;
				}
            }
 
            @Override
            public boolean onCreateActionMode(ActionMode mode, Menu menu) {
                // Inflate the menu for the CAB
                MenuInflater inflater = mode.getMenuInflater();
                inflater.inflate(R.menu.gallery_popmenu, menu);
                MenuItem item = (MenuItem) menu.findItem(R.id.action_delete);
                item.setVisible(true);
                return true;
            }

            @Override
            public void onDestroyActionMode(ActionMode mode) {
                // Here you can make any necessary updates to the activity when
                // the CAB is removed. By default, selected items are deselected/unchecked.
            }

            @Override
            public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
                // Here you can perform updates to the CAB due to
                // an invalidate() request
                return false;
            }
        });
	}
	
	private class GridListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			
			Intent intent = new Intent(getActivity(), Gallery_Full_Activity.class);
			int arg[] = {position, mImageCount};
			intent.putExtra(EXTRAS, arg);
			startActivityForResult(intent,Util.LOCALPHOTO_FULLIMAGE_REQUEST_CODE);
		}
	}
	
	public static void onActivityResult(int result){
		Log.d(TAG, "called at: " + Global.time());
		grid.smoothScrollToPosition(result);
	}
	
	
}
