# PrefListener Live
## This SDK work in pair with "PrefListener" Plugin for Android Studio


### "PrefListener Live" doesn't share your data!!!
[![](https://jitpack.io/v/ogogolabs/pref-listener-live.svg)](https://jitpack.io/#ogogolabs/pref-listener-live)


### PrefListener is SDK for listening updates from:
- SharedPreferences
- DataStore<Preferences>

### Under the hood, SDK using:
- SharedPreferences
- DataStore<Preferences>
- Room
- Socket

### Installation

SDK use permissions
```xaml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
```

Add it in your root build.gradle at the end of repositories:
```groovy
dependencyResolutionManagement {
	repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
	repositories {
		mavenCentral()
		maven { url 'https://jitpack.io' }
	}
}
```

Add the dependency
```groovy
dependencies {
    implementation 'com.github.ogogolabs:pref-listener-live:${$last_version}'
}
```

Init sdk in Application class
```kotlin
class MyApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        PrefListener.init(this@MyApplication)
    }
}
```
Add SharedPreferences source:
```kotlin
PrefListener.addSharedPreferencesSource(fileName = "SharedPrefFileName")
```
Add DataStore source:
```kotlin
PrefListener.addDatastoreSource(dataStore = dataStore, dataStoreAliasName = "alias_file_name")
```


## Installation "PrefListener" Android Studio Plugin

- Go to "Settings"->"Plugins"->"Manage Plugin Repositories"->Add new "Custom Plugin Repository"
```html
https://firebasestorage.googleapis.com/v0/b/preferencesloader.appspot.com/o/updatePlugins.xml?alt=media
```
- On "Marketplace" tab find "PrefListener" from "Ogogo Labs"
- Restart AS


## License

MIT

**Free Software, Hell Yeah!**