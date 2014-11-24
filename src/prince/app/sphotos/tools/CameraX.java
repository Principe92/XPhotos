package prince.app.sphotos.tools;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.MediaStore;

public class CameraX {
	private static int ID;
	private String mImagePath; 
	private Activity mContext;
	private File mImageFolder;
	
	/**
	 * 
	 * @param context - activity calling the camera INTENT
	 * @param intentID - unique intent id for capturing results
	 * @param directory - where the image would be saved
	 */
	public CameraX(Activity context, int intentID, File directory){
	this.mContext = context;
	ID = intentID;
	this.mImageFolder = directory;
	}
	
	public Bitmap getResult(){
		return Global.getInstance().getImageFromExternal(mImagePath);
	}
	
	public void TakePicture(){
		Intent mTakePic = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
		
		Uri address = null;
		
		try {
			address = Uri.fromFile(createImageFileName());
		} catch (IOException ex) {}
		
	        // Continue only if the File was successfully created
	        if (address != null) {
	        	mTakePic.putExtra(MediaStore.EXTRA_OUTPUT, address);
	        	mContext.startActivityForResult(mTakePic, ID);
	        }
	}
	
	public void galleryAddPic() {
	    Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_MOUNTED);
	    File file = new File(mImagePath);
	    Uri contentUri = Uri.fromFile(file);
	    mediaScanIntent.setData(contentUri);
	    mContext.sendBroadcast(mediaScanIntent);
	}

	public File createImageFileName() throws IOException {
		// Create an image file name
		SimpleDateFormat timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", java.util.Locale.getDefault());
		Date today = Calendar.getInstance().getTime();
	    String imageFileName = "IMG_XP_" + timeStamp.format(today);
	    File newImage = new File(mImageFolder.getPath() + File.separator + imageFileName + ".jpg");
	    

	    // Save a file: path for use with ACTION_VIEW intents
	    mImagePath = newImage.getPath();
	    return newImage;
	}
}
