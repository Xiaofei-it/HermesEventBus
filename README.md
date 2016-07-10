# Hermes-EventBus

Hermes-EventBus is a library for using EventBus between processes, useful in IPC or plugin-in
development. It has the same APIs as EventBus and easy to use.

[Chinese Readme 中文文档](https://github.com/Xiaofei-it/Hermes-EventBus/blob/master/README-zh-CN.md)

Note that this library is based on EventBus 3.0.0 and if you are using the lower version, you should
modify your code. Otherwise you will not receive any event!!!

It is useless to name the method receiving events "onEventXXX". Instead, you should add @Subscribe
annotation on the method, as the following does:

```
@Subscribe(threadMode = ThreadMode.MAIN)
public void showText(String text) {
    textView.setText(text);
}
```

#Principle

The library is based on two libraries: [Hermes](https://github.com/Xiaofei-it/Hermes) and
[EventBus](https://github.com/greenrobot/EventBus).

The event post is based on EventBus and the IPC is based on Hermes, a smart, novel and easy-to-use
framework for Android Inter-Process Communication (IPC).

<img src="doc/figure.png" width="1000" height="500"/>

This library will choose a process as the main process, and regard the other processes as the
sub-process.

Each time an event is posted, the library does the following:

1. Use the Hermes library to send the event to the main process.

2. The main process uses EventBus to post the event within the main process.

3. The main process uses the Hermes library to send the event to all of the sub-processes.

4. Each sub-process uses EventBus to post the event within itself.

#Usage

This library can post events not only within a single app which has more than one process, but also
between distinct apps.

##Within a single app

If you only want to post and receive events within a single app which has more than one process,
then do the following three steps:

###Step 1

Add the following into your gradle file:

```
dependencies {
    compile 'xiaofei.library:hermes-eventbus:0.1.1'
}
```

For maven, please use the following:

```
<dependency>
  <groupId>xiaofei.library</groupId>
  <artifactId>hermes-eventbus</artifactId>
  <version>0.1.1</version>
  <type>pom</type>
</dependency>
```

###Step 2

In the onCreate method of your application class, add the following:

```
HermesEventBus.getDefault().init(this);
```

###Step 3

Each time you use EventBus, replace "EventBus" with "HermesEventBus", as the following does:

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
    compile 'xiaofei.library:hermes-eventbus:0.1.1'
}
```

For maven, please use the following:

```
<dependency>
  <groupId>xiaofei.library</groupId>
  <artifactId>hermes-eventbus</artifactId>
  <version>0.1.1</version>
  <type>pom</type>
</dependency>
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

The event posted between apps should have the same package name, the same class name and the same
methods.

And remember to keep all of your event classes and the methods within the classes in the
proguard-rule files.

###Step 4

In the onCreate method of the application class of the main app, add the following:

```
HermesEventBus.getDefault().init(this);
```

In the onCreate method of the application class of other apps, add the following:

```
HermesEventBus.getDefault().connectApp(this, packageName);
```

The "packageName" is the package name of the main app.

###Step 5

Each time you use EventBus, replace "EventBus" with "HermesEventBus", as the following does:

```
HermesEventBus.getDefault().register(this);

HermesEventBus.getDefault().post(new Event());
```

HermesEventBus can also post and receive event within a process, so do not use EventBus any more if
you have already being using HermesEventBus.