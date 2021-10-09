package com.pengpp;

import android.app.Activity;
import android.os.Bundle;

import com.pengpp.annotations.PConfig;

@PConfig()
public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }
}