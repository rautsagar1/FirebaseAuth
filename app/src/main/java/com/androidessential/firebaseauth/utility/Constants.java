package com.androidessential.firebaseauth.utility;

/**
* Constants class store most important strings and paths of the app
*/
public final class Constants {
  /**
   * Constants related to locations in Firebase,
   */

  public static final String FIREBASE_LOCATION_USERS = "users";

    /**
   * Constants for Firebase object properties
   */
  public static final String FIREBASE_PROPERTY_TIMESTAMP = "timestamp";
  /**
   * Constants for Firebase URL
   */
  public static final String FIREBASE_URL ="https://fir-auth-fc818.firebaseio.com/";
  public static final String FIREBASE_URL_USERS = FIREBASE_URL + "/" + FIREBASE_LOCATION_USERS;

  /**
   * constant for firebase login
   */
  public static final String PASSWORD_PROVIDER = "password";
  public static final String KEY_PROVIDER = "PROVIDER";
  public static final String KEY_ENCODED_EMAIL = "ENCODED_EMAIL";
  public static final String KEY_SIGNUP_EMAIL = "SIGNUP_EMAIL";

}
