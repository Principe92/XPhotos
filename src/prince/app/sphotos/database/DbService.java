package prince.app.sphotos.database;

import prince.app.sphotos.tools.Global;
import android.app.Service;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;


public abstract class DbService extends Service{
	
	private static final String TAG = DbService.class.getSimpleName();
	public static final String DB_INTENT_KEY = "db_key";
	public static final String EXTRAS_STRING = "db_command";
	public static final String EXTRAS_BOOL = "self_check";
	public static final int READ_TB = 100;
	public static final int UPDATE_TB = 400;
	public static final int CREATE_TB = 500;
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		public ServiceHandler(Looper looper) {
			super(looper);
		}
		
		@Override
		public void handleMessage(Message msg) {
			Bundle exBundle = msg.getData();
			String table;
			
			switch(msg.arg2){
			
			case READ_TB:
				table = exBundle.getString(EXTRAS_STRING);
				readDb(table);
				break;
				
			case UPDATE_TB:
				table = exBundle.getString(EXTRAS_STRING);
				boolean check = exBundle.getBoolean(EXTRAS_BOOL, false);
				updateDb(table, check);
				break;
				
			case CREATE_TB:
				String sqlCMD = exBundle.getString(EXTRAS_STRING);
				SQLiteDatabase db = Global.getInstance().getFBDb().getWritableDatabase();
				createTable(db, sqlCMD);
				break;
				
			default:
				break;
			}
			
			stopSelf(msg.arg1);
		}
	}
	
	@Override
	public void onCreate() {
		// Start up the thread running the service. Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block. We also make it
		// background priority so CPU-intensive work will not disrupt our UI.
		
		HandlerThread thread = new HandlerThread("ServiceStartArguments",
				Process.THREAD_PRIORITY_BACKGROUND);
		thread.start();
		// Get the HandlerThread's Looper and use it for our Handler
		mServiceLooper = thread.getLooper();
		mServiceHandler = new ServiceHandler(mServiceLooper);
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// For each start request, send a message to start a job and deliver the
		// start ID so we know which request we're stopping when we finish the job
		Message msg = mServiceHandler.obtainMessage();
		int key = intent.getIntExtra(DB_INTENT_KEY, -1);
		Bundle mBond = new Bundle();
		
		switch (key){
		
		case READ_TB:
			msg.arg2 = READ_TB;
			mBond.putString(EXTRAS_STRING, intent.getStringExtra(EXTRAS_STRING));
			break;
			
		case UPDATE_TB:
			msg.arg2 = UPDATE_TB;
			mBond.putString(EXTRAS_STRING, intent.getStringExtra(EXTRAS_STRING));
			mBond.putBoolean(EXTRAS_BOOL, intent.getBooleanExtra(EXTRAS_BOOL, false));
			break;
			
		case CREATE_TB:
			msg.arg2 = CREATE_TB;
			mBond.putString(EXTRAS_STRING, intent.getStringExtra(EXTRAS_STRING));
			break;
			
		default:
			break;
		}
		
		msg.arg1 = startId;
		msg.setData(mBond);
		mServiceHandler.sendMessage(msg);
		
		// 	If we get killed, after returning from here, restart
		return START_STICKY;
	}
	
	@Override
	public IBinder onBind(Intent intent) {
		// 	We don't provide binding, so return null
		return null;
	}
	
	@Override
	public void onDestroy() {
		Toast.makeText(this, "DataBase Access Done", Toast.LENGTH_SHORT).show();
	}
	
	protected abstract void readDb(String table);
	
	protected abstract void updateDb(String table, boolean check);
	
	public static boolean isTableCreated(SQLiteDatabase db, String tableName){
		try {
			Cursor cr = db.query(tableName, null, null, null, null, null, null);
			cr.close();
		} catch (SQLiteException e){
			if (e.getMessage().toString().contains("no such table")){
				return false;
			}
		}
		
		return true;
	}
	
	public int rowSize(SQLiteDatabase db, String tableName){
		if (isTableCreated(db, tableName)){
			Cursor cr = db.query(tableName, null, null, null, null, null, null);
			if (cr != null) return cr.getCount();
		}
		
		return 0;
	}

	public Cursor getCursor(SQLiteDatabase db, String table, String[] projection, String selection,
			String[] selectionArgs, String groupBy, String having, String sortOrder) {
		return db.query(table, projection, selection, selectionArgs, groupBy, having, sortOrder);
	}

	public static void createTable(SQLiteDatabase db, String command){
		db.execSQL(command);
	}
}