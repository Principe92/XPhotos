package prince.app.sphotos.database;

import java.util.EventListener;

import prince.app.sphotos.database.FBDbContract.ImageHelper;
import prince.app.sphotos.tools.FBINIT;
import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Images;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class ImagesAccess extends DbService{
	private static final String TAG = ImagesAccess.class.getSimpleName();
	private final SQLiteDatabase dbWr = Global.getInstance().getFBDb().getWritableDatabase();
	private final SQLiteDatabase dbRd = Global.getInstance().getFBDb().getReadableDatabase();
	private static ImageDbListener DbListener;
	
	public interface ImageDbListener extends EventListener{
		void onDbStart();
		void onDbStop(boolean success);
	}
	
	public static void setListener(ImageDbListener ir){
		DbListener = ir;
	}

	@Override
	protected void readDb(String table) {
		// TODO Auto-generated method stub

		Cursor cr = (isTableCreated(dbWr, table)) ? getCursor(dbRd, table, CursorProj.IMAGE_DB_PROJECTION, null, null, null, null, null) : null;
		
		if (cr != null && cr.moveToFirst()){
			int index;
			do {
				Images image = new Images();
				index = cr.getInt(cr.getColumnIndexOrThrow(ImageHelper._ID));
				image.mImageThumbURL 	= cr.getString(cr.getColumnIndexOrThrow(ImageHelper.IMAGE_THUMB));
				image.mImageURL 		= cr.getString(cr.getColumnIndexOrThrow(ImageHelper.IMAGE_THUMB));
						
				FBINIT.sImagesArray.append(index, image);
				Log.e(TAG, "image " + index + " for : " + table + " added to ARRAY");
			} 
			while (cr.moveToNext());
					
			cr.close();
			
			// Announce the availability of images
			if (DbListener != null) DbListener.onDbStop(true);
		}
		

		else { 
			Log.e(TAG, "!!! images not available in db !!!");
			if (cr != null) cr.close();
			if (DbListener != null) DbListener.onDbStop(false);
		}
		
	}

	@Override
	protected void updateDb(String table, boolean arrayCheck) {
		// table created?
		if (!isTableCreated(dbWr, table)){ // no
			// create table
			createTable(dbWr, ImageHelper.getCreate(table));
			Log.i(TAG, "!!! creating table: " + table + " for first time !!!");
			
			// insert new values
			synchronized(FBINIT.sImagesArray){
				write(table);
			}
		}
		
		else if (rowSize(dbWr, table) == 0) write(table);
		
		else { // yes
			if (FBINIT.sImagesArray.size() > 0){
				int i;
				for (i = 0; i< FBINIT.sImagesArray.size(); i++){
					
					String selection = ImageHelper._ID + " LIKE ?";
					String[] selectionArgs = { String.valueOf(i) };
					dbWr.update(table, cvImages(i, true), selection, selectionArgs);
				}
				
				Log.e(TAG, "# " + i + " ROWS updated in table: " + table);
				FBINIT.IMAGES_UPDATED = false;
				
			}
		}
		
	}
			
	private void write(String table){
		Log.e(TAG, " !!! Writing images of  tb:" + table + " to db !!!");
		
		if (FBINIT.sImagesArray.size() > 0){
			for (int i=0; i< FBINIT.sImagesArray.size(); i++){
				long newRowId = dbWr.insert(table, null, cvImages(i, false));
				
				Log.i(TAG, "image #" + i + "; rowId: " + newRowId + "; inserted at tb: " + table);
			}
		}
	}
	
	private ContentValues cvImages(int index, boolean update){
		Images image;
		
		synchronized (FBINIT.sImagesArray){
			image = FBINIT.sImagesArray.get(index);
		}
		
		ContentValues values = new ContentValues();
		
		if (!update) values.put(ImageHelper._ID, index);
		values.put(ImageHelper.IMAGE_THUMB, 	image.mImageThumbURL);
		values.put(ImageHelper.IMAGE_FULL, 		image.mImageURL);
		
		return values;
	}
}
