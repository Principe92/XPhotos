package prince.app.sphotos.tools;

import java.util.List;

import prince.app.sphotos.R;
import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;


public class ListViewListener extends ArrayAdapter<Account> {
	Context context;
	private String owner = "okoriepc@outlook.com";

	public ListViewListener(Context context, int resourceId, List<Account> item) {
		super(context, resourceId, item);
		this.context = context;
	}
	
	private class ViewHolder {
        ImageView imageView;
        TextView accountTitle;
        TextView accountOwner;
    }
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder = null;
        Account rowItem = getItem(position);

 
        LayoutInflater mInflater = (LayoutInflater) context
                .getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.social_media_accounts, parent, false);
            holder = new ViewHolder();
            holder.accountTitle = (TextView) convertView.findViewById(R.id.list_name);
            holder.imageView = (ImageView) convertView.findViewById(R.id.list_image);
            holder.accountOwner = (TextView) convertView.findViewById(R.id.list_owner);
            convertView.setTag(holder);
        } else
            holder = (ViewHolder) convertView.getTag();
        
        holder.accountTitle.setText(rowItem.getTitle());
        holder.imageView.setImageResource(rowItem.getImageId());
        holder.accountOwner.setText(owner);
        holder.accountOwner.setVisibility(View.VISIBLE);
 
        return convertView;
    }
}
