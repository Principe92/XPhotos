package prince.app.sphotos;

import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.ImageAdapterX;
import prince.app.sphotos.tools.ImageAdapterX.LoadGridListener;
import prince.app.sphotos.ui.RecyclingImageView;
import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ImageView;

public class Offline_Image_Fragment extends Fragment {
	private static final String TAG = Offline_Image_Fragment.class.getSimpleName();
	
	private static GridView sGrid;
	private static ImageAdapterX imageAdapter;
	private int imageSize, imageSpacing;
	
	
	@Override
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
		
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
		        
		     //   mImageFetcher.loadImage(position, imageView, Util.LOCALPHOTO_THUMBNAIL_LOAD_ID);
		        return imageView;
			}

			@Override
			public int getGridSize() {
				return 10;
			}});
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle oldState){
		super.onCreateView(inflater, parent, oldState);
		View view = inflater.inflate(R.layout.grid, parent, false);
		
		initGrid(view);
		
		return view;
	}
	
	private void initGrid(View view){
		sGrid = (GridView) view.findViewById(R.id.grid);
		sGrid.setClipToPadding(false);
		sGrid.setOnItemClickListener(new GridListener()); 
        sGrid.setAdapter(imageAdapter);
        sGrid.setVerticalScrollBarEnabled(true);
        sGrid.setFastScrollEnabled(true);
        
        
        sGrid.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
					@Override
                    public void onGlobalLayout() {
                        if (imageAdapter.getNumColumns() == 0) {

                        	final int numColumns = (int) Math.floor(
                        			sGrid.getWidth() / (imageSize + imageSpacing));
                        	if (numColumns > 0) {
                                final int columnWidth =
                                        (sGrid.getWidth() / numColumns) - imageSpacing;
                                imageAdapter.setNumColumns(numColumns);
                                imageAdapter.setItemHeight(columnWidth);

                                if (Global.hasJellyBean()) {
                                	sGrid.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                } else {
                                	sGrid.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                }
                            }
                        }
                    }
                });
     
        sGrid.setNumColumns(Global.numberOfColumns(getActivity(),sGrid, imageSize,imageSpacing));
        sGrid.setAdapter(imageAdapter);
	}
	
	private class GridListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				
		}
	}
	
	public static void onActivityResult(int result){
		Log.d(TAG, "called at: " + Global.time());
		 sGrid.smoothScrollToPosition(result);
	}

}
