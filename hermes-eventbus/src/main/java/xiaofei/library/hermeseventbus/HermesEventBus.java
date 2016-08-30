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
import android.util.Log;

import org.greenrobot.eventbus.EventBus;

import xiaofei.library.concurrentutils.ObjectCanary;
import xiaofei.library.concurrentutils.util.Action;
import xiaofei.library.concurrentutils.util.Function;
import xiaofei.library.hermes.Hermes;
import xiaofei.library.hermes.HermesService;

/**
 * Created by Xiaofei on 16/6/4.
 */
public class HermesEventBus {

    private static final String TAG = "HermesEventBus";

    private static final String HERMES_SERVICE_DISCONNECTED = "Hermes service disconnected!";

    private static final String UNKNOWN_ERROR = "An unknown error occurred. Please report this to the author.";

    private static final int STATE_UNDEFINED = -1;

    private static final int STATE_DISCONNECTED = 0;

    private static final int STATE_CONNECTING = 1;

    private static final int STATE_CONNECTED = 2;

    private static volatile HermesEventBus sInstance = null;

    private final EventBus mEventBus;

    private volatile Context mContext;

    private volatile boolean mMainProcess;

    private volatile ObjectCanary<IMainService> mRemoteApis;

    private volatile MainService mMainApis;

    private volatile int mState = STATE_UNDEFINED;

    /**
     * TODO
     *
     * 1. Consider more about the interleaving, especially when the service is being connected or disconnected.
     *
     * 2.
     *
     */

    private HermesEventBus() {
        mEventBus = EventBus.getDefault();
        mRemoteApis = new ObjectCanary<IMainService>();
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
            mState = STATE_CONNECTING;
            Hermes.setHermesListener(new HermesListener());
            Hermes.connect(context, Service.class);
            Hermes.register(SubService.class);
        }
    }

    public void connectApp(Context context, String packageName) {
        mContext = context;
        mMainProcess = false;
        mState = STATE_CONNECTING;
        Hermes.setHermesListener(new HermesListener());
        Hermes.connectApp(context, packageName);
        Hermes.register(SubService.class);
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

    private void actionInternal(final Action<IMainService> action) {
        if (mMainProcess) {
            action.call(mMainApis);
        } else {
            if (mState == STATE_DISCONNECTED) {
                Log.w(TAG, HERMES_SERVICE_DISCONNECTED);
            } else if (mState == STATE_CONNECTING) {
                mRemoteApis.actionNonNullNonBlocking(new Action<IMainService>() {
                    @Override
                    public void call(IMainService o) {
                        action.call(o);
                    }
                });
            } else if (mState == STATE_CONNECTED) {
                action.call(mRemoteApis.get());
            } else {
                throw new IllegalStateException(UNKNOWN_ERROR);
            }
        }
    }

    public void post(final Object event) {
        actionInternal(new Action<IMainService>() {
            @Override
            public void call(IMainService o) {
                o.post(event);
            }
        });
    }

    public void cancelEventDelivery(final Object event) {
        actionInternal(new Action<IMainService>() {
            @Override
            public void call(IMainService o) {
                o.cancelEventDelivery(event);
            }
        });
    }

    public void postSticky(final Object event) {
        actionInternal(new Action<IMainService>() {
            @Override
            public void call(IMainService o) {
                o.postSticky(event);
            }
        });
    }


    private <T> T calculateInternal(final Function<IMainService, ? extends T> function) {
        if (mMainProcess) {
            return function.call(mMainApis);
        } else {
            if (mState == STATE_DISCONNECTED) {
                Log.w(TAG, HERMES_SERVICE_DISCONNECTED);
                return null;
            } else if (mState == STATE_CONNECTING) {
                return mRemoteApis.calculateNonNull(new Function<IMainService, T>() {
                    @Override
                    public T call(IMainService o) {
                        return function.call(o);
                    }
                });
            } else if (mState == STATE_CONNECTED) {
                return function.call(mRemoteApis.get());
            } else {
                throw new IllegalStateException(UNKNOWN_ERROR);
            }
        }
    }

    public <T> T getStickyEvent(final Class<T> eventType) {
        return calculateInternal(new Function<IMainService, T>() {
            @Override
            public T call(IMainService o) {
                return eventType.cast(o.getStickyEvent(eventType.getName()));
            }
        });
    }

    public <T> T removeStickyEvent(final Class<T> eventType) {
        return calculateInternal(new Function<IMainService, T>() {
            @Override
            public T call(IMainService o) {
                return eventType.cast(o.removeStickyEvent(eventType.getName()));
            }
        });
    }

    public Boolean removeStickyEvent(final Object event) {
        return calculateInternal(new Function<IMainService, Boolean>() {
            @Override
            public Boolean call(IMainService o) {
                return o.removeStickyEvent(event);
            }
        });
    }

    public void removeAllStickyEvents() {
        actionInternal(new Action<IMainService>() {
            @Override
            public void call(IMainService o) {
                o.removeAllStickyEvents();
            }
        });
    }

    public boolean hasSubscriberForEvent(Class<?> eventClass) {
        return mEventBus.hasSubscriberForEvent(eventClass);
    }

    public class HermesListener extends xiaofei.library.hermes.HermesListener {

        @Override
        public void onHermesConnected(Class<? extends HermesService> service) {
            // Here something should be paid attention to.
            // If we has started MyService in a sub-process and then kill the main process,
            // and if we has not set a default uncaught exception handler,
            // then the following will happen:
            // 1. HermesListener.onHermesDisconnected() is invoked, and an error occurs:
            //    "Error occurs. Error 2: Service Unavailable: Check whether you have connected Hermes."
            // 2. A new main process is created.
            // 3. The sub-process where MyService runs will resume the Hermes connection with
            //    the new main process, and HermesListener.onHermesConnected() is invoked.
            // 4. The sub-process crashes in either of the following two ways:
            //    (1) After getting the instance of IMainService, the GC runs because there is no strong
            //        reference to the previous IMainService. However, the main process does not
            //        have the corresponding MainService and thus throw an exception.
            //    (2) In the previous version, mRemoteApis was set null when the service is disconnected.
            //        Then a NPE was thrown when the service is reconnected.
            /**
            Log.v(TAG, "Hermes connected in Process " + Process.myPid());
            mRemoteApis.set(Hermes.getInstanceInService(service, IMainService.class));
            mRemoteApis.action(new Action<IMainService>() {
                @Override
                public void call(IMainService o) {
                    o.register(Process.myPid(), SubService.getInstance());
                }
            });
             */
            IMainService mainService = Hermes.getInstanceInService(service, IMainService.class);
            mainService.register(Process.myPid(), SubService.getInstance());
            if (mRemoteApis == null) {
                mRemoteApis = new ObjectCanary<IMainService>();
            }
            mRemoteApis.set(mainService);
            mState = STATE_CONNECTED;
        }

        @Override
        public void onHermesDisconnected(Class<? extends HermesService> service) {
            // Log.v(TAG, "Hermes disconnected in Process " + Process.myPid());
            mRemoteApis.action(new Action<IMainService>() {
                @Override
                public void call(IMainService o) {
                    o.unregister(Process.myPid());
                }
            });
            mRemoteApis.set(null);
            mRemoteApis = null;
            mState = STATE_DISCONNECTED;
        }
    }

    public static class Service extends HermesService {

    }

}
