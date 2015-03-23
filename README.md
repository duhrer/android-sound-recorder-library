# What is this thing?

This project provides a simple sound recorder for use on devices that do not come with one installed.  It is meant to be added to an Android application and then listed as one of its available activities.

# Using the sound recorder in your project

To use the recorder in your project, you must do two things:

1. Include it in your Gradle dependencies.
2. Define the activity in your AndroidManifest.xml
3. (optional) To actually use the activity, your app should launch the standard sound recording intent (see below).

If you want to see all of this wired together in a real project, check out the source for [Free Speech](https://bitbucket.org/duhrer/free-speech-for-android/).

## Including the sound recorder in your Gradle dependencies

In your Android project, you will need to add the dependency to any modules that use the sound recorder library.  You do this by editing the module (and not the project) `build.gradle` file and updating your `dependencies` block, as in this example:

    :::
    dependencies {
        compile 'com.blogspot.tonyatkins:recorder:2.0.1'
    }

This library is published as an AAR file to [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Ca%3A%22recorder%22) and [jCenter](https://bintray.com/bintray/jcenter/com.blogspot.tonyatkins%3Arecorder/).  Once you've added the dependency, it should be available in your project as soon as you clean and rebuild the project.

If you want to see the list of available versions, check out either the Maven Central or JCenter links above.  Please note that the 1.x versions were designed to work with Maven.  You should only use 2.0.1 or higher with your Gradle project.

## Defining the activity in your AndroidManifest.xml

You will need to define the (external) activity in your `AndroidManifest.xml` file, as follows:

    :::xml
    <!--
            Sound Recorder activity, which requires the following external library project:
                                 https://bitbucket.org/duhrer/android-sound-recorder-library
    -->
    <activity
        android:name="com.blogspot.tonyatkins.recorder.activity.RecordSoundActivity"
        android:exported="false"
        android:icon="@drawable/icon"
        android:label="@string/app_name" >
        <intent-filter>
            <action android:name="android.provider.MediaStore.RECORD_SOUND" />

            <category android:name="android.intent.category.DEFAULT" />
        </intent-filter>
    </activity>

Note that you can choose the label and icon, in this example I have picked up the standard icon and app name for my existing project.  thus, when a user presses the "record" button, they simply see that my application can handle the recording for them.  They never have to know that they're using this library at all... :)

The comment pointing to the source URL isn't strictly necessary, but you should of course make it clear you're using the library (it's part of the license, after all).

## Launching the standard sound recording intent

To be able to see this library in action, you also need to launch an intent whose action is `android.provider.MediaStore.RECORD_SOUND` (the standard intent for recording a sound).

The only option that the recorder supports is setting the filename, as in this example:

    :::java
    Intent recordSoundIntent = new Intent(this, RecordSoundActivity.class);
    recordSoundIntent.putExtra(RecordSoundActivity.FILE_NAME_KEY, tempButton.getLabel());

# Contributing

If you're using this library, giving credit is the least you can do.  If you want to do more, consider becoming a contributor.

The library could use a bit of work in terms of tests, design, adding additional translations, you name it.

If you would like to help, please get in touch or simply file an issue on BitBucket with a problem you've encountered or feature you'd like to see.
