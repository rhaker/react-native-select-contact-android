package com.rhaker.reactnativeselectcontacts;

import android.app.Activity;
import android.os.Build;
import android.provider.ContactsContract;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.CountDownTimer;
import android.util.Log;

import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ActivityEventListener;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

public class SelectContactsManager extends ReactContextBaseJavaModule implements ActivityEventListener  {

  // initialize variables
  static final int PICK_CONTACT = 109888123;
  private Activity mActivity = null;
  static int foundFlag = 0;
  static int interval = 0;
  static WritableMap map;
  static CountDownTimer counter;

  // set the activity - pulled in from Main
  public SelectContactsManager(ReactApplicationContext reactContext) {
    super(reactContext);
    // Add the listener for `onActivityResult`
    reactContext.addActivityEventListener(this);
  }

  public void onNewIntent(Intent intent) {

  }

  public void onActivityResult(final int requestCode, final int resultCode, final Intent intent) {
    this.handleActivityResult(requestCode, resultCode, intent);
  }

  public void onActivityResult(Activity activity, final int requestCode, final int resultCode, final Intent intent) {
    this.handleActivityResult(requestCode, resultCode, intent);
  }

  // handle the user selection of a certain contact
  public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
      // check if pickContact was invoked else opt out of activity
      if(mActivity == null){
          return false;
      }

      switch (requestCode) {

          case (PICK_CONTACT) :

            if (resultCode == mActivity.RESULT_OK) {

               Uri contactData = data.getData();
               Cursor c =  mActivity.managedQuery(contactData, null, null, null, null);

               // set the data - phone, email, and name
               if (c.moveToFirst()) {

                   map = Arguments.createMap();
                   String id = c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                   String hasPhone = c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1")) {
                      map.putArray("phoneNumbers", getPhones(mActivity, id));
                    }

                    map.putArray("emailAddresses", getEmails(mActivity, id));

                    String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    map.putString("name", name);
                    map.putString("id", id);

              }

              // set the flag to indicate selection made (polling stops)
              foundFlag = 1;

            } else if (resultCode == mActivity.RESULT_CANCELED) {

              // set the flag to indicate user hit back button in address book (polling stops)
              foundFlag = 2;

            }

         break;

     } // end switch

      return true;
  }


  @ReactMethod
  public void pickContact(ReadableMap options, Callback callback) {
    mActivity = getCurrentActivity();

    // initialize variable
    final Callback callbackFinal = callback;

    // reset values in case multiple picks
    interval = 0;
    foundFlag = 0;
    map = null;
    counter = null;
    int timeout = 45000;

    if(options != null &&  options.getInt("timeout") > 0){
        timeout = options.getInt("timeout");
        Log.i("RNSelectContacts", "custom timeout set: " + timeout + " ms");
    }

    // check if android version < 5.0
    if ((android.os.Build.VERSION.RELEASE.startsWith("1.")) ||
            (android.os.Build.VERSION.RELEASE.startsWith("2.")) ||
            (android.os.Build.VERSION.RELEASE.startsWith("3.")) ||
            (android.os.Build.VERSION.RELEASE.startsWith("4."))) {

      callbackFinal.invoke(generateCustomError("android version not supported"), null);

    } else {

      // start the activity to pick the contact from addressbook
      Intent pickContactIntent = new Intent( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
      mActivity.startActivityForResult(pickContactIntent, PICK_CONTACT);

      // poll for user input selection - max of 45 seconds
      counter = new CountDownTimer(timeout, 1000) {

          @Override
          public void onTick(long millisUntilFinished) {
            // cancel polling if user picks
            if (foundFlag == 1) {
              // cancel countdown, send result
              counter.cancel();
              callbackFinal.invoke(null,map);
            }

            // cancel polling if user hits back button
            if (foundFlag == 2) {
              // cancel countdown, send result
              counter.cancel();
              // send user canceled
              callbackFinal.invoke(generateCustomError("user canceled"), null);

            }

          }

          @Override
          public void onFinish() {
            // poll for 45 cycles - or 45 seconds max
            if (foundFlag == 0) {
              // send timed out result
              callbackFinal.invoke(generateCustomError("timed out"), null);

            }

          }

      }.start();

    }

  }
  private WritableMap getErrorMeta(){
      WritableMap meta = Arguments.createMap();
      meta.putString("androidVersion", Build.VERSION.RELEASE);
      return meta;
  }

  private WritableMap generateCustomError(String message){
      WritableMap map = Arguments.createMap();
      map.putString("message", message);
      map.putMap("meta", getErrorMeta());
      return map;
  }
  // error payload function to catch any exception to send to javascript
  private WritableMap makeErrorPayload(Exception ex) {
    WritableMap error = Arguments.createMap();
    error.putString("message", ex.getMessage());
    return error;
  }

  private WritableArray getPhones(Activity mActivity, String id){
      WritableArray array = Arguments.createArray();

      Cursor cursor = mActivity.getContentResolver().query(
              ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
              ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
              null, null);
      while (cursor != null && cursor.moveToNext()) {
          String cNumber = cursor.getString(cursor.getColumnIndex("data1"));
          if(!cNumber.isEmpty()){
              WritableMap map = Arguments.createMap();
              map.putString("number", cNumber);
              array.pushMap(map);
          }
      }

      return array;
  }

  private WritableArray getEmails(Activity mActivity, String id){
      WritableArray array = Arguments.createArray();

      Cursor cursor = mActivity.getContentResolver().query(
              ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,
              ContactsContract.CommonDataKinds.Email.CONTACT_ID
                      + " = ?", new String[] { id }, null);

      while (cursor != null && cursor.moveToNext()) {
          String email = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
          if(!email.isEmpty()){
              WritableMap map = Arguments.createMap();
              map.putString("email", email);
              array.pushMap(map);
          }
      }

      return array;
  }


  @Override
  public String getName() {
    return "SelectContacts";
  }
}
