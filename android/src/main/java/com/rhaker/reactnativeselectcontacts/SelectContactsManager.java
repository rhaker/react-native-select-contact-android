package com.rhaker.reactnativeselectcontacts;

import android.app.Activity;
import android.provider.ContactsContract;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.CountDownTimer;

import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.WritableMap;

import java.util.Map;

public class SelectContactsManager extends ReactContextBaseJavaModule {

  // initialize variables
  static final int PICK_CONTACT = 1;
  private Activity mActivity = null;
  static int foundFlag = 0;
  static int interval = 0;
  static WritableMap map;
  static CountDownTimer counter;

  // set the activity - pulled in from Main
  public SelectContactsManager(ReactApplicationContext reactContext, Activity activity) {
    super(reactContext);
    mActivity = activity;
  }

  // handle the user selection of a certain contact
  public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {

      switch (requestCode) {

          case (PICK_CONTACT) :

            if (resultCode == mActivity.RESULT_OK) {

               Uri contactData = data.getData();
               Cursor c =  mActivity.managedQuery(contactData, null, null, null, null);

               // set the data - phone, email, and name
               if (c.moveToFirst()) {

                   map = Arguments.createMap();
                   String id =c.getString(c.getColumnIndexOrThrow(ContactsContract.Contacts._ID));
                   String hasPhone =c.getString(c.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER));

                    if (hasPhone.equalsIgnoreCase("1")) {

                      Cursor phones = mActivity.getContentResolver().query(
                                 ContactsContract.CommonDataKinds.Phone.CONTENT_URI,null,
                                 ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = "+ id,
                                 null, null);
                      phones.moveToFirst();
                      String cNumber = phones.getString(phones.getColumnIndex("data1"));
                      map.putString("phone", cNumber);
                    }

                    Cursor emailCur = mActivity.getContentResolver().query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,null,
                            ContactsContract.CommonDataKinds.Email.CONTACT_ID
                                    + " = ?", new String[] { id }, null);

                    String email = null;

                    if ((emailCur != null) && (emailCur.moveToNext())) {
                      email = emailCur.getString(emailCur.getColumnIndex(ContactsContract.CommonDataKinds.Email.DATA));
                      map.putString("email", email);
                    }

                    String name = c.getString(c.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
                    map.putString("name", name);

              }

              // set the flag to indicate selection made (polling stops)
              foundFlag = 1;

           } // end if activity result ok

         break;

     } // end switch

      return true;
  }


  @ReactMethod
  public void pickContact(Callback callback) {

    // initialize variable
    final Callback callbackFinal = callback;

    // reset values in case multiple picks
    interval = 0;
    foundFlag = 0;
    map = null;
    counter = null;

    // check if android version < 4.0
    if ((android.os.Build.VERSION.RELEASE.startsWith("1.")) || (android.os.Build.VERSION.RELEASE.startsWith("2.")) || (android.os.Build.VERSION.RELEASE.startsWith("3."))) {

      callbackFinal.invoke(null,"android version not supported");

    } else {

      // start the activity to pick the contact from addressbook
      Intent pickContactIntent = new Intent( Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI );
      mActivity.startActivityForResult(pickContactIntent, PICK_CONTACT);

      // poll for user input selection - max of 45 seconds
      counter = new CountDownTimer(45000, 1000) {

          @Override
          public void onTick(long millisUntilFinished) {

            // cancel polling if user picks
            if (foundFlag == 1) {

              // cancel countdown, send result
              counter.cancel();
              callbackFinal.invoke(null,map);
            }

          }

          @Override
          public void onFinish() {

            // poll for 45 cycles - or 45 seconds max
            if (foundFlag == 0) {

              // send timed out result
              callbackFinal.invoke(null,"timed out");

            }

          }

      }.start();

    }

  }

  // error payload function to catch any exception to send to javascript
  private WritableMap makeErrorPayload(Exception ex) {
    WritableMap error = Arguments.createMap();
    error.putString("message", ex.getMessage());
    return error;
  }

  @Override
  public String getName() {
    return "SelectContacts";
  }
}
