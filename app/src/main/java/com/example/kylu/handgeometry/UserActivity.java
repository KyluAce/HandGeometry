package com.example.kylu.handgeometry;


import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
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


public class UserActivity extends Activity {

    private TextView peselText;
    private TextView nameText;
    private TextView surnameText;
    private User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        peselText = (TextView)findViewById(R.id.peselText);
        nameText = (TextView)findViewById(R.id.imieText);
        surnameText = (TextView)findViewById(R.id.nazwiskoText);
    }

    public void chceckUser(View v) {

        user = new User(nameText.getText().toString(),surnameText.getText().toString(),peselText.getText().toString());

        new HttpUser(this).execute("http://192.168.43.77:8080/geometry-1.0.0-BUILD-SNAPSHOT/checkuser",peselText.getText().toString());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private ProgressDialog progressDialog;

    private class HttpUser extends AsyncTask<String,Void,String> {

        private Activity activity;

        public HttpUser(Activity activity) {
            this.activity = activity;
        }

        @Override
        protected String doInBackground(String... urls) {
            HttpRequest httpRequest = new HttpRequest();
            Map<String, String> params = new HashMap<>();
            params.put("pesel", urls[1]);
            httpRequest.setParams(params);
            return httpRequest.get(urls[0]);
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

            if (Boolean.parseBoolean(result)) {
                Intent intent = new Intent(activity, CameraActivity.class);
                intent.putExtra("name", user.getName());
                intent.putExtra("surname", user.getSurname());
                intent.putExtra("pesel", user.getPesel());
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(activity, "Uzytkownik o podanym peselu juz istnieje!",
                        Toast.LENGTH_LONG).show();
            }

        }

    }
}
