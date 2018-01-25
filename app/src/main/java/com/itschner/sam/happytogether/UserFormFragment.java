package com.itschner.sam.happytogether;

import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.media.ExifInterface;
import android.support.v4.content.FileProvider;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

import static android.app.Activity.RESULT_OK;

public class UserFormFragment extends Fragment implements View.OnClickListener {

    private static final int REQUEST_IMGAE_CAPTURE = 1;
    private static final int PICK_IMAGE_REQUEST = 234;
    private EditText nameEditText;
    private TextView Uri;
    private ImageButton galleryButton;
    private ImageButton cameraButton;
    private Button uploadButton;
    private ImageView profileImage;
    private static Uri filepath;
    private ProgressDialog progressDialog;
    private static String savedName;
    private static String savedURI = "default";
    private static Bitmap picture;

    private DoneVariable done;
    private boolean userCreationDone;
    private boolean photoUploadDone;

    private StorageReference storageReference;
    private FirebaseAuth firebaseAuth;
    private FirebaseDatabase firebaseDatabase;
    private DatabaseReference databaseReference;

    private boolean hasCamera(){
        return getActivity().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    public void getCurrentUserID(){
        Query query = databaseReference.orderByChild("email").equalTo(firebaseAuth.getCurrentUser().getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot singleSnapshot : children){
                    User user = singleSnapshot.getValue(User.class);
                    Uri.setText(user.getUserID());
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void uploadPic(String name){
        if(filepath!=null && firebaseAuth.getCurrentUser() != null)
        {
            progressDialog.setTitle("Uploading...");
            progressDialog.show();
            //"UserProfile/user.jpg"

            StorageReference imageRef = storageReference.child(firebaseAuth.getCurrentUser().getEmail() + "/" + name + ".jpg" );
            imageRef.putFile(filepath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            //Get URL to uploaded Content
                            progressDialog.hide();
                            Toast.makeText(getActivity(), "Upload Successful", Toast.LENGTH_SHORT).show();
                            photoUploadDone = true;
                            done.setDone(userCreationDone,photoUploadDone);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.hide();
                            Toast.makeText(getActivity(), "Upload Failed", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0 * taskSnapshot.getBytesTransferred()/taskSnapshot.getTotalByteCount());
                            progressDialog.setMessage( (int)progress + "% Uploaded");
                        }
                    });
        }
        else if (firebaseAuth.getCurrentUser() == null){
            Toast.makeText(getActivity(), "User not Logged In", Toast.LENGTH_SHORT).show();
        }
        else if (filepath == null){
            Toast.makeText(getActivity(), "A profile picture is required", Toast.LENGTH_SHORT).show();
        }
    }

    private void createNewUser(){
        if (firebaseAuth.getCurrentUser() !=null){
            String userID = databaseReference.push().getKey();
            User newUser = new User(nameEditText.getText().toString(),userID,firebaseAuth.getCurrentUser().getEmail());
            databaseReference.child(userID).setValue(newUser)
                    .addOnSuccessListener(getActivity(),new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Toast.makeText(getContext(), "User Successfully Created", Toast.LENGTH_SHORT).show();
                            userCreationDone = true;
                            done.setDone(userCreationDone,photoUploadDone);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(getContext(), "User Creation Failed", Toast.LENGTH_SHORT).show();
                        }
                    });
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("User Data Form");

        progressDialog = new ProgressDialog(getActivity());
        profileImage = (ImageView) getView().findViewById(R.id.profileImageView);
        nameEditText = (EditText) getView().findViewById(R.id.nameEditText);
        galleryButton = (ImageButton) getView().findViewById(R.id.galleryButton);
        cameraButton = (ImageButton) getView().findViewById(R.id.cameraButton);
        Uri = (TextView) getView().findViewById(R.id.uri);
        uploadButton = (Button) getView().findViewById(R.id.uploadButton);

        storageReference = FirebaseStorage.getInstance().getReference();
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseDatabase = FirebaseDatabase.getInstance();
        databaseReference = firebaseDatabase.getReference("users");

        if (!hasCamera()){
            cameraButton.setAlpha(0);
            cameraButton.setEnabled(false);
        }

        galleryButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
        uploadButton.setOnClickListener(this);

        Uri.setText(savedURI);
        if(savedName != null) nameEditText.setText(savedName);

        if (picture != null){
            profileImage.setImageBitmap(picture);
            setPic(profileImage);
        }

        done = new DoneVariable();
        done.setListener(new DoneVariable.ChangeListener() {
            @Override
            public void onChange() {
                ((MainActivity)getActivity()).FragmentChange(R.id.fragment_place);
            }
        });

        //Resizes the fragment to fit into its layout
        /*
        View newView = getView();
        RelativeLayout.LayoutParams p = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT);
        newView.setLayoutParams(p);
        newView.requestLayout();*/
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_user_form, container, false);
    }

    private void launchCamera(View view){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getActivity().getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile();

            }catch (IOException ex){
                Toast.makeText(getActivity(), "Error while creating image", Toast.LENGTH_SHORT).show();
            }
            if(photoFile!=null){
                filepath = FileProvider.getUriForFile(getActivity(),"com.itschner.sam.happytogether",photoFile);
                i.putExtra(MediaStore.EXTRA_OUTPUT, filepath);
                //Takes a picture, passes data to onActivityResult
                startActivityForResult(i, REQUEST_IMGAE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getActivity().getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        return image;
    }

    private void showFileChooser(){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(i.ACTION_GET_CONTENT);
        startActivityForResult(i.createChooser(i,"Select Image"),PICK_IMAGE_REQUEST);
    }

    public int dpToPx(int dp) {
        float density = getActivity().getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    private void setPic (ImageView view){
        Bitmap bitmap = null;

        try {
            Drawable drawing = view.getDrawable();
            bitmap = ((BitmapDrawable) drawing).getBitmap();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("No drawable on given view");
        } catch (ClassCastException e) {

        }

        // Get current dimensions AND the desired bounding box
        int width = 0;

        try {
            width = bitmap.getWidth();
        } catch (NullPointerException e) {
            throw new NoSuchElementException("Can't find bitmap on given view/drawable");
        }

        int height = bitmap.getHeight();
        int bounding = dpToPx(400);
        Log.i("Test", "original width = " + Integer.toString(width));
        Log.i("Test", "original height = " + Integer.toString(height));
        Log.i("Test", "bounding = " + Integer.toString(bounding));

        // Determine how much to scale: the dimension requiring less scaling is
        // closer to the its side. This way the image always stays inside your
        // bounding box AND either x/y axis touches it.
        float xScale = ((float) bounding) / width;
        float yScale = ((float) bounding) / height;
        float scale = (xScale <= yScale) ? xScale : yScale;
        Log.i("Test", "xScale = " + Float.toString(xScale));
        Log.i("Test", "yScale = " + Float.toString(yScale));
        Log.i("Test", "scale = " + Float.toString(scale));

        // Create a matrix for the scaling and add the scaling data
        Matrix matrix = new Matrix();
        matrix.postScale(scale, scale);

        // Create a new bitmap and convert it to a format understood by the ImageView
        Bitmap scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, width, height, matrix, true);
        width = scaledBitmap.getWidth(); // re-use
        height = scaledBitmap.getHeight(); // re-use
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);
        Log.i("Test", "scaled width = " + Integer.toString(width));
        Log.i("Test", "scaled height = " + Integer.toString(height));

        // Apply the scaled bitmap
        view.setImageDrawable(result);

        // Now change ImageView's dimensions to match the scaled image
        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) view.getLayoutParams();
        params.gravity = Gravity.CENTER;
        params.width = width;
        params.height = height;
        view.setLayoutParams(params);

        Log.i("Test", "done");
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filepath = data.getData();
            savedURI = filepath.toString();
            savedName = nameEditText.getText().toString().trim();
            picture = rotateImage(filepath);
            //Uri.setText(filepath.toString());
            //profileImage.setImageBitmap(rotateImage(filepath));
            //setPic(profileImage);
        }
        else if(requestCode == REQUEST_IMGAE_CAPTURE && resultCode == RESULT_OK){
            picture = rotateImage(filepath);
            savedURI = filepath.toString();
            savedName = nameEditText.getText().toString().trim();

            //profileImage.setImageBitmap(rotateImage(filepath));
            //setPic(profileImage);
            //Uri.setText(filepath.toString());
            /*

            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            profileImage.setImageBitmap(photo);
            */
        }
    }

    public static int calculateInSampleSize(int inWidth,int inHeight, int reqWidth, int reqHeight) {
        // Raw height and width of image
        int inSampleSize = 1;

        if (inHeight > reqHeight || inWidth > reqWidth) {

            final int halfHeight = inHeight / 2;
            final int halfWidth = inWidth / 2;

            // Calculate the largest inSampleSize value that is a power of 2 and keeps both
            // height and width larger than the requested height and width.
            while ((halfHeight / inSampleSize) >= reqHeight
                    && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2;
            }
        }

        return inSampleSize;
    }

    //Scale down image here
    public Bitmap rotateImage (Uri filepath) {
        Bitmap bitmap = null;
        try{
            bitmap = MediaStore.Images.Media.getBitmap(getActivity().getContentResolver(), filepath);
            InputStream inputStream = getActivity().getContentResolver().openInputStream(filepath);
            ExifInterface ei = new ExifInterface(inputStream);
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
                    bitmap = rotateImage(bitmap, 0);
            }
        }
        catch (IOException e){
            Toast.makeText(getActivity(), "Rotate Image Failed", Toast.LENGTH_SHORT).show();
        }
        return bitmap;
    }

    public Bitmap rotateImage(Bitmap source,float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        return Bitmap.createBitmap(source,0,0,source.getWidth(),source.getHeight(),matrix,true);
    }


    @Override
    public void onClick(View view) {
        if(view == galleryButton){
            showFileChooser();
        }
        else if(view == cameraButton && hasCamera()){
            launchCamera(cameraButton);
        }
        else if(view == uploadButton){
            if (!nameEditText.getText().toString().isEmpty()){
                uploadPic("profile");
                createNewUser();

                //
                //((MainActivity)getActivity()).FragmentChange(R.id.fragment_place);
                //


                //DatabaseReference ref = databaseReference;
                //ref = ref.child("-L0LnzDbOhM_VHvGTQdf").child("pending").child("0");
                //ref.setValue("Updated");
                //getCurrentUserID();
            }
        }
    }
}
