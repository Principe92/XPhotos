package prince.app.sphotos.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;

import prince.app.sphotos.Gallery_Main_Fragment;
import prince.app.sphotos.Gallery.CacheService;
import prince.app.sphotos.Gallery.DataSource;
import prince.app.sphotos.Gallery.ImageList;
import prince.app.sphotos.Gallery.UriTexture;
import prince.app.sphotos.Gallery.Util;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.provider.MediaStore;
import android.util.Log;

/**
 * Fetches Images from the External SDCard
 * @author Principe
 *
 */
public class ImageFetcherX extends ImageWorkerX{
	private static final String TAG = ImageFetcherX.class.getSimpleName();
	private static DataSource imageSource;
	private Context mContext;
	private int mImageWidth;
    private int mImageHeight;
	
	public ImageFetcherX(Context context, int reqWidth, int reqHeight){
		super(context);
		mContext = context;
        mImageWidth = reqWidth;
        mImageHeight = reqHeight;
	}
	
	public ImageFetcherX(Context context, int reqWidth){
		super(context);
		mContext = context;
        mImageWidth = reqWidth;
        mImageHeight = reqWidth;
	}
	
	private Bitmap processFullBitmap(Object params){
		Bitmap bitmap = null;
		int columnIndex = 0;
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = mContext.getContentResolver().query(MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI,
                projection,
                null, 
                null, 
                null);
        
        if (cursor != null) {
            columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToPosition((Integer) params);
            String imagePath = cursor.getString(columnIndex);
            
            FileInputStream is = null;
            BufferedInputStream bis = null;
            Log.e(TAG, "Full Image Path: " + imagePath);
            Gallery_Main_Fragment.mImageCount = cursor.getCount();
            
            try {
            	is = new FileInputStream(new File(imagePath));
            	bis = new BufferedInputStream(is);
                
            	bitmap = ImageResizer.decodeSampledBitmapFromDescriptor(is.getFD(), 150, 150, getImageCache());
            	cursor.close();
            	return bitmap;
            } 
            catch (Exception e) {
                //Try to recover
            }
            finally {
                try {
                    if (bis != null) {
                        bis.close();
                    }
                    if (is != null) {
                        is.close();
                    }
                    cursor.close();
                    projection = null;
                } catch (Exception e) {
                }
            } 
        }
        return bitmap;
    }

	@Override
	protected Bitmap processBitmap(Object data, Boolean loadThumb) {
		return (loadThumb) ? 
				getBitmapForIndex(mContext, (int) data) // return thumbnail
				: processFullBitmap(data);				// return full image
	}
	
	public Bitmap getBitmapForIndex(Context context, int currentSlideshowCounter) {
        ImageList list = CacheService.getImageList(context);
        // Once we have the id and the thumbid, we can return a bitmap
        // First we select a random numbers
        if (list.ids == null)
            return null;
        
        int index = currentSlideshowCounter;
        long cacheId = list.thumbids[index];
        final String uri = CacheService.BASE_CONTENT_STRING_IMAGES + list.ids[index];
        Bitmap retVal = null;
        try {
            retVal = UriTexture.createFromUri(context, uri, UriTexture.MAX_RESOLUTION, UriTexture.MAX_RESOLUTION, cacheId, null);
            if (retVal != null) {
                retVal = Util.rotate(retVal, list.orientation[index]);
            }
        } catch (OutOfMemoryError e) {
            ;
        } catch (IOException e) {
            ;
        } catch (URISyntaxException e) {
            ;
        }
        return retVal;
    }

	public static void setDataSource(DataSource myData) {
		imageSource = myData;
		
	}
    
}
