package com.itschner.sam.happytogether;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
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

import org.w3c.dom.Text;

public class Login extends Template implements View.OnClickListener{

    private EditText emailText;
    private EditText passwordText;
    private Button submitButton;
    private ProgressDialog progressDialog;
    private TextView toRegister;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setTitle("Login");
        firebaseAuth = FirebaseAuth.getInstance();

        /*if (firebaseAuth.getCurrentUser() != null){
            Intent i = new Intent(Login.this,UserInfoForm.class);
            startActivity(i);
        }*/

        if(firebaseAuth.getCurrentUser() != null){
            Toast.makeText(this, "Already logged in. Logout to switch users!", Toast.LENGTH_SHORT).show();
            NavUtils.navigateUpFromSameTask(Login.this);
        }


        emailText = (EditText) findViewById(R.id.email);
        passwordText = (EditText) findViewById(R.id.password);
        submitButton = (Button) findViewById(R.id.submit);
        toRegister = (TextView) findViewById(R.id.textViewLogin);

        progressDialog = new ProgressDialog(this);

        submitButton.setOnClickListener(this);
        toRegister.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == submitButton){
            userLogin();
        }
        else if (view == toRegister){
            finish();
            Intent i = new Intent(this,RegisterUser.class);
            startActivity(i);
            overridePendingTransition(android.R.anim.fade_in,android.R.anim.fade_out);
        }
    }

    private void userLogin(){
        String email = emailText.getText().toString().trim();
        String pass = passwordText.getText().toString().trim();
        if (email.isEmpty()){
            Toast.makeText(this, "Please enter a valid email", Toast.LENGTH_SHORT).show();
            return;
        }
        if (pass.isEmpty()){
            Toast.makeText(this, "Please enter a password", Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog.setMessage("Logging in...");
        progressDialog.show();

        firebaseAuth.signInWithEmailAndPassword(email,pass).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()){
                    Toast.makeText(Login.this, "Login Sucessful", Toast.LENGTH_SHORT).show();
                    NavUtils.navigateUpFromSameTask(Login.this);
                }
                else{
                    Toast.makeText(Login.this, "Login failed", Toast.LENGTH_SHORT).show();
                }
                progressDialog.dismiss();
            }
        });

    }
}
