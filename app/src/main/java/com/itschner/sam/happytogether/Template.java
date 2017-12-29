package com.itschner.sam.happytogether;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.media.ExifInterface;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;

public abstract class Template extends AppCompatActivity {

    public FirebaseAuth firebaseAuth;

    //Reloads Logout in action bar whenever screen shows
    @Override
    public void onResume(){
        super.onResume();
        invalidateOptionsMenu();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch(item.getItemId()){
            case R.id.action_logout:
                firebaseAuth.signOut();
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                return true;
        }
        return false;
    }

    //Is called when invalidateoptionsmenu is called
    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if (firebaseAuth != null){
            if (firebaseAuth.getCurrentUser()!=null && menu.findItem(R.id.action_logout)==null){
                MenuInflater inflater = getMenuInflater();
                inflater.inflate(R.menu.logout,menu);}

            if (menu.findItem(R.id.action_logout)!=null && firebaseAuth.getCurrentUser()==null){
                menu.removeItem(R.id.action_logout);
            }
        }
        return true;
    }

    //Checks if user is logged in and redirects them
    public void RedirectToLogin (AppCompatActivity activity,FirebaseAuth firebaseAuth){
        if (firebaseAuth.getCurrentUser() == null){
            Intent i = new Intent(activity,Login.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }
    }

    public Bitmap rotateImage (Uri filepath) {
        Bitmap bitmap = null;
        try{
            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filepath);
            InputStream inputStream = getContentResolver().openInputStream(filepath);
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
            }
        }
        catch (IOException e){
            Toast.makeText(this, "Nope", Toast.LENGTH_SHORT).show();
        }
        return bitmap;
    }

    public static Bitmap rotateImage(Bitmap source,float angle){
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);
        return Bitmap.createBitmap(source,0,0,source.getWidth(),source.getHeight(),matrix,true);
    }

    public boolean MenuSelect(AppCompatActivity activity1,Class active){
        Intent i = new Intent(activity1,active);
        startActivity(i);
        overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        return true;
    }
}
