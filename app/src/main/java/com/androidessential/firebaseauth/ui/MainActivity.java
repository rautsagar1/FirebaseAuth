package com.androidessential.firebaseauth.ui;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import com.androidessential.firebaseauth.R;
import com.androidessential.firebaseauth.model.User;
import com.androidessential.firebaseauth.utility.Constants;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class MainActivity extends BaseActivity {

    private DatabaseReference mUserRef;
    private ValueEventListener mUserRefListener;
    TextView textView ;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


    }

    public void onlogoutpressed(View view) {

        FirebaseAuth.getInstance().signOut();


    }

}
