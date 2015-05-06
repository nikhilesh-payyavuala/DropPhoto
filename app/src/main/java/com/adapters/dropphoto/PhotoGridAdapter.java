package com.adapters.dropphoto;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.activity.nikhilesh.dropphoto.R;
import com.dropbox.client2.DropboxAPI;
import com.dropbox.client2.android.AndroidAuthSession;
import com.dropbox.client2.DropboxAPI.Entry;


import org.w3c.dom.Text;

import java.util.ArrayList;

/**
 * Created by Nikhilesh on 5/4/15.
 */
public class PhotoGridAdapter extends BaseAdapter {

    Context context;
    ArrayList<Drawable> thumbs;
    ArrayList<Entry> entries;
    //DropboxAPI<?> mApi;
    public PhotoGridAdapter(Context context,ArrayList<Drawable> thumbs, ArrayList<Entry> entries){
        this.context = context;
        this.thumbs = thumbs;
        this.entries = entries;

    }

    public int getCount() {
        return thumbs.size();
    }

    public Object getItem(int position) {
        return null;
    }

    public long getItemId(int position) {
        return 0;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        View v = convertView;
        if(v==null){
            v = LayoutInflater.from(parent.getContext()).inflate(R.layout.image_grid, null);

        }
        ImageView im = (ImageView)v.findViewById(R.id.grid_item_image);
        im.setImageDrawable(thumbs.get(position));
        TextView path = (TextView)v.findViewById(R.id.path);
        TextView type = (TextView)v.findViewById(R.id.type);
        TextView date = (TextView)v.findViewById(R.id.date);
        Entry e = entries.get(position);
        path.setText(e.fileName());
        type.setText(e.mimeType);
        date.setText(e.modified);


        return v;
    }
}
