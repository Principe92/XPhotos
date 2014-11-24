package prince.app.sphotos.bgtask;

import java.util.EventListener;

import prince.app.sphotos.Request.RequestX;
import prince.app.sphotos.tools.FBINIT;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class FetchData extends RecieverX{
	private final String TAG = FetchData.class.getSimpleName();
	public static final String FETCH_DATA = "fetch_data";
	
	private static WifiListener connection;
		
	public interface WifiListener extends EventListener{
		void onConnection();
	}
		
	public static void setListener(WifiListener ir){
		connection = ir;
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		
		String action = intent.getAction();
		if(action.equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
			final ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
			final NetworkInfo info = connManager.getActiveNetworkInfo();
	            if (info != null && info.isConnected()) {
	                // TODO: start or restart pending downloads
	            	
	            	if (connection != null) connection.onConnection();
	            	
	            	Log.e(TAG, "!!! Starting saved Request !!!");
	            	// run requests
	            	runGraphReqs();
	            	
	            }
		}
	     // we're not connected 
	}
	
	private void runGraphReqs(){
		int count = FBINIT.sRequests.size();
		Log.e(TAG, " # " + count + " saved Request");
		
		if (!FBINIT.sReqStack.isEmpty()){
			for (int i=0; i<count; i++){
				String reqId = FBINIT.sReqStack.pop();
			
				synchronized(FBINIT.sReqLock){
					RequestX req = FBINIT.sRequests.get(reqId);
					if (req != null && !req.mStarted && !req.mFinished){
						req.run();
					}
				}
			}
		}
	}
		
}