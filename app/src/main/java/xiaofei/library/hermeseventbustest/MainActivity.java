package xiaofei.library.hermeseventbustest;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import xiaofei.library.hermes.Hermes;
import xiaofei.library.hermeseventbus.HermesEventBus;

/**
 * Created by Xiaofei on 16/6/26.
 */
public class MainActivity extends AppCompatActivity {

    private TextView textView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        HermesEventBus.getDefault().register(this);
        //Hermes.getVersion();
        setContentView(R.layout.activity_main);
        textView = (TextView) findViewById(R.id.tv);
        findViewById(R.id.button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getApplicationContext(), SecondActivity.class));
            }
        });
        findViewById(R.id.post_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.v("EricZhao", "post");
                        HermesEventBus.getDefault().post("event");
                    }
                }.start();
            }
        });
        findViewById(R.id.post_sticky_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new Thread() {
                    public void run() {
                        try {
                            sleep(3000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }
                        Log.v("EricZhao", "post sticky");
                        HermesEventBus.getDefault().postSticky("sticky event");
                    }
                }.start();
            }
        });
        findViewById(R.id.get_sticky_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), HermesEventBus.getDefault().getStickyEvent(String.class), Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.remove_all_sticky_events).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HermesEventBus.getDefault().removeAllStickyEvents();
                Toast.makeText(getApplicationContext(), "RemoveAllStickyEvents", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.remove_sticky_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                HermesEventBus.getDefault().removeStickyEvent("sticky event");
                Toast.makeText(getApplicationContext(), "RemoveStickyEvent", Toast.LENGTH_SHORT).show();
            }
        });
        findViewById(R.id.remove_get_sticky_event).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getApplicationContext(), HermesEventBus.getDefault().removeStickyEvent(String.class), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void g(String s) {
        textView.setText(s);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HermesEventBus.getDefault().unregister(this);
    }
}
