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

package xiaofei.library.hermeseventbustest;

import android.app.Application;

import xiaofei.library.hermeseventbus.HermesEventBus;

/**
 * Created by Xiaofei on 16/6/26.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        HermesEventBus.getDefault().init(this);
        //The following is to check whether the potential bug caused by a dead lock
        //in the main thread has been fixed.
        HermesEventBus.getDefault().post("Event posted before connection from sub-process");
    }
}
