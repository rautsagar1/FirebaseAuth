package com.androidessential.firebaseauth.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidessential.firebaseauth.R;
import com.androidessential.firebaseauth.utility.Constants;
import com.androidessential.firebaseauth.utility.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
/**
 * Represents Sign in screen and functionality of the app
 */

public class LoginActivity extends BaseActivity {
    private static final String LOG_TAG = LoginActivity.class.getSimpleName();

    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextEmailInput, mEditTextPasswordInput;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private SharedPreferences mSharedPref;
    private SharedPreferences.Editor mSharedPrefEditor;
    boolean verifiedStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mSharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        mSharedPrefEditor = mSharedPref.edit();

        initializeScreen();
        // START initialize_auth
        mAuth = FirebaseAuth.getInstance();
        // START auth_state_listener
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null ) {
                    if(user.isEmailVerified()) {
                         Log.d(LOG_TAG, "verification : checking email verified " + user.isEmailVerified());
                         String provider = user.getProviders().get(0);
                         if (provider.equals(Constants.PASSWORD_PROVIDER)) {
                             setAuthenticatedUserPasswordProvider(user);
                             Log.d(LOG_TAG, "verification : password provider is email verified: " + user.isEmailVerified());
                         } else {
                             Log.e(LOG_TAG, getString(R.string.log_error_invalid_provider) + provider);
                         }

                  /* Save provider name and encodedEmail for later use and start MainActivity */
                         mSharedPrefEditor.putString(Constants.KEY_PROVIDER, provider).apply();
                         mSharedPrefEditor.putString(Constants.KEY_ENCODED_EMAIL, mEncodedEmail).apply();

                         Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                         intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                         startActivity(intent);
                         Log.d(LOG_TAG, " Verification : opening mai activity:" + user.getEmail());
                         finish();

                     } else {
                         // if user Email not verified then open Emailverification activity .
                        Log.d(LOG_TAG, " Verification : emai verified" + user.isEmailVerified());
                        Intent intent = new Intent(LoginActivity.this, EmailVerification.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        Log.d(LOG_TAG, " Verification : not verified");
                        finish();
                    }
                    } else {
                    // User is signed out
                    Log.d(LOG_TAG, " Verification : user signed_out");
                }
            }
        };

        /**
         * Call signInPassword() when user taps "Done" keyboard action
         */
        mEditTextPasswordInput.setOnEditorActionListener
                (new TextView.OnEditorActionListener() {
                     @Override
                     public boolean onEditorAction(TextView textView, int actionId, KeyEvent keyEvent) {

                         if (actionId == EditorInfo.IME_ACTION_DONE || keyEvent.getAction() == KeyEvent.ACTION_DOWN) {
                             signInPassword();
                         }
                         return true;
                     }
                 }
                );
    }

    // [START on_start_add_listener]

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    // [END on_start_add_listener]
    // [START on_stop_remove_listener]
    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    // [END on_stop_remove_listener]
    @Override
    protected void onResume() {
        super.onResume();
        /**
         * Get the newly registered user email if present, use null as default value
         */
        String signupEmail = mSharedPref.getString(Constants.KEY_SIGNUP_EMAIL, null);
        if (signupEmail != null) {
            mEditTextEmailInput.setText(signupEmail);
            /**
             * Clear signupEmail sharedPreferences to make sure that they are used just once
             */
            mSharedPrefEditor.putString(Constants.KEY_SIGNUP_EMAIL, null).apply();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
    }
    /**
     * Override onCreateOptionsMenu to inflate nothing
     *
     * @param menu The menu with which nothing will happen
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return true;
    }

    /**
     * Sign in with Password provider when user clicks sign in button
     */
    public void onSignInPressed(View view) {
        signInPassword();
    }

    /**
     * Open CreateAccountActivity when user taps on "Create Account" TextView
     */
    public void onSignUpPressed(View view) {
        Intent intent = new Intent(LoginActivity.this, CreateAccountActivity.class);
        startActivity(intent);
    }

    /**
     * start password reset flow when user press forget password
     * @param view
     */
    public void onForgetPasswordPressed(View view){
        Log.d(LOG_TAG, "forget Paasword pressed");
        Intent intent = new Intent(LoginActivity.this, ResetPassword.class);
        startActivity(intent);
        Log.d(LOG_TAG, "forget Paasword going to rsetpassword activity");
            }


    /**
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextEmailInput = (EditText) findViewById(R.id.edit_text_email);
        mEditTextPasswordInput = (EditText) findViewById(R.id.edit_text_password);
        LinearLayout linearLayoutLoginActivity = (LinearLayout) findViewById(R.id.linear_layout_login_activity);
        initializeBackground(linearLayoutLoginActivity);

        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getString(R.string.progress_dialog_authenticating_with_firebase));
        mAuthProgressDialog.setCancelable(false);

          }
    /**
     * Sign in with Password provider (used when user taps "Done" action on keyboard)
     */
    public void signInPassword() {
       final  String email = mEditTextEmailInput.getText().toString();
        String password = mEditTextPasswordInput.getText().toString();
        if (email.equals("")) {
            mEditTextEmailInput.setError(getString(R.string.error_cannot_be_empty));
            return;
        }
        if (password.equals("")) {
            mEditTextPasswordInput.setError(getString(R.string.error_cannot_be_empty));
            return;
        }
        mAuthProgressDialog.show();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        Log.d(LOG_TAG, " Verification : signIn With Email:onComplete:" + task.isSuccessful());
                        // If siin fails, display a message to the user. If sign in succeeds
                        // the auth state listener will be notified and logic to handle the
                        // signed in user can be handled in the listener.
                        if (!task.isSuccessful()) {
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthInvalidUserException e) {
                                mEditTextEmailInput.setError(getString(R.string.error_message_email_issue));
                                mEditTextEmailInput.requestFocus();
                            } catch (FirebaseAuthInvalidCredentialsException e) {
                                Log.d(LOG_TAG, "email :" + email);
                                mEditTextPasswordInput.setError(e.getMessage());
                                mEditTextPasswordInput.requestFocus();
                            } catch (FirebaseNetworkException e) {
                                showErrorToast(getString(R.string.error_message_failed_sign_in_no_network));
                            } catch (Exception e) {
                                Log.e(LOG_TAG, e.getMessage());
                            }
                            Log.w(LOG_TAG, "signInWithEmail:failed", task.getException());
                            Toast.makeText(LoginActivity.this,"login error",
                                    Toast.LENGTH_SHORT).show();
                        }
                        mAuthProgressDialog.dismiss();
                    }
                });
    }
    /**
     * Helper method that makes sure a user is created if the user
     * logs in with Firebase's email/password provider.
     *
     * @param user firebaseUser object returned from onAuthenticated
     */
    private void setAuthenticatedUserPasswordProvider(FirebaseUser user) {
        Log.d(LOG_TAG, "Verification : set auth user password method starts ");

        final String unprocessedEmail = user.getEmail().toLowerCase();
        mEncodedEmail = Utils.encodeEmail(unprocessedEmail);
    }


    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_LONG).show();
    }
}