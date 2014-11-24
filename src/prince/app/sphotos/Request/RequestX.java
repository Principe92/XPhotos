package prince.app.sphotos.Request;

import com.facebook.Request;
import com.facebook.RequestBatch;

public class RequestX {
	
	public Request mRequest;
	
	public boolean mStarted = false;
	
	public boolean mPaused = false;
	
	public boolean mStopped = false;
	
	public boolean mFinished = false;
	
	public String mTag;
	
	public RequestBatch mBatchRequest;
	
	public void run(){
		if (mRequest != null){
			mStarted = true;
			mRequest.executeAsync();
		}
		
		else if (mBatchRequest != null){
			mStarted = true;
			mBatchRequest.executeAsync();
		}
	}
	
}
