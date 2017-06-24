package com.androidessential.firebaseauth.ui;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.androidessential.firebaseauth.R;
import com.androidessential.firebaseauth.model.User;
import com.androidessential.firebaseauth.utility.Constants;
import com.androidessential.firebaseauth.utility.Utils;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

//import com.udacity.firebase.shoppinglistplusplus.model.user;

/**
 * Represents Sign up screen and functionality of the app
 */
public class CreateAccountActivity extends BaseActivity {
    private static final String LOG_TAG = CreateAccountActivity.class.getSimpleName();
    private ProgressDialog mAuthProgressDialog;
    private EditText mEditTextUsernameCreate, mEditTextEmailCreate, mEditTextPasswordCreate;
    private String mUserName, mUserEmail, mPassword;
    private DatabaseReference mdatabaseRef;


    private FirebaseAuth mAuth;

    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_account);

        /**
         * creating a firebase refrence
         */
        mdatabaseRef = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL);

        /**
         * Link layout elements from XML and setup the progress dialog
         */
        initializeScreen();
        mAuth = FirebaseAuth.getInstance();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();

                if (user != null) {
                    // User is signed in
                    // NOTE: this Activity should get open only when the user is not signed in, otherwise
                    // the user will receive another verification email. 

                    sendVerificationEmail();
                } else {
                    // User is signed out
                    Log.d(LOG_TAG, "onAuthStateChanged:signed_out");
                }

            }
        };
    }


    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }

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
     * Link layout elements from XML and setup the progress dialog
     */
    public void initializeScreen() {
        mEditTextUsernameCreate = (EditText) findViewById(R.id.edit_text_username_create);
        mEditTextEmailCreate = (EditText) findViewById(R.id.edit_text_email_create);
        mEditTextPasswordCreate = (EditText) findViewById(R.id.edit_text_password_create);
        LinearLayout linearLayoutCreateAccountActivity = (LinearLayout) findViewById(R.id.linear_layout_create_account_activity);
        initializeBackground(linearLayoutCreateAccountActivity);



        /* Setup the progress dialog that is displayed later when authenticating with Firebase */
        mAuthProgressDialog = new ProgressDialog(this);
        mAuthProgressDialog.setTitle(getResources().getString(R.string.progress_dialog_loading));
        mAuthProgressDialog.setMessage(getResources().getString(R.string.progress_dialog_creating_user_with_firebase));
        mAuthProgressDialog.setCancelable(false);

    }

    /**
     * Open LoginActivity when user taps on " already have account Sign in" textView
     */
    public void onSignInPressed(View view) {
        Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    /**
     * Create new account using Firebase email/password provider
     */
    public void onCreateAccountPressed(View view) {
        /**
         * client side error check of the three values that the
         * user entered. If any of the three values fail a check, stop
         * this method.
         */
        mUserName = mEditTextUsernameCreate.getText().toString();
        mUserEmail = mEditTextEmailCreate.getText().toString().toLowerCase();
        mPassword = mEditTextPasswordCreate.getText().toString();


/**
 * Check that email and user name are okay
 */
        boolean validEmail = isEmailValid(mUserEmail);
        boolean validUserName = isUserNameValid(mUserName);
        boolean validPassword = isPasswordValid(mPassword);
        if (!validEmail || !validUserName || !validPassword)
            return;

        /**
         * If everything was valid show the progress dialog to indicate that
         * account creation has started
         */
        mAuthProgressDialog.show();

        /**
         * Create new user with specified email and password
         */
        mAuth.createUserWithEmailAndPassword(mUserEmail, mPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {

                        Log.d(LOG_TAG, " createUserWithEmail:onComplete:" + task.isSuccessful());

                        // if task is not successful show error
                        if (!task.isSuccessful()) {
                            mAuthProgressDialog.dismiss();

                            try {
                                throw task.getException();
                            } catch (FirebaseAuthUserCollisionException e) {
                                mEditTextEmailCreate.setError(getString(R.string.error_email_taken));
                                mEditTextEmailCreate.requestFocus();
                            } catch (FirebaseNetworkException e) {
                                showErrorToast(getString(R.string.error_message_failed_sign_in_no_network));

                            } catch (Exception e) {
                                Log.e(LOG_TAG, e.getMessage());
                            }
                            Toast.makeText(CreateAccountActivity.this, "error occured",
                                    Toast.LENGTH_LONG).show();
                        } else {

                            // successfully account created
                            // now the AuthStateListener runs the onAuthStateChanged callback
                            mAuthProgressDialog.dismiss();

                        }
                    }

                });

    }

    private void sendVerificationEmail() {
        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        user.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                                                               @Override
                                                               public void onComplete(@NonNull Task<Void> task) {
                                                                   if (task.isSuccessful()) {
                                                                       /**
                                                                        * Save name and email to sharedPreferences to create User database record
                                                                        * when the registered user will sign in for the first time
                                                                        */
                                                                       SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(CreateAccountActivity.this);
                                                                       SharedPreferences.Editor spe = sp.edit();
                                                                       spe.putString(Constants.KEY_SIGNUP_EMAIL, mUserEmail).apply();

                                                                       createUserInFirebaseHelper();

                                                                       Log.d(LOG_TAG, "verification email is send to:" + user.getEmail());
                                                                       // after email is sent just logout the user and finish this activity
                                                                       Toast.makeText(CreateAccountActivity.this, " A verification Emmail was sent to" + user.getEmail() + "Please Check Your Inbox..! ", Toast.LENGTH_LONG).show();

                                                                       // sign out user
                                                                       FirebaseAuth.getInstance().signOut();

                                                                       Intent intent = new Intent(CreateAccountActivity.this, LoginActivity.class);
                                                                       intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                                       startActivity(intent);
                                                                       finish();

                                                                   } else {
                                                                       // email not sent, so display message and restart the activity or do whatever you wish to do
                                                                       Log.d(LOG_TAG, "verification email not send" + task.getException());
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

    /**
     * Creates a new user in Firebase from the Java POJO
     */

    private void createUserInFirebaseHelper() {


        mEncodedEmail = Utils.encodeEmail(mUserEmail);
        final DatabaseReference userLocation = FirebaseDatabase.getInstance().getReferenceFromUrl(Constants.FIREBASE_URL_USERS).child(mEncodedEmail);

        /**
         * See if there is already a user (for example, if they already logged in with an associated
         *Google account.
         */
        userLocation.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                               /* If there is no user, make one */
                if (dataSnapshot.getValue() == null) {
                    /* Set raw version of date to the ServerValue.TIMESTAMP value and save into dateCreatedMap */
                    HashMap<String, Object> timestampJoined = new HashMap<>();
                    timestampJoined.put(Constants.FIREBASE_PROPERTY_TIMESTAMP, ServerValue.TIMESTAMP);
                    User newuser = new User(mUserName, mEncodedEmail, timestampJoined);
                    userLocation.setValue(newuser);
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.d(LOG_TAG, "creating user error" + databaseError.getMessage());
            }
        });
    }

    private boolean isEmailValid(String email) {
        boolean isGoodEmail =
                (email != null && Patterns.EMAIL_ADDRESS.matcher(email).matches());
        if (!isGoodEmail) {
            mEditTextEmailCreate.setError(String.format(getString(R.string.error_invalid_email_not_valid),
                    email));
            return false;
        }
        return isGoodEmail;
    }

    private boolean isUserNameValid(String userName) {
        if (userName.equals("")) {
            mEditTextUsernameCreate.setError(getResources().getString(R.string.error_cannot_be_empty));
            return false;
        }
        return true;
    }

    private boolean isPasswordValid(String password) {
        if (password.length() < 6) {
            mEditTextPasswordCreate.setError(getResources().getString(R.string.error_invalid_password_not_valid));
            return false;
        }
        return true;
    }

    /**
     * Show error toast to users
     */
    private void showErrorToast(String message) {
        Toast.makeText(CreateAccountActivity.this, message, Toast.LENGTH_LONG).show();
    }
}
