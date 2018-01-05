package com.itschner.sam.happytogether;


import android.app.AlertDialog;
import android.app.Fragment;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.support.media.ExifInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

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
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class LoggedInNewFragment extends Fragment implements View.OnClickListener {

    private Button inviteButton;
    private TextView userNameTextView;
    private ImageView profileImageView;
    private ListView pendingList;
    private ListView partnerList;
    private DatabaseReference firebaseDatabase;
    private DatabaseReference ref;
    private FirebaseAuth firebaseAuth;
    private StorageReference storageReference;
    private Calendar c = Calendar.getInstance();
    private SimpleDateFormat df = new SimpleDateFormat("yy/MM/dd HH:mm:ss");

    public LoggedInNewFragment() {
        // Required empty public constructor
    }


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_logged_in_new, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();
        inviteButton = (Button) getView().findViewById(R.id.inviteButton);
        userNameTextView = getView().findViewById(R.id.userName);
        profileImageView = getView().findViewById(R.id.profileImageView);
        pendingList = getView().findViewById(R.id.pendingListView);
        partnerList = getView().findViewById(R.id.partnerListView);

        getActivity().setTitle("Home");

        //Download profile image
        StorageReference imageRef = storageReference.child(firebaseAuth.getCurrentUser().getEmail() + "/profile.jpg" );
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
                    profileImageView.setImageBitmap(bitmap);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }

        inviteButton.setOnClickListener(this);

        //TESTING for reading pending list
        //{
            Query query = firebaseDatabase.orderByChild("email").equalTo(firebaseAuth.getCurrentUser().getEmail());
            query.addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    for (DataSnapshot singleSnapshot : children){
                        User user = singleSnapshot.getValue(User.class);

                        String Name = "Hello "+ user.Fname + " " +user.Lname + "!";
                        userNameTextView.setText(Name);

                        List<String> invites = new ArrayList<>(user.pending.values());
                        List<String> partners = new ArrayList<>(user.partners.values());

                        for (String invite:invites) {
                            if (invite.contentEquals("dummy")){
                                invites.remove(invite);
                            }
                        }

                        for (String partner:partners) {
                            if (partner.contentEquals("dummy")){
                                partners.remove(partner);
                            }
                        }

                        String[] pendingArray = new String[invites.size()];
                        pendingArray = invites.toArray(pendingArray);

                        String[] partnerArray = new String[partners.size()];
                        partnerArray = partners.toArray(partnerArray);

                        if (partnerArray != null && pendingArray != null){
                            ListAdapter pendingAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,pendingArray);
                            ListAdapter partnerAdapter = new ArrayAdapter<>(getContext(),android.R.layout.simple_list_item_1,partnerArray);

                            partnerList.setAdapter(partnerAdapter);
                            pendingList.setAdapter(pendingAdapter);
                        }
                    }
                }
                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        //}
    }

    public static Bitmap rotateImage(Bitmap source,float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source,0,0,source.getWidth(),source.getHeight(),matrix,true);
    }

    public void InviteAlert(){
        ref = firebaseDatabase;
        final Context context = getContext();
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_invite_form,null);
        final EditText email = (EditText) view.findViewById(R.id.inviteEmail);
        Button inviteButton = (Button) view.findViewById(R.id.sendInvite);
        Button cancelButton = (Button) view.findViewById(R.id.cancelButton);
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!email.getText().toString().isEmpty()){
                    //FIXED
                    final String emailText = email.getText().toString().trim();
                    final String date = df.format(c.getTime());
                    Query query = ref.orderByChild("email").equalTo(emailText);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            if (dataSnapshot.getChildrenCount() == 0){
                                Toast.makeText(context, "This user's email was not found", Toast.LENGTH_SHORT).show();
                            }
                            else {
                                for (DataSnapshot singleSnapshot : children) {
                                    if (!firebaseAuth.getCurrentUser().getEmail().contains(emailText)) {
                                        User recUser = singleSnapshot.getValue(User.class);
                                        ref = ref.child(recUser.userID).child("pending");
                                        ref.child(date).setValue(firebaseAuth.getCurrentUser().getEmail());
                                        Toast.makeText(context, "Sent", Toast.LENGTH_SHORT).show();
                                        dialog.dismiss();
                                    }
                                    else {
                                        Toast.makeText(context, "Cannot send invite to self!", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            }
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                }else {
                    Toast.makeText(getContext(), "Please Enter the user's email", Toast.LENGTH_SHORT).show();
                }
            }
        });
        dialog.setView(view);
        dialog.show();
    }

    @Override
    public void onClick(View view) {
        if (view == inviteButton){
            InviteAlert();
        }
    }
}
