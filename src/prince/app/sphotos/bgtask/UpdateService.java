package prince.app.sphotos.bgtask;

import prince.app.sphotos.database.AlbumsAccess;
import prince.app.sphotos.tools.UpdateFiles;
import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Process;
import android.widget.Toast;

public class UpdateService extends Service{
	public static final String TAG = UpdateService.class.getSimpleName();
	public static final String INTENT_KEY = "key";
	public static final String EXTRAS = "extras";
	
	public static final int UPDATE_GRID_PHOTOS = 100;
	public static final int UPDATE_ALBUMS = 200;
	
	private Looper mServiceLooper;
	private ServiceHandler mServiceHandler;
	
	// Handler that receives messages from the thread
	private final class ServiceHandler extends Handler {
		  
		public ServiceHandler(Looper looper) {
			super(looper);
		}
	      
		@Override
		public void handleMessage(Message msg) {
	          
			switch(msg.arg2){
	    	  	
				case UPDATE_GRID_PHOTOS:
	    	  		UpdateFiles.uAllGrid();
	    	  		break;
	    	  		
	    	  	case UPDATE_ALBUMS:
	    	  		AlbumsAccess.mCacheUpdateDb();
	    	  		break;
	    	  		
	    	  	default:
	    	  		break;
			}
	    	  
			stopSelf(msg.arg1);
		}
	}

	@Override
	public void onCreate() {
		// Start up the thread running the service.  Note that we create a
		// separate thread because the service normally runs in the process's
		// main thread, which we don't want to block.  We also make it
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
	      
		int key = intent.getIntExtra(INTENT_KEY, -1);
	      
		switch (key){
	      
	      	case UPDATE_GRID_PHOTOS:
	      		msg.arg2 = UPDATE_GRID_PHOTOS;
	      		break;
	      		
	      	case UPDATE_ALBUMS:
	      		msg.arg2 = UPDATE_ALBUMS;
	      		break;
	      		
	      	default:
	      		break;
		}
	      
	      
		msg.arg1 = startId;
		mServiceHandler.sendMessage(msg);

		// If we get killed, after returning from here, restart
		return START_STICKY;
	}

	@Override
	public IBinder onBind(Intent intent) {
		// We don't provide binding, so return null
		return null;
	}

	@Override
	public void onDestroy() {
		Toast.makeText(this, "UpdateService DONE", Toast.LENGTH_SHORT).show();
	}

}
