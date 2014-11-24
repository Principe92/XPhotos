package prince.app.sphotos;

import java.util.ArrayList;

import prince.app.sphotos.tools.FBINIT;
import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;

public class Fragment_Favorite extends ListFragment{

	
	private Button mOkay;
	private FavoriteListener listen;
	private int mChecked;
	
	public static Fragment_Favorite newInstance(){
		return new Fragment_Favorite();
	}
	
	public interface FavoriteListener{
		public void onOkay(int position);
	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        
        try {
            listen = (FavoriteListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement FavoriteListener");
        }
    }
	
	@Override
	public void onCreate(Bundle oldState){
		super.onCreate(oldState);
		
		setRetainInstance(true);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup parent, Bundle oldState){
		super.onCreateView(inflater, parent, oldState);
		
		View view = inflater.inflate(R.layout.favorite_main_list, parent, false);
		
		setUpList(view);
		
		return view;
	}
	
	private void setUpList(View view){
		// initialize button
		mOkay = (Button) view.findViewById(R.id.fav_button);
		mOkay.setOnClickListener(new OnClickListener(){

			@Override
			public void onClick(View v) {
				listen.onOkay(mChecked + 2);
				
			}});
		
		//Set the adapter for the list view
		ArrayList<String> albumNames = new ArrayList<String>();
        int albumSize = FBINIT.sAlbumsArray.size();
        
        for (int i = 2; i < albumSize; i++) {
        	albumNames.add(FBINIT.sAlbumsArray.get(i).mAlbumName);
        }
		
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_list_item_single_choice,albumNames);
        
        setListAdapter(adapter);
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
	    super.onActivityCreated(savedInstanceState);

	    getListView().setOnItemClickListener(new OnItemClickListener() {
	            @Override
	            public void onItemClick(AdapterView<?> arg0, View arg1, int position, long arg3) {
	            	mChecked = position;
	            }});
	    
	    getListView().setSelection(0);

	}

}
