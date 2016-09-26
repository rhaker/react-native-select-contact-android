## react-native-select-contact-android

This is a react native module that launches the addressbook and when the user taps on a contact various fields (name, phone, and email) are returned. This is for android version 5 (or higher) only.

For ios, you can use one of the many select-contact-* modules I have previously built.  

## Installation

```js
npm install react-native-select-contact-android --save
```

## Usage Example

```js
var SelectContacts = require('react-native-select-contact-android')

SelectContacts.pickContact({timeout: 45000}, (err, contacts) => {

  if (err){
    console.log("there was an error. possibly permissions denied.")
  } else if (contacts == "user canceled") {
    console.log("user hit back button in contact picker");
  } else if (contacts == "timed out") {
    console.log("timed out");
  } else if (contacts == "android version not supported") {
    console.log("invalid android version");
  } else {
    console.log(contacts)
    console.log(contacts.name);
    console.log(contacts.phone);
    console.log(contacts.email);
  }

})
```

### Options

| Property  | Description  |
|---|---|
|  **timeout** (number)  |  Value in milliseconds (ms) that states how long to wait for the user to select a contact <br/> Default: `45000` |  


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

* register module (in android/app/src/main/java/[your-app-namespace]/MainApplication.java)
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

* add Contacts permission (in android/app/src/main/AndroidManifest.xml)
```xml
...
  <uses-permission android:name="android.permission.READ_CONTACTS" />
...
```
## Additional Notes

The phone and email will be returned as undefined (if they don't exist), so you should check for the case where value is null, empty, or undefined.

## Error Callback

The following will cause a callback that indicates an error (use the console.log to see the specific message):

1) Android Version below 5.0 is used.

2) User denies access to the addressbook

3) The user takes longer than 45 seconds to pick a contact.

4) User hits the back button and never picks a contact.

## Acknowledgements and Special Notes

The approach taken in this module relies heavily on @satya164 comments at https://github.com/facebook/react-native/issues/3334. If you are using a module that also uses his approach, you might have to make some adjustments. Special thanks also to @rt2zz and his react-native-contacts repo. Finally,
Brent Vatne is always amazingly helpful with everything and deserves to be thanked.
