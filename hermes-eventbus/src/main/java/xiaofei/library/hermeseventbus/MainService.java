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

import org.greenrobot.eventbus.EventBus;

import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;

import xiaofei.library.hermes.annotation.ClassId;
import xiaofei.library.hermes.annotation.GetInstance;
import xiaofei.library.hermes.annotation.MethodId;

/**
 * Created by Xiaofei on 16/6/9.
 */
@ClassId("MainService")
public class MainService implements IMainService {

    private static volatile MainService sInstance = null;

    // This may contain some sub-services corresponding to some dead sub-processes,
    // especially if the sub-process does not disconnect the service before it dies.
    // Sending an event to a dead process will cause a DeadObjectException,
    // but will not crash the app.
    private ConcurrentHashMap<Integer, ISubService> mSubServices;

    private EventBus mEventBus;

    private MainService() {
        mEventBus = EventBus.getDefault();
        mSubServices = new ConcurrentHashMap<>();
    }

    @GetInstance
    public static MainService getInstance() {
        if (sInstance == null) {
            synchronized (MainService.class) {
                if (sInstance == null) {
                    sInstance = new MainService();
                }
            }
        }
        return sInstance;
    }

    @MethodId("register")
    @Override
    public void register(int pid, ISubService subService) {
        mSubServices.put(pid, subService);
    }

    @MethodId("unregister")
    @Override
    public void unregister(int pid) {
        mSubServices.remove(pid);
    }

    @MethodId("post")
    @Override
    public void post(Object event) {
        mEventBus.post(event);
        Collection<ISubService> subServices = mSubServices.values();
        for (ISubService subService : subServices) {
            subService.post(event);
        }
    }

    @MethodId("removeStickyEvent(Object)")
    @Override
    public boolean removeStickyEvent(Object event) {
        return mEventBus.removeStickyEvent(event);
    }

    @MethodId("removeAllStickyEvents")
    @Override
    public void removeAllStickyEvents() {
        mEventBus.removeAllStickyEvents();
    }

    @MethodId("cancelEventDelivery")
    @Override
    public void cancelEventDelivery(Object event) {
        mEventBus.cancelEventDelivery(event);
        Collection<ISubService> subServices = mSubServices.values();
        for (ISubService subService : subServices) {
            subService.cancelEventDelivery(event);
        }
    }

    @MethodId("postSticky")
    @Override
    public void postSticky(Object event) {
        mEventBus.postSticky(event);
        Collection<ISubService> subServices = mSubServices.values();
        for (ISubService subService : subServices) {
            subService.post(event);
        }
    }

    @MethodId("getStickyEvent")
    @Override
    public Object getStickyEvent(String eventType) {
        try {
            Class<?> clazz = Class.forName(eventType);
            return mEventBus.getStickyEvent(clazz);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }

    @MethodId("removeStickyEvent(String)")
    @Override
    public Object removeStickyEvent(String eventType) {
        try {
            Class<?> clazz = Class.forName(eventType);
            return mEventBus.removeStickyEvent(clazz);
        } catch (ClassNotFoundException e) {
            return null;
        }
    }
}
