package com.itschner.sam.happytogether;


import android.app.AlertDialog;
import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SimpleTimeZone;

public class LoggedInNewFragment extends Fragment implements View.OnClickListener {

    private Button inviteButton;
    private DatabaseReference firebaseDatabase;
    private DatabaseReference ref;
    private FirebaseAuth firebaseAuth;
    private Calendar c = Calendar.getInstance();
    private SimpleDateFormat df = new SimpleDateFormat("yy-MM-dd HH-mm-ss");

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
        firebaseDatabase = FirebaseDatabase.getInstance().getReference("users");
        firebaseAuth = FirebaseAuth.getInstance();
        inviteButton = (Button) getView().findViewById(R.id.inviteButton);
        getActivity().setTitle("Home");

        inviteButton.setOnClickListener(this);
        Query query = firebaseDatabase.orderByChild("email").equalTo(firebaseAuth.getCurrentUser().getEmail());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                for (DataSnapshot singleSnapshot : children){
                    User user = singleSnapshot.getValue(User.class);

                    List<String> invites = new ArrayList<>(user.pending.values());
                    for (String invite:invites) {
                        getActivity().setTitle(invite);
                    }
                }
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    public void InviteAlert(){
        ref = firebaseDatabase.child("users");
        final AlertDialog dialog = new AlertDialog.Builder(getActivity()).create();
        LayoutInflater inflater = LayoutInflater.from(getActivity());
        View view = inflater.inflate(R.layout.dialog_invite_form,null);
        final EditText email = (EditText) view.findViewById(R.id.inviteEmail);
        Button inviteButton = (Button) view.findViewById(R.id.sendInvite);
        inviteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!email.getText().toString().isEmpty()){
                    //Probably not working because of casting to User does not work because of missing pending list
                    //Probably going to have to use an update or enter dummy values upon user creation
                    String emailText = email.getText().toString();
                    final String date = df.format(c.getTime());
                    Query query = ref.orderByChild("email").equalTo(emailText);
                    query.addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            Iterable<DataSnapshot> children = dataSnapshot.getChildren();
                            for(DataSnapshot singleSnapshot : children){
                                User recUser = singleSnapshot.getValue(User.class);
                                ref = ref.child(recUser.userID).child("pending");
                                ref.child(date).setValue(firebaseAuth.getCurrentUser().getEmail());
                                Toast.makeText(getContext(), "Sent", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });
                    //TODO: Add cancel button as well, get the email and query with it
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
