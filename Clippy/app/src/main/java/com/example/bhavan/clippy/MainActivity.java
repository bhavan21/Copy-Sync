package com.example.bhavan.clippy;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;

import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "ClipboardManager";
    private static final String MYPREF = "MYPREF";
    private ClipboardManager mClipboardManager;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mClipboardManager = (ClipboardManager) getSystemService(CLIPBOARD_SERVICE);
        sharedPreferences = getSharedPreferences(MYPREF, Context.MODE_PRIVATE);


//        if (mClipboardManager!=null){
//            mClipboardManager.addPrimaryClipChangedListener(mOnPrimaryClipChangedListener);
//            Log.e("LOG", "mClipboardManager not null");
//        }else{
//            Log.e("LOG", "mClipboardManager is null");
//        }

        startService(new Intent(this, ClipboardMonitorService.class));
    }


    @Override
    public void onDestroy() {
        super.onDestroy();

        Log.e("LOG", "mClipboardManager closed");
//        if (mClipboardManager != null) {
//            mClipboardManager.removePrimaryClipChangedListener(mOnPrimaryClipChangedListener);
//        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {

            AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainActivity.this);
            alertDialog.setTitle("Laptop details");
            final EditText ipAddress = new EditText(MainActivity.this);
            final EditText port = new EditText(MainActivity.this);




            String ipAddressValue = sharedPreferences.getString("ipAddress","");
            ipAddress.setText(ipAddressValue);
            String portValue = sharedPreferences.getString("port","");
            port.setText(portValue);

            TextView ipAddressHeading = new TextView(MainActivity.this);
            ipAddressHeading.setText("IP Address: ");
            TextView portHeading = new TextView(MainActivity.this);
            portHeading.setText("Port: ");

            ipAddressHeading.setPadding(0,20,0,10);
            portHeading.setPadding(0,20,0,10);

            ipAddress.setInputType(InputType.TYPE_CLASS_TEXT);
            port.setInputType(InputType.TYPE_CLASS_NUMBER);


            LinearLayout ll=new LinearLayout(this);
            ll.setOrientation(LinearLayout.VERTICAL);
            ll.setPadding(50,10,50,10);
            ll.addView(ipAddressHeading);
            ll.addView(ipAddress);
            ll.addView(portHeading);
            ll.addView(port);
            alertDialog.setView(ll);

            alertDialog.setCancelable(true);

            alertDialog.setPositiveButton("SAVE",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            SharedPreferences.Editor editor=sharedPreferences.edit();
                                editor.putString("ipAddress",ipAddress.getText().toString());
                                editor.putString("port",port.getText().toString());
                                editor.apply();
                                Toast.makeText(getBaseContext(),"Laptop details stored successfully",Toast.LENGTH_SHORT).show();
                            }
                    });

            alertDialog.setNegativeButton("CANCEL",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });

            alertDialog.show();



            return true;
        }

        return super.onOptionsItemSelected(item);
    }

//    private ClipboardManager.OnPrimaryClipChangedListener mOnPrimaryClipChangedListener =
//            new ClipboardManager.OnPrimaryClipChangedListener() {
//                @Override
//                public void onPrimaryClipChanged() {
//                    Log.d(TAG, "onPrimaryClipChanged");
//                    ClipData clip = mClipboardManager.getPrimaryClip();
//                    CharSequence mTextToWrite = clip.getItemAt(0).getText();
//                    Log.e(TAG,mTextToWrite.toString());
//                    new Request().execute(mTextToWrite.toString());
//                }
//            };

    private class Request extends AsyncTask<String, Void, String> {

        @Override
        protected String doInBackground(String... params) {
            String output="";
            Boolean isConnected;
            Boolean isSent;

            SocketManager socket = SocketManager.getInstance();
            if (socket.isConnected()) {
                isConnected=true;
            }else{
                isConnected = socket.connectSocket("192.168.0.108", 1997);
            }
            if (isConnected){
                Log.d("Status: ","Connected to server.................");

                isSent = socket.send(params[0]);
                if(isSent){
                    Log.d("Status: ","Data sent successfully.................");

                    String message= socket.recv();

                    if(message!=null){
                        output = message;
                        Log.e("Message",message+"===============================");
                    }

                }else {
                    Log.d("Status: ","Data not sent.................");
                }


            }else {
                Log.i("Status: ","Not connected to socket.................");
            }

            return output;
        }

        @Override
        protected void onPostExecute(String output) {
            if(output.equals("")){
                Toast.makeText(getBaseContext(), "Unable to connect to server", Toast.LENGTH_SHORT).show();
            }else if(output.equals("ok")){
                Toast.makeText(getBaseContext(), "Copied text sent successfully", Toast.LENGTH_SHORT).show();
            }else{
                Toast.makeText(getBaseContext(), "Something went wrong", Toast.LENGTH_SHORT).show();
            }

        }
    }

}


