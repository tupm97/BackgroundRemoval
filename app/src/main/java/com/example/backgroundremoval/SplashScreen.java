package com.example.backgroundremoval;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;

public class SplashScreen extends AppCompatActivity {

    private final String fileName="viettelbackgroundremoval.tflite";
    private final String url="https://github.com/tupm97/BackgroundRemoval/releases/download/viettelbackgroundremoval/viettelbackgroundremoval.tflite";
    private ProgressDialog mProgressDialog;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mProgressDialog = new ProgressDialog(SplashScreen.this);
        mProgressDialog.setMessage("Download model");
        mProgressDialog.setIndeterminate(true);
        mProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        mProgressDialog.setCancelable(false);
        initPermission();

        File file=new File(Environment.getExternalStorageDirectory().getPath()+"/"+fileName);
        if(!file.exists()){
            ApiClient apiClient= new ApiClient(this);
            apiClient.execute(url);
        }
        else{
            Intent intent= new Intent(SplashScreen.this,MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            startActivity(intent);
        }
    }

    public void initPermission(){
        if(Build.VERSION.SDK_INT>=Build.VERSION_CODES.M){
            if(checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED &&
                    checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)!=PackageManager.PERMISSION_GRANTED){
                if(shouldShowRequestPermissionRationale(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        || shouldShowRequestPermissionRationale(Manifest.permission.READ_EXTERNAL_STORAGE)){
                    Toast.makeText(this,"Permission storage is granted",Toast.LENGTH_SHORT).show();
                }
                else{
                    Toast.makeText(this,"Permission storage don't granted",Toast.LENGTH_SHORT).show();
                }
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE},2);

            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode==2){
            if(grantResults.length==2 && grantResults[0]==PackageManager.PERMISSION_GRANTED && grantResults[1]==PackageManager.PERMISSION_GRANTED){
                Toast.makeText(this,"Permission storage is granted",Toast.LENGTH_SHORT).show();
            }
        }
        else{
            initPermission();
        }
    }

    public class ApiClient extends AsyncTask<String,Integer,String> {

        private Context context;
        private PowerManager.WakeLock mWakeLock;

        public ApiClient(Context context) {
            this.context = context;
        }

        @Override
        protected String doInBackground(String... sUrl) {
            InputStream input = null;
            OutputStream output = null;
            URLConnection connection = null;
            try {
                File out=new File(Environment.getExternalStorageDirectory().getPath()+"/"+fileName);
//                if(!out.exists()){
//                    out.createNewFile();
//                    Log.d("main", "doInBackground: creat file");
//                }
                URL url = new URL(sUrl[0]);
                connection =  url.openConnection();
                connection.connect();
                int fileLength = connection.getContentLength();

                // download the file
                input = new BufferedInputStream(url.openStream(),10*1024);
                output=new FileOutputStream(out);

                byte data[] = new byte[1024];
                long total = 0;
                int count=0;
                while ((count = input.read(data)) != -1) {
                    // allow canceling with back button
                    if (isCancelled()) {
                        input.close();
                        return null;
                    }
                    total += count;
                    // publishing the progress....
                    if (fileLength > 0) // only if total length is known
                        publishProgress((int) (total * 100 / fileLength));
                    output.write(data, 0, count);
                    //   Log.d("main", "doInBackground: ");
                }
            } catch (Exception e) {
                e.printStackTrace();
                return "Connected fail! Problem connect to server! Please check your network";
            } finally {
                try {
                    if (output != null)
                        output.close();
                    if (input != null)
                        input.close();
                } catch (IOException ignored) {
                }

            }
            return null;
        }

        // Once Music File is downloaded
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // take CPU lock to prevent CPU from going off if the user
            // presses the power button during download
            PowerManager pm = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                    getClass().getName());
            mWakeLock.acquire();
            mProgressDialog.show();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            super.onProgressUpdate(progress);
            // if we get here, length is known, now set indeterminate to false
            mProgressDialog.setIndeterminate(false);
            mProgressDialog.setMax(100);
            mProgressDialog.setProgress(progress[0]);
        }

        @Override
        protected void onPostExecute(String result) {
            mWakeLock.release();
            mProgressDialog.dismiss();
            if (result != null)
                Toast.makeText(context,"Download error: "+result, Toast.LENGTH_LONG).show();
            else{
                Toast.makeText(context,"File downloaded", Toast.LENGTH_SHORT).show();
                Intent intent= new Intent(SplashScreen.this,MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                startActivity(intent);

            }
            Log.d("main", "onPostExecute: "+result);
        }
    }
}
