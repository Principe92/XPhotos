package prince.app.sphotos;

import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.ImageAdapterX;
import prince.app.sphotos.tools.ImageAdapterX.LoadGridListener;
import prince.app.sphotos.tools.MultiSwipeRefreshLayout;
import prince.app.sphotos.util.ImageCache.ImageCacheParams;
import android.annotation.TargetApi;
import android.app.Activity;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.ProgressBar;

public abstract class FragmentX extends Fragment{
	private static String TAG;
	
	private static GridView mGridView;
	private ImageAdapterX imageAdapter;
	private boolean mGridVisible;
	private MultiSwipeRefreshLayout mSwipeRefreshLayout;
	private ProgressBar mProgress;
	
	protected FragmentXListener mCallback;

	    // Container Activity must implement this interface
	    public interface FragmentXListener {
	        public void onDbError();
	    }

	    @Override
	    public void onAttach(Activity activity) {
	        super.onAttach(activity);
	        
	        // This makes sure that the container activity has implemented
	        // the callback interface. If not, it throws an exception
	        try {
	            mCallback = (FragmentXListener) activity;
	        } catch (ClassCastException e) {
	            throw new ClassCastException(activity.toString()
	                    + " must implement OnHeadlineSelectedListener");
	        }
	    }

	
	@Override
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
		
		setRetainInstance(true);
		
		TAG = getClassName();
		mGridVisible = !isSwipeRefresheable();
		
		onCreateInit();
        
