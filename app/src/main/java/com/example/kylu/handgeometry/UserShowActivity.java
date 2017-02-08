package com.example.kylu.handgeometry;


import android.content.Intent;
import android.os.Bundle;
import android.app.Activity;
import android.widget.EditText;
import android.widget.TextView;

public class UserShowActivity extends Activity {

    TextView textView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_show);

        Intent i =getIntent();

        textView = (TextView)findViewById(R.id.imienazwisko);
        textView.setText(i.getStringExtra("namesurname")+"!");
    }

}
