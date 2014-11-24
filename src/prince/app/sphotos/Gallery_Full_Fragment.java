package prince.app.sphotos;

import prince.app.sphotos.tools.Global;
import prince.app.sphotos.tools.Util;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ImageView;

public class Gallery_Full_Fragment extends Fragment {
    private static final String IMAGE_DATA_EXTRA = "resId";
    private static final String TAG = Gallery_Full_Fragment.class.getSimpleName();
    private int mImageNum;
    private ImageView mImageView;

    static Gallery_Full_Fragment newInstance(int imageNum) {
        final Gallery_Full_Fragment f = new Gallery_Full_Fragment();
        final Bundle args = new Bundle();
        args.putInt(IMAGE_DATA_EXTRA, imageNum);
        f.setArguments(args);
        return f;
    }

    // Empty constructor, required as per Fragment docs
    public Gallery_Full_Fragment() {}

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mImageNum = getArguments() != null ? getArguments().getInt(IMAGE_DATA_EXTRA) : -1;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
        // image_detail_fragment.xml contains just an ImageView
        final View v = inflater.inflate(R.layout.gallery_full_main, container, false);
        mImageView = (ImageView) v.findViewById(R.id.imageView);
        return v;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (Gallery_Full_Activity.class.isInstance(getActivity())) {
            // Call out to Local_FullImageActivity to load the bitmap in a background thread
        	Log.e(TAG, "Over Here");
            ((Gallery_Full_Activity) getActivity()).getmImageFetcher().loadImage(mImageNum, mImageView, Util.LOCALPHOTO_FULLIMAGE_LOAD_ID, false);
        }
        
     // Pass clicks on the ImageView to the parent activity to handle
        if (OnClickListener.class.isInstance(getActivity()) && Global.hasHoneycomb()) {
            mImageView.setOnClickListener((OnClickListener) getActivity());
        }
    }
}