		imageAdapter = new ImageAdapterX();
		imageAdapter.initListener(new LoadGridListener(){

			@Override
			public View getView(int position, View convertView, ViewGroup parent, GridView.LayoutParams params, int mItemHeight) {
				return getViewX(position, convertView, parent, params, mItemHeight);
			}

			@Override
			public int getGridSize() {
				return getGridSizeX();
			}});
		
		
		Log.e(TAG, "NEWLY CREATED - - - - -");
		
	}
	
	public static void onActivityResult(int pos) {
		Log.e(TAG, "onActivityResult: Smooth scroll !!!");
		 mGridView.smoothScrollToPosition(pos);
	 }
	
	protected boolean isOnline(){
		return getActivity() != null;
	}
	
	protected abstract void onCreateInit();
	
	/**
	 * Method called when we when to load new data to the GridView
	 */
	protected abstract void refreshData();
	
	protected abstract void personalGridInit();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle oldState){
		super.onCreateView(inflater, parent, oldState);
		View view = inflater.inflate(getFragmentLayout(), parent, false);
		
		setupView(view);
		
		return view;
	}
	
	@Override
	public void onViewCreated(View view, Bundle oldeState) {
		super.onViewCreated(view, oldeState);
		
		if (isSwipeRefresheable()){
	        
			// Tell the MultiSwipeRefreshLayout which views are swipeable.
			mSwipeRefreshLayout.setSwipeableChildren(viewToSwipe());
		        
			mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
				@Override
				public void onRefresh() {
					refreshData();
					Log.i(TAG, "onRefresh called from SwipeRefreshLayout");
				}
			});
		}
	}
	
	private void setupView(View view){
		// init swipe refresh function if available
		if (isSwipeRefresheable()){
			// Retrieve the SwipeRefreshLayout and GridView instances
			mSwipeRefreshLayout = (MultiSwipeRefreshLayout) view.findViewById(swipeLayout());
			
        
			// Set the color scheme of the SwipeRefreshLayout by providing 4 color resource ids
		/*	mSwipeRefreshLayout.setColorSchemeResources(
					R.color.swipe_color_1, R.color.swipe_color_2,
					R.color.swipe_color_3, R.color.swipe_color_4); */
			mSwipeRefreshLayout.setColorSchemeResources(	swipeColor()[0], 
															swipeColor()[1],
															swipeColor()[2], 
															swipeColor()[3]);
        
			// Retrieve the progress bar instance
			mProgress = (ProgressBar) view.findViewById(progressBar());
		}
        
		// init GridView
		mGridView = (GridView) view.findViewById(getGridId());
		mGridView.setClipToPadding(false);
		mGridView.setOnItemClickListener(new GridListener()); 
        mGridView.setAdapter(imageAdapter);
        mGridView.setVerticalScrollBarEnabled(true);
        mGridView.setFastScrollEnabled(true);
        
        personalGridInit();
        
        
        mGridView.getViewTreeObserver().addOnGlobalLayoutListener(
                new ViewTreeObserver.OnGlobalLayoutListener() {
                    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
					@Override
                    public void onGlobalLayout() {
                        if (imageAdapter.getNumColumns() == 0) {

                        	final int numColumns = (int) Math.floor(
                        			mGridView.getWidth() / (getImageSize() + getImageSpacing()));
                        	if (numColumns > 0) {
                                final int columnWidth =
                                        (mGridView.getWidth() / numColumns) - getImageSpacing();
                                imageAdapter.setNumColumns(numColumns);
                                imageAdapter.setItemHeight(columnWidth);

                                if (Global.hasJellyBean()) {
                                	mGridView.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                } else {
                                	mGridView.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                }
                            }
                        }
                    }
                });
     
        mGridView.setNumColumns(Global.numberOfColumns(getActivity(),mGridView, getImageSize(),getImageSpacing()));
        mGridView.setAdapter(imageAdapter);
        
        if (ismGridVisible()){
	    	mGridView.setVisibility(View.VISIBLE);
			if (mProgress != null) mProgress.setVisibility(View.INVISIBLE);
			Log.d(TAG, "!!! GridView set to visible onCreatView !!!");
	    }
		
        Log.e(TAG, "VIEW CREATED - - - - -");
	}
	
	@Override
	public void onPause() {
		super.onPause();
		onPauseX();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		onDestroyX();
	}
	
	@Override
	public void onResume() {
		super.onResume();
		onResumeX();
	}
	
	private class GridListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
				onItemClickX(parent, view, position,id);
		}
	}
	
	protected abstract int [] swipeColor();
	
	public void updateGrid(){
		imageAdapter.notifyDataSetChanged();
		Log.i(TAG, "!!! GridView Updated !!!");
	}
	
	/**
	 * Get the xml filename of the fragment layout
	 * @return The integer value of the xml file 
	 */
	protected abstract int getFragmentLayout();
	
	/**
	 * Retrieve the view to be placed in each container
	 * @param position
	 * @param convertView
	 * @param parent
	 * @param params
	 * @param mItemHeight
	 * @return The view
	 */
	protected abstract View getViewX(int position, View convertView, ViewGroup parent, GridView.LayoutParams params, int mItemHeight);
	
	/**
	 * Retrieve the size of the grid
	 * @return The size
	 */
	protected abstract int getGridSizeX();
	
	/**
	 * Retrieve the spacing between each image in the grid
	 * @return The spacing
	 */
	protected abstract int getImageSpacing();
	
	/**
	 * Retrieve the size of each image in the grid
	 * @return The size
	 */
	protected abstract int getImageSize();
	
	protected abstract String getClassName();
	
	/**
	 * Initialize cache and then return an instance of it
	 * @return
	 */
	protected abstract ImageCacheParams getImageCache();
	
	protected abstract void onDestroyX();
	
	protected abstract void onPauseX();
	
	protected abstract void onResumeX();
	
	protected abstract boolean isSwipeRefresheable();
	
	protected abstract int getGridId();
	
	protected abstract void onItemClickX(AdapterView<?> parent, View view, int position,long id);
	
	protected abstract int swipeLayout();
	
	protected abstract int progressBar();
	
	protected abstract int viewToSwipe();

	public MultiSwipeRefreshLayout getmSwipeRefreshLayout() {
		return mSwipeRefreshLayout;
	}

	public void setmSwipeRefreshLayout(MultiSwipeRefreshLayout mSwipeRefreshLayout) {
		this.mSwipeRefreshLayout = mSwipeRefreshLayout;
	}

	public static GridView getmGridView() {
		return mGridView;
	}

	public static void setmGridView(GridView mGridView) {
		FragmentX.mGridView = mGridView;
	}
	
	public boolean isGridViewVisible(){
		return (mGridView != null) ? mGridView.getVisibility() == View.VISIBLE : false;
	}

	public ProgressBar getmProgress() {
		return mProgress;
	}

	public void setmProgress(ProgressBar mProgress) {
		this.mProgress = mProgress;
	}

	public boolean ismGridVisible() {
		return mGridVisible;
	}

	public void setmGridVisible(boolean mGridVisible) {
		this.mGridVisible = mGridVisible;
	}
}
