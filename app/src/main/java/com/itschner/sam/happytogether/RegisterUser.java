package com.itschner.sam.happytogether;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

public class RegisterUser extends Template implements View.OnClickListener {

    private EditText emailText;
    private EditText passwordText;
    private Button submitButton;
    private TextView loginText;
    private ProgressDialog progressDialog;

    private List<Boolean> checkList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register_user);
        setTitle("Register");
        firebaseAuth = FirebaseAuth.getInstance();

        progressDialog = new ProgressDialog(this);

        submitButton = (Button) findViewById(R.id.submit);
        passwordText = (EditText) findViewById(R.id.password);
        emailText = (EditText) findViewById(R.id.email);
        loginText = findViewById(R.id.toLogin);

        submitButton.setOnClickListener(this);
        loginText.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        if (item.getItemId() == android.R.id.home){
            finish();
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
            return true;
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view == submitButton){
            final String email = emailText.getText().toString().trim();
            final String password = passwordText.getText().toString().trim();
            if (email.isEmpty()){
                Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
                return;
            }
            if (password.isEmpty()){
                Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
                return;
            }

            progressDialog.setMessage("Registering...");
            progressDialog.show();

            firebaseAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast.makeText(RegisterUser.this, "Registered Successfully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(RegisterUser.this, "Register Failed", Toast.LENGTH_SHORT).show();
                    }
                    progressDialog.setMessage("Logging you in...");
                    firebaseAuth.signInWithEmailAndPassword(email, password)
                            .addOnCompleteListener(RegisterUser.this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (task.isSuccessful()) {
                                        Toast.makeText(RegisterUser.this, "Log in Success", Toast.LENGTH_SHORT).show();
                                        NavUtils.navigateUpFromSameTask(RegisterUser.this);
                                    } else {
                                        Toast.makeText(RegisterUser.this, "Log in Failed", Toast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                    progressDialog.hide();
                }
            });
        }
        else if(view == loginText){
            finish();
            Intent i = new Intent(this,Login.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }
    }
}
