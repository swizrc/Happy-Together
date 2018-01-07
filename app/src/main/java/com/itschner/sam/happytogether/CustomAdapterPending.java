package com.itschner.sam.happytogether;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import static com.itschner.sam.happytogether.LoggedInNewFragment.rotateImage;

/**
 * Created by Sam on 1/2/2018.
 */

class CustomAdapterPending extends ArrayAdapter<String> {

    private FirebaseAuth firebaseAuth = FirebaseAuth.getInstance();
    private DatabaseReference firebaseDatabase = FirebaseDatabase.getInstance().getReference("users");

    Map<String,Bitmap> downloadedImages = new ArrayMap<>();

    CustomAdapterPending(Context context, String[] items){
        super(context, R.layout.custom_row_pending,items);
    }

    public void setProfileImage(final ImageView imageView,String email) {
        //Download profile image

        final String path = (email + "/profile.jpg");
        if (!downloadedImages.containsKey(path)) {
            StorageReference storageReference = FirebaseStorage.getInstance().getReference();
            StorageReference imageRef = storageReference.child(path);
            try {
                final File tmpFile = File.createTempFile("img", "jpg");
                imageRef.getFile(tmpFile).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                    @Override
                    public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                        Bitmap bitmap = BitmapFactory.decodeFile(tmpFile.getAbsolutePath());
                        try {
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
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        downloadedImages.put(path, bitmap);
                        imageView.setImageBitmap(bitmap);
                    }
                });
            }
            catch(Exception e){
                e.printStackTrace();
            }
        }
        else{
            imageView.setImageBitmap(downloadedImages.get(email));
        }
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        PendingHolder holder;

        if(convertView == null){
            LayoutInflater inflater = LayoutInflater.from(getContext());
            convertView = inflater.inflate(R.layout.custom_row_pending,parent,false);
            holder = new PendingHolder();

            holder.nameView = convertView.findViewById(R.id.nameText);
            holder.imageView = convertView.findViewById(R.id.picImgView);
            holder.acceptButton = convertView.findViewById(R.id.acceptButton);
            holder.declineButton = convertView.findViewById(R.id.declineButton);
        }
        else{
            holder = (PendingHolder) convertView.getTag();
        }

        final String singleItem = getItem(position);

        holder.nameView.setText(singleItem);
        setProfileImage(holder.imageView,singleItem);

        holder.acceptButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query query = firebaseDatabase.orderByChild("email").equalTo(firebaseAuth.getCurrentUser().getEmail());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot singleSnapshot : children){
                            User user = singleSnapshot.getValue(User.class);
                            for(String date:user.pending.keySet()){
                                if(user.pending.get(date).equals(singleItem)){
                                    int temp_num = 0;
                                    for(String highest: user.partners.keySet()){
                                        if(!highest.contains("dummy")){
                                            if(Integer.parseInt(highest)>temp_num)
                                                temp_num = Integer.parseInt(highest);
                                        }

                                    }
                                    temp_num = temp_num + 1;
                                    firebaseDatabase.child(user.getUserID()).child("partners").child(Integer.toString(temp_num)).setValue(user.pending.get(date));
                                    firebaseDatabase.child(user.getUserID()).child("pending").child(date).removeValue();
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        holder.declineButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Query query = firebaseDatabase.orderByChild("email").equalTo(firebaseAuth.getCurrentUser().getEmail());
                query.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                        for (DataSnapshot singleSnapshot : children){
                            User user = singleSnapshot.getValue(User.class);
                            for(String date:user.pending.keySet()){
                                if(user.pending.get(date).equals(singleItem)){
                                    firebaseDatabase.child(user.getUserID()).child("pending").child(date).removeValue();
                                }
                            }

                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }
        });
        //How to setImage to be retrieved???
        //Use the singleItem string to retrieve it!

        return convertView;
    }
}
