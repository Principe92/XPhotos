package prince.app.sphotos.tools;

import prince.app.sphotos.util.ImageFetcher;
import android.os.AsyncTask;
import android.widget.ImageView;

public final class Tasks extends AsyncTask<Object, Object, Object>{
	private final static String TAG = Tasks.class.getSimpleName();
	public static final int IMAGES_WAIT = 0;
	public static final int ALBUMS_WAIT = 1;
	
	private ImageView mView;
	private ImageFetcher mFetcher;
	private static int KEY;
	String address;

	public Tasks(ImageView view, ImageFetcher fetcher, int key){
		this.mView = view;
		this.mFetcher = fetcher;
		Tasks.KEY = key;
	}
	
	@Override
	protected Object doInBackground(Object... params) {
		
		switch(KEY){
		
		case IMAGES_WAIT:
			synchronized(FBINIT.sImagesArray_LOCK){
				while (!FBINIT.IMAGES_READY){
					try {
						FBINIT.sImagesArray_LOCK.wait();
					} catch (InterruptedException e) {}
				}
			}
				
			synchronized (FBINIT.sImagesArray) {
				address = FBINIT.sImagesArray.get((int) params[0]).mImageThumbURL;
			//	Log.d(TAG, "fetching image " + params[0] + " with id: " + FBINIT.sImagesArray.get((int) params[0]).mImageID);
			}
			break;
			
		case ALBUMS_WAIT:
			synchronized(FBINIT.sAlbumsArray_LOCK){
				while (!FBINIT.ALBUMS_READY){
					try {
						FBINIT.sAlbumsArray_LOCK.wait();
					} catch (InterruptedException e) {}
				}
			}
			
			synchronized (FBINIT.sAlbumsArray) {
				address = FBINIT.sAlbumsArray.get((int) params[0]).mAlbumCoverURL;
			}
			break;
			
		default:
			address = null;
			break;
		} 
		
		
		
		return address;
	}
	
	
	@Override
	protected void onPostExecute(Object result){
		
		switch(KEY){
		case IMAGES_WAIT:
			// fetch the image using the URL
			mFetcher.loadImage(result, mView, Util.ALBUM_IMAGES_LOAD_ID);
			break;
			
		case ALBUMS_WAIT:
			mFetcher.loadImage(address, mView, Util.ALBUM_COVERPHOTO_LOAD_ID);
			break;
			
		default:
			break;
		}
	}

}
