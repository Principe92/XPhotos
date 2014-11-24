package prince.app.sphotos.database;

import prince.app.sphotos.database.FBDbContract.AlbumHelper;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class AlbumSQL extends SQLiteOpenHelper{
	// If you change the database schema, you must increment the database version.
    public static int DB_VERSION = 1;
    private static final String TAG = AlbumSQL.class.getSimpleName();
    

	public AlbumSQL(Context context, String dbName){
		super(context, dbName, null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.i(TAG, "!!! creating db !!!");
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		
		Log.i(TAG, " !!! upgrade going on !!!");
		
		
		DB_VERSION = newVersion;
		db.execSQL(AlbumHelper.SQL_DELETE_ENTRIES);
	 
        onCreate(db);
		
	}
	
	@Override
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		onUpgrade(db, oldVersion, newVersion);
	}

}
