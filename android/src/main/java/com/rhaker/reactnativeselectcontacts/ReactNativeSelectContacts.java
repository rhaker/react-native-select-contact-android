package com.rhaker.reactnativeselectcontacts;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;

import com.facebook.react.ReactPackage;
import com.facebook.react.bridge.NativeModule;
import com.facebook.react.bridge.JavaScriptModule;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.uimanager.ViewManager;

import java.util.*;

public class ReactNativeSelectContacts implements ReactPackage {

  private Activity mActivity = null;
  private SelectContactsManager mModuleInstance;

  @Override
  public List<NativeModule> createNativeModules(ReactApplicationContext reactContext) {
    mModuleInstance = new SelectContactsManager(reactContext);
    return Arrays.<NativeModule>asList(mModuleInstance);
  }

  @Override
  public List<Class<? extends JavaScriptModule>> createJSModules() {
    return Collections.emptyList();
  }

  @Override
  public List<ViewManager> createViewManagers(ReactApplicationContext reactContext) {
    return Arrays.<ViewManager>asList();
  }

  public boolean handleActivityResult(final int requestCode, final int resultCode, final Intent data) {
    return mModuleInstance.handleActivityResult(requestCode, resultCode, data);
  }

}
