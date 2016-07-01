# Hermes-EventBus

Hermes-EventBus is a library for using EventBus between processes, useful in IPC or plugin-in
development. It has the same APIs as EventBus and easy to use.

#Principle

The library is based on two libraries: [Hermes](https://github.com/Xiaofei-it/Hermes) and
[EventBus](https://github.com/greenrobot/EventBus).

The event post is based on EventBus and the IPC is based on Hermes, a smart, novel and easy-to-use
framework for Android Inter-Process Communication (IPC).

#Usage

This library can post events not only within an app but also between distinct apps.

##Within a single app
If you only want to post and receive events within a single app, then do the following four steps:

###Step 1

Add the following into your gradle file:

```
dependencies {
    compile 'xiaofei.library:hermes-eventbus:0.1.0'
}
```

###Step 2

In your AndroidManifest.xml, add the service below:

```
<service android:name="xiaofei.library.hermes.HermesService$HermesService0"/>
```

You can add some attributes to the service, if necessary.

###Step 3

In the onCreate method of your application class, add the following:

```
HermesEventBus.getDefault().init(this);
```

###Step 4

Every time you use EventBus, replace "EventBus" with "HermesEventBus", as the following does:

```
HermesEventBus.getDefault().register(this);

HermesEventBus.getDefault().post(new Event());
```

HermesEventBus can also post and receive event within a process, so do not use EventBus any more if
you have already being using HermesEventBus.

##Between apps

If you want to post and receive events between apps, then do the following:


###Step 1

Add the following into the gradle file of each app:

```
dependencies {
    compile 'xiaofei.library:hermes-eventbus:0.1.0'
}
```

###Step 2

Choose an app as a main app. You can choose an arbitrary app as the main app, but a long-lived app
is preferred.

In the AndroidManifest.xml of the main app, add the service below:

```
<service android:name="xiaofei.library.hermes.HermesService$HermesService0"/>
```

You can add some attributes to the service, if necessary.

###Step 3

In the onCreate method of the application class of the main app, add the following:

```
HermesEventBus.getDefault().init(this);
```

In the onCreate method of the application class of other apps, add the following:

```
HermesEventBus.getDefault().connectApp(this, packageName);
```

The "packageName" is the package name of the main app.

###Step 4

Every time you use EventBus, replace "EventBus" with "HermesEventBus", as the following does:

```
HermesEventBus.getDefault().register(this);

HermesEventBus.getDefault().post(new Event());
```

HermesEventBus can also post and receive event within a process, so do not use EventBus any more if
you have already being using HermesEventBus.