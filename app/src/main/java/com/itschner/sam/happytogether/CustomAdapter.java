package com.itschner.sam.happytogether;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by Sam on 1/2/2018.
 */

class CustomAdapter extends ArrayAdapter<String> {
    CustomAdapter(Context context,String[] items){
        super(context, R.layout.custom_row,items);

    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_row,parent,false);

        String singleItem = getItem(position);
        TextView textView = customView.findViewById(R.id.nameText);
        ImageView imageView = customView.findViewById(R.id.picImgView);

        textView.setText(singleItem);
        //How to setImage to be retrieved???
        //Use the singleItem string to retrieve it!

        return super.getView(position, convertView, parent);
    }
}
