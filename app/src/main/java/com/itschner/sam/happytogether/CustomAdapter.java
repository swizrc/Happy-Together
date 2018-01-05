package com.itschner.sam.happytogether;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;

import static com.itschner.sam.happytogether.LoggedInNewFragment.rotateImage;

/**
 * Created by Sam on 1/2/2018.
 */

class CustomAdapter extends ArrayAdapter<String> {

    CustomAdapter(Context context,String[] items){
        super(context, R.layout.custom_row,items);
    }

    public void setProfileImage(final ImageView imageView,String email){
        //Download profile image
        StorageReference storageReference = FirebaseStorage.getInstance().getReference();
        FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();

        StorageReference imageRef = storageReference.child(email + "/profile.jpg" );
        try{
            final File tmpFile = File.createTempFile("img","jpg");
            imageRef.getFile(tmpFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                    Bitmap bitmap = BitmapFactory.decodeFile(tmpFile.getAbsolutePath());
                    try{
                        ExifInterface ei = new ExifInterface(tmpFile.getAbsolutePath());
                        int orientation = ei.getAttributeInt(ExifInterface.TAG_ORIENTATION,
                                ExifInterface.ORIENTATION_UNDEFINED);
                        switch (orientation) {
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                bitmap = rotateImage(bitmap, 90);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_180:
                                bitmap = rotateImage(bitmap, 180);
                                break;
                            case ExifInterface.ORIENTATION_ROTATE_270:
                                bitmap = rotateImage(bitmap, 270);
                                break;
                            default:
                        }
                    }
                    catch (IOException e) {
                        e.printStackTrace();
                    }
                    imageView.setImageBitmap(bitmap);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        LayoutInflater inflater = LayoutInflater.from(getContext());
        View customView = inflater.inflate(R.layout.custom_row,parent,false);

        String singleItem = getItem(position);
        TextView textView = customView.findViewById(R.id.nameText);
        ImageView imageView = customView.findViewById(R.id.picImgView);
        Button acceptButton = customView.findViewById(R.id.acceptButton);
        Button declineButton = customView.findViewById(R.id.declineButton);

        textView.setText(singleItem);
        setProfileImage(imageView,singleItem);
        //How to setImage to be retrieved???
        //Use the singleItem string to retrieve it!

        return customView;
    }
}
