package com.androidessential.firebaseauth.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.androidessential.firebaseauth.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class ResetPassword extends BaseActivity {
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private ProgressDialog mAuthProgressDialog;
    private static final String LOG_TAG = ResetPassword.class.getSimpleName();
    private EditText mEditTextEmailInput;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(LOG_TAG, "forget reset activity created");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);
        mAuth = FirebaseAuth.getInstance();

        mEditTextEmailInput = (EditText) findViewById(R.id.edit_text_email_reset_password);
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_resetting_Password_with_firebase));
        mAuthProgressDialog.setCancelable(false);

    }

    public void OnResetPasswordPress(View view) {
        final String email = mEditTextEmailInput.getText().toString();
        mAuthProgressDialog.show();
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (!task.isSuccessful()) {
                    mAuthProgressDialog.dismiss();
                    try {
                        throw task.getException();
                    } catch (Exception e) {
                        mEditTextEmailInput.setError(getString(R.string.error_something_went_wrong));
                        Log.d(LOG_TAG, "Email not sent" + e.getMessage());
                    }
                } else {
                    Toast.makeText(ResetPassword.this, "Password reset Link is sent to your Registred E-mail id ", Toast.LENGTH_LONG).show();

                    Intent intent = new Intent(ResetPassword.this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                    finish();

                    mAuthProgressDialog.dismiss();
                }
            }
        });

    }
}






