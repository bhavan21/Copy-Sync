/*
 * Copyright 2013 Tristan Waddington
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.example.bhavan.clippy;

import android.app.Service;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.Date;

/**
 * Monitors the {@link ClipboardManager} for changes and logs the text to a file.
 */
public class ClipboardMonitorService extends Service {
    private static final String TAG = "ClipboardManager";
    private static final String MYPREF = "MYPREF";
    private ClipboardManager mClipboardManager;
    private SharedPreferences sharedPreferences;

    @Override
    public void onCreate() {
        super.onCreate();

        // TODO: Show an ongoing notification when this service is running.
        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        sharedPreferences = getSharedPreferences(MYPREF, Context.MODE_PRIVATE);


        if (mClipboardManager!=null){
            mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
            Log.e("LOG", "mClipboardManager not null");
        }else{
            Log.e("LOG", "mClipboardManager is null");
        }

    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mClipboardManager != null) {
            mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
            new ClipboardManager.OnPrimaryClipChangedListener() {
                @Override
                public void onPrimaryClipChanged() {
                    Log.d(TAG, "onPrimaryClipChanged");
                    ClipData clip = mClipboardManager.getPrimaryClip();
                    CharSequence mTextToWrite = clip.getItemAt(0).getText();
                    Log.e(TAG,mTextToWrite.toString());
                    new Request().execute(mTextToWrite.toString());
                }
            };

    private class Request extends AsyncTask<String, Void, String> {

        protected String doInBackground(String... params) {
            String output="";
            Boolean isConnected;
            Boolean isSent;

            SocketManager socket = SocketManager.getInstance();
            if (socket.isConnected()) {
                isConnected=true;
            }else{
                String ipAddressValue = sharedPreferences.getString("ipAddress","");
                String portValue = sharedPreferences.getString("port","");
                if(ipAddressValue.equals("") || portValue.equals("")){
                    output="NoIP";
                    return output;
                }

                Integer port = Integer.parseInt(portValue);
                isConnected = socket.connectSocket(ipAddressValue, port);
            }
            if (isConnected){
                Log.d("Status: ","Connected to server.................");

                isSent = socket.send(params[0]);
                if(isSent){
                    Log.d("Status: ","Data sent successfully.................");

                    String message= socket.recv();

                    if(message!=null){
                        output = message;
                    }
                }else {
                    Log.d("Status: ","Data not sent.................");
                }


            }else {
                Log.i("Status: ","Not connected to socket.................");
            }

            return output;
        }

        protected void onPostExecute(String output) {
            if(output.equals("")){
                Toast.makeText(getBaseContext(), "Unable to connect to laptop", Toast.LENGTH_SHORT).show();
            }else if(output.equals("NoIP")){
                Toast.makeText(getBaseContext(), "Please add IP address of laptop in app to send copied text", Toast.LENGTH_SHORT).show();
            }
            else if(output.equals("ok")){
                Toast.makeText(getBaseContext(), "Copied text sent successfully", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getBaseContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }

        }
    }

}