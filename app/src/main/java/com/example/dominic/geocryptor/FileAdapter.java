package com.example.dominic.geocryptor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.io.File;
import java.util.ArrayList;

/**
 * Created by Dominic on 8/24/2017.
 */

public class FileAdapter extends BaseAdapter{

    private Context myContext;
    private LayoutInflater myInflator;
    private ArrayList<File> data;

    public FileAdapter(Context context, ArrayList<File> items){
        myContext = context;
        data = items;
        myInflator = (LayoutInflater) myContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

   public int getCount() {
       return data.size();
   }

   public Object getItem(int position){
       return data.get(position);
   }

   public long getItemId(int position) {
       return position;
   }

   public View getView(int position, View convertView, ViewGroup parent){
       View rowView = myInflator.inflate(R.layout.list_item_file, parent, false);

       TextView titleTextView = (TextView) rowView.findViewById(R.id.file_list_title);

       File file = (File) getItem(position);

       titleTextView.setText(file.getName());

       return rowView;
   }
}
