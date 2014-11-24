package prince.app.sphotos;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.widget.LoginButton;

public class FacebookLogin extends Fragment{
	
	private static final String[] PERMISSIONS = {	"user_friends", 
													"user_photos",  
													"user_tagged_places",  
													"read_friendlists"};
	
	private UiLifecycleHelper mUiHelper;
	
	public static FacebookLogin newInstance(){
		return new FacebookLogin();
	}
	
	private Session.StatusCallback mCallBack = new Session.StatusCallback() {
	    @Override
	    public void call(Session session, 
	            SessionState state, Exception exception) {
	        onSessionStateChange(session, state, exception);
	    }
	};
	
	@Override
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
		mUiHelper = new UiLifecycleHelper(getActivity(), mCallBack);
	    mUiHelper.onCreate(oldState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, 
	        ViewGroup container, 
	        Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fb_login, container, false);
	    
	    LoginButton authButton = (LoginButton) view.findViewById(R.id.authButton);
	    authButton.setFragment(this);
	    if (Session.getActiveSession().equals(SessionState.CLOSED))authButton.setReadPermissions(PERMISSIONS);

	    return view;
	}
	
	private void onSessionStateChange(Session session, SessionState state, Exception exception) {}
	
	@Override
	public void onResume() {
	    super.onResume();
	    mUiHelper.onResume();
	}
	
	@Override
	public void onPause() {
	    super.onPause();
	    mUiHelper.onPause();
	}
	
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    mUiHelper.onActivityResult(requestCode, resultCode, data);
	}
	
	@Override
	public void onSaveInstanceState(Bundle outState) {
	    super.onSaveInstanceState(outState);
	    mUiHelper.onSaveInstanceState(outState);
	}
}
