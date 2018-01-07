package com.itschner.sam.happytogether;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;


import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends Template {

    private Boolean oneShot = false;
    private FirebaseDatabase database;
    private DatabaseReference databaseReference;
    private BottomNavigationView navigation;

    @Override
    public boolean onPrepareOptionsMenu(Menu menu){
        if (firebaseAuth.getCurrentUser()!=null && menu.findItem(R.id.action_logout)==null){
            MenuInflater inflater = getMenuInflater();
            inflater.inflate(R.menu.logout,menu);}
        if (menu.findItem(R.id.action_logout)!=null && firebaseAuth.getCurrentUser()==null){
            menu.removeItem(R.id.action_logout);
        }
        return true;
    }

    private BottomNavigationView.OnNavigationItemSelectedListener mOnNavigationItemSelectedListener
            = new BottomNavigationView.OnNavigationItemSelectedListener() {
        @Override
        public boolean onNavigationItemSelected(@NonNull MenuItem item) {
            switch (item.getItemId()) {
                case R.id.navigation_login:
                    return MenuSelect(MainActivity.this,Login.class);
                case R.id.navigation_register:
                    if (firebaseAuth.getCurrentUser() == null){
                        return MenuSelect(MainActivity.this,RegisterUser.class);
                    }
                    else{
                        Toast.makeText(MainActivity.this, "Cannot Register while Logged In", Toast.LENGTH_SHORT).show();
                    }
            }
            return false;
        }
    };

    public void FragmentChange(final int frag_id){
        if(firebaseAuth.getCurrentUser() != null) {
            Query query = databaseReference.orderByChild("email").equalTo(firebaseAuth.getCurrentUser().getEmail());
            query.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                    if (dataSnapshot.getChildrenCount() == 0){
                        Fragment fragment = new UserFormFragment();
                        android.app.FragmentManager fm = getFragmentManager();
                        FragmentTransaction ft = fm.beginTransaction();
                        ft.replace(frag_id, fragment);
                        ft.commit();
                    }
                    else{
                        for (DataSnapshot singleSnapshot : children) {
                            User user = singleSnapshot.getValue(User.class);
                            if (user.Fname != null && user.Lname != null) {
                                Fragment fragment = new LoggedInNewFragment();
                                android.app.FragmentManager fm = getFragmentManager();
                                FragmentTransaction ft = fm.beginTransaction();
                                ft.replace(frag_id, fragment);
                                ft.commit();
                            }
                            else {
                                Fragment fragment = new UserFormFragment();
                                android.app.FragmentManager fm = getFragmentManager();
                                FragmentTransaction ft = fm.beginTransaction();
                                ft.replace(frag_id, fragment);
                                ft.commit();
                            }
                        }
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {

                }
            });
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Happy Together");
        firebaseAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        databaseReference = database.getReference().child("users");

        navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onResume(){
        super.onResume();
        if (firebaseAuth.getCurrentUser() == null && !oneShot){
            navigation.getMenu().removeItem(R.menu.logged_in_new);
            navigation.inflateMenu(R.menu.not_logged_in);
            navigation.setVisibility(View.VISIBLE);
            navigation.setActivated(true);
            RedirectToLogin(this,firebaseAuth);
            Toast.makeText(this, "A user must be logged in", Toast.LENGTH_SHORT).show();
            oneShot = true;
            if(oneShot){
                setTitle("True");
            }else{
                setTitle("False");
            }
        }
        else if(firebaseAuth.getCurrentUser() != null && oneShot){
            navigation.getMenu().removeItem(R.menu.not_logged_in);
            /*navigation.setVisibility(View.INVISIBLE);
            navigation.setActivated(false);*/
            navigation.inflateMenu(R.menu.logged_in_new);
            oneShot = false;
        }
        else if(firebaseAuth.getCurrentUser() == null){
            RedirectToLogin(this,firebaseAuth);
            Toast.makeText(this, "A user must be logged in", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResumeFragments() {
        super.onResumeFragments();
        FragmentChange(R.id.fragment_place);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        switch(item.getItemId()){
            case R.id.action_logout:
                firebaseAuth.signOut();
                Toast.makeText(this, "Logged Out", Toast.LENGTH_SHORT).show();
                invalidateOptionsMenu();
                navigation.inflateMenu(R.menu.not_logged_in);
                navigation.setVisibility(View.VISIBLE);
                navigation.setActivated(true);
                oneShot = true;
                RedirectToLogin(this,firebaseAuth);
                return true;
        }
        return false;
    }
}
