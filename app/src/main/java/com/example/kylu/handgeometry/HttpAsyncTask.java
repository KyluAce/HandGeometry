package com.example.kylu.handgeometry;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class HttpAsyncTask extends AsyncTask<String, Void, String> {

    private Activity activity;
    private Class next;
    private ProgressDialog progressDialog;
    private Map<String,String> params = new HashMap<>();

    public HttpAsyncTask(Activity activity,Class next) {
        this.activity = activity;
        this.next=next;
    }

    public void setParams(Map<String,String> params) {
        this.params = params;
    }

    @Override
    protected String doInBackground(String... urls) {
        return get(urls[0]);
    }

    @Override
    protected void onPreExecute() {
        progressDialog = new ProgressDialog(activity);
        progressDialog.setTitle("Obliczanie");
        progressDialog.setMessage("Loading...");
        progressDialog.show();
    }

    @Override
    protected void onPostExecute(String result) {
        progressDialog.dismiss();

        if(!Boolean.parseBoolean(result)) {
            Intent intent = new Intent(activity, next);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
            activity.startActivity(intent);
        } else {
            Toast.makeText(activity, "Uzytkownik o podanym peselu juz istnieje!",
                    Toast.LENGTH_LONG).show();
        }

    }

    private String get(String url){
        InputStream inputStream = null;
        String result = "";
        try {

            HttpClient httpclient = new DefaultHttpClient();

            HttpPost httpPost = new HttpPost(url);

            List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();

            for (Map.Entry<String, String> entry : params.entrySet()) {
                nameValuePairs.add(new BasicNameValuePair(entry.getKey(),entry.getValue()));
            }

            httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs));

            HttpResponse httpResponse = httpclient.execute(httpPost);

            inputStream = httpResponse.getEntity().getContent();

            if(inputStream != null)
                result = convertInputStreamToString(inputStream);
            else
                result = "Did not work!";

        } catch (Exception e) {
            Log.d("InputStream", e.getLocalizedMessage());
        }

        return result;
    }

    private String convertInputStreamToString(InputStream inputStream) throws IOException {
        BufferedReader bufferedReader = new BufferedReader( new InputStreamReader(inputStream));
        String line = "";
        String result = "";
        while((line = bufferedReader.readLine()) != null)
            result += line;

        inputStream.close();
        return result;

    }
}
