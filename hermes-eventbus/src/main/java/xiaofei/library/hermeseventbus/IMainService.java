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

import xiaofei.library.hermes.annotation.ClassId;
import xiaofei.library.hermes.annotation.MethodId;

/**
 * Created by Xiaofei on 16/6/4.
 */
@ClassId("MainService")
public interface IMainService {

    @MethodId("register")
    void register(int pid, ISubService subService);

    @MethodId("unregister")
    void unregister(int pid);

    @MethodId("post")
    void post(Object event);

    @MethodId("postSticky")
    void postSticky(Object event);

    @MethodId("cancelEventDelivery")
    void cancelEventDelivery(Object event);

    @MethodId("getStickyEvent")
    Object getStickyEvent(String eventType);

    @MethodId("removeStickyEvent(String)")
    Object removeStickyEvent(String eventType);

    @MethodId("removeStickyEvent(Object)")
    boolean removeStickyEvent(Object event);

    @MethodId("removeAllStickyEvents")
    void removeAllStickyEvents();

}
