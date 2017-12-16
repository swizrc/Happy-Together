package com.itschner.sam.happytogether;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.content.FileProvider;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.NoSuchElementException;

//Make into a fragment

public class UserInfoForm extends Template implements View.OnClickListener {
    private static final int REQUEST_IMGAE_CAPTURE = 1;
    private static final int PICK_IMAGE_REQUEST = 234;
    private EditText nameEditText;
    private TextView Uri;
    private ImageButton galleryButton;
    private ImageButton cameraButton;
    private ImageView profileImage;
    private Uri filepath;
    private ProgressDialog progressDialog;
    private String currentPhotoPath;

    private StorageReference storageReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_info_form);
        setTitle("Enter User Details");

        storageReference = FirebaseStorage.getInstance().getReference();

        progressDialog = new ProgressDialog(this);
        profileImage = (ImageView) findViewById(R.id.profileImageView);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        galleryButton = (ImageButton) findViewById(R.id.galleryButton);
        cameraButton = (ImageButton) findViewById(R.id.cameraButton);
        Uri = (TextView) findViewById(R.id.uri);

        if (!hasCamera()){
            cameraButton.setAlpha(0);
            cameraButton.setEnabled(false);
        }

        galleryButton.setOnClickListener(this);
        cameraButton.setOnClickListener(this);
    }

    private int dpToPx(int dp) {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null){
            filepath = data.getData();
            Uri.setText(filepath.toString());
            profileImage.setImageBitmap(rotateImage(filepath));
            setPic(profileImage);
        }
        else if(requestCode == REQUEST_IMGAE_CAPTURE && resultCode == RESULT_OK){
            profileImage.setImageBitmap(rotateImage(filepath));
            setPic(profileImage);
            Uri.setText(filepath.toString());
            /*
            Bundle extras = data.getExtras();
            Bitmap photo = (Bitmap) extras.get("data");
            profileImage.setImageBitmap(photo);
            */
        }
    }

    //TODO: Relate upload URL to User info
    private void UploadPic(String name){
        if(filepath!=null)
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
                            Toast.makeText(UserInfoForm.this, "Upload Successful", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            progressDialog.hide();
                            Toast.makeText(UserInfoForm.this, "Upload Failed", Toast.LENGTH_SHORT).show();
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
        else{
            Toast.makeText(this, "Upload Failed", Toast.LENGTH_SHORT).show();
        }
    }

    private void launchCamera(View view){
        Intent i = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (i.resolveActivity(getPackageManager()) != null) {
            File photoFile = null;
            try{
                photoFile = createImageFile();
            }catch (IOException ex){
                Toast.makeText(this, "Error while creating image", Toast.LENGTH_SHORT).show();
            }
            if(photoFile!=null){
                filepath = FileProvider.getUriForFile(this,"com.itschner.sam.happytogether",photoFile);
                i.putExtra(MediaStore.EXTRA_OUTPUT, filepath);
                //Takes a picture, passes data to onActivityResult
                startActivityForResult(i, REQUEST_IMGAE_CAPTURE);
            }
        }
    }

    private File createImageFile() throws IOException{
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(imageFileName,".jpg",storageDir);

        currentPhotoPath = image.getAbsolutePath();
        return image;
    }

    private void showFileChooser(){
        Intent i = new Intent();
        i.setType("image/*");
        i.setAction(i.ACTION_GET_CONTENT);
        startActivityForResult(i.createChooser(i,"Select Image"),PICK_IMAGE_REQUEST);
    }

    private boolean hasCamera(){
        return getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_ANY);
    }

    @Override
    public void onClick(View view) {
        if(view == galleryButton){
            showFileChooser();
        }
        else if(view == cameraButton && hasCamera()){
            launchCamera(cameraButton);
        }
    }
}
