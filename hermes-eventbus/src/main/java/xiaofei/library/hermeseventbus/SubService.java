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

import xiaofei.library.hermes.annotation.MethodId;

/**
 * Created by Xiaofei on 16/6/25.
 */
public class SubService implements ISubService {

    private static volatile SubService sInstance = null;

    private EventBus mEventBus;

    private SubService() {
        mEventBus = EventBus.getDefault();
    }

    public static SubService getInstance() {
        if (sInstance == null) {
            synchronized (SubService.class) {
                if (sInstance == null) {
                    sInstance = new SubService();
                }
            }
        }
        return sInstance;
    }

    @MethodId("post")
    @Override
    public void post(Object event) {
        mEventBus.post(event);
    }

    @MethodId("cancelEventDelivery")
    @Override
    public void cancelEventDelivery(Object event) {
        mEventBus.cancelEventDelivery(event);
    }

}
