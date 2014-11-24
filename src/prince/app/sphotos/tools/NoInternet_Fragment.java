package prince.app.sphotos.tools;

import prince.app.sphotos.R;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;


public class NoInternet_Fragment extends Fragment{
	private Button rButton;
	private Button nButton;
	clickListener  listener;
	
	
	// Container Activity must implement this interface
    public interface clickListener {
        public void onRetry();
        public void onCheck();
    }
    
    @Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		// Retain this fragment across configuration changes.
		setRetainInstance(true);
		
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception
        try {
            listener = (clickListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement LoginListener");
        }
    }


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		super.onCreateView(inflater, container, savedInstanceState);
		View view = inflater.inflate(R.layout.check_connection, container, false); 
		
		rButton = (Button) view.findViewById(R.id.btn_retry);
		nButton = (Button) view.findViewById(R.id.btn_check_network);
		rButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		          listener.onRetry();
		        }
		});
		nButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
		          listener.onCheck();
		        }
		});
		
		return view;
	}
	

}