package com.androidessential.firebaseauth.ui;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.view.View;
import android.widget.Toast;

import com.androidessential.firebaseauth.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class EmailVerification extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_verification);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.EmailVerification_continue_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                // sign out user to reload verification var (as in firebase current version we need to sign out user or else verification is always false)
                FirebaseAuth.getInstance().signOut();
            }
        });
    }

    public void OnResendEmailPress(View view) {

        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {

         @Override
          public void onComplete(@NonNull Task<Void> task) {
            if (task.isSuccessful()) {
       // after email is sent just logout the user and finish this activity
            Toast.makeText(EmailVerification.this, " A verification Emmail was sent to" + user.getEmail() + "Please Check Your Inbox..! ", Toast.LENGTH_LONG).show();
      // sign out user
           FirebaseAuth.getInstance().signOut();
           Intent intent = new Intent(EmailVerification.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
            } else {
        // email not sent, so display message and restart the activity or do whatever you wish to do

            Toast.makeText(EmailVerification.this, " Oh Crap..! something not working ", Toast.LENGTH_LONG).show();
            //restart this activity
            overridePendingTransition(0, 0);
            finish();
            overridePendingTransition(0, 0);
            startActivity(getIntent());
            }
          }
        }
      );
  }

}
