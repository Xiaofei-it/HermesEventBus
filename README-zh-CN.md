# HermesEventBus

HermesEventBus是一个基于EventBus的、能在进程间发送和接收event的库，在IPC或者插件开发中非常有用。它底层基于
EventBus，并且和EventBus有相同API。

[EventBus](https://github.com/greenrobot/EventBus)是Android系统上使用最广泛的简化模块之间通信的库。

注意：本库基于EventBus 3.0.0。如果你之前使用的是老版本，那么必须修改你的代码，否则将无法接收event。
但是修改比较简单。

之前使用“onEventXXX”名字的方法要加上注解，并且在后面附上线程模式：

```
@Subscribe(threadMode = ThreadMode.MAIN)
public void showText(String text) {
    textView.setText(text);
}
```

#原理

本库基于两个库开发：[Hermes](https://github.com/Xiaofei-it/Hermes)和
[EventBus](https://github.com/greenrobot/EventBus)。

事件收发是基于EventBus，IPC通信是基于Hermes。Hermes是一个简单易用的Android IPC库。

<img src="doc/figure.png" width="600" height="400"/>

本库首先选一个进程作为主进程，将其他进程作为子进程。

每次一个event被发送都会经过以下四步：

1、使用Hermes库将event传递给主进程。

2、主进程使用EventBus在主进程内部发送event。

3、主进程使用Hermes库将event传递给所有的子进程。

4、每个子进程使用EventBus在子进程内部发送event。

另外还使用了[Concurrent-Utils](https://github.com/Xiaofei-it/Concurrent-Utils)库。

#用法

本库能在app内实现多进程event收发，也可以跨app实现event收发。

##单一app内的用法

如果你在单一app内进行多进程开发，那么只需要做以下三步：

###Step 1

在gradle文件中加入下面的依赖:

```
dependencies {
    compile 'xiaofei.library:hermes-eventbus:0.1.1'
}
```

如果你使用Maven，那么加入下面的依赖：

```
<dependency>
  <groupId>xiaofei.library</groupId>
  <artifactId>hermes-eventbus</artifactId>
  <version>0.1.1</version>
  <type>pom</type>
</dependency>
```

###Step 2

在Application的onCreate中加上以下语句进行初始化：

```
HermesEventBus.getDefault().init(this);
```

###Step 3

每次使用EventBus的时候，用HermesEventBus代替EventBus。

```
HermesEventBus.getDefault().register(this);

HermesEventBus.getDefault().post(new Event());
```

HermesEventBus也能够在一个进程间传递event，所以如果你已经使用了HermesEventBus，那么就不要再使用EventBus了。

##多个app间的用法（使用DroidPlugin的时候就是这种情况）

如果你想在多个app间收发event，那么就做如下几步：

###Step 1

在每个app的gradle文件中加入依赖：

```
dependencies {
    compile 'xiaofei.library:hermes-eventbus:0.1.1'
}
```

如果使用Maven，那么就加入下面的依赖：

```
<dependency>
  <groupId>xiaofei.library</groupId>
  <artifactId>hermes-eventbus</artifactId>
  <version>0.1.1</version>
  <type>pom</type>
</dependency>
```

###Step 2

选择一个app作为主app。你可以选择任意app作为主app，但最好选择那个存活时间最长的app。

在使用DroidPlugin的时候，你可以把宿主app作为主app。

在主app的AndroidManifest.xml中加入下面的service：

```
<service android:name="xiaofei.library.hermes.HermesService$HermesService0"/>
```

你可以加上一些属性。

###Step 3

在app间收发的事件类必须有相同的包名、相同的类名和相同的方法。

务必记住在代码混淆的时候将这些类keep！！！

###Step 4

在主app的application类的onCreate方法中加入：

```
HermesEventBus.getDefault().init(this);
```

在其他app的Application类的onCreate方法中加入：

```
HermesEventBus.getDefault().connectApp(this, packageName);
```

“packageName”指的是主app的包名。

###Step 5

每次使用EventBus的时候，用HermesEventBus代替EventBus。

```
HermesEventBus.getDefault().register(this);

HermesEventBus.getDefault().post(new Event());
```

HermesEventBus也能够在一个进程间传递event，所以如果你已经使用了HermesEventBus，那么就不要再使用EventBus了。

#友情链接

Xiaofei's GitHub: [https://github.com/Xiaofei-it](https://github.com/Xiaofei-it)

[Hermes](https://github.com/Xiaofei-it/Hermes)是一套新颖巧妙易用的Android进程间通信IPC框架。

[AndroidDataStorage](https://github.com/Xiaofei-it/AndroidDataStorage)是一个简洁易用并且具有高性能的Android存储库。

[ComparatorGenerator](https://github.com/Xiaofei-it/ComparatorGenerator)是一个易用的生成Comparator的工具类。在排序时特别有用。

#License

Copyright (C) 2016 Xiaofei, ele.me

HermesEventBus binaries and source code can be used according to the [Apache License, Version 2.0](http://www.apache.org/licenses/LICENSE-2.0.html).
