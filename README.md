## react-native-select-contact-android

This is a react native module that launches the addressbook and when the user taps on a contact various fields (name, phone, and email) are returned. This is for android version 5 (or higher) only.

For ios, you can use one of the many select-contact-* modules I have previously built.  

If you want to be able to select multiple contacts at once for android, please use https://github.com/lwhiteley/react-native-android-contactpicker by @lwhiteley

## Installation

```js
npm install react-native-select-contact-android --save
```

## Usage Example: Single Contact

```js
var SelectContacts = require('react-native-select-contact-android')

SelectContacts.pickContact({
  timeout: 45000, multiple: false,
  theme: SelectContacts.Themes.LIGHT
},
(err, contact) => {

  if (err){
    if(typeof err === 'object'){
      if (err.message == "user canceled") {
        console.log("user hit back button in contact picker");
      } else if (err.message == "timed out") {
        console.log("timed out");
      } else if (err.message == "android version not supported") {
        console.log("invalid android version");
      }
    }
    // log out err object
    console.log(err)
  } else {  
    console.log(contact)
    /**
    Sample contact:
    {
      id: "100",
      name: "John Doe",
      phoneNumbers: [ {"number": "+1-555-555-5555"} ],
      emailAddresses: [ {"email": "john.doe@email.com"} ]
    }
    **/
  }

})
```

## Usage Example: Single Contact

### Options

| Property  | Description  |
|---|---|
|  **timeout** (number)  |  Value in milliseconds (ms) that states how long to wait for the user to select a contact <br/> Default: `45000` |
|  **multiple** (boolean)  |  When true, will enable the multi-select view <br/> Default: `false` |  
|  **theme** (int)  |  This option sets the theme for  [Android-ContactPicker](https://github.com/1gravity/Android-ContactPicker) multi-select view only <br/> Default: `SelectContacts.Themes.LIGHT` |  

### Constants

```
SelectContacts.Themes = {
  DARK,
  LIGHT
}
```

## Getting Started - Android
* In `android/settings.gradle`
```gradle
...
include ':react-native-select-contact-android'
project(':react-native-select-contact-android').projectDir = new File(settingsDir, '../node_modules/react-native-select-contact-android/android')
```

* In `android/app/build.gradle`
```gradle
...
dependencies {
    ...
    compile project(':react-native-select-contact-android')
}
```

* register module (in android/app/src/main/java/{your-app-namespace}/MainApplication.java)
```java
import com.rhaker.reactnativeselectcontacts.ReactNativeSelectContacts; // <------ add import

public class MainApplication extends Application implements ReactApplication {
  private final ReactNativeHost mReactNativeHost = new ReactNativeHost(this) {

    ...

    @Override
    protected List<ReactPackage> getPackages() {
      return Arrays.<ReactPackage>asList(
          new MainReactPackage(),
          new ReactNativeSelectContacts()
      );
    }
  };

  ...
}
```

* Add Contacts permission and Activity (in android/app/src/main/AndroidManifest.xml)
```xml
...
  <uses-permission android:name="android.permission.READ_CONTACTS" />
...

<application
     android:name=".MainApplication"
     android:allowBackup="true"
     android:label="@string/app_name"
     android:icon="@mipmap/ic_launcher"
     android:theme="@style/AppTheme">

     ...

        <activity
            android:name="com.onegravity.contactpicker.core.ContactPickerActivity"
            android:enabled="true"
            android:exported="false" >

            <intent-filter>
                <category android:name="android.intent.category.DEFAULT" />
                <category android:name="android.intent.category.BROWSABLE" />
            </intent-filter>
        </activity>

      ...
</application>
```
## Additional Notes

- The properties phoneNumbers and emailAddresses will be returned as empty arrays if no phone numbers or emails are found.
- The multi-select view was implemented using [Android-ContactPicker](https://github.com/1gravity/Android-ContactPicker). Please see its documentation to be more informed about it, if necessary.

### Error Callback

The following will cause a callback that indicates an error (use the console.log to see the specific message):

1) Android Version below 5.0 is used.

2) User denies access to the addressbook

3) The user takes longer than 45 seconds to pick a contact.

4) User hits the back button and never picks a contact.

### Known issues

- If you select too many contacts, there will be an exception that crashes the app. [details](https://www.neotechsoftware.com/blog/android-intent-size-limit). Possible solution would be to find a way to limit the selected contacts

## Acknowledgements and Special Notes

This module has been updated by [@lwhiteley](https://github.com/lwhiteley) to handle RN version 0.29+. Special thanks for all the hard work!

The approach prior to @lwhiteley's version relied heavily on @satya164 comments at https://github.com/facebook/react-native/issues/3334. If you are using a module that also uses his approach, you might have to make some adjustments. Special thanks also to @rt2zz and his react-native-contacts repo. Finally,
Brent Vatne is always amazingly helpful with everything and deserves to be thanked.
