/**
 *
 * Copyright 2016 Xiaofei
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package xiaofei.library.hermeseventbus;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Process;

import org.greenrobot.eventbus.EventBus;

import xiaofei.library.hermes.Hermes;
import xiaofei.library.hermes.HermesListener;
import xiaofei.library.hermes.HermesService;

/**
 * Created by Xiaofei on 16/6/4.
 */
public class HermesEventBus {

    private static volatile HermesEventBus sInstance = null;

    private EventBus mEventBus;

    private Context mContext;

    private boolean mMainProcess;

    private IMainService mApis;

    private MainService mMainApis;

    private HermesEventBus() {
        mEventBus = EventBus.getDefault();
    }

    public static HermesEventBus getDefault() {
        if (sInstance == null) {
            synchronized (HermesEventBus.class) {
                if (sInstance == null) {
                    sInstance = new HermesEventBus();
                }
            }
        }
        return sInstance;
    }

    private static boolean isMainProcess(Context context) {
        return context.getPackageName().equals(getCurrentProcessName(context));
    }

    private static String getCurrentProcessName(Context context) {
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (activityManager == null) {
            return null;
        }
        for (ActivityManager.RunningAppProcessInfo processInfo : activityManager.getRunningAppProcesses()) {
            if (processInfo.pid == Process.myPid()) {
                return processInfo.processName;
            }
        }
        return null;
    }

    public void init(Context context) {
        mContext = context;
        mMainProcess = isMainProcess(context.getApplicationContext());
        if (mMainProcess) {
            Hermes.init(context);
            Hermes.register(MainService.class);
            mMainApis = MainService.getInstance();
        } else {
            Hermes.setHermesListener(new HermesListener() {
                @Override
                public void onHermesConnected(Class<? extends HermesService> service) {
                    mApis = Hermes.getInstanceInService(service, IMainService.class);
                    mApis.register(Process.myPid(), SubService.getInstance());
                }
            });
            Hermes.connect(context);
        }
    }

    public void connectApp(Context context, String packageName) {
        mContext = context;
        mMainProcess = false;
        Hermes.connectApp(context, packageName);
    }

    public void destroy() {
        if (!mMainProcess) {
            Hermes.disconnect(mContext);
        }
    }

    public void register(Object subscriber) {
        mEventBus.register(subscriber);
    }

    public boolean isRegistered(Object subscriber) {
        return mEventBus.isRegistered(subscriber);
    }

    public void unregister(Object subscriber) {
        mEventBus.unregister(subscriber);
    }

    public void post(Object event) {
        if (mMainProcess) {
            mMainApis.post(event);
        } else {
            mApis.post(event);
        }
    }

    public void cancelEventDelivery(Object event) {
        if (mMainProcess) {
            mMainApis.cancelEventDelivery(event);
        } else {
            mApis.cancelEventDelivery(event);
        }
    }

    public void postSticky(Object event) {
        if (mMainProcess) {
            mMainApis.postSticky(event);
        } else {
            mApis.postSticky(event);
        }
    }

    public <T> T getStickyEvent(Class<T> eventType) {
        if (mMainProcess) {
            return eventType.cast(mMainApis.getStickyEvent(eventType.getName()));
        } else {
            return eventType.cast(mApis.getStickyEvent(eventType.getName()));
        }
    }

    public <T> T removeStickyEvent(Class<T> eventType) {
        if (mMainProcess) {
            return eventType.cast(mMainApis.removeStickyEvent(eventType.getName()));
        } else {
            return eventType.cast(mApis.removeStickyEvent(eventType.getName()));
        }
    }

    public boolean removeStickyEvent(Object event) {
        if (mMainProcess) {
            return mMainApis.removeStickyEvent(event);
        } else {
            return mApis.removeStickyEvent(event);
        }
    }

    public void removeAllStickyEvents() {
        if (mMainProcess) {
            mMainApis.removeAllStickyEvents();
        } else {
            mApis.removeAllStickyEvents();
        }
    }

    public boolean hasSubscriberForEvent(Class<?> eventClass) {
        return mEventBus.hasSubscriberForEvent(eventClass);
    }

    //TODO 消息队列
}
