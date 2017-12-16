package com.itschner.sam.happytogether;

import android.app.Fragment;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.content.Intent;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;

public class MainActivity extends Template {

    private Boolean oneShot = false;

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

    public void FragmentChange(){
        Fragment fragment;


    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Happy Together");
        firebaseAuth = FirebaseAuth.getInstance();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        navigation.setOnNavigationItemSelectedListener(mOnNavigationItemSelectedListener);
    }

    @Override
    public void onResume(){
        super.onResume();
        BottomNavigationView navigation = (BottomNavigationView) findViewById(R.id.navigation);
        if (firebaseAuth.getCurrentUser() == null && !oneShot){
            navigation.getMenu().removeItem(R.menu.logged_in_new);
            navigation.inflateMenu(R.menu.not_logged_in);
            navigation.setVisibility(View.VISIBLE);
            navigation.setActivated(true);
            navigation.setSelectedItemId(R.id.navigation_login);
            Toast.makeText(this, "A user must be logged in", Toast.LENGTH_SHORT).show();
            oneShot = true;
        }
        else if(firebaseAuth.getCurrentUser() != null && oneShot){
            navigation.getMenu().removeItem(R.menu.not_logged_in);
            /*navigation.setVisibility(View.INVISIBLE);
            navigation.setActivated(false);*/
            navigation.inflateMenu(R.menu.logged_in_new);
            oneShot = false;
        }
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
                return true;
        }
        return false;
    }
}
